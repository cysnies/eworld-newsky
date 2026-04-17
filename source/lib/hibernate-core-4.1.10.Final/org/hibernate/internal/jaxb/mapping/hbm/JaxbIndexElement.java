package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "index-element",
   propOrder = {"column"}
)
public class JaxbIndexElement {
   protected List column;
   @XmlAttribute(
      name = "column"
   )
   protected String columnAttribute;
   @XmlAttribute
   protected String length;
   @XmlAttribute
   protected String type;

   public List getColumn() {
      if (this.column == null) {
         this.column = new ArrayList();
      }

      return this.column;
   }

   public String getColumnAttribute() {
      return this.columnAttribute;
   }

   public void setColumnAttribute(String value) {
      this.columnAttribute = value;
   }

   public String getLength() {
      return this.length;
   }

   public void setLength(String value) {
      this.length = value;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String value) {
      this.type = value;
   }
}
