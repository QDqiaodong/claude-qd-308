package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GymClassDao {

    private static final String BASE = "select c.id, c.class_code as classCode, c.class_name as className,"
            + " c.coach_name as coachName, c.class_date as classDate, c.start_time as startTime,"
            + " c.end_time as endTime, c.area_id as areaId, a.area_name as areaName,"
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

    /**
     * 同一训练区、上课时段罩住 hhmm 这一刻、还没上完的团课，已报名人数加总。
     * 在馆人头 = 在馆体验客 + 这个数。
     */
    public int overlapSeatUsed(long areaId, String date, String hhmm) {
        Integer n = jdbc.queryForObject("select coalesce(sum(seat_used), 0) from gym_class"
                + " where area_id = ? and class_date = ? and class_state <> '已完成'"
                + " and start_time is not null and end_time is not null"
                + " and start_time <= ? and end_time >= ?", Integer.class, areaId, date, hhmm, hhmm);
        return n == null ? 0 : n;
    }

    public int insert(String code, String name, String coach, String date, String start, String end,
                      Long areaId, int total, int used, String state) {
        return jdbc.update("insert into gym_class (class_code, class_name, coach_name, class_date,"
                + " start_time, end_time, area_id, seat_total, seat_used, class_state)"
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                code, name, coach, date, start, end, areaId, total, used, state);
    }

    public int update(long id, String name, String coach, String date, String start, String end,
                      Long areaId, Integer total, Integer used, String state) {
        return jdbc.update("update gym_class set class_name = ?, coach_name = ?, class_date = ?, start_time = ?,"
                + " end_time = ?, area_id = ?,"
                + " seat_total = coalesce(?, seat_total), seat_used = coalesce(?, seat_used),"
                + " class_state = ? where id = ?", name, coach, date, start, end, areaId, total, used, state, id);
    }
}
