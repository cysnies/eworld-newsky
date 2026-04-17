package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "mapped-superclass",
   propOrder = {"description", "idClass", "excludeDefaultListeners", "excludeSuperclassListeners", "entityListeners", "prePersist", "postPersist", "preRemove", "postRemove", "preUpdate", "postUpdate", "postLoad", "attributes"}
)
public class JaxbMappedSuperclass {
   protected String description;
   @XmlElement(
      name = "id-class"
   )
   protected JaxbIdClass idClass;
   @XmlElement(
      name = "exclude-default-listeners"
   )
   protected JaxbEmptyType excludeDefaultListeners;
   @XmlElement(
      name = "exclude-superclass-listeners"
   )
   protected JaxbEmptyType excludeSuperclassListeners;
   @XmlElement(
      name = "entity-listeners"
   )
   protected JaxbEntityListeners entityListeners;
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
   protected JaxbAttributes attributes;
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute
   protected JaxbAccessType access;
   @XmlAttribute(
      name = "metadata-complete"
   )
   protected Boolean metadataComplete;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbIdClass getIdClass() {
      return this.idClass;
   }

   public void setIdClass(JaxbIdClass value) {
      this.idClass = value;
   }

   public JaxbEmptyType getExcludeDefaultListeners() {
      return this.excludeDefaultListeners;
   }

   public void setExcludeDefaultListeners(JaxbEmptyType value) {
      this.excludeDefaultListeners = value;
   }

   public JaxbEmptyType getExcludeSuperclassListeners() {
      return this.excludeSuperclassListeners;
   }

   public void setExcludeSuperclassListeners(JaxbEmptyType value) {
      this.excludeSuperclassListeners = value;
   }

   public JaxbEntityListeners getEntityListeners() {
      return this.entityListeners;
   }

   public void setEntityListeners(JaxbEntityListeners value) {
      this.entityListeners = value;
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

   public JaxbAttributes getAttributes() {
      return this.attributes;
   }

   public void setAttributes(JaxbAttributes value) {
      this.attributes = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }

   public Boolean isMetadataComplete() {
      return this.metadataComplete;
   }

   public void setMetadataComplete(Boolean value) {
      this.metadataComplete = value;
   }
}
