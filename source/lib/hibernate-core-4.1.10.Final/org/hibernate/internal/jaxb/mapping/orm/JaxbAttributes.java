package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "attributes",
   propOrder = {"description", "id", "embeddedId", "basic", "version", "manyToOne", "oneToMany", "oneToOne", "manyToMany", "elementCollection", "embedded", "_transient"}
)
public class JaxbAttributes {
   protected String description;
   protected List id;
   @XmlElement(
      name = "embedded-id"
   )
   protected JaxbEmbeddedId embeddedId;
   protected List basic;
   protected List version;
   @XmlElement(
      name = "many-to-one"
   )
   protected List manyToOne;
   @XmlElement(
      name = "one-to-many"
   )
   protected List oneToMany;
   @XmlElement(
      name = "one-to-one"
   )
   protected List oneToOne;
   @XmlElement(
      name = "many-to-many"
   )
   protected List manyToMany;
   @XmlElement(
      name = "element-collection"
   )
   protected List elementCollection;
   protected List embedded;
   @XmlElement(
      name = "transient"
   )
   protected List _transient;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public List getId() {
      if (this.id == null) {
         this.id = new ArrayList();
      }

      return this.id;
   }

   public JaxbEmbeddedId getEmbeddedId() {
      return this.embeddedId;
   }

   public void setEmbeddedId(JaxbEmbeddedId value) {
      this.embeddedId = value;
   }

   public List getBasic() {
      if (this.basic == null) {
         this.basic = new ArrayList();
      }

      return this.basic;
   }

   public List getVersion() {
      if (this.version == null) {
         this.version = new ArrayList();
      }

      return this.version;
   }

   public List getManyToOne() {
      if (this.manyToOne == null) {
         this.manyToOne = new ArrayList();
      }

      return this.manyToOne;
   }

   public List getOneToMany() {
      if (this.oneToMany == null) {
         this.oneToMany = new ArrayList();
      }

      return this.oneToMany;
   }

   public List getOneToOne() {
      if (this.oneToOne == null) {
         this.oneToOne = new ArrayList();
      }

      return this.oneToOne;
   }

   public List getManyToMany() {
      if (this.manyToMany == null) {
         this.manyToMany = new ArrayList();
      }

      return this.manyToMany;
   }

   public List getElementCollection() {
      if (this.elementCollection == null) {
         this.elementCollection = new ArrayList();
      }

      return this.elementCollection;
   }

   public List getEmbedded() {
      if (this.embedded == null) {
         this.embedded = new ArrayList();
      }

      return this.embedded;
   }

   public List getTransient() {
      if (this._transient == null) {
         this._transient = new ArrayList();
      }

      return this._transient;
   }
}
