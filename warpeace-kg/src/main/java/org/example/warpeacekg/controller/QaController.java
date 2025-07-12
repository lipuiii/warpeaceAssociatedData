package org.example.warpeacekg.controller;

import org.example.warpeacekg.kgqa.QaParseService;
import org.example.warpeacekg.service.SparqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
public class QaController {

    @Autowired
    private QaParseService qaParseService;

    @Autowired
    private SparqlService sparqlService;

    @GetMapping("/answer")
    public Map<String, Object> getAnswer(@RequestParam("name") String question) {
        List<String> parseResult = qaParseService.extractEntityAndRelation(question);
        if (parseResult == null || parseResult.size() < 2) {
            return Map.of("msg", "无法识别问题中的人物和关系", "data", List.of());
        }

        String person = parseResult.get(0);
        String relationLabel = parseResult.get(1);

        // 映射中文关系关键词为 RDF 属性
        String predicate = switch (relationLabel) {
            case "父亲" -> "hasFather";
            case "母亲" -> "hasMother";
            case "妻子", "丈夫", "配偶" -> "hasSpouse";
            case "儿子", "女儿", "子女" -> "hasChild";
            case "朋友" -> "friendOf";
            case "恋人" -> "loverOf";
            case "兄弟", "姐妹", "兄妹" -> "hasSibling";
            case "仆人", "雇主", "精神指引" -> "employedBy";
            default -> "relatedTo";  // fallback
        };

        Map<String, Object> result = sparqlService.queryRelation(person, predicate, relationLabel);
        return Map.of(
                "msg", "success",
                "data", List.of(
                        result,
                        result.getOrDefault("textAnswer", "")
                )
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestParam("character_name") String name) {
        Map<String, String> profile = sparqlService.queryPersonProfile(name);

        if (profile.isEmpty()) {
            return Map.of(
                    "text", "<div class='alert alert-warning'><i class='fa fa-exclamation-triangle'></i> 未找到该人物信息</div>"
            );
        }

        // 构建 HTML 信息展示
        StringBuilder html = new StringBuilder("<dl class='basicInfo-block basicInfo-left'>");
        profile.forEach((key, value) -> {
            html.append("<dt>").append(key).append("</dt>")
                    .append("<dd>").append(value).append("</dd>");
        });
        html.append("</dl>");

        return Map.of("text", html.toString());
    }
}
