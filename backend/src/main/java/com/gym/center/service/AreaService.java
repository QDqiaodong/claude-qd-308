package com.gym.center.service;

import com.gym.center.dao.AreaDao;
import com.gym.center.dto.BizException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AreaService {

    private final AreaDao dao;

    public AreaService(AreaDao dao) {
        this.dao = dao;
    }

    public List<Map<String, Object>> list(String state, String keyword) {
        return dao.list().stream()
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("areaState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("areaCode")).contains(keyword)
                        || String.valueOf(r.get("areaName")).contains(keyword))
                .toList();
    }

    @Transactional
    public Map<String, Object> save(Map<String, Object> form) {
        Long id = num(form.get("id"));
        String code = str(form.get("areaCode"));
        String name = str(form.get("areaName"));
        Map<String, Object> existed = id == null ? null : dao.one(id);
        if (existed != null) {
            if (code == null) {
                code = str(existed.get("areaCode"));
            }
            if (name == null) {
                name = str(existed.get("areaName"));
            }
        }
        if (code == null || name == null) {
            throw new BizException("训练区编号和名称都得填");
        }
        Map<String, Object> same = dao.byCode(code);
        if (same != null && (id == null || !num(same.get("id")).equals(id))) {
            throw new BizException("训练区编号 " + code + " 重复了");
        }
        Integer size = intOrNull(form.get("floorSize"));
        Integer capacity = intOrNull(form.get("capacity"));
        if (size != null && size <= 0) {
            throw new BizException("面积得是正数");
        }
        if (capacity != null && capacity <= 0) {
            throw new BizException("容纳人数得是正数");
        }
        String state = str(form.get("areaState"));
        if (state == null) {
            state = existed == null ? "开放" : str(existed.get("areaState"));
        }
        if ("停用".equals(state) && existed != null && !"停用".equals(existed.get("areaState"))
                && dao.countMachines(id) > 0) {
            throw new BizException("这个训练区里还摆着器械，先挪走再停用");
        }
        if (existed == null) {
            dao.insert(code, name, size, capacity, state);
        } else {
            dao.update(id, name, size, capacity, state);
        }
        return dao.byCode(code);
    }

    static String str(Object o) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    static Long num(Object o) {
        if (o == null || String.valueOf(o).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(o).trim());
    }

    static Integer intOrNull(Object o) {
        if (o == null || String.valueOf(o).isBlank()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(o).trim());
    }
}
