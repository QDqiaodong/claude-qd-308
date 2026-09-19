package com.gym.center.service;

import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.LockerDao;
import com.gym.center.dao.TrialTicketDao;
import com.gym.center.dto.BizException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 更衣柜。柜子的真实状态 = 台账状态（空闲/故障）+ 有没有在馆单占着，
 * 占用不另存，免得柜门钥匙和单子对不上。
 */
@Service
public class LockerService {

    private final LockerDao dao;
    private final TrialTicketDao tickets;

    public LockerService(LockerDao dao, TrialTicketDao tickets) {
        this.dao = dao;
        this.tickets = tickets;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .peek(this::fillState)
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("state")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("lockerCode")).contains(keyword))
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
        String state = str(form.get("lockerState"));
        if (state == null) {
            state = existed == null ? "空闲" : str(existed.get("lockerState"));
        }
        if (!"空闲".equals(state) && !"故障".equals(state)) {
            throw new BizException("柜子状态只能是：空闲 / 故障（占用由入场单说了算）");
        }
        if (existed == null) {
            dao.insert(code, state);
        } else {
            if ("故障".equals(state) && tickets.countLiveByLocker(id) > 0) {
                throw new BizException("柜子「" + code + "」还有人用着，等这张单离场了再标故障");
            }
            dao.update(id, state);
        }
        return dao.one(dao.byCode(code) == null ? id : num(dao.byCode(code).get("id")));
    }

    /** 把占用情况填上：故障 > 占用 > 空闲。 */
    private void fillState(Map<String, Object> r) {
        String base = str(r.get("lockerState"));
        boolean held = r.get("holdTicketId") != null;
        r.put("state", "故障".equals(base) ? "故障" : held ? "占用" : "空闲");
    }
}
