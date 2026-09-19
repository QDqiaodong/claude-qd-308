package com.gym.center.controller;

import com.gym.center.service.AreaService;
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

/** 这一组接口进出的都是 Map，没有实体类。 */
@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaService service;

    public AreaController(AreaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String state,
                                          @RequestParam(required = false) String keyword) {
        return service.list(state, keyword);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> form) {
        return service.save(form);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> form) {
        form.put("id", id);
        return service.save(form);
    }
}
