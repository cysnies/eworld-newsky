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
   name = "resultset-element",
   propOrder = {"returnScalarOrReturnOrReturnJoin"}
)
public class JaxbResultsetElement {
   @XmlElements({@XmlElement(
   name = "load-collection",
   type = JaxbLoadCollectionElement.class
), @XmlElement(
   name = "return",
   type = JaxbReturnElement.class
), @XmlElement(
   name = "return-scalar",
   type = JaxbReturnScalarElement.class
), @XmlElement(
   name = "return-join",
   type = JaxbReturnJoinElement.class
)})
   protected List returnScalarOrReturnOrReturnJoin;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public List getReturnScalarOrReturnOrReturnJoin() {
      if (this.returnScalarOrReturnOrReturnJoin == null) {
         this.returnScalarOrReturnOrReturnJoin = new ArrayList();
      }

      return this.returnScalarOrReturnOrReturnJoin;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
