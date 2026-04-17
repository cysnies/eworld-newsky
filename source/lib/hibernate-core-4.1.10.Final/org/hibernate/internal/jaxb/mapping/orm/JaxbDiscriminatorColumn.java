package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "discriminator-column"
)
public class JaxbDiscriminatorColumn {
   @XmlAttribute
   protected String name;
   @XmlAttribute(
      name = "discriminator-type"
   )
   protected JaxbDiscriminatorType discriminatorType;
   @XmlAttribute(
      name = "column-definition"
   )
   protected String columnDefinition;
   @XmlAttribute
   protected Integer length;

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public JaxbDiscriminatorType getDiscriminatorType() {
      return this.discriminatorType;
   }

   public void setDiscriminatorType(JaxbDiscriminatorType value) {
      this.discriminatorType = value;
   }

   public String getColumnDefinition() {
      return this.columnDefinition;
   }

   public void setColumnDefinition(String value) {
      this.columnDefinition = value;
   }

   public Integer getLength() {
      return this.length;
   }

   public void setLength(Integer value) {
      this.length = value;
   }
}
