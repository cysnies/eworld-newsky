package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "post-remove",
   propOrder = {"description"}
)
public class JaxbPostRemove {
   protected String description;
   @XmlAttribute(
      name = "method-name",
      required = true
   )
   protected String methodName;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public String getMethodName() {
      return this.methodName;
   }

   public void setMethodName(String value) {
      this.methodName = value;
   }
}
