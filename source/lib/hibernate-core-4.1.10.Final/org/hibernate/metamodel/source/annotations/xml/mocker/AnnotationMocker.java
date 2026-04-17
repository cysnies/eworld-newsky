package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAssociationOverride;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributeOverride;
import org.hibernate.internal.jaxb.mapping.orm.JaxbCollectionTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEnumType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbJoinColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbJoinTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbLob;
import org.hibernate.internal.jaxb.mapping.orm.JaxbOrderColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrimaryKeyJoinColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTemporalType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

abstract class AnnotationMocker extends AbstractMocker {
   private EntityMappingsMocker.Default defaults;

   AnnotationMocker(IndexBuilder indexBuilder, EntityMappingsMocker.Default defaults) {
      super(indexBuilder);
      this.defaults = defaults;
   }

   abstract void process();

   protected EntityMappingsMocker.Default getDefaults() {
      return this.defaults;
   }

   protected boolean isDefaultCascadePersist() {
      return this.defaults.isCascadePersist() != null && this.defaults.isCascadePersist();
   }

   protected AnnotationInstance parserJoinTable(JaxbJoinTable joinTable, AnnotationTarget target) {
      if (joinTable == null) {
         return null;
      } else {
         DefaultConfigurationHelper.INSTANCE.applyDefaults((SchemaAware)(new SchemaAware.JoinTableSchemaAware(joinTable)), this.getDefaults());
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", joinTable.getName(), annotationValueList);
         MockHelper.stringValue("catalog", joinTable.getCatalog(), annotationValueList);
         MockHelper.stringValue("schema", joinTable.getSchema(), annotationValueList);
         this.nestedJoinColumnList("joinColumns", joinTable.getJoinColumn(), annotationValueList);
         this.nestedJoinColumnList("inverseJoinColumns", joinTable.getInverseJoinColumn(), annotationValueList);
         this.nestedUniqueConstraintList("uniqueConstraints", joinTable.getUniqueConstraint(), annotationValueList);
         return this.create(JOIN_TABLE, target, annotationValueList);
      }
   }

   private AnnotationInstance parserAssociationOverride(JaxbAssociationOverride associationOverride, AnnotationTarget target) {
      if (associationOverride == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", associationOverride.getName(), annotationValueList);
         if (associationOverride instanceof JaxbAssociationOverrideProxy) {
            JaxbAssociationOverrideProxy proxy = (JaxbAssociationOverrideProxy)associationOverride;
            MockHelper.addToCollectionIfNotNull(annotationValueList, proxy.getJoinColumnsAnnotationValue());
            MockHelper.addToCollectionIfNotNull(annotationValueList, proxy.getJoinTableAnnotationValue());
         } else {
            this.nestedJoinColumnList("joinColumns", associationOverride.getJoinColumn(), annotationValueList);
            MockHelper.nestedAnnotationValue("joinTable", this.parserJoinTable(associationOverride.getJoinTable(), (AnnotationTarget)null), annotationValueList);
         }

         return this.create(ASSOCIATION_OVERRIDE, target, annotationValueList);
      }
   }

   private AnnotationValue[] nestedJoinColumnList(String name, List columns, List annotationValueList) {
      if (!MockHelper.isNotEmpty(columns)) {
         return MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY;
      } else {
         AnnotationValue[] values = new AnnotationValue[columns.size()];

         for(int i = 0; i < columns.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserJoinColumn((JaxbJoinColumn)columns.get(i), (AnnotationTarget)null);
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
         return values;
      }
   }

   protected AnnotationInstance parserColumn(JaxbColumn column, AnnotationTarget target) {
      if (column == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", column.getName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", column.getColumnDefinition(), annotationValueList);
         MockHelper.stringValue("table", column.getTable(), annotationValueList);
         MockHelper.booleanValue("unique", column.isUnique(), annotationValueList);
         MockHelper.booleanValue("nullable", column.isNullable(), annotationValueList);
         MockHelper.booleanValue("insertable", column.isInsertable(), annotationValueList);
         MockHelper.booleanValue("updatable", column.isUpdatable(), annotationValueList);
         MockHelper.integerValue("length", column.getLength(), annotationValueList);
         MockHelper.integerValue("precision", column.getPrecision(), annotationValueList);
         MockHelper.integerValue("scale", column.getScale(), annotationValueList);
         return this.create(COLUMN, target, annotationValueList);
      }
   }

   private AnnotationInstance parserAttributeOverride(JaxbAttributeOverride attributeOverride, AnnotationTarget target) {
      if (attributeOverride == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", attributeOverride.getName(), annotationValueList);
         if (attributeOverride instanceof JaxbAttributeOverrideProxy) {
            JaxbAttributeOverrideProxy proxy = (JaxbAttributeOverrideProxy)attributeOverride;
            MockHelper.addToCollectionIfNotNull(annotationValueList, proxy.getColumnAnnotationValue());
         } else {
            MockHelper.nestedAnnotationValue("column", this.parserColumn(attributeOverride.getColumn(), (AnnotationTarget)null), annotationValueList);
         }

         return this.create(ATTRIBUTE_OVERRIDE, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserOrderColumn(JaxbOrderColumn orderColumn, AnnotationTarget target) {
      if (orderColumn == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", orderColumn.getName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", orderColumn.getColumnDefinition(), annotationValueList);
         MockHelper.booleanValue("nullable", orderColumn.isNullable(), annotationValueList);
         MockHelper.booleanValue("insertable", orderColumn.isInsertable(), annotationValueList);
         MockHelper.booleanValue("updatable", orderColumn.isUpdatable(), annotationValueList);
         return this.create(ORDER_COLUMN, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserJoinColumn(JaxbJoinColumn column, AnnotationTarget target) {
      if (column == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", column.getName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", column.getColumnDefinition(), annotationValueList);
         MockHelper.stringValue("table", column.getTable(), annotationValueList);
         MockHelper.stringValue("referencedColumnName", column.getReferencedColumnName(), annotationValueList);
         MockHelper.booleanValue("unique", column.isUnique(), annotationValueList);
         MockHelper.booleanValue("nullable", column.isNullable(), annotationValueList);
         MockHelper.booleanValue("insertable", column.isInsertable(), annotationValueList);
         MockHelper.booleanValue("updatable", column.isUpdatable(), annotationValueList);
         return this.create(JOIN_COLUMN, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserLob(JaxbLob lob, AnnotationTarget target) {
      return lob == null ? null : this.create(LOB, (AnnotationTarget)target);
   }

   protected AnnotationInstance parserTemporalType(JaxbTemporalType temporalType, AnnotationTarget target) {
      return temporalType == null ? null : this.create(TEMPORAL, target, MockHelper.enumValueArray("value", TEMPORAL_TYPE, temporalType));
   }

   protected AnnotationInstance parserEnumType(JaxbEnumType enumerated, AnnotationTarget target) {
      return enumerated == null ? null : this.create(ENUMERATED, target, MockHelper.enumValueArray("value", ENUM_TYPE, enumerated));
   }

   protected AnnotationInstance parserPrimaryKeyJoinColumn(JaxbPrimaryKeyJoinColumn primaryKeyJoinColumn, AnnotationTarget target) {
      if (primaryKeyJoinColumn == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", primaryKeyJoinColumn.getName(), annotationValueList);
         MockHelper.stringValue("referencedColumnName", primaryKeyJoinColumn.getReferencedColumnName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", primaryKeyJoinColumn.getColumnDefinition(), annotationValueList);
         return this.create(PRIMARY_KEY_JOIN_COLUMN, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserPrimaryKeyJoinColumnList(List primaryKeyJoinColumnList, AnnotationTarget target) {
      if (MockHelper.isNotEmpty(primaryKeyJoinColumnList)) {
         return primaryKeyJoinColumnList.size() == 1 ? this.parserPrimaryKeyJoinColumn((JaxbPrimaryKeyJoinColumn)primaryKeyJoinColumnList.get(0), target) : this.create(PRIMARY_KEY_JOIN_COLUMNS, target, this.nestedPrimaryKeyJoinColumnList("value", primaryKeyJoinColumnList, (List)null));
      } else {
         return null;
      }
   }

   protected AnnotationValue[] nestedPrimaryKeyJoinColumnList(String name, List constraints, List annotationValueList) {
      if (!MockHelper.isNotEmpty(constraints)) {
         return MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY;
      } else {
         AnnotationValue[] values = new AnnotationValue[constraints.size()];

         for(int i = 0; i < constraints.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserPrimaryKeyJoinColumn((JaxbPrimaryKeyJoinColumn)constraints.get(i), (AnnotationTarget)null);
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
         return values;
      }
   }

   protected void getAnnotationInstanceByTarget(DotName annName, AnnotationTarget target, Operation operation) {
      Map<DotName, List<AnnotationInstance>> annotatedMap = this.indexBuilder.getIndexedAnnotations(this.getTargetName());
      if (annotatedMap.containsKey(annName)) {
         List<AnnotationInstance> annotationInstanceList = (List)annotatedMap.get(annName);
         if (MockHelper.isNotEmpty(annotationInstanceList)) {
            for(AnnotationInstance annotationInstance : annotationInstanceList) {
               AnnotationTarget annotationTarget = annotationInstance.target();
               if (MockHelper.targetEquals(target, annotationTarget) && operation.process(annotationInstance)) {
                  return;
               }
            }
         }

      }
   }

   protected AnnotationInstance parserAttributeOverrides(List attributeOverrides, AnnotationTarget target) {
      if (target == null) {
         throw new AssertionFailure("target can not be null");
      } else if (attributeOverrides != null && !attributeOverrides.isEmpty()) {
         Set<String> names = new HashSet();

         for(JaxbAttributeOverride attributeOverride : attributeOverrides) {
            names.add(attributeOverride.getName());
         }

         Operation operation = new AttributeOverrideOperation(names, attributeOverrides);
         this.getAnnotationInstanceByTarget(ATTRIBUTE_OVERRIDES, target, new ContainerOperation(operation));
         this.getAnnotationInstanceByTarget(ATTRIBUTE_OVERRIDE, target, operation);
         if (attributeOverrides.size() == 1) {
            return this.parserAttributeOverride((JaxbAttributeOverride)attributeOverrides.get(0), target);
         } else {
            AnnotationValue[] values = new AnnotationValue[attributeOverrides.size()];

            for(int i = 0; i < values.length; ++i) {
               values[i] = MockHelper.nestedAnnotationValue("", this.parserAttributeOverride((JaxbAttributeOverride)attributeOverrides.get(i), (AnnotationTarget)null));
            }

            return this.create(ATTRIBUTE_OVERRIDES, target, new AnnotationValue[]{AnnotationValue.createArrayValue("value", values)});
         }
      } else {
         return null;
      }
   }

   protected AnnotationInstance parserAssociationOverrides(List associationOverrides, AnnotationTarget target) {
      if (target == null) {
         throw new AssertionFailure("target can not be null");
      } else if (associationOverrides != null && !associationOverrides.isEmpty()) {
         Set<String> names = new HashSet();

         for(JaxbAssociationOverride associationOverride : associationOverrides) {
            names.add(associationOverride.getName());
         }

         Operation operation = new AssociationOverrideOperation(names, associationOverrides);
         this.getAnnotationInstanceByTarget(ASSOCIATION_OVERRIDES, target, new ContainerOperation(operation));
         this.getAnnotationInstanceByTarget(ASSOCIATION_OVERRIDE, target, operation);
         if (associationOverrides.size() == 1) {
            return this.parserAssociationOverride((JaxbAssociationOverride)associationOverrides.get(0), target);
         } else {
            AnnotationValue[] values = new AnnotationValue[associationOverrides.size()];

            for(int i = 0; i < values.length; ++i) {
               values[i] = MockHelper.nestedAnnotationValue("", this.parserAssociationOverride((JaxbAssociationOverride)associationOverrides.get(i), (AnnotationTarget)null));
            }

            return this.create(ASSOCIATION_OVERRIDES, target, new AnnotationValue[]{AnnotationValue.createArrayValue("value", values)});
         }
      } else {
         return null;
      }
   }

   protected AnnotationInstance parserCollectionTable(JaxbCollectionTable collectionTable, AnnotationTarget target) {
      if (collectionTable == null) {
         return null;
      } else {
         DefaultConfigurationHelper.INSTANCE.applyDefaults((SchemaAware)(new SchemaAware.CollectionTableSchemaAware(collectionTable)), this.getDefaults());
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", collectionTable.getName(), annotationValueList);
         MockHelper.stringValue("catalog", collectionTable.getCatalog(), annotationValueList);
         MockHelper.stringValue("schema", collectionTable.getSchema(), annotationValueList);
         this.nestedJoinColumnList("joinColumns", collectionTable.getJoinColumn(), annotationValueList);
         this.nestedUniqueConstraintList("uniqueConstraints", collectionTable.getUniqueConstraint(), annotationValueList);
         return this.create(COLLECTION_TABLE, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserJoinColumnList(List joinColumnList, AnnotationTarget target) {
      if (MockHelper.isNotEmpty(joinColumnList)) {
         if (joinColumnList.size() == 1) {
            return this.parserJoinColumn((JaxbJoinColumn)joinColumnList.get(0), target);
         } else {
            AnnotationValue[] values = this.nestedJoinColumnList("value", joinColumnList, (List)null);
            return this.create(JOIN_COLUMNS, target, values);
         }
      } else {
         return null;
      }
   }

   protected AnnotationInstance create(DotName name) {
      return this.create(name, MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY);
   }

   protected AnnotationInstance create(DotName name, AnnotationValue[] annotationValues) {
      return this.create(name, this.getTarget(), annotationValues);
   }

   protected AnnotationInstance create(DotName name, List annotationValueList) {
      return this.create(name, this.getTarget(), annotationValueList);
   }

   protected abstract DotName getTargetName();

   protected abstract AnnotationTarget getTarget();

   protected AnnotationInstance push(AnnotationInstance annotationInstance) {
      if (annotationInstance != null && annotationInstance.target() != null) {
         this.indexBuilder.addAnnotationInstance(this.getTargetName(), annotationInstance);
      }

      return annotationInstance;
   }

   class ContainerOperation implements Operation {
      private Operation child;

      ContainerOperation(Operation child) {
         this.child = child;
      }

      public boolean process(AnnotationInstance annotationInstance) {
         AnnotationValue value = annotationInstance.value();
         AnnotationInstance[] indexedAttributeOverridesValues = value.asNestedArray();

         for(AnnotationInstance ai : indexedAttributeOverridesValues) {
            this.child.process(ai);
         }

         return true;
      }
   }

   class AttributeOverrideOperation implements Operation {
      private Set names;
      private List attributeOverrides;

      AttributeOverrideOperation(Set names, List attributeOverrides) {
         this.names = names;
         this.attributeOverrides = attributeOverrides;
      }

      public boolean process(AnnotationInstance annotationInstance) {
         String name = annotationInstance.value("name").asString();
         if (!this.names.contains(name)) {
            JaxbAttributeOverrideProxy attributeOverride = AnnotationMocker.this.new JaxbAttributeOverrideProxy();
            attributeOverride.setName(name);
            attributeOverride.setColumnAnnotationValue(annotationInstance.value("column"));
            this.attributeOverrides.add(attributeOverride);
         }

         return false;
      }
   }

   class AssociationOverrideOperation implements Operation {
      private Set names;
      private List associationOverrides;

      AssociationOverrideOperation(Set names, List associationOverrides) {
         this.names = names;
         this.associationOverrides = associationOverrides;
      }

      public boolean process(AnnotationInstance annotationInstance) {
         String name = annotationInstance.value("name").asString();
         if (!this.names.contains(name)) {
            JaxbAssociationOverrideProxy associationOverride = AnnotationMocker.this.new JaxbAssociationOverrideProxy();
            associationOverride.setName(name);
            associationOverride.setJoinColumnsAnnotationValue(annotationInstance.value("joinColumns"));
            associationOverride.setJoinTableAnnotationValue(annotationInstance.value("joinTable"));
            this.associationOverrides.add(associationOverride);
         }

         return false;
      }
   }

   class JaxbAssociationOverrideProxy extends JaxbAssociationOverride {
      private AnnotationValue joinTableAnnotationValue;
      private AnnotationValue joinColumnsAnnotationValue;

      AnnotationValue getJoinColumnsAnnotationValue() {
         return this.joinColumnsAnnotationValue;
      }

      void setJoinColumnsAnnotationValue(AnnotationValue joinColumnsAnnotationValue) {
         this.joinColumnsAnnotationValue = joinColumnsAnnotationValue;
      }

      AnnotationValue getJoinTableAnnotationValue() {
         return this.joinTableAnnotationValue;
      }

      void setJoinTableAnnotationValue(AnnotationValue joinTableAnnotationValue) {
         this.joinTableAnnotationValue = joinTableAnnotationValue;
      }
   }

   class JaxbAttributeOverrideProxy extends JaxbAttributeOverride {
      private AnnotationValue columnAnnotationValue;

      AnnotationValue getColumnAnnotationValue() {
         return this.columnAnnotationValue;
      }

      void setColumnAnnotationValue(AnnotationValue columnAnnotationValue) {
         this.columnAnnotationValue = columnAnnotationValue;
      }
   }

   protected interface Operation {
      boolean process(AnnotationInstance var1);
   }
}
