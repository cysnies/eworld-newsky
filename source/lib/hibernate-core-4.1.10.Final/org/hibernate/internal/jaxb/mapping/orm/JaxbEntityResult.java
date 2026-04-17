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
   name = "entity-result",
   propOrder = {"fieldResult"}
)
public class JaxbEntityResult {
   @XmlElement(
      name = "field-result"
   )
   protected List fieldResult;
   @XmlAttribute(
      name = "entity-class",
      required = true
   )
   protected String entityClass;
   @XmlAttribute(
      name = "discriminator-column"
   )
   protected String discriminatorColumn;

   public List getFieldResult() {
      if (this.fieldResult == null) {
         this.fieldResult = new ArrayList();
      }

      return this.fieldResult;
   }

   public String getEntityClass() {
      return this.entityClass;
   }

   public void setEntityClass(String value) {
      this.entityClass = value;
   }

   public String getDiscriminatorColumn() {
      return this.discriminatorColumn;
   }

   public void setDiscriminatorColumn(String value) {
      this.discriminatorColumn = value;
   }
}
