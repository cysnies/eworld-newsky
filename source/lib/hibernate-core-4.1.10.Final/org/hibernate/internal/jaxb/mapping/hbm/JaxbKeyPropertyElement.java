package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "key-property-element",
   propOrder = {"meta", "column", "type"}
)
public class JaxbKeyPropertyElement {
   protected List meta;
   protected List column;
   protected JaxbTypeElement type;
   @XmlAttribute
   protected String access;
   @XmlAttribute(
      name = "column"
   )
   protected String columnAttribute;
   @XmlAttribute
   protected String length;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "type"
   )
   protected String typeAttribute;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getColumn() {
      if (this.column == null) {
         this.column = new ArrayList();
      }

      return this.column;
   }

   public JaxbTypeElement getType() {
      return this.type;
   }

   public void setType(JaxbTypeElement value) {
      this.type = value;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
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

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public String getTypeAttribute() {
      return this.typeAttribute;
   }

   public void setTypeAttribute(String value) {
      this.typeAttribute = value;
   }
}
