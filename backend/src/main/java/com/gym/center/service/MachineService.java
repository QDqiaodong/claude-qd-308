package com.gym.center.service;

import static com.gym.center.service.AreaService.intOrNull;
import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.AreaDao;
import com.gym.center.dao.MachineDao;
import com.gym.center.dto.BizException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MachineService {

    private final MachineDao dao;
    private final AreaDao areas;

    public MachineService(MachineDao dao, AreaDao areas) {
        this.dao = dao;
        this.areas = areas;
    }

    public List<Map<String, Object>> list(Long areaId, String state, String keyword) {
        return dao.list().stream()
                .filter(r -> areaId == null || areaId.equals(num(r.get("areaId"))))
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("machineState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("machineCode")).contains(keyword)
                        || String.valueOf(r.get("machineName")).contains(keyword))
                .toList();
    }

    @Transactional
    public Map<String, Object> save(Map<String, Object> form) {
        Long id = num(form.get("id"));
        Map<String, Object> existed = id == null ? null : dao.one(id);
        String code = str(form.get("machineCode"));
        String name = str(form.get("machineName"));
        if (existed != null) {
            if (code == null) {
                code = str(existed.get("machineCode"));
            }
            if (name == null) {
                name = str(existed.get("machineName"));
            }
        }
        if (code == null || name == null) {
            throw new BizException("器械编号和名称都得填");
        }
        Map<String, Object> same = dao.byCode(code);
        if (same != null && (id == null || !num(same.get("id")).equals(id))) {
            throw new BizException("器械编号 " + code + " 重复了");
        }
        Long areaId = num(form.get("areaId"));
        if (areaId == null && existed != null) {
            areaId = num(existed.get("areaId"));
        }
        if (areaId != null) {
            Map<String, Object> area = areas.one(areaId);
            if (area == null) {
                throw new BizException("要放的训练区不存在");
            }
            if ("停用".equals(area.get("areaState"))) {
                throw new BizException("训练区「" + area.get("areaName") + "」停用了，器械不能往里放");
            }
        }
        String state = str(form.get("machineState"));
        if (state == null) {
            state = existed == null ? "可用" : str(existed.get("machineState"));
        }
        String type = str(form.get("machineType"));
        if (type == null && existed != null) {
            type = str(existed.get("machineType"));
        }
        if (existed == null) {
            dao.insert(code, name, type, areaId, state);
        } else {
            dao.update(id, name, type, areaId, state);
        }
        return dao.byCode(code);
    }
}
