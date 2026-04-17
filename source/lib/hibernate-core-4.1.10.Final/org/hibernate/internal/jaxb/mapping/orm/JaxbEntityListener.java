package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "entity-listener",
   propOrder = {"description", "prePersist", "postPersist", "preRemove", "postRemove", "preUpdate", "postUpdate", "postLoad"}
)
public class JaxbEntityListener {
   protected String description;
   @XmlElement(
      name = "pre-persist"
   )
   protected JaxbPrePersist prePersist;
   @XmlElement(
      name = "post-persist"
   )
   protected JaxbPostPersist postPersist;
   @XmlElement(
      name = "pre-remove"
   )
   protected JaxbPreRemove preRemove;
   @XmlElement(
      name = "post-remove"
   )
   protected JaxbPostRemove postRemove;
   @XmlElement(
      name = "pre-update"
   )
   protected JaxbPreUpdate preUpdate;
   @XmlElement(
      name = "post-update"
   )
   protected JaxbPostUpdate postUpdate;
   @XmlElement(
      name = "post-load"
   )
   protected JaxbPostLoad postLoad;
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbPrePersist getPrePersist() {
      return this.prePersist;
   }

   public void setPrePersist(JaxbPrePersist value) {
      this.prePersist = value;
   }

   public JaxbPostPersist getPostPersist() {
      return this.postPersist;
   }

   public void setPostPersist(JaxbPostPersist value) {
      this.postPersist = value;
   }

   public JaxbPreRemove getPreRemove() {
      return this.preRemove;
   }

   public void setPreRemove(JaxbPreRemove value) {
      this.preRemove = value;
   }

   public JaxbPostRemove getPostRemove() {
      return this.postRemove;
   }

   public void setPostRemove(JaxbPostRemove value) {
      this.postRemove = value;
   }

   public JaxbPreUpdate getPreUpdate() {
      return this.preUpdate;
   }

   public void setPreUpdate(JaxbPreUpdate value) {
      this.preUpdate = value;
   }

   public JaxbPostUpdate getPostUpdate() {
      return this.postUpdate;
   }

   public void setPostUpdate(JaxbPostUpdate value) {
      this.postUpdate = value;
   }

   public JaxbPostLoad getPostLoad() {
      return this.postLoad;
   }

   public void setPostLoad(JaxbPostLoad value) {
      this.postLoad = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }
}
