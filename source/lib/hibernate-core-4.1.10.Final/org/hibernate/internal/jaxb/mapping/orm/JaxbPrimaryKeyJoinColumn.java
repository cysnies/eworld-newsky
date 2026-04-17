package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "primary-key-join-column"
)
public class JaxbPrimaryKeyJoinColumn {
   @XmlAttribute
   protected String name;
   @XmlAttribute(
      name = "referenced-column-name"
   )
   protected String referencedColumnName;
   @XmlAttribute(
      name = "column-definition"
   )
   protected String columnDefinition;

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getReferencedColumnName() {
      return this.referencedColumnName;
   }

   public void setReferencedColumnName(String value) {
      this.referencedColumnName = value;
   }

   public String getColumnDefinition() {
      return this.columnDefinition;
   }

   public void setColumnDefinition(String value) {
      this.columnDefinition = value;
   }
}
