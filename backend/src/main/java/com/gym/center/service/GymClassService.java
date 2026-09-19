package com.gym.center.service;

import static com.gym.center.service.AreaService.intOrNull;
import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.GymClassDao;
import com.gym.center.dto.BizException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 团课：名额是硬的，报满了就不能再加人。 */
@Service
public class GymClassService {

    private final GymClassDao dao;

    public GymClassService(GymClassDao dao) {
        this.dao = dao;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("classState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("classCode")).contains(keyword)
                        || String.valueOf(r.get("className")).contains(keyword))
                .toList();
    }

    @Transactional
    public Map<String, Object> save(Map<String, Object> form) {
        Long id = num(form.get("id"));
        Map<String, Object> existed = id == null ? null : dao.one(id);
        String code = str(form.get("classCode"));
        if (existed != null) {
            if (code == null) {
                code = str(existed.get("classCode"));
            }
        }
        if (code == null) {
            throw new BizException("课程编号不能空着");
        }
        Map<String, Object> same = dao.byCode(code);
        if (same != null && (id == null || !num(same.get("id")).equals(id))) {
            throw new BizException("课程编号 " + code + " 重复了");
        }
        String name = orDefault(form, existed, "className");
        String coach = orDefault(form, existed, "coachName");
        String date = orDefault(form, existed, "classDate");
        String start = orDefault(form, existed, "startTime");
        String state = orDefault(form, existed, "classState");
        if (state == null) {
            state = "待开课";
        }
        if (name == null || date == null) {
            throw new BizException("课程名称和日期都得填");
        }
        Integer total = intOrNull(form.get("seatTotal"));
        if (total == null && existed != null) {
            total = intOrNull(existed.get("seatTotal"));
        }
        Integer used = intOrNull(form.get("seatUsed"));
        if (used == null && existed != null) {
            used = intOrNull(existed.get("seatUsed"));
        }
        if (total != null && total <= 0) {
            throw new BizException("名额得是正数");
        }
        if (used != null && used < 0) {
            throw new BizException("已报名人数不能为负");
        }
        if (used != null && total != null && used > total) {
            throw new BizException("已报名 " + used + " 人，超过了 " + total + " 个名额");
        }
        if (existed == null) {
            dao.insert(code, name, coach, date, start, total == null ? 0 : total,
                    used == null ? 0 : used, state);
        } else {
            dao.update(id, name, coach, date, start, total, used, state);
        }
        return dao.byCode(code);
    }

    private String orDefault(Map<String, Object> form, Map<String, Object> existed, String key) {
        String v = str(form.get(key));
        if (v != null) {
            return v;
        }
        return existed == null ? null : str(existed.get(key));
    }
}
