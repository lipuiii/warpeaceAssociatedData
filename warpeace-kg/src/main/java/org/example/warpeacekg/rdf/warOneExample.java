package org.example.warpeacekg.rdf;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class warOneExample {
    public static void main(String[] args) {
        String NS = "http://wrrc.org/wap#";

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // Ontology header
        Ontology ontology = model.createOntology("http://wrrc.org/wap");
        ontology.addLabel("War-and-Peace Minimal Ontology (with Family)", "en");
        ontology.addComment("Ultra-lightweight ontology for character relations and family membership in War and Peace", "en");

        // -------------------------------
        // Classes
        // -------------------------------
        OntClass person = model.createClass(NS + "Person");
        person.addLabel("Person", "en");
        person.addLabel("人物", "zh");
        person.addComment("Any real person appearing in the novel", "en");

        OntClass family = model.createClass(NS + "Family");
        family.addLabel("Family", "en");
        family.addLabel("家族", "zh");
        family.addComment("A noble family or household", "en");

        // -------------------------------
        // Object Properties
        // -------------------------------
        createObjectProperty(model, NS, "hasFather", person, person, "has father", "父亲", true, NS + "childOf");
        createObjectProperty(model, NS, "hasMother", person, person, "has mother", "母亲", true, NS + "childOf");
        createSymmetricProperty(model, NS, "hasSpouse", person, person, "has spouse", "配偶");
        createObjectProperty(model, NS, "hasChild", person, person, "has child", "子女", true, NS + "childOf");
        createObjectProperty(model, NS, "childOf", person, person, "child of", "子/女", false, null);
        createSymmetricProperty(model, NS, "hasSibling", person, person, "has sibling", "兄弟姐妹");

        createObjectProperty(model, NS, "serves", person, person, "serves", "服务于", true, NS + "employedBy");
        createObjectProperty(model, NS, "employedBy", person, person, "employed by", "受雇于", false, null);
        createSymmetricProperty(model, NS, "friendOf", person, person, "friend of", "好友");
        createSymmetricProperty(model, NS, "loverOf", person, person, "lover of", "恋慕");
        createSymmetricProperty(model, NS, "cellmateOf", person, person, "cellmate of", "狱友");

        createObjectProperty(model, NS, "belongsToFamily", person, family, "belongs to family", "属于家族", false, null);

        // -------------------------------
        // Data Properties
        // -------------------------------
        createDataProperty(model, NS, "chineseName", person, XSD.xstring.getURI(), "Chinese name", "中文名");
        createDataProperty(model, NS, "russianName", person, XSD.xstring.getURI(), "Russian name", "俄文名");
        createDataProperty(model, NS, "nationality", person, XSD.xstring.getURI(), "nationality", "国籍");
        createDataProperty(model, NS, "ethnicity", person, XSD.xstring.getURI(), "ethnicity", "民族");
        createDataProperty(model, NS, "firstAppearance", person, XSD.xstring.getURI(), "first appearance", "首次出场");
        createDataProperty(model, NS, "title", person, XSD.xstring.getURI(), "title / identity", "身份");

        // -------------------------------
        // Write TTL file
        // -------------------------------
        try (OutputStream out = new FileOutputStream("./backend/src/main/java/rdf/war_and_peace_ontology.ttl")) {
            model.write(out, "Turtle");
            System.out.println("Ontology written to war_and_peace_ontology.ttl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 工具方法：创建对象属性
    private static void createObjectProperty(OntModel model, String ns, String name, OntClass domain, OntClass range,
                                             String labelEN, String labelZH, boolean hasInverse, String inverseURI) {
        ObjectProperty prop = model.createObjectProperty(ns + name);
        prop.addLabel(labelEN, "en");
        prop.addLabel(labelZH, "zh");
        prop.addDomain(domain);
        prop.addRange(range);
        if (hasInverse && inverseURI != null) {
            ObjectProperty inverse = model.getObjectProperty(inverseURI);
            if (inverse != null) {
                prop.addInverseOf(inverse);
            } else {
                inverse = model.createObjectProperty(inverseURI);
                prop.addInverseOf(inverse);
            }
        }
    }

    // 工具方法：创建对称属性
    private static void createSymmetricProperty(OntModel model, String ns, String name, OntClass domain, OntClass range,
                                                String labelEN, String labelZH) {
        ObjectProperty prop = model.createObjectProperty(ns + name);
        prop.addLabel(labelEN, "en");
        prop.addLabel(labelZH, "zh");
        prop.addDomain(domain);
        prop.addRange(range);
        prop.addRDFType(OWL.SymmetricProperty);
    }

    // 工具方法：创建数据属性
    private static void createDataProperty(OntModel model, String ns, String name, OntClass domain, String xsdRange,
                                           String labelEN, String labelZH) {
        DatatypeProperty prop = model.createDatatypeProperty(ns + name);
        prop.addLabel(labelEN, "en");
        prop.addLabel(labelZH, "zh");
        prop.addDomain(domain);
        prop.addRange(model.getResource(xsdRange));
    }
}
