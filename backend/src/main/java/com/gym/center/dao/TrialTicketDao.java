package com.gym.center.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TrialTicketDao {

    private static final String BASE = "select t.id, t.ticket_code as ticketCode, t.guest_name as guestName,"
            + " t.area_id as areaId, a.area_name as areaName, a.area_state as areaState,"
            + " t.locker_id as lockerId, k.locker_code as lockerCode,"
            + " t.visit_date as visitDate, t.time_slot as timeSlot,"
            + " t.referrer_id as referrerId, m.member_name as referrerName, m.member_code as referrerCode,"
            + " m.member_state as referrerState, m.expire_date as referrerExpire,"
            + " t.ticket_state as ticketState, t.key_returned as keyReturned,"
            + " date_format(t.created_at, '%Y-%m-%d %H:%i') as createdAt,"
            + " date_format(t.key_returned_at, '%Y-%m-%d %H:%i') as keyReturnedAt,"
            + " date_format(t.left_at, '%Y-%m-%d %H:%i') as leftAt"
            + " from gym_trial_ticket t"
            + " join gym_area a on a.id = t.area_id"
            + " join gym_locker k on k.id = t.locker_id"
            + " left join gym_member m on m.id = t.referrer_id";

    private final JdbcTemplate jdbc;

    public TrialTicketDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by t.id desc");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where t.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 离场、补记钥匙前锁住这张单，别和别的动作打架。 */
    public Map<String, Object> oneForUpdate(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id, ticket_code as ticketCode, guest_name as guestName, area_id as areaId,"
                        + " locker_id as lockerId, ticket_state as ticketState, key_returned as keyReturned"
                        + " from gym_trial_ticket where id = ? for update", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 该训练区里还在馆的体验客人数 —— 在馆人头的第一半。 */
    public int countInGym(long areaId) {
        Integer n = jdbc.queryForObject("select count(*) from gym_trial_ticket"
                + " where area_id = ? and ticket_state = '在馆'", Integer.class, areaId);
        return n == null ? 0 : n;
    }

    /** 这个柜是不是还被人占着（有没离场的单）。 */
    public int countLiveByLocker(long lockerId) {
        Integer n = jdbc.queryForObject("select count(*) from gym_trial_ticket"
                + " where locker_id = ? and ticket_state = '在馆'", Integer.class, lockerId);
        return n == null ? 0 : n;
    }

    /** 插单并返回自增 id，单号由调用方按 id 补写。 */
    public long insert(String guestName, long areaId, long lockerId, String visitDate, String timeSlot,
                       Long referrerId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into gym_trial_ticket (ticket_code, guest_name, area_id, locker_id, visit_date,"
                            + " time_slot, referrer_id, ticket_state, key_returned, created_at)"
                            + " values ('', ?, ?, ?, ?, ?, ?, '在馆', 0, now())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, guestName);
            ps.setLong(2, areaId);
            ps.setLong(3, lockerId);
            ps.setString(4, visitDate);
            ps.setString(5, timeSlot);
            if (referrerId == null) {
                ps.setNull(6, java.sql.Types.BIGINT);
            } else {
                ps.setLong(6, referrerId);
            }
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? 0 : key.longValue();
    }

    public int assignCode(long id, String code) {
        return jdbc.update("update gym_trial_ticket set ticket_code = ? where id = ?", code, id);
    }

    public int markKeyReturned(long id) {
        return jdbc.update("update gym_trial_ticket set key_returned = 1, key_returned_at = now()"
                + " where id = ? and ticket_state = '在馆'", id);
    }

    public int markLeft(long id) {
        return jdbc.update("update gym_trial_ticket set ticket_state = '已离场', left_at = now()"
                + " where id = ? and ticket_state = '在馆'", id);
    }
}
