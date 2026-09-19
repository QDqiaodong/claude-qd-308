package com.gym.center.controller;

import com.gym.center.service.TrialTicketService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trial-tickets")
public class TrialTicketController {

    private final TrialTicketService service;

    public TrialTicketController(TrialTicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String state,
                                          @RequestParam(required = false) String keyword) {
        return service.list(state, keyword);
    }

    /** 各训练区此刻的在馆人头，给开单和场务对账用。 */
    @GetMapping("/board")
    public List<Map<String, Object>> board() {
        return service.board();
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> form) {
        return service.create(form);
    }

    /** 前台补记：钥匙还了。 */
    @PostMapping("/{id}/key")
    public Map<String, Object> returnKey(@PathVariable Long id) {
        return service.returnKey(id);
    }

    /** 离场收口：钥匙没还的话，这里会被拦住。 */
    @PostMapping("/{id}/leave")
    public Map<String, Object> leave(@PathVariable Long id) {
        return service.leave(id);
    }
}
