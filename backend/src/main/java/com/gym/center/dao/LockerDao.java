package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 更衣柜。表里只存「空闲 / 故障」；「占用」不落库，
 * 由「在馆」的入场单推出来 —— 单子不走离场收口，柜子就一直算占着。
 */
@Repository
public class LockerDao {

    private static final String BASE = "select l.id, l.locker_code as lockerCode, l.locker_state as lockerState,"
            + " t.id as holdTicketId, t.ticket_code as holdTicketCode, t.guest_name as holdGuestName"
            + " from gym_locker l"
            + " left join gym_trial_ticket t on t.locker_id = l.id and t.ticket_state = '在馆'";

    private final JdbcTemplate jdbc;

    public LockerDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by l.id");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where l.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> byCode(String code) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id, locker_code as lockerCode, locker_state as lockerState"
                        + " from gym_locker where locker_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 开单抢柜子时锁住这一行，同一个柜不会被两张单同时拿走。 */
    public Map<String, Object> oneForUpdate(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id, locker_code as lockerCode, locker_state as lockerState"
                        + " from gym_locker where id = ? for update", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String state) {
        return jdbc.update("insert into gym_locker (locker_code, locker_state) values (?, ?)", code, state);
    }

    public int update(long id, String state) {
        return jdbc.update("update gym_locker set locker_state = ? where id = ?", state, id);
    }
}
