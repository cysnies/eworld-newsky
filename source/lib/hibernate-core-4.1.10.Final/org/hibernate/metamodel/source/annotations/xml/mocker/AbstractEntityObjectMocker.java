package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.AssertionFailure;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListeners;
import org.hibernate.internal.jaxb.mapping.orm.JaxbIdClass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostLoad;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostPersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrePersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreUpdate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

abstract class AbstractEntityObjectMocker extends AnnotationMocker {
   private ListenerMocker listenerParser;
   protected AbstractAttributesBuilder attributesBuilder;
   protected ClassInfo classInfo;
   private boolean isPreProcessCalled = false;

   AbstractEntityObjectMocker(IndexBuilder indexBuilder, EntityMappingsMocker.Default defaults) {
      super(indexBuilder, defaults);
   }

   final void preProcess() {
      this.applyDefaults();
      this.classInfo = this.indexBuilder.createClassInfo(this.getClassName());
      DotName classDotName = this.classInfo.name();
      if (this.isMetadataComplete()) {
         this.indexBuilder.metadataComplete(classDotName);
      }

      this.parserAccessType(this.getAccessType(), this.getTarget());
      this.isPreProcessCalled = true;
   }

   final void process() {
      if (!this.isPreProcessCalled) {
         throw new AssertionFailure("preProcess should be called before process");
      } else {
         if (this.getAccessType() == null) {
            JaxbAccessType accessType = AccessHelper.getEntityAccess(this.getTargetName(), this.indexBuilder);
            if (accessType == null) {
               accessType = this.getDefaults().getAccess();
            }

            this.parserAccessType(accessType, this.getTarget());
         }

         this.processExtra();
         if (this.isExcludeDefaultListeners()) {
            this.create(EXCLUDE_DEFAULT_LISTENERS);
         }

         if (this.isExcludeSuperclassListeners()) {
            this.create(EXCLUDE_SUPERCLASS_LISTENERS);
         }

         this.parserIdClass(this.getIdClass());
         if (this.getAttributes() != null) {
            this.getAttributesBuilder().parser();
         }

         if (this.getEntityListeners() != null) {
            this.getListenerParser().parser(this.getEntityListeners());
         }

         this.getListenerParser().parser(this.getPrePersist());
         this.getListenerParser().parser(this.getPreRemove());
         this.getListenerParser().parser(this.getPreUpdate());
         this.getListenerParser().parser(this.getPostPersist());
         this.getListenerParser().parser(this.getPostUpdate());
         this.getListenerParser().parser(this.getPostRemove());
         this.getListenerParser().parser(this.getPostLoad());
         this.indexBuilder.finishEntityObject(this.getTargetName(), this.getDefaults());
      }
   }

   protected abstract void processExtra();

   protected abstract void applyDefaults();

   protected abstract boolean isMetadataComplete();

   protected abstract boolean isExcludeDefaultListeners();

   protected abstract boolean isExcludeSuperclassListeners();

   protected abstract JaxbIdClass getIdClass();

   protected abstract JaxbEntityListeners getEntityListeners();

   protected abstract JaxbAccessType getAccessType();

   protected abstract String getClassName();

   protected abstract JaxbPrePersist getPrePersist();

   protected abstract JaxbPreRemove getPreRemove();

   protected abstract JaxbPreUpdate getPreUpdate();

   protected abstract JaxbPostPersist getPostPersist();

   protected abstract JaxbPostUpdate getPostUpdate();

   protected abstract JaxbPostRemove getPostRemove();

   protected abstract JaxbPostLoad getPostLoad();

   protected abstract JaxbAttributes getAttributes();

   protected ListenerMocker getListenerParser() {
      if (this.listenerParser == null) {
         this.listenerParser = new ListenerMocker(this.indexBuilder, this.classInfo);
      }

      return this.listenerParser;
   }

   protected AbstractAttributesBuilder getAttributesBuilder() {
      if (this.attributesBuilder == null) {
         this.attributesBuilder = new AttributesBuilder(this.indexBuilder, this.classInfo, this.getAccessType(), this.getDefaults(), this.getAttributes());
      }

      return this.attributesBuilder;
   }

   protected AnnotationInstance parserIdClass(JaxbIdClass idClass) {
      if (idClass == null) {
         return null;
      } else {
         String className = MockHelper.buildSafeClassName(idClass.getClazz(), this.getDefaults().getPackageName());
         return this.create(ID_CLASS, MockHelper.classValueArray("value", className, this.indexBuilder.getServiceRegistry()));
      }
   }

   protected DotName getTargetName() {
      return this.classInfo.name();
   }

   protected AnnotationTarget getTarget() {
      return this.classInfo;
   }
}
