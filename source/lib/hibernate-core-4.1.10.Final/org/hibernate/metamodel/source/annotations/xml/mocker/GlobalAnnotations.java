package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntity;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityMappings;
import org.hibernate.internal.jaxb.mapping.orm.JaxbId;
import org.hibernate.internal.jaxb.mapping.orm.JaxbNamedNativeQuery;
import org.hibernate.internal.jaxb.mapping.orm.JaxbNamedQuery;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSequenceGenerator;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSqlResultSetMapping;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTableGenerator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

class GlobalAnnotations implements JPADotNames {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, GlobalAnnotations.class.getName());
   private Map sequenceGeneratorMap = new HashMap();
   private Map tableGeneratorMap = new HashMap();
   private Map namedQueryMap = new HashMap();
   private Map namedNativeQueryMap = new HashMap();
   private Map sqlResultSetMappingMap = new HashMap();
   private Map annotationInstanceMap = new HashMap();
   private List indexedAnnotationInstanceList = new ArrayList();
   private Set defaultNamedNativeQueryNames = new HashSet();
   private Set defaultNamedQueryNames = new HashSet();
   private Set defaultNamedGenerators = new HashSet();
   private Set defaultSqlResultSetMappingNames = new HashSet();

   Map getAnnotationInstanceMap() {
      return this.annotationInstanceMap;
   }

   AnnotationInstance push(DotName name, AnnotationInstance annotationInstance) {
      if (name != null && annotationInstance != null) {
         List<AnnotationInstance> list = (List)this.annotationInstanceMap.get(name);
         if (list == null) {
            list = new ArrayList();
            this.annotationInstanceMap.put(name, list);
         }

         list.add(annotationInstance);
         return annotationInstance;
      } else {
         return null;
      }
   }

   void addIndexedAnnotationInstance(List annotationInstanceList) {
      if (MockHelper.isNotEmpty(annotationInstanceList)) {
         this.indexedAnnotationInstanceList.addAll(annotationInstanceList);
      }

   }

   boolean hasGlobalConfiguration() {
      return !this.namedQueryMap.isEmpty() || !this.namedNativeQueryMap.isEmpty() || !this.sequenceGeneratorMap.isEmpty() || !this.tableGeneratorMap.isEmpty() || !this.sqlResultSetMappingMap.isEmpty();
   }

   Map getNamedNativeQueryMap() {
      return this.namedNativeQueryMap;
   }

   Map getNamedQueryMap() {
      return this.namedQueryMap;
   }

   Map getSequenceGeneratorMap() {
      return this.sequenceGeneratorMap;
   }

   Map getSqlResultSetMappingMap() {
      return this.sqlResultSetMappingMap;
   }

   Map getTableGeneratorMap() {
      return this.tableGeneratorMap;
   }

   public void filterIndexedAnnotations() {
      for(AnnotationInstance annotationInstance : this.indexedAnnotationInstanceList) {
         this.pushIfNotExist(annotationInstance);
      }

   }

   private void pushIfNotExist(AnnotationInstance annotationInstance) {
      DotName annName = annotationInstance.name();
      boolean isNotExist = false;
      if (annName.equals(SQL_RESULT_SET_MAPPINGS)) {
         AnnotationInstance[] annotationInstances = annotationInstance.value().asNestedArray();

         for(AnnotationInstance ai : annotationInstances) {
            this.pushIfNotExist(ai);
         }
      } else {
         AnnotationValue value = annotationInstance.value("name");
         String name = value.asString();
         isNotExist = annName.equals(TABLE_GENERATOR) && !this.tableGeneratorMap.containsKey(name) || annName.equals(SEQUENCE_GENERATOR) && !this.sequenceGeneratorMap.containsKey(name) || annName.equals(NAMED_QUERY) && !this.namedQueryMap.containsKey(name) || annName.equals(NAMED_NATIVE_QUERY) && !this.namedNativeQueryMap.containsKey(name) || annName.equals(SQL_RESULT_SET_MAPPING) && !this.sqlResultSetMappingMap.containsKey(name);
      }

      if (isNotExist) {
         this.push(annName, annotationInstance);
      }

   }

   void collectGlobalMappings(JaxbEntityMappings entityMappings, EntityMappingsMocker.Default defaults) {
      for(JaxbSequenceGenerator generator : entityMappings.getSequenceGenerator()) {
         this.put(generator, defaults);
         this.defaultNamedGenerators.add(generator.getName());
      }

      for(JaxbTableGenerator generator : entityMappings.getTableGenerator()) {
         this.put(generator, defaults);
         this.defaultNamedGenerators.add(generator.getName());
      }

      for(JaxbNamedQuery namedQuery : entityMappings.getNamedQuery()) {
         this.put(namedQuery);
         this.defaultNamedQueryNames.add(namedQuery.getName());
      }

      for(JaxbNamedNativeQuery namedNativeQuery : entityMappings.getNamedNativeQuery()) {
         this.put(namedNativeQuery);
         this.defaultNamedNativeQueryNames.add(namedNativeQuery.getName());
      }

      for(JaxbSqlResultSetMapping sqlResultSetMapping : entityMappings.getSqlResultSetMapping()) {
         this.put(sqlResultSetMapping);
         this.defaultSqlResultSetMappingNames.add(sqlResultSetMapping.getName());
      }

   }

   void collectGlobalMappings(JaxbEntity entity, EntityMappingsMocker.Default defaults) {
      for(JaxbNamedQuery namedQuery : entity.getNamedQuery()) {
         if (!this.defaultNamedQueryNames.contains(namedQuery.getName())) {
            this.put(namedQuery);
         } else {
            LOG.warn("Named Query [" + namedQuery.getName() + "] duplicated.");
         }
      }

      for(JaxbNamedNativeQuery namedNativeQuery : entity.getNamedNativeQuery()) {
         if (!this.defaultNamedNativeQueryNames.contains(namedNativeQuery.getName())) {
            this.put(namedNativeQuery);
         } else {
            LOG.warn("Named native Query [" + namedNativeQuery.getName() + "] duplicated.");
         }
      }

      for(JaxbSqlResultSetMapping sqlResultSetMapping : entity.getSqlResultSetMapping()) {
         if (!this.defaultSqlResultSetMappingNames.contains(sqlResultSetMapping.getName())) {
            this.put(sqlResultSetMapping);
         }
      }

      JaxbSequenceGenerator sequenceGenerator = entity.getSequenceGenerator();
      if (sequenceGenerator != null && !this.defaultNamedGenerators.contains(sequenceGenerator.getName())) {
         this.put(sequenceGenerator, defaults);
      }

      JaxbTableGenerator tableGenerator = entity.getTableGenerator();
      if (tableGenerator != null && !this.defaultNamedGenerators.contains(tableGenerator.getName())) {
         this.put(tableGenerator, defaults);
      }

      JaxbAttributes attributes = entity.getAttributes();
      if (attributes != null) {
         for(JaxbId id : attributes.getId()) {
            sequenceGenerator = id.getSequenceGenerator();
            if (sequenceGenerator != null) {
               this.put(sequenceGenerator, defaults);
            }

            tableGenerator = id.getTableGenerator();
            if (tableGenerator != null) {
               this.put(tableGenerator, defaults);
            }
         }
      }

   }

   private static JaxbSequenceGenerator overrideGenerator(JaxbSequenceGenerator generator, EntityMappingsMocker.Default defaults) {
      if (StringHelper.isEmpty(generator.getSchema()) && defaults != null) {
         generator.setSchema(defaults.getSchema());
      }

      if (StringHelper.isEmpty(generator.getCatalog()) && defaults != null) {
         generator.setCatalog(defaults.getCatalog());
      }

      return generator;
   }

   private static JaxbTableGenerator overrideGenerator(JaxbTableGenerator generator, EntityMappingsMocker.Default defaults) {
      if (StringHelper.isEmpty(generator.getSchema()) && defaults != null) {
         generator.setSchema(defaults.getSchema());
      }

      if (StringHelper.isEmpty(generator.getCatalog()) && defaults != null) {
         generator.setCatalog(defaults.getCatalog());
      }

      return generator;
   }

   private void put(JaxbNamedNativeQuery query) {
      if (query != null) {
         this.checkQueryName(query.getName());
         this.namedNativeQueryMap.put(query.getName(), query);
      }

   }

   private void checkQueryName(String name) {
      if (this.namedQueryMap.containsKey(name) || this.namedNativeQueryMap.containsKey(name)) {
         throw new MappingException("Duplicated query mapping " + name, (Origin)null);
      }
   }

   private void put(JaxbNamedQuery query) {
      if (query != null) {
         this.checkQueryName(query.getName());
         this.namedQueryMap.put(query.getName(), query);
      }

   }

   private void put(JaxbSequenceGenerator generator, EntityMappingsMocker.Default defaults) {
      if (generator != null) {
         Object old = this.sequenceGeneratorMap.put(generator.getName(), overrideGenerator(generator, defaults));
         if (old != null) {
            LOG.duplicateGeneratorName(generator.getName());
         }
      }

   }

   private void put(JaxbTableGenerator generator, EntityMappingsMocker.Default defaults) {
      if (generator != null) {
         Object old = this.tableGeneratorMap.put(generator.getName(), overrideGenerator(generator, defaults));
         if (old != null) {
            LOG.duplicateGeneratorName(generator.getName());
         }
      }

   }

   private void put(JaxbSqlResultSetMapping mapping) {
      if (mapping != null) {
         Object old = this.sqlResultSetMappingMap.put(mapping.getName(), mapping);
         if (old != null) {
            throw new MappingException("Duplicated SQL result set mapping " + mapping.getName(), (Origin)null);
         }
      }

   }
}
