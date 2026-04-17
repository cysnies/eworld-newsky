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
   name = "properties-element",
   propOrder = {"propertyOrManyToOneOrComponent"}
)
public class JaxbPropertiesElement {
   @XmlElements({@XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
)})
   protected List propertyOrManyToOneOrComponent;
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

   public List getPropertyOrManyToOneOrComponent() {
      if (this.propertyOrManyToOneOrComponent == null) {
         this.propertyOrManyToOneOrComponent = new ArrayList();
      }

      return this.propertyOrManyToOneOrComponent;
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
