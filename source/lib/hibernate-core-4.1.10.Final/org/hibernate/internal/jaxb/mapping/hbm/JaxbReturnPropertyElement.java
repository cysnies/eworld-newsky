package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "return-property-element",
   propOrder = {"returnColumn"}
)
public class JaxbReturnPropertyElement {
   @XmlElement(
      name = "return-column"
   )
   protected List returnColumn;
   @XmlAttribute
   protected String column;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public List getReturnColumn() {
      if (this.returnColumn == null) {
         this.returnColumn = new ArrayList();
      }

      return this.returnColumn;
   }

   public String getColumn() {
      return this.column;
   }

   public void setColumn(String value) {
      this.column = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = ""
   )
   public static class JaxbReturnColumn {
      @XmlAttribute(
         required = true
      )
      protected String name;

      public String getName() {
         return this.name;
      }

      public void setName(String value) {
         this.name = value;
      }
   }
}
