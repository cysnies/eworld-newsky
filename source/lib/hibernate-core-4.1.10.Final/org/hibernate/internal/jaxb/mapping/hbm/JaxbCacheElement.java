package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "cache-element"
)
public class JaxbCacheElement {
   @XmlAttribute
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String include;
   @XmlAttribute
   protected String region;
   @XmlAttribute(
      required = true
   )
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String usage;

   public String getInclude() {
      return this.include == null ? "all" : this.include;
   }

   public void setInclude(String value) {
      this.include = value;
   }

   public String getRegion() {
      return this.region;
   }

   public void setRegion(String value) {
      this.region = value;
   }

   public String getUsage() {
      return this.usage;
   }

   public void setUsage(String value) {
      this.usage = value;
   }
}
