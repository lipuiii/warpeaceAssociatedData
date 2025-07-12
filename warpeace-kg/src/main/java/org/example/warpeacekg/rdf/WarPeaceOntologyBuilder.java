package org.example.warpeacekg.rdf;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class WarPeaceOntologyBuilder {

    public static void main(String[] args) {
        final String NS = "http://wrrc.org/wap#";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Ontology o = m.createOntology("http://wrrc.org/wap");
        o.addLabel("War-and-Peace Extended Ontology", "en");
        o.addLabel("战争与和平扩展本体", "zh");
        o.addComment("Complete lightweight ontology covering Person, Family, Event, Philosophy, Religion, Society, Culture, Military", "en");

        /* ------------------ 1. 类 ------------------ */
        OntClass person   = cls(m, NS + "Person",   "Person", "人物");
        OntClass family   = cls(m, NS + "Family",   "Family", "家族");
        OntClass event    = cls(m, NS + "Event",    "Historical Event", "历史事件");
        OntClass philo    = cls(m, NS + "Philosophy", "Philosophy", "思想哲学");
        OntClass relig    = cls(m, NS + "ReligiousSystem", "Religious System", "宗教体系");
        OntClass society  = cls(m, NS + "SocialInstitution", "Social Institution", "社会制度");
        OntClass culture  = cls(m, NS + "CulturalSymbol", "Cultural Symbol", "文化符号");
        OntClass military = cls(m, NS + "MilitaryUnit", "Military Unit", "军事单位");
        OntClass occup    = cls(m, NS + "Occupation", "Occupation", "职业");

        /* ------------------ 2. 人物 ↔ 人物 ------------------ */
        op(m, NS + "hasFather", person, person, "has father", "父亲", true, NS + "childOf");
        op(m, NS + "hasMother", person, person, "has mother", "母亲", true, NS + "childOf");
        op(m, NS + "hasChild",  person, person, "has child",  "子女",  true, NS + "childOf");
        op(m, NS + "childOf",   person, person, "child of",   "子/女", false, null);

        symOp(m, NS + "hasSpouse",  person, person, "has spouse",  "配偶");
        symOp(m, NS + "hasSibling", person, person, "has sibling", "兄弟姐妹");
        symOp(m, NS + "loverOf",    person, person, "lover of",    "恋慕");
        symOp(m, NS + "friendOf",   person, person, "friend of",   "好友");
        symOp(m, NS + "cellmateOf", person, person, "cellmate of", "狱友");

        /* ------------------ 3. 人物 ↔ 家族 ------------------ */
        op(m, NS + "belongsToFamily", person, family, "belongs to family", "属于家族", false, null);
        op(m, NS + "familyAffectedBy", family, event, "family affected by", "家族受影响于", false, null);

        /* ------------------ 4. 人物 ↔ 事件 / 哲学 / 宗教 / 制度 / 文化 / 军事 / 职业 ------------------ */
        op(m, NS + "hasRoleInEvent",     person, event,    "has role in event",     "在事件中担任角色", false, null);
        op(m, NS + "adheresToPhilosophy",person, philo,    "adheres to philosophy",   "信奉思想", false, null);
        op(m, NS + "practicesReligion",  person, relig,    "practices religion",      "实践宗教", false, null);
        op(m, NS + "subjectToInstitution",person,society,  "subject to institution",  "受制于制度", false, null);
        op(m, NS + "embodiesSymbol",     person, culture,  "embodies symbol",         "体现文化符号", false, null);
        op(m, NS + "servesInUnit",       person, military, "serves in unit",          "服役单位", false, null);
        op(m, NS + "hasOccupation",      person, occup,    "has occupation",          "职业", false, null);

        /* ------------------ 5. 主仆 / 雇佣 ------------------ */
        op(m, NS + "employedBy", person, person, "employed by", "受雇于", true, NS + "serves");
        op(m, NS + "serves",     person, person, "serves",      "服务于", true, NS + "employedBy");

        /* ------------------ 6. 数据属性 ------------------ */
        dp(m, NS + "chineseName",   person, XSD.xstring, "Chinese name", "中文名");
        dp(m, NS + "russianName",   person, XSD.xstring, "Russian name", "俄文名");
        dp(m, NS + "nationality",   person, XSD.xstring, "nationality", "国籍");
        dp(m, NS + "ethnicity",     person, XSD.xstring, "ethnicity", "民族");
        dp(m, NS + "title",         person, XSD.xstring, "title / identity", "身份");
        dp(m, NS + "firstAppearance",person,XSD.xstring, "first appearance", "首次出场");
        dp(m, NS + "eventDate",      event,  XSD.date,    "event date", "事件日期");
        dp(m, NS + "eventLocation",  event,  XSD.xstring, "event location", "事件地点");
        dp(m, NS + "eventResult",    event,  XSD.xstring, "event result", "事件结果");

        /* ------------------ 7. 写入文件 ------------------ */
        try (OutputStream out = new FileOutputStream("src/main/java/org/example/warpeacekg/rdf/war_and_peace_ontology_new.ttl")) {
            m.write(out, "Turtle");
            System.out.println("✅ Extended ontology written to war_and_peace_ontology_new.ttl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ------------ 工具方法 ------------ */
    private static OntClass cls(OntModel m, String uri, String labelEN, String labelZH) {
        OntClass c = m.createClass(uri);
        c.addLabel(labelEN, "en");
        c.addLabel(labelZH, "zh");
        return c;
    }

    private static ObjectProperty op(OntModel m, String uri, OntClass domain, OntClass range,
                                     String labelEN, String labelZH, boolean inv, String invURI) {
        ObjectProperty p = m.createObjectProperty(uri);
        p.addLabel(labelEN, "en");
        p.addLabel(labelZH, "zh");
        p.addDomain(domain);
        p.addRange(range);
        if (inv && invURI != null) {
            ObjectProperty invProp = m.createObjectProperty(invURI);
            p.addInverseOf(invProp);
        }
        return p;
    }

    private static ObjectProperty symOp(OntModel m, String uri, OntClass domain, OntClass range,
                                        String labelEN, String labelZH) {
        ObjectProperty p = op(m, uri, domain, range, labelEN, labelZH, false, null);
        p.addProperty(RDFS.subPropertyOf, OWL.SymmetricProperty);
        return p;
    }

    private static DatatypeProperty dp(OntModel m, String uri, OntClass domain, Resource range,
                                       String labelEN, String labelZH) {
        DatatypeProperty dp = m.createDatatypeProperty(uri);
        dp.addLabel(labelEN, "en");
        dp.addLabel(labelZH, "zh");
        dp.addDomain(domain);
        dp.addRange(range);
        return dp;
    }
}