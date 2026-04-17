package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "id",
   propOrder = {"column", "generatedValue", "temporal", "tableGenerator", "sequenceGenerator"}
)
public class JaxbId {
   protected JaxbColumn column;
   @XmlElement(
      name = "generated-value"
   )
   protected JaxbGeneratedValue generatedValue;
   protected JaxbTemporalType temporal;
   @XmlElement(
      name = "table-generator"
   )
   protected JaxbTableGenerator tableGenerator;
   @XmlElement(
      name = "sequence-generator"
   )
   protected JaxbSequenceGenerator sequenceGenerator;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected JaxbAccessType access;

   public JaxbColumn getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumn value) {
      this.column = value;
   }

   public JaxbGeneratedValue getGeneratedValue() {
      return this.generatedValue;
   }

   public void setGeneratedValue(JaxbGeneratedValue value) {
      this.generatedValue = value;
   }

   public JaxbTemporalType getTemporal() {
      return this.temporal;
   }

   public void setTemporal(JaxbTemporalType value) {
      this.temporal = value;
   }

   public JaxbTableGenerator getTableGenerator() {
      return this.tableGenerator;
   }

   public void setTableGenerator(JaxbTableGenerator value) {
      this.tableGenerator = value;
   }

   public JaxbSequenceGenerator getSequenceGenerator() {
      return this.sequenceGenerator;
   }

   public void setSequenceGenerator(JaxbSequenceGenerator value) {
      this.sequenceGenerator = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }
}
