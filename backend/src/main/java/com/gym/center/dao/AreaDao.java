package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 这个仓不用实体类 —— SQL 里直接用别名把列名取成前端要的样子，
 * 拿回来就是 Map，省掉一层映射。
 */
@Repository
public class AreaDao {

    private static final String BASE = "select id, area_code as areaCode, area_name as areaName,"
            + " floor_size as floorSize, capacity, area_state as areaState from gym_area";

    private final JdbcTemplate jdbc;

    public AreaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by id");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 开体验单时锁住这一行，同一个训练区的开单排队走，在馆人头才数得准。 */
    public Map<String, Object> lockOne(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where id = ? for update", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where area_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String name, Integer size, Integer capacity, String state) {
        return jdbc.update("insert into gym_area (area_code, area_name, floor_size, capacity, area_state)"
                + " values (?, ?, ?, ?, ?)", code, name, size, capacity, state);
    }

    public int update(long id, String name, Integer size, Integer capacity, String state) {
        return jdbc.update("update gym_area set area_name = ?, floor_size = ?, capacity = ?, area_state = ?"
                + " where id = ?", name, size, capacity, state, id);
    }

    public int countMachines(long areaId) {
        Integer n = jdbc.queryForObject("select count(*) from gym_machine where area_id = ?", Integer.class, areaId);
        return n == null ? 0 : n;
    }
}
