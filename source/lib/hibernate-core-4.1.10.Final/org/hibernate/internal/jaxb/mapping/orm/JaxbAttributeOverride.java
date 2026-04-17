package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "attribute-override",
   propOrder = {"description", "column"}
)
public class JaxbAttributeOverride {
   protected String description;
   @XmlElement(
      required = true
   )
   protected JaxbColumn column;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbColumn getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumn value) {
      this.column = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
