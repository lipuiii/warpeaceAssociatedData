package org.example.warpeacekg.rdf;

import com.fasterxml.jackson.databind.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Json2TtlExtended {
    private static final String NS    = "http://wrrc.org/wap#";
    private static final String GRAPH = "http://wrrc.org/wap/graph";

    /* ---------- 主方法 ---------- */
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newInputStream(
                Paths.get("src/main/java/org/example/warpeacekg/rdf/character2.json")));

        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("", NS);

        /* 1. 家族实例（具体命名） */
        Map<Integer, String> familyMap = Map.of(
                0, "别祖霍夫家族",
                1, "罗斯托夫家族",
                2, "库拉金家族",
                3, "鲍尔康斯基家族",
                4, "其他"
        );



        familyMap.forEach((cat, id) -> {
            Resource fam = m.createResource(NS + id)
                    .addProperty(RDF.type, m.createResource(NS + "Family"))
                    .addLiteral(m.createProperty(NS + "familyName"), id);
        });
        /* 2. 人物数据 */
        JsonNode persons = root.get("人物数据");
        persons.fields().forEachRemaining(e -> {
            String id  = clean(e.getKey());
            JsonNode d = e.getValue();



            Resource p = m.createResource(NS + id)
                    .addProperty(RDF.type, m.createResource(NS + "Person"))
                    .addLiteral(m.createProperty(NS + "chineseName"),   text(d, "中文名"))
                    .addLiteral(m.createProperty(NS + "russianName"),   text(d, "俄文名"))
                    .addLiteral(m.createProperty(NS + "nationality"),   text(d, "国籍"))
                    .addLiteral(m.createProperty(NS + "ethnicity"),     text(d, "民族"))
                    .addLiteral(m.createProperty(NS + "firstAppearance"),text(d, "出处"))
                    .addLiteral(m.createProperty(NS + "title"),         text(d, "身份"))
                    .addProperty(m.createProperty(NS + "belongsToFamily"),
                            m.createResource(NS + familyMap.get(d.get("category").asInt())));

            /* 2.1 单值对象属性 */
            addSingleRelation(m, p, "hasFather",    d, "父亲");
            addSingleRelation(m, p, "hasMother",    d, "母亲");
            addSingleRelation(m, p, "hasSpouse",    d, "妻子");
            addSingleRelation(m, p, "hasSpouse",    d, "丈夫");
            addSingleRelation(m, p, "hasChild",     d, "儿子");
            addSingleRelation(m, p, "hasChild",     d, "女儿");
            addSingleRelation(m, p, "friendOf",     d, "好友");
            addSingleRelation(m, p, "loverOf",      d, "恋人");

            /* 2.2 数组对象属性 */
            addArrayRelation(m, p, "hasChild",   d, "子女");
            addArrayRelation(m, p, "hasSibling", d, "姐妹");
            addArrayRelation(m, p, "hasSibling", d, "兄弟");
            addArrayRelation(m, p, "hasSibling", d, "哥哥");
            addArrayRelation(m, p, "hasSibling", d, "姐姐");
            addArrayRelation(m, p, "hasSibling", d, "弟弟");
            addArrayRelation(m, p, "hasSibling", d, "妹妹");
            addArrayRelation(m, p, "hasNiece",   d, "侄女");

            /* 2.3 主仆/雇佣/职业 */
            addServantRelations(m, p, d);

            /* 2.4 职业实例 */
            addOccupation(m, p, d);
        });

        /* 3. 事件实例 */
        JsonNode events = root.get("历史事件");
        events.fields().forEachRemaining(e -> {
            String id  = clean(e.getKey());
            JsonNode d = e.getValue();
            Resource evt = m.createResource(NS + id)
                    .addProperty(RDF.type, m.createResource(NS + "Event"))
                    .addLiteral(m.createProperty(NS + "eventDate"),    text(d, "日期"))
                    .addLiteral(m.createProperty(NS + "eventLocation"), text(d, "地点"))
                    .addLiteral(m.createProperty(NS + "eventResult"),   text(d, "结果"));

            /* 事件 ↔ 人物 */
            if (d.has("相关人物")) {
                d.get("相关人物").forEach(n ->
                        m.add(evt, m.createProperty(NS + "involvesPerson"),
                                m.createResource(NS + clean(n.asText()))));
            }
        });

        /* 4. 关系链接（兜底） */
        root.get("关系链接").forEach(l -> {
            String src = clean(l.get("source").asText());
            String tar = clean(l.get("target").asText());
            String rel = map(l.get("value").asText());
            m.add(m.createResource(NS + src), m.createProperty(NS + rel), m.createResource(NS + tar));
        });

        /* 5. 写出 Turtle（可导入 Virtuoso） */
        try (OutputStream out = Files.newOutputStream(
                Paths.get("src/main/java/org/example/warpeacekg/rdf/war_and_peace_data_new.ttl"))) {
            m.write(out, "Turtle");
            System.out.println("✅ war_and_peace_data_new.ttl 已生成");
        }
    }

    /* ---------- 工具 ---------- */
    private static String text(JsonNode n, String key) {
        return n.has(key) && !n.get(key).isNull() ? n.get(key).asText() : "";
    }

    private static String clean(String s) {
        return s.replaceAll("\\s+", "_");
    }

    private static void addSingleRelation(Model m, Resource s, String prop, JsonNode d, String key) {
        if (d.has(key) && !d.get(key).isNull()) {
            m.add(s, m.createProperty(NS + prop),
                    m.createResource(NS + clean(d.get(key).asText())));
        }
    }

    private static void addArrayRelation(Model m, Resource s, String prop, JsonNode d, String key) {
        if (!d.has(key)) return;
        JsonNode arr = d.get(key);
        if (arr.isArray()) {
            arr.forEach(n -> m.add(s, m.createProperty(NS + prop),
                    m.createResource(NS + clean(n.asText()))));
        }
    }

    private static void addServantRelations(Model m, Resource s, JsonNode d) {
        for (String k : Arrays.asList("主人", "雇主", "照顾对象", "学生", "精神指引")) {
            if (d.has(k) && !d.get(k).isNull()) {
                Resource o = m.createResource(NS + clean(d.get(k).asText()));
                m.add(s, m.createProperty(NS + "employedBy"), o);
            }
        }
    }

    private static void addOccupation(Model m, Resource p, JsonNode d) {
        String occ = text(d, "身份");
        if (!occ.isEmpty()) {
            Resource o = m.createResource(NS + "Occupation_" + clean(occ));
            o.addProperty(RDF.type, m.createResource(NS + "Occupation"))
                    .addLiteral(m.createProperty(NS + "occupationTitle"), occ);
            m.add(p, m.createProperty(NS + "hasOccupation"), o);
        }
    }

    private static final Map<String, String> RelationPatterns = Map.ofEntries(
            /* 亲属（可自动反向） */
            Map.entry("父.*子",  "hasChild"),
            Map.entry("父.*女",  "hasChild"),
            Map.entry("母.*子",  "hasChild"),
            Map.entry("母.*女",  "hasChild"),
            Map.entry("夫妻",    "hasSpouse"),
            Map.entry("叔.*侄",  "hasNiece"),
            Map.entry("兄弟.*",  "hasSibling"),
            Map.entry("姐妹.*",  "hasSibling"),
            Map.entry("哥哥.*",  "hasSibling"),
            Map.entry("姐姐.*",  "hasSibling"),
            Map.entry("弟弟.*",  "hasSibling"),
            Map.entry("妹妹.*",  "hasSibling"),

            /* 主仆 / 雇佣 */
            Map.entry("仆人|保姆|车夫|厨子|驯犬师|女管家|猎手|近侍|马夫|花匠|乐师|舞蹈教师|家庭教师|女仆|仆从|侍者|管家", "employedBy"),

            /* 事件-人物 快速关键字 */
            Map.entry("参战|负伤|亲历|支援|指挥|决策|放弃|撤退|溃退", "hasRoleInEvent"),
            Map.entry("财产损失", "familyAffectedBy"),
            Map.entry("狱友",    "cellmateOf"),
            Map.entry("密友",    "friendOf"),
            Map.entry("恋慕",    "loverOf")
    );

    /* ---------- 实际映射函数 ---------- */
    private static String map(String zh) {
        String key = zh.replaceAll("\\s+", "");          // 去空格
        for (Map.Entry<String, String> e : RelationPatterns.entrySet()) {
            if (key.matches(".*" + e.getKey() + ".*")) {
                return e.getValue();
            }
        }
        return "relatedTo";   // 兜底
    }
}