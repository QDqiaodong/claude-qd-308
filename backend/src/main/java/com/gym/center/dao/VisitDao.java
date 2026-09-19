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
public class VisitDao {

    private static final String BASE = "select v.id, v.visit_code as visitCode, v.guest_name as guestName,"
            + " v.area_id as areaId, a.area_name as areaName,"
            + " v.locker_id as lockerId, l.locker_code as lockerCode,"
            + " v.referrer_id as referrerId, m.member_name as referrerName, m.member_code as referrerCode,"
            + " v.visit_state as visitState, v.key_returned as keyReturned,"
            + " v.enter_time as enterTime, v.leave_time as leaveTime"
            + " from gym_visit v"
            + " left join gym_area a on a.id = v.area_id"
            + " left join gym_locker l on l.id = v.locker_id"
            + " left join gym_member m on m.id = v.referrer_id";

    private final JdbcTemplate jdbc;

    public VisitDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(BASE + " order by v.id desc");
    }

    public Map<String, Object> one(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where v.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 这个区里状态还是「在馆」的体验客人数。 */
    public int countActiveByArea(long areaId) {
        Integer n = jdbc.queryForObject("select count(*) from gym_visit where area_id = ? and visit_state = '在馆'",
                Integer.class, areaId);
        return n == null ? 0 : n;
    }

    /** 这个柜子是不是还被某张没离场的单子占着。 */
    public int countActiveByLocker(long lockerId) {
        Integer n = jdbc.queryForObject("select count(*) from gym_visit where locker_id = ? and visit_state = '在馆'",
                Integer.class, lockerId);
        return n == null ? 0 : n;
    }

    /** 占着这个柜子的那张在馆单（没有就是 null）。 */
    public Map<String, Object> activeByLocker(long lockerId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                BASE + " where v.locker_id = ? and v.visit_state = '在馆'", lockerId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 先插一行拿到自增 id（单号给个唯一的临时值），再把单号刷成 VS-xxxx。 */
    public long insert(String guest, long areaId, long lockerId, Long referrerId, String enterTime) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into gym_visit (visit_code, guest_name, area_id, locker_id, referrer_id,"
                            + " visit_state, key_returned, enter_time)"
                            + " values (concat('TMP', uuid_short()), ?, ?, ?, ?, '在馆', 0, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, guest);
            ps.setLong(2, areaId);
            ps.setLong(3, lockerId);
            if (referrerId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, referrerId);
            }
            ps.setString(5, enterTime);
            return ps;
        }, kh);
        long id = kh.getKey().longValue();
        jdbc.update("update gym_visit set visit_code = ? where id = ?", String.format("VS-%04d", id), id);
        return id;
    }

    public int markKeyReturned(long id) {
        return jdbc.update("update gym_visit set key_returned = 1 where id = ? and visit_state = '在馆'", id);
    }

    public int markLeft(long id, String leaveTime) {
        return jdbc.update("update gym_visit set visit_state = '已离场', leave_time = ?"
                + " where id = ? and visit_state = '在馆'", leaveTime, id);
    }
}
