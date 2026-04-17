package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "loader-element"
)
public class JaxbLoaderElement {
   @XmlAttribute(
      name = "query-ref",
      required = true
   )
   protected String queryRef;

   public String getQueryRef() {
      return this.queryRef;
   }

   public void setQueryRef(String value) {
      this.queryRef = value;
   }
}
