package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "unique-constraint",
   propOrder = {"columnName"}
)
public class JaxbUniqueConstraint {
   @XmlElement(
      name = "column-name",
      required = true
   )
   protected List columnName;
   @XmlAttribute
   protected String name;

   public List getColumnName() {
      if (this.columnName == null) {
         this.columnName = new ArrayList();
      }

      return this.columnName;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
