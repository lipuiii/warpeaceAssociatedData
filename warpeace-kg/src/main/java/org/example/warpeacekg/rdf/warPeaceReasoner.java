package org.example.warpeacekg.rdf;

import org.apache.jena.rdf.model.*;

import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class warPeaceReasoner {

    public static void main(String[] args) throws Exception {
        // 1. 加载原始 TTL 数据
        Model rawModel = ModelFactory.createDefaultModel();
        rawModel.read(new FileInputStream("src/main/java/org/example/warpeacekg/rdf/war_and_peace_data_new.ttl"), null, "TURTLE");

        // 2. 加载规则文件
        List<Rule> rules = Rule.rulesFromURL("src/main/java/org/example/warpeacekg/rdf/warpeace_rules.rules");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);

        // 3. 执行推理
        InfModel infModel = ModelFactory.createInfModel(reasoner, rawModel);

        // 4. 合并推理结果（infModel 包含 rawModel，无需重复添加）
        // 注意：此处 infModel 已包含原始数据 + 推理结论
        // 可直接写出合并结果
        try (FileOutputStream out = new FileOutputStream("src/main/java/org/example/warpeacekg/rdf/war_and_peace_merged.ttl")) {
            RDFDataMgr.write(out, infModel, Lang.TURTLE);
            System.out.println("✅ 合并后的TTL文件已写出：war_and_peace_merged.ttl");
        }
    }
}
