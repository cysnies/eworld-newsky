package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListeners;
import org.hibernate.internal.jaxb.mapping.orm.JaxbIdClass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMappedSuperclass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostLoad;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostPersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrePersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreUpdate;
import org.jboss.logging.Logger;

class MappedSuperclassMocker extends AbstractEntityObjectMocker {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MappedSuperclassMocker.class.getName());
   private JaxbMappedSuperclass mappedSuperclass;

   MappedSuperclassMocker(IndexBuilder indexBuilder, JaxbMappedSuperclass mappedSuperclass, EntityMappingsMocker.Default defaults) {
      super(indexBuilder, defaults);
      this.mappedSuperclass = mappedSuperclass;
   }

   protected void applyDefaults() {
      DefaultConfigurationHelper.INSTANCE.applyDefaults(this.mappedSuperclass, this.getDefaults());
   }

   protected void processExtra() {
      this.create(MAPPED_SUPERCLASS);
   }

   protected JaxbAttributes getAttributes() {
      return this.mappedSuperclass.getAttributes();
   }

   protected JaxbAccessType getAccessType() {
      return this.mappedSuperclass.getAccess();
   }

   protected boolean isMetadataComplete() {
      return this.mappedSuperclass.isMetadataComplete() != null && this.mappedSuperclass.isMetadataComplete();
   }

   protected boolean isExcludeDefaultListeners() {
      return this.mappedSuperclass.getExcludeDefaultListeners() != null;
   }

   protected boolean isExcludeSuperclassListeners() {
      return this.mappedSuperclass.getExcludeSuperclassListeners() != null;
   }

   protected JaxbIdClass getIdClass() {
      return this.mappedSuperclass.getIdClass();
   }

   protected JaxbEntityListeners getEntityListeners() {
      return this.mappedSuperclass.getEntityListeners();
   }

   protected String getClassName() {
      return this.mappedSuperclass.getClazz();
   }

   protected JaxbPrePersist getPrePersist() {
      return this.mappedSuperclass.getPrePersist();
   }

   protected JaxbPreRemove getPreRemove() {
      return this.mappedSuperclass.getPreRemove();
   }

   protected JaxbPreUpdate getPreUpdate() {
      return this.mappedSuperclass.getPreUpdate();
   }

   protected JaxbPostPersist getPostPersist() {
      return this.mappedSuperclass.getPostPersist();
   }

   protected JaxbPostUpdate getPostUpdate() {
      return this.mappedSuperclass.getPostUpdate();
   }

   protected JaxbPostRemove getPostRemove() {
      return this.mappedSuperclass.getPostRemove();
   }

   protected JaxbPostLoad getPostLoad() {
      return this.mappedSuperclass.getPostLoad();
   }
}
