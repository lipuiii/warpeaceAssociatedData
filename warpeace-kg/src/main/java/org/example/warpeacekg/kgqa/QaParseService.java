package org.example.warpeacekg.kgqa;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaParseService {
    public List<String> extractEntityAndRelation(String question) {
        if (question == null || question.isBlank()) return null;

        String person = null;
        String relation = null;

        // 方法1：从问题中匹配人物
        for (String character : WarAndPeaceCharacters.CHARACTERS) {
            if (question.contains(character)) {
                person = character;
                break;
            }
        }

        // 方法2：尝试通过“的”语法结构推测人物
        if (person == null && question.contains("的")) {
            String[] parts = question.split("的");
            if (parts.length > 1 && parts[0].length() >= 2) {
                person = parts[0];
            }
        }

        // 方法3：匹配关系关键词
        for (String keyword : RelationKeywords.KEYWORDS) {
            if (question.contains(keyword)) {
                relation = keyword;
                break;
            }
        }

        if (person != null && relation != null) {
            return List.of(person, relation, "n");  // "n"是为了兼容原问答结构
        }

        return null;
    }
}
