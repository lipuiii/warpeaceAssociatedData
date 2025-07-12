package org.example.warpeacekg.rdf;

import com.fasterxml.jackson.databind.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.io.*;
import java.nio.file.*;

public class json2ttl {
    private static final String NS       = "http://wrrc.org/wap#";
    private static final String GRAPH    = "http://wrrc.org/wap/graph";

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newInputStream(Paths.get("backend/src/main/java/rdf/characters.json")));

        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("", NS);

        /* 1. 家族 */
        for (int cat = 0; cat < 5; cat++) {
            Resource fam = m.createResource(NS + "Family_" + cat)
                    .addProperty(RDF.type, m.createResource(NS + "Family"))
                    .addLiteral(m.createProperty(NS + "familyName"), "家族_" + cat);
        }

        /* 2. 人物 + 数据属性 */
        JsonNode persons = root.get("人物数据");
        persons.fields().forEachRemaining(e -> {
            String id   = e.getKey().replaceAll("\\s+", "_");
            JsonNode d  = e.getValue();

            Resource p = m.createResource(NS + id)
                    .addProperty(RDF.type, m.createResource(NS + "Person"))
                    .addLiteral(m.createProperty(NS + "chineseName"), d.get("中文名").asText())
                    .addLiteral(m.createProperty(NS + "russianName"), d.get("俄文名").asText())
                    .addLiteral(m.createProperty(NS + "nationality"), d.get("国籍").asText())
                    .addLiteral(m.createProperty(NS + "ethnicity"), d.get("民族").asText())
                    .addLiteral(m.createProperty(NS + "firstAppearance"), d.get("出处").asText())
                    .addLiteral(m.createProperty(NS + "title"), d.get("身份").asText())
                    .addProperty(m.createProperty(NS + "belongsToFamily"),
                            m.createResource(NS + "Family_" + d.get("category").asInt()));

            /* 3. 单值关系 */
            addSingle(m, p, "hasFather",    d, "父亲");
            addSingle(m, p, "hasChild",     d, "儿子");
            addSingle(m, p, "hasChild",     d, "女儿");
            addSingle(m, p, "hasSpouse",    d, "妻子");
            addSingle(m, p, "hasSpouse",    d, "丈夫");
            addSingle(m, p, "friendOf",     d, "好友");
            addSingle(m, p, "loverOf",      d, "恋人");
            addSingle(m, p, "employedBy",   d, "主人");
            addSingle(m, p, "employedBy",   d, "雇主");
            addSingle(m, p, "employedBy",   d, "照顾对象");
            addSingle(m, p, "employedBy",   d, "学生");
            addSingle(m, p, "employedBy",   d, "精神指引");

            /* 4. 数组关系 */
            addArray(m, p, "hasChild",   d, "子女");
            addArray(m, p, "hasSibling", d, "姐妹");
            addArray(m, p, "hasSibling", d, "兄弟");
            addArray(m, p, "hasSibling", d, "哥哥");
            addArray(m, p, "hasSibling", d, "姐姐");
            addArray(m, p, "hasSibling", d, "弟弟");
            addArray(m, p, "hasSibling", d, "妹妹");
        });

        /* 5. 关系链接 */
        root.get("关系链接").forEach(l -> {
            String src = l.get("source").asText().replaceAll("\\s+", "_");
            String tar = l.get("target").asText().replaceAll("\\s+", "_");
            String rel = map(l.get("value").asText());
            m.add(m.createResource(NS + src), m.createProperty(NS + rel), m.createResource(NS + tar));
        });

        /* 6. 写出 */
        try (OutputStream out = Files.newOutputStream(Paths.get("backend/src/main/java/rdf/war_and_peace_data.ttl"))) {
            m.write(out, "Turtle");
            System.out.println("war_and_peace_data.ttl 已生成");
        }
    }

    /* ---------- 工具 ---------- */
    private static void addSingle(Model m, Resource s, String prop, JsonNode d, String key) {
        if (d.has(key) && !d.get(key).isNull()) {
            String v = d.get(key).asText();
            m.add(s, m.createProperty(NS + prop), m.createResource(NS + v.replaceAll("\\s+", "_")));
        }
    }

    private static void addArray(Model m, Resource s, String prop, JsonNode d, String key) {
        if (!d.has(key)) return;
        JsonNode arr = d.get(key);
        if (!arr.isArray()) return;
        for (JsonNode n : arr) {
            String v = n.asText();
            m.add(s, m.createProperty(NS + prop), m.createResource(NS + v.replaceAll("\\s+", "_")));
        }
    }

    private static String map(String zh) {
        switch (zh) {
            case "父子": return "hasChild";
            case "叔侄": return "hasNiece";
            case "夫妻": return "hasSpouse";
            case "仆人": case "保姆": case "车夫": case "厨子": case "驯犬师": case "女管家": case "猎手":
            case "近侍": case "马夫": case "花匠": case "乐师": case "舞蹈教师": case "家庭教师":
            case "女仆": case "仆从": case "女教徒": case "精神指引":
                return "employedBy";
            case "密友": return "friendOf";
            case "恋慕": return "loverOf";
            case "狱友": return "cellmateOf";
            default: return "relatedTo";
        }
    }
}