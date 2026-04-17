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
   name = "embedded-id",
   propOrder = {"attributeOverride"}
)
public class JaxbEmbeddedId {
   @XmlElement(
      name = "attribute-override"
   )
   protected List attributeOverride;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected JaxbAccessType access;

   public List getAttributeOverride() {
      if (this.attributeOverride == null) {
         this.attributeOverride = new ArrayList();
      }

      return this.attributeOverride;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }
}
