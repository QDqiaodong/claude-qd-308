package com.gym.center.service;

import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.AreaDao;
import com.gym.center.dao.GymClassDao;
import com.gym.center.dao.LockerDao;
import com.gym.center.dao.MemberDao;
import com.gym.center.dao.VisitDao;
import com.gym.center.dto.BizException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 体验入场单。
 * 开单是一个动作：训练区容纳和更衣柜占用在同一个事务里校验、落单，
 * 不分两步走，不然柜门钥匙和在馆人头第二天对不齐。
 * 按场务的流程：开单只登记入场，不把体验客转成会员，也不碰介绍人的卡状态。
 */
@Service
public class VisitService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final VisitDao dao;
    private final AreaDao areas;
    private final LockerDao lockers;
    private final MemberDao members;
    private final GymClassDao classes;

    public VisitService(VisitDao dao, AreaDao areas, LockerDao lockers, MemberDao members, GymClassDao classes) {
        this.dao = dao;
        this.areas = areas;
        this.lockers = lockers;
        this.members = members;
        this.classes = classes;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("visitState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("visitCode")).contains(keyword)
                        || String.valueOf(r.get("guestName")).contains(keyword))
                .toList();
    }

    /** 各区在馆人头看板：在馆体验客 + 此刻正在上课的团课报名数，对着容纳上限看。 */
    public List<Map<String, Object>> board() {
        String today = LocalDate.now().toString();
        String now = LocalTime.now().format(HM);
        return areas.list().stream()
                .map(a -> {
                    long areaId = num(a.get("id"));
                    int guests = dao.countActiveByArea(areaId);
                    int classUsed = classes.overlapSeatUsed(areaId, today, now);
                    a.put("guests", guests);
                    a.put("classUsed", classUsed);
                    a.put("headcount", guests + classUsed);
                    return a;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> open(Map<String, Object> form) {
        String guest = str(form.get("guestName"));
        Long areaId = num(form.get("areaId"));
        Long lockerId = num(form.get("lockerId"));
        if (guest == null) {
            throw new BizException("体验客称呼得填");
        }
        if (areaId == null) {
            throw new BizException("训练区还没选，单子落不下去");
        }
        if (lockerId == null) {
            throw new BizException("更衣柜还没选，单子落不下去");
        }
        // 锁住训练区这一行：同一个区的开单排队走，容量才数得准
        Map<String, Object> area = areas.lockOne(areaId);
        if (area == null) {
            throw new BizException("训练区不存在");
        }
        if (!"开放".equals(area.get("areaState"))) {
            throw new BizException("训练区「" + area.get("areaName") + "」停用了，新单不能进");
        }
        Map<String, Object> locker = lockers.one(lockerId);
        if (locker == null) {
            throw new BizException("更衣柜不存在");
        }
        if ("故障".equals(locker.get("lockerState"))) {
            throw new BizException("柜子「" + locker.get("lockerCode") + "」故障了，换一格");
        }
        if (dao.countActiveByLocker(lockerId) > 0) {
            throw new BizException("柜子「" + locker.get("lockerCode") + "」还占着，钥匙没收回不能给别人");
        }
        Integer capacity = area.get("capacity") == null ? null : Integer.valueOf(String.valueOf(area.get("capacity")));
        if (capacity != null) {
            String today = LocalDate.now().toString();
            String now = LocalTime.now().format(HM);
            int guests = dao.countActiveByArea(areaId);
            int classUsed = classes.overlapSeatUsed(areaId, today, now);
            if (guests + classUsed + 1 > capacity) {
                throw new BizException("「" + area.get("areaName") + "」现在在馆 " + (guests + classUsed)
                        + " 人（体验客 " + guests + " + 团课 " + classUsed + "），再加就超过容纳上限 "
                        + capacity + " 了");
            }
        }
        // 介绍会员只是写在单子上的备注：停卡、过期都能当介绍人，这里不改他的卡状态
        Long referrerId = num(form.get("referrerId"));
        if (referrerId != null && members.one(referrerId) == null) {
            throw new BizException("介绍会员不存在");
        }
        long id = dao.insert(guest, areaId, lockerId, referrerId, LocalDateTime.now().format(TS));
        return dao.one(id);
    }

    /** 补记钥匙归还：前台把钥匙收回来了记一笔，记完才能办离场。 */
    @Transactional
    public Map<String, Object> returnKey(long id) {
        Map<String, Object> v = dao.one(id);
        if (v == null) {
            throw new BizException("这张入场单不存在");
        }
        if (!"在馆".equals(v.get("visitState"))) {
            throw new BizException("这张单已经离场了");
        }
        dao.markKeyReturned(id);
        return dao.one(id);
    }

    /** 离场收口：钥匙没交回，离场不成立，柜子继续占着、人头继续算。 */
    @Transactional
    public Map<String, Object> leave(long id) {
        Map<String, Object> v = dao.one(id);
        if (v == null) {
            throw new BizException("这张入场单不存在");
        }
        if (!"在馆".equals(v.get("visitState"))) {
            throw new BizException("这张单已经离场了");
        }
        if (!Boolean.TRUE.equals(v.get("keyReturned"))) {
            throw new BizException("钥匙还没交回，先补记钥匙归还再离场");
        }
        dao.markLeft(id, LocalDateTime.now().format(TS));
        return dao.one(id);
    }
}
