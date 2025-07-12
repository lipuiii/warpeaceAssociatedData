package org.example.warpeacekg.controller;

import org.example.warpeacekg.service.SparqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // 跨域允许前端调用
public class Controller {

    @Autowired
    private SparqlService sparqlQueryService;

    @GetMapping("/person/{name}/graph")
    public Map<String, Object> getPersonGraph(@PathVariable("name") String name) {
        return sparqlQueryService.getPersonRelationGraph(name);
    }
    @GetMapping("/graph")
    public Map<String, Object> getGraph(@RequestParam(defaultValue = "all") String type) {
        if ("all".equals(type)) {
            return sparqlQueryService.getAllRelationsGraph();
        }
        // 可扩展其他类型
        return Map.of();
    }

}
