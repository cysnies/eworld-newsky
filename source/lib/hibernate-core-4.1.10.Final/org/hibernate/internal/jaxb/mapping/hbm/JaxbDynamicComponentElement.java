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
   name = "dynamic-component-element",
   propOrder = {"propertyOrManyToOneOrOneToOne"}
)
public class JaxbDynamicComponentElement {
   @XmlElements({@XmlElement(
   name = "one-to-one",
   type = JaxbOneToOneElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "primitive-array",
   type = JaxbPrimitiveArrayElement.class
), @XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "set",
   type = JaxbSetElement.class
), @XmlElement(
   name = "bag",
   type = JaxbBagElement.class
), @XmlElement(
   name = "map",
   type = JaxbMapElement.class
), @XmlElement(
   name = "list",
   type = JaxbListElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
), @XmlElement(
   name = "array",
   type = JaxbArrayElement.class
)})
   protected List propertyOrManyToOneOrOneToOne;
   @XmlAttribute
   protected String access;
   @XmlAttribute
   protected Boolean insert;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "optimistic-lock"
   )
   protected Boolean optimisticLock;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute
   protected Boolean update;

   public List getPropertyOrManyToOneOrOneToOne() {
      if (this.propertyOrManyToOneOrOneToOne == null) {
         this.propertyOrManyToOneOrOneToOne = new ArrayList();
      }

      return this.propertyOrManyToOneOrOneToOne;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public boolean isInsert() {
      return this.insert == null ? true : this.insert;
   }

   public void setInsert(Boolean value) {
      this.insert = value;
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

   public boolean isOptimisticLock() {
      return this.optimisticLock == null ? true : this.optimisticLock;
   }

   public void setOptimisticLock(Boolean value) {
      this.optimisticLock = value;
   }

   public boolean isUnique() {
      return this.unique == null ? false : this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public boolean isUpdate() {
      return this.update == null ? true : this.update;
   }

   public void setUpdate(Boolean value) {
      this.update = value;
   }
}
