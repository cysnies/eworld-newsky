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
   name = "return-element",
   propOrder = {"returnDiscriminatorAndReturnProperty"}
)
public class JaxbReturnElement {
   @XmlElements({@XmlElement(
   name = "return-discriminator",
   type = JaxbReturnDiscriminator.class
), @XmlElement(
   name = "return-property",
   type = JaxbReturnPropertyElement.class
)})
   protected List returnDiscriminatorAndReturnProperty;
   @XmlAttribute
   protected String alias;
   @XmlAttribute(
      name = "class"
   )
   protected String clazz;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute(
      name = "lock-mode"
   )
   protected JaxbLockModeAttribute lockMode;

   public List getReturnDiscriminatorAndReturnProperty() {
      if (this.returnDiscriminatorAndReturnProperty == null) {
         this.returnDiscriminatorAndReturnProperty = new ArrayList();
      }

      return this.returnDiscriminatorAndReturnProperty;
   }

   public String getAlias() {
      return this.alias;
   }

   public void setAlias(String value) {
      this.alias = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public JaxbLockModeAttribute getLockMode() {
      return this.lockMode == null ? JaxbLockModeAttribute.READ : this.lockMode;
   }

   public void setLockMode(JaxbLockModeAttribute value) {
      this.lockMode = value;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = ""
   )
   public static class JaxbReturnDiscriminator {
      @XmlAttribute(
         required = true
      )
      protected String column;

      public String getColumn() {
         return this.column;
      }

      public void setColumn(String value) {
         this.column = value;
      }
   }
}
