package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.MappingException;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListener;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListeners;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostLoad;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostPersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrePersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreUpdate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class ListenerMocker extends AbstractMocker {
   private final ClassInfo classInfo;

   ListenerMocker(IndexBuilder indexBuilder, ClassInfo classInfo) {
      super(indexBuilder);
      this.classInfo = classInfo;
   }

   AnnotationInstance parser(JaxbEntityListeners entityListeners) {
      if (entityListeners.getEntityListener().isEmpty()) {
         throw new MappingException("No child element of <entity-listener> found under <entity-listeners>.");
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList(1);
         List<String> clazzNameList = new ArrayList(entityListeners.getEntityListener().size());

         for(JaxbEntityListener listener : entityListeners.getEntityListener()) {
            MockHelper.addToCollectionIfNotNull(clazzNameList, listener.getClazz());
            this.parserEntityListener(listener);
         }

         MockHelper.classArrayValue("value", clazzNameList, annotationValueList, this.indexBuilder.getServiceRegistry());
         return this.create(ENTITY_LISTENERS, this.classInfo, annotationValueList);
      }
   }

   private void parserEntityListener(JaxbEntityListener listener) {
      String clazz = listener.getClazz();
      ClassInfo tempClassInfo = this.indexBuilder.createClassInfo(clazz);
      ListenerMocker mocker = this.createListenerMocker(this.indexBuilder, tempClassInfo);
      mocker.parser(listener.getPostLoad());
      mocker.parser(listener.getPostPersist());
      mocker.parser(listener.getPostRemove());
      mocker.parser(listener.getPostUpdate());
      mocker.parser(listener.getPrePersist());
      mocker.parser(listener.getPreRemove());
      mocker.parser(listener.getPreUpdate());
      this.indexBuilder.finishEntityObject(tempClassInfo.name(), (EntityMappingsMocker.Default)null);
   }

   protected ListenerMocker createListenerMocker(IndexBuilder indexBuilder, ClassInfo classInfo) {
      return new ListenerMocker(indexBuilder, classInfo);
   }

   AnnotationInstance parser(JaxbPrePersist callback) {
      return callback == null ? null : this.create(PRE_PERSIST, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPreRemove callback) {
      return callback == null ? null : this.create(PRE_REMOVE, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPreUpdate callback) {
      return callback == null ? null : this.create(PRE_UPDATE, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPostPersist callback) {
      return callback == null ? null : this.create(POST_PERSIST, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPostUpdate callback) {
      return callback == null ? null : this.create(POST_UPDATE, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPostRemove callback) {
      return callback == null ? null : this.create(POST_REMOVE, this.getListenerTarget(callback.getMethodName()));
   }

   AnnotationInstance parser(JaxbPostLoad callback) {
      return callback == null ? null : this.create(POST_LOAD, this.getListenerTarget(callback.getMethodName()));
   }

   private AnnotationTarget getListenerTarget(String methodName) {
      return MockHelper.getTarget(this.indexBuilder.getServiceRegistry(), this.classInfo, methodName, MockHelper.TargetType.METHOD);
   }

   protected AnnotationInstance push(AnnotationInstance annotationInstance) {
      if (annotationInstance != null && annotationInstance.target() != null) {
         this.indexBuilder.addAnnotationInstance(this.classInfo.name(), annotationInstance);
      }

      return annotationInstance;
   }
}
