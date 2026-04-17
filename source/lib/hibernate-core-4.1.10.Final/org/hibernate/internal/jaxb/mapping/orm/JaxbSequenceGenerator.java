package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "sequence-generator",
   propOrder = {"description"}
)
public class JaxbSequenceGenerator {
   protected String description;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "sequence-name"
   )
   protected String sequenceName;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected String schema;
   @XmlAttribute(
      name = "initial-value"
   )
   protected Integer initialValue;
   @XmlAttribute(
      name = "allocation-size"
   )
   protected Integer allocationSize;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getSequenceName() {
      return this.sequenceName;
   }

   public void setSequenceName(String value) {
      this.sequenceName = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String value) {
      this.schema = value;
   }

   public Integer getInitialValue() {
      return this.initialValue;
   }

   public void setInitialValue(Integer value) {
      this.initialValue = value;
   }

   public Integer getAllocationSize() {
      return this.allocationSize;
   }

   public void setAllocationSize(Integer value) {
      this.allocationSize = value;
   }
}
