package com.gym.center.service;

import static com.gym.center.service.AreaService.intOrNull;
import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.AreaDao;
import com.gym.center.dao.GymClassDao;
import com.gym.center.dao.LockerDao;
import com.gym.center.dao.MemberDao;
import com.gym.center.dao.TrialTicketDao;
import com.gym.center.dto.BizException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 体验入场单。开单是一个动作：查训练区容纳、占更衣柜、落单，在同一个事务里做完，
 * 拆开做的话第二天柜门钥匙和在馆人头就对不上了。
 *
 * 按场务的规矩来：介绍人只是单子上的备注（停卡、卡过期都能当介绍人），
 * 开单不把体验客转成会员，也不动介绍人的卡状态 —— 这里只读会员档案，从不写。
 */
@Service
public class TrialTicketService {

    static final List<String> SLOTS = List.of("上午", "下午", "晚上");

    private final TrialTicketDao dao;
    private final AreaDao areas;
    private final LockerDao lockers;
    private final GymClassDao classes;
    private final MemberDao members;

    public TrialTicketService(TrialTicketDao dao, AreaDao areas, LockerDao lockers,
                              GymClassDao classes, MemberDao members) {
        this.dao = dao;
        this.areas = areas;
        this.lockers = lockers;
        this.classes = classes;
        this.members = members;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("ticketState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("ticketCode")).contains(keyword)
                        || String.valueOf(r.get("guestName")).contains(keyword))
                .toList();
    }

    /** 各训练区此刻的人头：在馆体验客 + 今天当前时段的团课报名数，对着容纳上限看。 */
    public List<Map<String, Object>> board() {
        String today = LocalDate.now().toString();
        String slot = currentSlot();
        return areas.list().stream().map(a -> {
            long areaId = num(a.get("id"));
            int inGym = dao.countInGym(areaId);
            int classSeats = classSeats(areaId, today, slot);
            Integer capacity = intOrNull(a.get("capacity"));
            Map<String, Object> row = new HashMap<>();
            row.put("areaId", areaId);
            row.put("areaCode", a.get("areaCode"));
            row.put("areaName", a.get("areaName"));
            row.put("areaState", a.get("areaState"));
            row.put("capacity", capacity);
            row.put("inGym", inGym);
            row.put("classSeats", classSeats);
            row.put("left", capacity == null ? null : capacity - inGym - classSeats);
            return row;
        }).toList();
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> form) {
        String guest = str(form.get("guestName"));
        Long areaId = num(form.get("areaId"));
        Long lockerId = num(form.get("lockerId"));
        if (guest == null) {
            throw new BizException("体验客称呼得填上");
        }
        if (areaId == null) {
            throw new BizException("还没选要进的训练区，单子落不下去");
        }
        if (lockerId == null) {
            throw new BizException("还没选要占的更衣柜，单子落不下去");
        }
        String date = str(form.get("visitDate"));
        if (date == null) {
            date = LocalDate.now().toString();
        }
        String slot = str(form.get("timeSlot"));
        if (slot == null) {
            slot = currentSlot();
        }
        if (!SLOTS.contains(slot)) {
            throw new BizException("入场时段只能是：上午 / 下午 / 晚上");
        }
        // 介绍人只是备注：停卡、卡过期都能当介绍人；这里只确认人存在，不动他的卡
        Long referrerId = num(form.get("referrerId"));
        if (referrerId != null && members.one(referrerId) == null) {
            throw new BizException("介绍人没查到，备注写不上");
        }
        // 锁住训练区这一行再数人头，同区的单一张张开，容纳上限才是硬的
        Map<String, Object> area = areas.oneForUpdate(areaId);
        if (area == null) {
            throw new BizException("要进的训练区不存在");
        }
        if (!"开放".equals(area.get("areaState"))) {
            throw new BizException("训练区「" + area.get("areaName") + "」停用了，新单子不能再选它");
        }
        Integer capacity = intOrNull(area.get("capacity"));
        if (capacity != null) {
            int inGym = dao.countInGym(areaId);
            int classSeats = classSeats(areaId, date, slot);
            if (inGym + classSeats + 1 > capacity) {
                throw new BizException("「" + area.get("areaName") + "」现在馆内 " + inGym
                        + " 人、团课占 " + classSeats + " 人，容纳上限 " + capacity + "，这位进不去了");
            }
        }
        // 锁住柜子这一行再确认没被占，同一个柜不会同时发给两个人
        Map<String, Object> locker = lockers.oneForUpdate(lockerId);
        if (locker == null) {
            throw new BizException("要占的更衣柜不存在");
        }
        if ("故障".equals(locker.get("lockerState"))) {
            throw new BizException("柜子「" + locker.get("lockerCode") + "」故障了，换一柜");
        }
        if (dao.countLiveByLocker(lockerId) > 0) {
            throw new BizException("柜子「" + locker.get("lockerCode") + "」还被人占着，换一柜");
        }
        long id = dao.insert(guest, areaId, lockerId, date, slot, referrerId);
        dao.assignCode(id, String.format("TK-%04d", id));
        return dao.one(id);
    }

    /** 前台补记钥匙归还。钥匙还了，才谈得上离场。 */
    @Transactional
    public Map<String, Object> returnKey(long id) {
        Map<String, Object> t = dao.oneForUpdate(id);
        if (t == null) {
            throw new BizException("这张入场单不存在");
        }
        if (!"在馆".equals(t.get("ticketState"))) {
            throw new BizException("这张单子已经离场了");
        }
        if (!asBool(t.get("keyReturned"))) {
            dao.markKeyReturned(id);
        }
        return dao.one(id);
    }

    /** 离场收口：钥匙没交回，离场不成立，柜子继续占、人头继续算。 */
    @Transactional
    public Map<String, Object> leave(long id) {
        Map<String, Object> t = dao.oneForUpdate(id);
        if (t == null) {
            throw new BizException("这张入场单不存在");
        }
        if (!"在馆".equals(t.get("ticketState"))) {
            throw new BizException("这张单子已经离场了");
        }
        if (!asBool(t.get("keyReturned"))) {
            throw new BizException("钥匙还没交回，离场不成立；先补记钥匙归还");
        }
        dao.markLeft(id);
        return dao.one(id);
    }

    /** 同一训练区、同一天、上课时段和入场时段重叠的团课，已报名数加总 —— 在馆人头的第二半。 */
    private int classSeats(long areaId, String date, String slot) {
        int sum = 0;
        for (Map<String, Object> c : classes.byAreaDate(areaId, date)) {
            if (slot.equals(slotOf(str(c.get("startTime"))))) {
                Integer used = intOrNull(c.get("seatUsed"));
                sum += used == null ? 0 : used;
            }
        }
        return sum;
    }

    /** 上课时间点落在哪个时段：12 点前上午，12–18 点下午，18 点后晚上。 */
    static String slotOf(String hhmm) {
        if (hhmm == null || hhmm.length() < 2) {
            return null;
        }
        int h;
        try {
            h = Integer.parseInt(hhmm.substring(0, 2));
        } catch (NumberFormatException e) {
            return null;
        }
        if (h < 12) {
            return "上午";
        }
        if (h < 18) {
            return "下午";
        }
        return "晚上";
    }

    static String currentSlot() {
        return slotOf(String.format("%02d:00", LocalTime.now().getHour()));
    }

    static boolean asBool(Object o) {
        if (o instanceof Boolean b) {
            return b;
        }
        return o != null && ("1".equals(String.valueOf(o)) || "true".equalsIgnoreCase(String.valueOf(o)));
    }
}
