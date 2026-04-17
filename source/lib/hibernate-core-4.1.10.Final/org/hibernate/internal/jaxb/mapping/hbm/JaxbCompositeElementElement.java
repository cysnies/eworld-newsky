package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "composite-element-element",
   propOrder = {"meta", "parent", "tuplizer", "propertyOrManyToOneOrAny"}
)
public class JaxbCompositeElementElement {
   protected List meta;
   protected JaxbParentElement parent;
   protected List tuplizer;
   @XmlElements({@XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "nested-composite-element",
   type = JaxbNestedCompositeElementElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
)})
   protected List propertyOrManyToOneOrAny;
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute
   protected String node;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public JaxbParentElement getParent() {
      return this.parent;
   }

   public void setParent(JaxbParentElement value) {
      this.parent = value;
   }

   public List getTuplizer() {
      if (this.tuplizer == null) {
         this.tuplizer = new ArrayList();
      }

      return this.tuplizer;
   }

   public List getPropertyOrManyToOneOrAny() {
      if (this.propertyOrManyToOneOrAny == null) {
         this.propertyOrManyToOneOrAny = new ArrayList();
      }

      return this.propertyOrManyToOneOrAny;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }
}
