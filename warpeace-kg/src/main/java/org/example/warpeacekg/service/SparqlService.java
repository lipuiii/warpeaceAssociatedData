package org.example.warpeacekg.service;

import org.apache.jena.query.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SparqlService {

    private static final String SPARQL_ENDPOINT = "http://localhost:8890/sparql";
    private static final String GRAPH_URI = "http://wrrc.org/wap/graph";
    private static final String PREFIX = "http://wrrc.org/wap#";

    /**
     * 查询某个人物的关系，转换成ECharts可用格式
     */
    public Map<String, Object> getPersonRelationGraph(String personChineseName) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> addedNodes = new HashSet<>();

        String sparql = """
            PREFIX wap: <http://wrrc.org/wap#>
            SELECT ?pName ?pFamily ?relation ?nName ?nFamily WHERE {
              GRAPH <http://wrrc.org/wap/graph> {
                ?person wap:chineseName ?pName .
                FILTER(STR(?pName) = "%s")  # 用STR包裹，忽略类型
            
                OPTIONAL { ?person wap:belongsToFamily ?pfamilyURI . ?pfamilyURI wap:familyName ?pFamily . }
            
                ?person ?relation ?other .
                ?other wap:chineseName ?nName .
                OPTIONAL { ?other wap:belongsToFamily ?nFamilyURI . ?nFamilyURI wap:familyName ?nFamily . }
            
                FILTER(STRSTARTS(STR(?relation), STR(wap:)))
                FILTER(?relation != wap:chineseName && ?relation != wap:belongsToFamily)
              }
            }
            """.formatted(personChineseName);

        Query query = QueryFactory.create(sparql);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.next();

                String pName = soln.getLiteral("pName").getString();
                String pFamily = soln.contains("pFamily") ? soln.getLiteral("pFamily").getString() : "未知";
                String nName = soln.getLiteral("nName").getString();
                String nFamily = soln.contains("nFamily") ? soln.getLiteral("nFamily").getString() : "未知";
                String relation = soln.get("relation").toString();
                String relationKey = relation.substring(relation.lastIndexOf("#") + 1);
                String relationLabel = switch (relationKey) {
                    case "hasFather"     -> "父亲";
                    case "hasMother"     -> "母亲";
                    case "hasChild"      -> "子女";        // 默认统一为子女
                    case "hasSpouse"     -> "配偶";
                    case "hasSibling"    -> "兄弟姐妹";
                    case "hasNiece"      -> "叔侄";
                    case "friendOf"      -> "好友";
                    case "loverOf"       -> "恋人";
                    case "cellmateOf"    -> "狱友";
                    case "employedBy"    -> "雇佣关系";    // 涵盖仆人/学生/精神指引/教师等

                    default -> relationKey; // 默认保留原始标签名
                };

                if (addedNodes.add(pName)) {
                    nodes.add(Map.of("name", pName, "category", pFamily));
                }
                if (addedNodes.add(nName)) {
                    nodes.add(Map.of("name", nName, "category", nFamily));
                }

                links.add(Map.of("source", pName, "target", nName, "value", relationLabel));
            }
        }

        return Map.of("data", nodes, "links", links);
    }
    public Map<String, Object> getAllRelationsGraph() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> addedNodes = new HashSet<>();
        Set<String> addedFamilies = new HashSet<>();
        List<Map<String, Object>> categories = new ArrayList<>();

        String sparql = """
        PREFIX wap: <http://wrrc.org/wap#>
        SELECT ?pName ?pFamily ?relation ?nName ?nFamily WHERE {
          GRAPH <http://wrrc.org/wap/graph> {
            ?person wap:chineseName ?pName .
            OPTIONAL {
                ?person wap:belongsToFamily ?pfamilyURI .
                ?pfamilyURI wap:familyName ?pFamily .
            }
            ?person ?relation ?other .
            ?other wap:chineseName ?nName .
            OPTIONAL {
                ?other wap:belongsToFamily ?nFamilyURI .
                ?nFamilyURI wap:familyName ?nFamily .
            }
            FILTER(STRSTARTS(STR(?relation), STR(wap:)))
            FILTER(?relation != wap:chineseName && ?relation != wap:belongsToFamily)
          }
        }
        """;

        Query query = QueryFactory.create(sparql);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.next();

                String pName = soln.getLiteral("pName").getString();
                String pFamily = soln.contains("pFamily") ? soln.getLiteral("pFamily").getString() : "未知";
                String nName = soln.getLiteral("nName").getString();
                String nFamily = soln.contains("nFamily") ? soln.getLiteral("nFamily").getString() : "未知";
                String relation = soln.get("relation").toString();
                String relationKey = relation.substring(relation.lastIndexOf("#") + 1);
                String relationLabel = switch (relationKey) {
                    case "hasFather"     -> "父亲";
                    case "hasMother"     -> "母亲";
                    case "hasChild"      -> "子女";        // 默认统一为子女
                    case "hasSpouse"     -> "配偶";
                    case "hasSibling"    -> "兄弟姐妹";
                    case "hasNiece"      -> "叔侄";
                    case "friendOf"      -> "好友";
                    case "loverOf"       -> "恋人";
                    case "cellmateOf"    -> "狱友";
                    case "employedBy"    -> "雇佣关系";    // 涵盖仆人/学生/精神指引/教师等

                    default -> relationKey; // 默认保留原始标签名
                };

                // 添加分类
                if (addedFamilies.add(pFamily)) {
                    categories.add(Map.of("name", pFamily));
                }
                if (addedFamilies.add(nFamily)) {
                    categories.add(Map.of("name", nFamily));
                }

                // 添加节点
                if (addedNodes.add(pName)) {
                    nodes.add(Map.of("name", pName, "category", pFamily));
                }
                if (addedNodes.add(nName)) {
                    nodes.add(Map.of("name", nName, "category", nFamily));
                }

                // 添加关系
                links.add(Map.of("source", pName, "target", nName, "value", relationLabel));
            }
        }

        return Map.of("data", nodes, "links", links, "categories", categories);
    }
    public Map<String, Object> queryRelation(String personName, String predicate, String relationLabel) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        // 用FILTER包含匹配找到目标人物，再查询关系目标
        String sparql = String.format("""
        PREFIX wap: <http://wrrc.org/wap#>
        SELECT ?targetName ?targetFamily WHERE {
          GRAPH <http://wrrc.org/wap/graph> {
            ?person wap:chineseName ?name .
            FILTER(CONTAINS(STR(?name), "%s"))  # 用STR包裹
            ?person wap:%s ?target .
            ?target wap:chineseName ?targetName .
            OPTIONAL {
              ?target wap:belongsToFamily ?famURI .
              ?famURI wap:familyName ?targetFamily .
            }
          }
        }
       
        """, personName, predicate);

        Query query = QueryFactory.create(sparql);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)) {
            ResultSet results = qexec.execSelect();

            // 先添加查询人物节点（只一次）
            nodes.add(Map.of("name", personName, "category", "查询人物"));

            while (results.hasNext()) {
                QuerySolution soln = results.next();

                String target = soln.getLiteral("targetName").getString();
                String targetFamily = soln.contains("targetFamily") ? soln.getLiteral("targetFamily").getString() : "未知";

                nodes.add(Map.of("name", target, "category", targetFamily));
                links.add(Map.of("source", personName, "target", target, "value", relationLabel));
            }
        }

        String textAnswer;
        if (!links.isEmpty()) {
            Object firstTarget = links.get(0).get("target");
            String targetName = firstTarget != null ? firstTarget.toString() : "未知";
            textAnswer = personName + "的" + relationLabel + "是：" + targetName;
        } else {
            textAnswer = "未找到" + personName + "的" + relationLabel;
        }

        return Map.of(
                "data", nodes,
                "links", links,
                "textAnswer", textAnswer
        );
    }

    public Map<String, String> queryPersonProfile(String name) {
        Map<String, String> profile = new LinkedHashMap<>();

        // 用FILTER包含匹配找到目标人物，再查询属性
        String sparql = String.format("""
        PREFIX wap: <http://wrrc.org/wap#>
        SELECT ?p ?o WHERE {
          GRAPH <http://wrrc.org/wap/graph> {
            ?person wap:chineseName ?name .
            FILTER(CONTAINS(STR(?name), "%s"))  # 加STR
            ?person ?p ?o .
          }
        }
        """, name);

        Query query = QueryFactory.create(sparql);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution soln = results.next();
                String predicate = soln.get("p").toString();
                String object = soln.get("o").isLiteral() ? soln.getLiteral("o").getString() : soln.get("o").toString();

                String label = switch (predicate.substring(predicate.lastIndexOf("#") + 1)) {
                    case "chineseName" -> "中文名";
                    case "russianName" -> "俄文名";
                    case "nationality" -> "国籍";
                    case "ethnicity" -> "民族";
                    case "firstAppearance" -> "首次登场";
                    case "title" -> "头衔/称谓";
                    case "identity" -> "身份";
                    case "source" -> "出自";
                    case "belongsToFamily" -> "所属家族";
                    default -> null;
                };

                if (label != null && !profile.containsKey(label)) {
                    profile.put(label, object);
                }
            }
        }

        return profile;
    }

}
