package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbColumnResult;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityResult;
import org.hibernate.internal.jaxb.mapping.orm.JaxbFieldResult;
import org.hibernate.internal.jaxb.mapping.orm.JaxbNamedNativeQuery;
import org.hibernate.internal.jaxb.mapping.orm.JaxbNamedQuery;
import org.hibernate.internal.jaxb.mapping.orm.JaxbQueryHint;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSequenceGenerator;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSqlResultSetMapping;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTableGenerator;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

class GlobalAnnotationMocker extends AbstractMocker {
   private GlobalAnnotations globalAnnotations;

   GlobalAnnotationMocker(IndexBuilder indexBuilder, GlobalAnnotations globalAnnotations) {
      super(indexBuilder);
      this.globalAnnotations = globalAnnotations;
   }

   void process() {
      if (!this.globalAnnotations.getTableGeneratorMap().isEmpty()) {
         for(JaxbTableGenerator generator : this.globalAnnotations.getTableGeneratorMap().values()) {
            this.parserTableGenerator(generator);
         }
      }

      if (!this.globalAnnotations.getSequenceGeneratorMap().isEmpty()) {
         for(JaxbSequenceGenerator generator : this.globalAnnotations.getSequenceGeneratorMap().values()) {
            this.parserSequenceGenerator(generator);
         }
      }

      if (!this.globalAnnotations.getNamedQueryMap().isEmpty()) {
         Collection<JaxbNamedQuery> namedQueries = this.globalAnnotations.getNamedQueryMap().values();
         if (namedQueries.size() > 1) {
            this.parserNamedQueries(namedQueries);
         } else {
            this.parserNamedQuery((JaxbNamedQuery)namedQueries.iterator().next());
         }
      }

      if (!this.globalAnnotations.getNamedNativeQueryMap().isEmpty()) {
         Collection<JaxbNamedNativeQuery> namedQueries = this.globalAnnotations.getNamedNativeQueryMap().values();
         if (namedQueries.size() > 1) {
            this.parserNamedNativeQueries(namedQueries);
         } else {
            this.parserNamedNativeQuery((JaxbNamedNativeQuery)namedQueries.iterator().next());
         }
      }

      if (!this.globalAnnotations.getSqlResultSetMappingMap().isEmpty()) {
         this.parserSqlResultSetMappings(this.globalAnnotations.getSqlResultSetMappingMap().values());
      }

      this.indexBuilder.finishGlobalConfigurationMocking(this.globalAnnotations);
   }

   private AnnotationInstance parserSqlResultSetMappings(Collection namedQueries) {
      AnnotationValue[] values = new AnnotationValue[namedQueries.size()];
      int i = 0;

      AnnotationInstance annotationInstance;
      for(Iterator<JaxbSqlResultSetMapping> iterator = namedQueries.iterator(); iterator.hasNext(); values[i++] = MockHelper.nestedAnnotationValue("", annotationInstance)) {
         annotationInstance = this.parserSqlResultSetMapping((JaxbSqlResultSetMapping)iterator.next());
      }

      return this.create(SQL_RESULT_SET_MAPPINGS, (AnnotationTarget)null, new AnnotationValue[]{AnnotationValue.createArrayValue("values", values)});
   }

   private AnnotationInstance parserSqlResultSetMapping(JaxbSqlResultSetMapping mapping) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", mapping.getName(), annotationValueList);
      this.nestedEntityResultList("entities", mapping.getEntityResult(), annotationValueList);
      this.nestedColumnResultList("columns", mapping.getColumnResult(), annotationValueList);
      return this.create(SQL_RESULT_SET_MAPPING, (AnnotationTarget)null, annotationValueList);
   }

   private AnnotationInstance parserEntityResult(JaxbEntityResult result) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("discriminatorColumn", result.getDiscriminatorColumn(), annotationValueList);
      this.nestedFieldResultList("fields", result.getFieldResult(), annotationValueList);
      MockHelper.classValue("entityClass", result.getEntityClass(), annotationValueList, this.indexBuilder.getServiceRegistry());
      return this.create(ENTITY_RESULT, (AnnotationTarget)null, annotationValueList);
   }

   private void nestedEntityResultList(String name, List entityResults, List annotationValueList) {
      if (MockHelper.isNotEmpty(entityResults)) {
         AnnotationValue[] values = new AnnotationValue[entityResults.size()];

         for(int i = 0; i < entityResults.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserEntityResult((JaxbEntityResult)entityResults.get(i));
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
      }

   }

   private AnnotationInstance parserColumnResult(JaxbColumnResult result) {
      return this.create(COLUMN_RESULT, (AnnotationTarget)null, MockHelper.stringValueArray("name", result.getName()));
   }

   private void nestedColumnResultList(String name, List columnResults, List annotationValueList) {
      if (MockHelper.isNotEmpty(columnResults)) {
         AnnotationValue[] values = new AnnotationValue[columnResults.size()];

         for(int i = 0; i < columnResults.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserColumnResult((JaxbColumnResult)columnResults.get(i));
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
      }

   }

   private AnnotationInstance parserFieldResult(JaxbFieldResult result) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", result.getName(), annotationValueList);
      MockHelper.stringValue("column", result.getColumn(), annotationValueList);
      return this.create(FIELD_RESULT, (AnnotationTarget)null, annotationValueList);
   }

   private void nestedFieldResultList(String name, List fieldResultList, List annotationValueList) {
      if (MockHelper.isNotEmpty(fieldResultList)) {
         AnnotationValue[] values = new AnnotationValue[fieldResultList.size()];

         for(int i = 0; i < fieldResultList.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserFieldResult((JaxbFieldResult)fieldResultList.get(i));
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
      }

   }

   private AnnotationInstance parserNamedNativeQueries(Collection namedQueries) {
      AnnotationValue[] values = new AnnotationValue[namedQueries.size()];
      int i = 0;

      AnnotationInstance annotationInstance;
      for(Iterator<JaxbNamedNativeQuery> iterator = namedQueries.iterator(); iterator.hasNext(); values[i++] = MockHelper.nestedAnnotationValue("", annotationInstance)) {
         annotationInstance = this.parserNamedNativeQuery((JaxbNamedNativeQuery)iterator.next());
      }

      return this.create(NAMED_NATIVE_QUERIES, (AnnotationTarget)null, new AnnotationValue[]{AnnotationValue.createArrayValue("values", values)});
   }

   private AnnotationInstance parserNamedNativeQuery(JaxbNamedNativeQuery namedNativeQuery) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", namedNativeQuery.getName(), annotationValueList);
      MockHelper.stringValue("query", namedNativeQuery.getQuery(), annotationValueList);
      MockHelper.stringValue("resultSetMapping", namedNativeQuery.getResultSetMapping(), annotationValueList);
      MockHelper.classValue("resultClass", namedNativeQuery.getResultClass(), annotationValueList, this.indexBuilder.getServiceRegistry());
      this.nestedQueryHintList("hints", namedNativeQuery.getHint(), annotationValueList);
      return this.create(NAMED_NATIVE_QUERY, (AnnotationTarget)null, annotationValueList);
   }

   private AnnotationInstance parserNamedQueries(Collection namedQueries) {
      AnnotationValue[] values = new AnnotationValue[namedQueries.size()];
      int i = 0;

      AnnotationInstance annotationInstance;
      for(Iterator<JaxbNamedQuery> iterator = namedQueries.iterator(); iterator.hasNext(); values[i++] = MockHelper.nestedAnnotationValue("", annotationInstance)) {
         annotationInstance = this.parserNamedQuery((JaxbNamedQuery)iterator.next());
      }

      return this.create(NAMED_QUERIES, (AnnotationTarget)null, new AnnotationValue[]{AnnotationValue.createArrayValue("values", values)});
   }

   private AnnotationInstance parserNamedQuery(JaxbNamedQuery namedQuery) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", namedQuery.getName(), annotationValueList);
      MockHelper.stringValue("query", namedQuery.getQuery(), annotationValueList);
      MockHelper.enumValue("lockMode", LOCK_MODE_TYPE, namedQuery.getLockMode(), annotationValueList);
      this.nestedQueryHintList("hints", namedQuery.getHint(), annotationValueList);
      return this.create(NAMED_QUERY, (AnnotationTarget)null, annotationValueList);
   }

   private AnnotationInstance parserQueryHint(JaxbQueryHint queryHint) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", queryHint.getName(), annotationValueList);
      MockHelper.stringValue("value", queryHint.getValue(), annotationValueList);
      return this.create(QUERY_HINT, (AnnotationTarget)null, annotationValueList);
   }

   private void nestedQueryHintList(String name, List constraints, List annotationValueList) {
      if (MockHelper.isNotEmpty(constraints)) {
         AnnotationValue[] values = new AnnotationValue[constraints.size()];

         for(int i = 0; i < constraints.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserQueryHint((JaxbQueryHint)constraints.get(i));
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
      }

   }

   private AnnotationInstance parserSequenceGenerator(JaxbSequenceGenerator generator) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", generator.getName(), annotationValueList);
      MockHelper.stringValue("catalog", generator.getCatalog(), annotationValueList);
      MockHelper.stringValue("schema", generator.getSchema(), annotationValueList);
      MockHelper.stringValue("sequenceName", generator.getSequenceName(), annotationValueList);
      MockHelper.integerValue("initialValue", generator.getInitialValue(), annotationValueList);
      MockHelper.integerValue("allocationSize", generator.getAllocationSize(), annotationValueList);
      return this.create(SEQUENCE_GENERATOR, (AnnotationTarget)null, annotationValueList);
   }

   private AnnotationInstance parserTableGenerator(JaxbTableGenerator generator) {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.stringValue("name", generator.getName(), annotationValueList);
      MockHelper.stringValue("catalog", generator.getCatalog(), annotationValueList);
      MockHelper.stringValue("schema", generator.getSchema(), annotationValueList);
      MockHelper.stringValue("table", generator.getTable(), annotationValueList);
      MockHelper.stringValue("pkColumnName", generator.getPkColumnName(), annotationValueList);
      MockHelper.stringValue("valueColumnName", generator.getValueColumnName(), annotationValueList);
      MockHelper.stringValue("pkColumnValue", generator.getPkColumnValue(), annotationValueList);
      MockHelper.integerValue("initialValue", generator.getInitialValue(), annotationValueList);
      MockHelper.integerValue("allocationSize", generator.getAllocationSize(), annotationValueList);
      this.nestedUniqueConstraintList("uniqueConstraints", generator.getUniqueConstraint(), annotationValueList);
      return this.create(TABLE_GENERATOR, (AnnotationTarget)null, annotationValueList);
   }

   protected AnnotationInstance push(AnnotationInstance annotationInstance) {
      return annotationInstance != null ? this.globalAnnotations.push(annotationInstance.name(), annotationInstance) : null;
   }
}
