package com.gym.center.controller;

import com.gym.center.service.VisitService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService service;

    public VisitController(VisitService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String state,
                                          @RequestParam(required = false) String keyword) {
        return service.list(state, keyword);
    }

    /** 各区在馆人头看板。 */
    @GetMapping("/board")
    public List<Map<String, Object>> board() {
        return service.board();
    }

    /** 开体验入场单：称呼、训练区、更衣柜一次写清，容纳和占用在这一个动作里校验。 */
    @PostMapping
    public Map<String, Object> open(@RequestBody Map<String, Object> form) {
        return service.open(form);
    }

    /** 补记钥匙归还。 */
    @PutMapping("/{id}/key")
    public Map<String, Object> returnKey(@PathVariable Long id) {
        return service.returnKey(id);
    }

    /** 离场收口：钥匙没交回会被拦住。 */
    @PutMapping("/{id}/leave")
    public Map<String, Object> leave(@PathVariable Long id) {
        return service.leave(id);
    }
}
