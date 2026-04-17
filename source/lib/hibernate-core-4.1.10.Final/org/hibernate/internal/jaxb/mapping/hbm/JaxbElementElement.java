package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "element-element",
   propOrder = {"columnOrFormula", "type"}
)
public class JaxbElementElement {
   @XmlElements({@XmlElement(
   name = "formula",
   type = String.class
), @XmlElement(
   name = "column",
   type = JaxbColumnElement.class
)})
   protected List columnOrFormula;
   protected JaxbTypeElement type;
   @XmlAttribute
   protected String column;
   @XmlAttribute
   protected String formula;
   @XmlAttribute
   protected String length;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "not-null"
   )
   protected Boolean notNull;
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

   public String getLength() {
      return this.length;
   }

   public void setLength(String value) {
      this.length = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public boolean isNotNull() {
      return this.notNull == null ? false : this.notNull;
   }

   public void setNotNull(Boolean value) {
      this.notNull = value;
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
}
