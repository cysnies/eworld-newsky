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
   name = "load-collection-element",
   propOrder = {"returnProperty"}
)
public class JaxbLoadCollectionElement {
   @XmlElement(
      name = "return-property"
   )
   protected List returnProperty;
   @XmlAttribute(
      required = true
   )
   protected String alias;
   @XmlAttribute(
      name = "lock-mode"
   )
   protected JaxbLockModeAttribute lockMode;
   @XmlAttribute(
      required = true
   )
   protected String role;

   public List getReturnProperty() {
      if (this.returnProperty == null) {
         this.returnProperty = new ArrayList();
      }

      return this.returnProperty;
   }

   public String getAlias() {
      return this.alias;
   }

   public void setAlias(String value) {
      this.alias = value;
   }

   public JaxbLockModeAttribute getLockMode() {
      return this.lockMode == null ? JaxbLockModeAttribute.READ : this.lockMode;
   }

   public void setLockMode(JaxbLockModeAttribute value) {
      this.lockMode = value;
   }

   public String getRole() {
      return this.role;
   }

   public void setRole(String value) {
      this.role = value;
   }
}
