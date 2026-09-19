package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GymClassDao {

    private static final String BASE = "select c.id, c.class_code as classCode, c.class_name as className,"
            + " c.coach_name as coachName, c.class_date as classDate, c.start_time as startTime,"
            + " c.area_id as areaId, a.area_name as areaName,"
            + " c.seat_total as seatTotal, c.seat_used as seatUsed, c.class_state as classState"
            + " from gym_class c left join gym_area a on a.id = c.area_id";

    private final JdbcTemplate jdbc;

    public GymClassDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by c.class_date, c.start_time");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where c.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where c.class_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 某天排在某训练区的课 —— 算在馆人头时，按入场时段再筛一遍。 */
    public List<Map<String, Object>> byAreaDate(long areaId, String date) {
        return jdbc.queryForList(BASE + " where c.area_id = ? and c.class_date = ?", areaId, date);
    }

    public int insert(String code, String name, String coach, String date, String start, Long areaId,
                      int total, int used, String state) {
        return jdbc.update("insert into gym_class (class_code, class_name, coach_name, class_date,"
                + " start_time, area_id, seat_total, seat_used, class_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                code, name, coach, date, start, areaId, total, used, state);
    }

    public int update(long id, String name, String coach, String date, String start, Long areaId,
                      Integer total, Integer used, String state) {
        return jdbc.update("update gym_class set class_name = ?, coach_name = ?, class_date = ?, start_time = ?,"
                + " area_id = ?, seat_total = coalesce(?, seat_total), seat_used = coalesce(?, seat_used),"
                + " class_state = ? where id = ?", name, coach, date, start, areaId, total, used, state, id);
    }
}
