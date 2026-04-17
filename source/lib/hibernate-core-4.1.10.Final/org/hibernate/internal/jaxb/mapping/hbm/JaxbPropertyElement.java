package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "property-element",
   propOrder = {"meta", "columnOrFormula", "type"}
)
public class JaxbPropertyElement implements SingularAttributeSource {
   protected List meta;
   @XmlElements({@XmlElement(
   name = "column",
   type = JaxbColumnElement.class
), @XmlElement(
   name = "formula",
   type = String.class
)})
   protected List columnOrFormula;
   protected JaxbTypeElement type;
   @XmlAttribute
   protected String access;
   @XmlAttribute
   protected String column;
   @XmlAttribute
   protected String formula;
   @XmlAttribute
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String generated;
   @XmlAttribute
   protected String index;
   @XmlAttribute
   protected Boolean insert;
   @XmlAttribute
   protected Boolean lazy;
   @XmlAttribute
   protected String length;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "not-null"
   )
   protected Boolean notNull;
   @XmlAttribute(
      name = "optimistic-lock"
   )
   protected Boolean optimisticLock;
   @XmlAttribute
   protected String precision;
   @XmlAttribute
   protected String scale;
   @XmlAttribute(
      name = "type"
   )
   protected String typeAttribute;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute(
      name = "unique-key"
   )
   protected String uniqueKey;
   @XmlAttribute
   protected Boolean update;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getColumnOrFormula() {
      if (this.columnOrFormula == null) {
         this.columnOrFormula = new ArrayList();
      }

      return this.columnOrFormula;
   }

   public JaxbTypeElement getType() {
      return this.type;
   }

   public void setType(JaxbTypeElement value) {
      this.type = value;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getColumn() {
      return this.column;
   }

   public void setColumn(String value) {
      this.column = value;
   }

   public String getFormula() {
      return this.formula;
   }

   public void setFormula(String value) {
      this.formula = value;
   }

   public String getGenerated() {
      return this.generated == null ? "never" : this.generated;
   }

   public void setGenerated(String value) {
      this.generated = value;
   }

   public String getIndex() {
      return this.index;
   }

   public void setIndex(String value) {
      this.index = value;
   }

   public Boolean isInsert() {
      return this.insert;
   }

   public void setInsert(Boolean value) {
      this.insert = value;
   }

   public boolean isLazy() {
      return this.lazy == null ? false : this.lazy;
   }

   public void setLazy(Boolean value) {
      this.lazy = value;
   }

   public String getLength() {
      return this.length;
   }

   public void setLength(String value) {
      this.length = value;
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

   public Boolean isNotNull() {
      return this.notNull;
   }

   public void setNotNull(Boolean value) {
      this.notNull = value;
   }

   public boolean isOptimisticLock() {
      return this.optimisticLock == null ? true : this.optimisticLock;
   }

   public void setOptimisticLock(Boolean value) {
      this.optimisticLock = value;
   }

   public String getPrecision() {
      return this.precision;
   }

   public void setPrecision(String value) {
      this.precision = value;
   }

   public String getScale() {
      return this.scale;
   }

   public void setScale(String value) {
      this.scale = value;
   }

   public String getTypeAttribute() {
      return this.typeAttribute;
   }

   public void setTypeAttribute(String value) {
      this.typeAttribute = value;
   }

   public boolean isUnique() {
      return this.unique == null ? false : this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public String getUniqueKey() {
      return this.uniqueKey;
   }

   public void setUniqueKey(String value) {
      this.uniqueKey = value;
   }

   public Boolean isUpdate() {
      return this.update;
   }

   public void setUpdate(Boolean value) {
      this.update = value;
   }
}
