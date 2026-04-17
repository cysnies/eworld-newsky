package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "type-element",
   propOrder = {"param"}
)
public class JaxbTypeElement {
   protected List param;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public List getParam() {
      if (this.param == null) {
         this.param = new ArrayList();
      }

      return this.param;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
