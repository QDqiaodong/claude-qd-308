package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LockerDao {

    private static final String BASE = "select id, locker_code as lockerCode, locker_name as lockerName,"
            + " locker_state as lockerState from gym_locker";

    private final JdbcTemplate jdbc;

    public LockerDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by id");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where locker_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String name, String state) {
        return jdbc.update("insert into gym_locker (locker_code, locker_name, locker_state) values (?, ?, ?)",
                code, name, state);
    }

    public int update(long id, String name, String state) {
        return jdbc.update("update gym_locker set locker_name = ?, locker_state = ? where id = ?",
                name, state, id);
    }
}
