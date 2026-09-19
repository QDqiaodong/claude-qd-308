package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GymClassDao {

    private static final String BASE = "select id, class_code as classCode, class_name as className,"
            + " coach_name as coachName, class_date as classDate, start_time as startTime,"
            + " seat_total as seatTotal, seat_used as seatUsed, class_state as classState from gym_class";

    private final JdbcTemplate jdbc;

    public GymClassDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by class_date, start_time");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where class_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String name, String coach, String date, String start,
                      int total, int used, String state) {
        return jdbc.update("insert into gym_class (class_code, class_name, coach_name, class_date,"
                + " start_time, seat_total, seat_used, class_state) values (?, ?, ?, ?, ?, ?, ?, ?)",
                code, name, coach, date, start, total, used, state);
    }

    public int update(long id, String name, String coach, String date, String start,
                      Integer total, Integer used, String state) {
        return jdbc.update("update gym_class set class_name = ?, coach_name = ?, class_date = ?, start_time = ?,"
                + " seat_total = coalesce(?, seat_total), seat_used = coalesce(?, seat_used),"
                + " class_state = ? where id = ?", name, coach, date, start, total, used, state, id);
    }
}
