package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MachineDao {

    private static final String BASE = "select m.id, m.machine_code as machineCode, m.machine_name as machineName,"
            + " m.machine_type as machineType, m.area_id as areaId, m.machine_state as machineState,"
            + " a.area_name as areaName from gym_machine m left join gym_area a on a.id = m.area_id";

    private final JdbcTemplate jdbc;

    public MachineDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by m.id");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where m.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where m.machine_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String name, String type, Long areaId, String state) {
        return jdbc.update("insert into gym_machine (machine_code, machine_name, machine_type, area_id, machine_state)"
                + " values (?, ?, ?, ?, ?)", code, name, type, areaId, state);
    }

    public int update(long id, String name, String type, Long areaId, String state) {
        return jdbc.update("update gym_machine set machine_name = ?, machine_type = ?, area_id = ?,"
                + " machine_state = ? where id = ?", name, type, areaId, state, id);
    }
}
