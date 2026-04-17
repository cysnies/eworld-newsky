package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListeners;
import org.hibernate.internal.jaxb.mapping.orm.JaxbIdClass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostLoad;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostPersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrePersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreUpdate;
import org.jboss.logging.Logger;

class EmbeddableMocker extends AbstractEntityObjectMocker {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EmbeddableMocker.class.getName());
   private JaxbEmbeddable embeddable;

   EmbeddableMocker(IndexBuilder indexBuilder, JaxbEmbeddable embeddable, EntityMappingsMocker.Default defaults) {
      super(indexBuilder, defaults);
      this.embeddable = embeddable;
   }

   protected AbstractAttributesBuilder getAttributesBuilder() {
      if (this.attributesBuilder == null) {
         this.attributesBuilder = new EmbeddableAttributesBuilder(this.indexBuilder, this.classInfo, this.getAccessType(), this.getDefaults(), this.embeddable.getAttributes());
      }

      return this.attributesBuilder;
   }

   protected void processExtra() {
      this.create(EMBEDDABLE);
   }

   protected void applyDefaults() {
      DefaultConfigurationHelper.INSTANCE.applyDefaults(this.embeddable, this.getDefaults());
   }

   protected boolean isMetadataComplete() {
      return this.embeddable.isMetadataComplete() != null && this.embeddable.isMetadataComplete();
   }

   protected boolean isExcludeDefaultListeners() {
      return false;
   }

   protected boolean isExcludeSuperclassListeners() {
      return false;
   }

   protected JaxbIdClass getIdClass() {
      return null;
   }

   protected JaxbEntityListeners getEntityListeners() {
      return null;
   }

   protected JaxbAccessType getAccessType() {
      return this.embeddable.getAccess();
   }

   protected String getClassName() {
      return this.embeddable.getClazz();
   }

   protected JaxbPrePersist getPrePersist() {
      return null;
   }

   protected JaxbPreRemove getPreRemove() {
      return null;
   }

   protected JaxbPreUpdate getPreUpdate() {
      return null;
   }

   protected JaxbPostPersist getPostPersist() {
      return null;
   }

   protected JaxbPostUpdate getPostUpdate() {
      return null;
   }

   protected JaxbPostRemove getPostRemove() {
      return null;
   }

   protected JaxbPostLoad getPostLoad() {
      return null;
   }

   protected JaxbAttributes getAttributes() {
      return null;
   }
}
