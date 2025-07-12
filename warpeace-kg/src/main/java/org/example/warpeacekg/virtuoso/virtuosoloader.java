package org.example.warpeacekg.virtuoso;

import virtuoso.jena.driver.VirtGraph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;

public class virtuosoloader {

    public static VirtGraph connectVirtuosoDB() {
        String urlDB = "jdbc:virtuoso://localhost:1111";
        String username = "dba";
        String password = "dba";
        String graph = "http://wrrc.org/wap/graph"; // 图的 URI

        VirtGraph virtGraph = new VirtGraph(graph, urlDB, username, password);

        System.out.println("Graph中RDF数据条数：" + virtGraph.getCount());
        System.out.println("连接Virtuoso示例结束");
        return virtGraph;
    }

    public static void insertVirtuosoRDFData(VirtGraph virtGraph, String ttlFilePath) {
        // 创建一个 Jena Model
        Model model = ModelFactory.createDefaultModel();

        // 读取 Turtle 文件并写入图
        RDFDataMgr.read(model, ttlFilePath);

        // 将 Model 中的数据添加到 VirtGraph
        model.listStatements().forEachRemaining(stmt -> {
            virtGraph.add(stmt.getSubject().asNode(), stmt.getPredicate().asNode(), stmt.getObject().asNode());
        });

        // 统计插入后的三元组数量
        long count = virtGraph.getCount();
        System.out.println("插入后，Graph中RDF数据条数：" + count);

        System.out.println("验证插入的数据：");
        virtGraph.find(null, null, null).forEachRemaining(triple -> {
            System.out.println(triple.toString());
        });

        System.out.println("连接Virtuoso插入RDF数据示例结束");
    }

    public static void main(String[] args) {
        // 连接到 Virtuoso 数据库
        VirtGraph virtGraph = connectVirtuosoDB();

        // 插入 RDF 数据
        insertVirtuosoRDFData(virtGraph, "src/main/java/org/example/warpeacekg/rdf/war_and_peace_merged.ttl");
    }
}