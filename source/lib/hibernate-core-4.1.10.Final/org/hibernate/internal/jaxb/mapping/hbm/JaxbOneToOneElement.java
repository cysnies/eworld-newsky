package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "one-to-one-element",
   propOrder = {"meta", "formula"}
)
public class JaxbOneToOneElement {
   protected List meta;
   protected List formula;
   @XmlAttribute
   protected String access;
   @XmlAttribute
   protected String cascade;
   @XmlAttribute(
      name = "class"
   )
   protected String clazz;
   @XmlAttribute
   protected Boolean constrained;
   @XmlAttribute(
      name = "embed-xml"
   )
   protected Boolean embedXml;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute
   protected JaxbFetchAttribute fetch;
   @XmlAttribute(
      name = "foreign-key"
   )
   protected String foreignKey;
   @XmlAttribute(
      name = "formula"
   )
   protected String formulaAttribute;
   @XmlAttribute
   protected JaxbLazyAttributeWithNoProxy lazy;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "outer-join"
   )
   protected JaxbOuterJoinAttribute outerJoin;
   @XmlAttribute(
      name = "property-ref"
   )
   protected String propertyRef;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getFormula() {
      if (this.formula == null) {
         this.formula = new ArrayList();
      }

      return this.formula;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getCascade() {
      return this.cascade;
   }

   public void setCascade(String value) {
      this.cascade = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public boolean isConstrained() {
      return this.constrained == null ? false : this.constrained;
   }

   public void setConstrained(Boolean value) {
      this.constrained = value;
   }

   public boolean isEmbedXml() {
      return this.embedXml == null ? true : this.embedXml;
   }

   public void setEmbedXml(Boolean value) {
      this.embedXml = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public JaxbFetchAttribute getFetch() {
      return this.fetch;
   }

   public void setFetch(JaxbFetchAttribute value) {
      this.fetch = value;
   }

   public String getForeignKey() {
      return this.foreignKey;
   }

   public void setForeignKey(String value) {
      this.foreignKey = value;
   }

   public String getFormulaAttribute() {
      return this.formulaAttribute;
   }

   public void setFormulaAttribute(String value) {
      this.formulaAttribute = value;
   }

   public JaxbLazyAttributeWithNoProxy getLazy() {
      return this.lazy;
   }

   public void setLazy(JaxbLazyAttributeWithNoProxy value) {
      this.lazy = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public JaxbOuterJoinAttribute getOuterJoin() {
      return this.outerJoin;
   }

   public void setOuterJoin(JaxbOuterJoinAttribute value) {
      this.outerJoin = value;
   }

   public String getPropertyRef() {
      return this.propertyRef;
   }

   public void setPropertyRef(String value) {
      this.propertyRef = value;
   }
}
