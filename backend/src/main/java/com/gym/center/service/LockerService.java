package com.gym.center.service;

import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.LockerDao;
import com.gym.center.dao.VisitDao;
import com.gym.center.dto.BizException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 更衣柜：柜子自己只记「正常 / 故障」，
 * 「空闲 / 使用中」由没离场的入场单推出来，两边不各记一份，才不会对不齐。
 */
@Service
public class LockerService {

    private final LockerDao dao;
    private final VisitDao visits;

    public LockerService(LockerDao dao, VisitDao visits) {
        this.dao = dao;
        this.visits = visits;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .peek(r -> {
                    Map<String, Object> active = visits.activeByLocker(num(r.get("id")));
                    if ("故障".equals(r.get("lockerState"))) {
                        r.put("useState", "故障");
                    } else if (active != null) {
                        r.put("useState", "使用中");
                        r.put("occupant", active.get("guestName"));
                        r.put("visitCode", active.get("visitCode"));
                    } else {
                        r.put("useState", "空闲");
                    }
                })
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("useState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("lockerCode")).contains(keyword)
                        || String.valueOf(r.get("lockerName")).contains(keyword))
                .toList();
    }

    @Transactional
    public Map<String, Object> save(Map<String, Object> form) {
        Long id = num(form.get("id"));
        Map<String, Object> existed = id == null ? null : dao.one(id);
        String code = str(form.get("lockerCode"));
        if (code == null && existed != null) {
            code = str(existed.get("lockerCode"));
        }
        if (code == null) {
            throw new BizException("柜子编号不能空着");
        }
        Map<String, Object> same = dao.byCode(code);
        if (same != null && (id == null || !num(same.get("id")).equals(id))) {
            throw new BizException("柜子编号 " + code + " 重复了");
        }
        String name = str(form.get("lockerName"));
        if (name == null && existed != null) {
            name = str(existed.get("lockerName"));
        }
        String state = str(form.get("lockerState"));
        if (state == null) {
            state = existed == null ? "正常" : str(existed.get("lockerState"));
        }
        if (!"正常".equals(state) && !"故障".equals(state)) {
            throw new BizException("柜子状态只有「正常 / 故障」两种");
        }
        if (existed == null) {
            dao.insert(code, name, state);
        } else {
            dao.update(id, name, state);
        }
        return dao.byCode(code);
    }
}
