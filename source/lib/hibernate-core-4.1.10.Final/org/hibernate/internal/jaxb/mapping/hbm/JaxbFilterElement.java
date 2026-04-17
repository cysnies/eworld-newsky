package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "filter-element",
   propOrder = {"content"}
)
public class JaxbFilterElement {
   @XmlElementRef(
      name = "aliases",
      namespace = "http://www.hibernate.org/xsd/hibernate-mapping",
      type = JAXBElement.class
   )
   @XmlMixed
   protected List content;
   @XmlAttribute
   protected String condition;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String autoAliasInjection;

   public List getContent() {
      if (this.content == null) {
         this.content = new ArrayList();
      }

      return this.content;
   }

   public String getCondition() {
      return this.condition;
   }

   public void setCondition(String value) {
      this.condition = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getAutoAliasInjection() {
      return this.autoAliasInjection == null ? "true" : this.autoAliasInjection;
   }

   public void setAutoAliasInjection(String value) {
      this.autoAliasInjection = value;
   }
}
