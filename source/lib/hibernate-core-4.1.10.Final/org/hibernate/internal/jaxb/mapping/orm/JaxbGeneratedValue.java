package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "generated-value"
)
public class JaxbGeneratedValue {
   @XmlAttribute
   protected JaxbGenerationType strategy;
   @XmlAttribute
   protected String generator;

   public JaxbGenerationType getStrategy() {
      return this.strategy;
   }

   public void setStrategy(JaxbGenerationType value) {
      this.strategy = value;
   }

   public String getGenerator() {
      return this.generator;
   }

   public void setGenerator(String value) {
      this.generator = value;
   }
}
