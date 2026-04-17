package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPersistenceUnitDefaults;
import org.hibernate.metamodel.source.annotations.xml.PseudoJpaDotNames;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

class PersistenceMetadataMocker extends AbstractMocker {
   private final JaxbPersistenceUnitDefaults persistenceUnitDefaults;
   private final GlobalAnnotations globalAnnotations = new GlobalAnnotations();
   private static final Map nameMapper = new HashMap();

   PersistenceMetadataMocker(IndexBuilder indexBuilder, JaxbPersistenceUnitDefaults persistenceUnitDefaults) {
      super(indexBuilder);
      this.persistenceUnitDefaults = persistenceUnitDefaults;
   }

   final void process() {
      this.parserAccessType(this.persistenceUnitDefaults.getAccess(), (AnnotationTarget)null);
      if (this.persistenceUnitDefaults.getDelimitedIdentifiers() != null) {
         this.create(PseudoJpaDotNames.DEFAULT_DELIMITED_IDENTIFIERS, (AnnotationTarget)null);
      }

      if (this.persistenceUnitDefaults.getEntityListeners() != null) {
         (new DefaultListenerMocker(this.indexBuilder, (ClassInfo)null)).parser(this.persistenceUnitDefaults.getEntityListeners());
      }

      this.indexBuilder.finishGlobalConfigurationMocking(this.globalAnnotations);
   }

   protected AnnotationInstance push(AnnotationInstance annotationInstance) {
      return annotationInstance != null ? this.globalAnnotations.push(annotationInstance.name(), annotationInstance) : null;
   }

   protected AnnotationInstance create(DotName name, AnnotationTarget target, AnnotationValue[] annotationValues) {
      DotName defaultName = (DotName)nameMapper.get(name);
      return defaultName == null ? null : super.create(defaultName, target, annotationValues);
   }

   static {
      nameMapper.put(ACCESS, PseudoJpaDotNames.DEFAULT_ACCESS);
      nameMapper.put(ENTITY_LISTENERS, PseudoJpaDotNames.DEFAULT_ENTITY_LISTENERS);
      nameMapper.put(POST_LOAD, PseudoJpaDotNames.DEFAULT_POST_LOAD);
      nameMapper.put(POST_REMOVE, PseudoJpaDotNames.DEFAULT_POST_REMOVE);
      nameMapper.put(POST_UPDATE, PseudoJpaDotNames.DEFAULT_POST_UPDATE);
      nameMapper.put(POST_PERSIST, PseudoJpaDotNames.DEFAULT_POST_PERSIST);
      nameMapper.put(PRE_REMOVE, PseudoJpaDotNames.DEFAULT_PRE_REMOVE);
      nameMapper.put(PRE_UPDATE, PseudoJpaDotNames.DEFAULT_PRE_UPDATE);
      nameMapper.put(PRE_PERSIST, PseudoJpaDotNames.DEFAULT_PRE_PERSIST);
      nameMapper.put(PseudoJpaDotNames.DEFAULT_DELIMITED_IDENTIFIERS, PseudoJpaDotNames.DEFAULT_DELIMITED_IDENTIFIERS);
   }

   private class DefaultListenerMocker extends ListenerMocker {
      DefaultListenerMocker(IndexBuilder indexBuilder, ClassInfo classInfo) {
         super(indexBuilder, classInfo);
      }

      protected AnnotationInstance push(AnnotationInstance annotationInstance) {
         return PersistenceMetadataMocker.this.push(annotationInstance);
      }

      protected AnnotationInstance create(DotName name, AnnotationTarget target, AnnotationValue[] annotationValues) {
         return PersistenceMetadataMocker.this.create(name, target, annotationValues);
      }

      protected ListenerMocker createListenerMocker(IndexBuilder indexBuilder, ClassInfo classInfo) {
         return PersistenceMetadataMocker.this.new DefaultListenerMocker(indexBuilder, classInfo);
      }
   }
}
