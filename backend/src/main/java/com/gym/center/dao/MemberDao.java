package com.gym.center.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDao {

    private static final String BASE = "select id, member_code as memberCode, member_name as memberName,"
            + " phone, card_level as cardLevel, expire_date as expireDate, member_state as memberState"
            + " from gym_member";

    private final JdbcTemplate jdbc;

    public MemberDao(JdbcTemplate jdbc) {
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
        List<Map<String, Object>> rows = jdbc.queryForList(BASE + " where member_code = ?", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insert(String code, String name, String phone, String level, String expire, String state) {
        return jdbc.update("insert into gym_member (member_code, member_name, phone, card_level,"
                + " expire_date, member_state) values (?, ?, ?, ?, ?, ?)",
                code, name, phone, level, expire, state);
    }

    public int update(long id, String name, String phone, String level, String expire, String state) {
        return jdbc.update("update gym_member set member_name = ?, phone = ?, card_level = ?,"
                + " expire_date = ?, member_state = ? where id = ?", name, phone, level, expire, state, id);
    }
}
