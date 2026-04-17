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
   name = "sql-result-set-mapping",
   propOrder = {"description", "entityResult", "columnResult"}
)
public class JaxbSqlResultSetMapping {
   protected String description;
   @XmlElement(
      name = "entity-result"
   )
   protected List entityResult;
   @XmlElement(
      name = "column-result"
   )
   protected List columnResult;
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

   public List getEntityResult() {
      if (this.entityResult == null) {
         this.entityResult = new ArrayList();
      }

      return this.entityResult;
   }

   public List getColumnResult() {
      if (this.columnResult == null) {
         this.columnResult = new ArrayList();
      }

      return this.columnResult;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
