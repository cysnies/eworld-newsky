package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "inheritance"
)
public class JaxbInheritance {
   @XmlAttribute
   protected JaxbInheritanceType strategy;

   public JaxbInheritanceType getStrategy() {
      return this.strategy;
   }

   public void setStrategy(JaxbInheritanceType value) {
      this.strategy = value;
   }
}
