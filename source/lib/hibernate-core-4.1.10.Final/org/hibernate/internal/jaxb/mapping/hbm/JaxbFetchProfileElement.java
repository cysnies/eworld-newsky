package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "fetch-profile-element",
   propOrder = {"fetch"}
)
public class JaxbFetchProfileElement {
   protected List fetch;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public List getFetch() {
      if (this.fetch == null) {
         this.fetch = new ArrayList();
      }

      return this.fetch;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = ""
   )
   public static class JaxbFetch {
      @XmlAttribute(
         required = true
      )
      protected String association;
      @XmlAttribute
      protected String entity;
      @XmlAttribute
      @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
      protected String style;

      public String getAssociation() {
         return this.association;
      }

      public void setAssociation(String value) {
         this.association = value;
      }

      public String getEntity() {
         return this.entity;
      }

      public void setEntity(String value) {
         this.entity = value;
      }

      public String getStyle() {
         return this.style == null ? "join" : this.style;
      }

      public void setStyle(String value) {
         this.style = value;
      }
   }
}
