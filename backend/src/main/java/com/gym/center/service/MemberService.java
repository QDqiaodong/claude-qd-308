package com.gym.center.service;

import static com.gym.center.service.AreaService.num;
import static com.gym.center.service.AreaService.str;

import com.gym.center.dao.MemberDao;
import com.gym.center.dto.BizException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberDao dao;

    public MemberService(MemberDao dao) {
        this.dao = dao;
    }

    /** 顺带把「过期没」也算出来一起给前端。 */
    public List<Map<String, Object>> list(String state, String keyword) {
        LocalDate today = LocalDate.now();
        return dao.list().stream()
                .filter(r -> state == null || state.isBlank() || state.equals(r.get("memberState")))
                .filter(r -> keyword == null || keyword.isBlank()
                        || String.valueOf(r.get("memberCode")).contains(keyword)
                        || String.valueOf(r.get("memberName")).contains(keyword))
                .peek(r -> {
                    String exp = str(r.get("expireDate"));
                    r.put("expired", exp != null && LocalDate.parse(exp).isBefore(today));
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> save(Map<String, Object> form) {
        Long id = num(form.get("id"));
        Map<String, Object> existed = id == null ? null : dao.one(id);
        String code = str(form.get("memberCode"));
        String name = str(form.get("memberName"));
        if (existed != null) {
            if (code == null) {
                code = str(existed.get("memberCode"));
            }
            if (name == null) {
                name = str(existed.get("memberName"));
            }
        }
        if (code == null || name == null) {
            throw new BizException("会员编号和姓名都得填");
        }
        Map<String, Object> same = dao.byCode(code);
        if (same != null && (id == null || !num(same.get("id")).equals(id))) {
            throw new BizException("会员编号 " + code + " 重复了");
        }
        String phone = str(form.get("phone"));
        if (phone != null && !phone.matches("\\d{11}")) {
            throw new BizException("手机号得是 11 位数字");
        }
        if (phone == null && existed != null) {
            phone = str(existed.get("phone"));
        }
        String level = str(form.get("cardLevel"));
        if (level == null && existed != null) {
            level = str(existed.get("cardLevel"));
        }
        String expire = str(form.get("expireDate"));
        if (expire == null && existed != null) {
            expire = str(existed.get("expireDate"));
        }
        String state = str(form.get("memberState"));
        if (state == null) {
            state = existed == null ? "正常" : str(existed.get("memberState"));
        }
        if (existed == null) {
            dao.insert(code, name, phone, level, expire, state);
        } else {
            dao.update(id, name, phone, level, expire, state);
        }
        return dao.one(dao.byCode(code) == null ? id : num(dao.byCode(code).get("id")));
    }
}
