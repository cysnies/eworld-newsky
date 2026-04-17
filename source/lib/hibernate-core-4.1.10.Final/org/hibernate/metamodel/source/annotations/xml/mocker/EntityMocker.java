package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.AccessType;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbDiscriminatorColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntity;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityListeners;
import org.hibernate.internal.jaxb.mapping.orm.JaxbIdClass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbInheritance;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostLoad;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostPersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPostUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPrePersist;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreRemove;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPreUpdate;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSecondaryTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTable;
import org.hibernate.internal.util.StringHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

class EntityMocker extends AbstractEntityObjectMocker {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityMocker.class.getName());
   private JaxbEntity entity;

   EntityMocker(IndexBuilder indexBuilder, JaxbEntity entity, EntityMappingsMocker.Default defaults) {
      super(indexBuilder, defaults);
      this.entity = entity;
   }

   protected String getClassName() {
      return this.entity.getClazz();
   }

   protected void processExtra() {
      this.create(ENTITY, MockHelper.stringValueArray("name", this.entity.getName()));
      if (this.entity.isCacheable() != null) {
         this.create(CACHEABLE, MockHelper.booleanValueArray("value", this.entity.isCacheable()));
      }

      if (StringHelper.isNotEmpty(this.entity.getDiscriminatorValue())) {
         this.create(DISCRIMINATOR_VALUE, MockHelper.stringValueArray("value", this.entity.getDiscriminatorValue()));
      }

      this.parserTable(this.entity.getTable());
      this.parserInheritance(this.entity.getInheritance());
      this.parserDiscriminatorColumn(this.entity.getDiscriminatorColumn());
      this.parserAttributeOverrides(this.entity.getAttributeOverride(), this.getTarget());
      this.parserAssociationOverrides(this.entity.getAssociationOverride(), this.getTarget());
      this.parserPrimaryKeyJoinColumnList(this.entity.getPrimaryKeyJoinColumn(), this.getTarget());
      this.parserSecondaryTableList(this.entity.getSecondaryTable(), this.getTarget());
   }

   private AnnotationInstance parserTable(JaxbTable table) {
      if (table == null) {
         return null;
      } else {
         DefaultConfigurationHelper.INSTANCE.applyDefaults((SchemaAware)(new SchemaAware.TableSchemaAware(table)), this.getDefaults());
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", table.getName(), annotationValueList);
         MockHelper.stringValue("catalog", table.getCatalog(), annotationValueList);
         MockHelper.stringValue("schema", table.getSchema(), annotationValueList);
         this.nestedUniqueConstraintList("uniqueConstraints", table.getUniqueConstraint(), annotationValueList);
         return this.create(TABLE, annotationValueList);
      }
   }

   protected AccessType getDefaultAccess() {
      return this.entity.getAccess() != null ? AccessType.valueOf(this.entity.getAccess().value()) : null;
   }

   protected AccessType getAccessFromIndex(DotName className) {
      Map<DotName, List<AnnotationInstance>> indexedAnnotations = this.indexBuilder.getIndexedAnnotations(className);
      List<AnnotationInstance> accessAnnotationInstances = (List)indexedAnnotations.get(ACCESS);
      if (MockHelper.isNotEmpty(accessAnnotationInstances)) {
         for(AnnotationInstance annotationInstance : accessAnnotationInstances) {
            if (annotationInstance.target() != null && annotationInstance.target() instanceof ClassInfo) {
               ClassInfo ci = (ClassInfo)annotationInstance.target();
               if (className.equals(ci.name())) {
                  return AccessType.valueOf(annotationInstance.value().asEnum());
               }
            }
         }
      }

      return null;
   }

   protected void applyDefaults() {
      DefaultConfigurationHelper.INSTANCE.applyDefaults(this.entity, this.getDefaults());
   }

   protected JaxbPrePersist getPrePersist() {
      return this.entity.getPrePersist();
   }

   protected JaxbPreRemove getPreRemove() {
      return this.entity.getPreRemove();
   }

   protected JaxbPreUpdate getPreUpdate() {
      return this.entity.getPreUpdate();
   }

   protected JaxbPostPersist getPostPersist() {
      return this.entity.getPostPersist();
   }

   protected JaxbPostUpdate getPostUpdate() {
      return this.entity.getPostUpdate();
   }

   protected JaxbPostRemove getPostRemove() {
      return this.entity.getPostRemove();
   }

   protected JaxbPostLoad getPostLoad() {
      return this.entity.getPostLoad();
   }

   protected JaxbAttributes getAttributes() {
      return this.entity.getAttributes();
   }

   protected boolean isMetadataComplete() {
      return this.entity.isMetadataComplete() != null && this.entity.isMetadataComplete();
   }

   protected boolean isExcludeDefaultListeners() {
      return this.entity.getExcludeDefaultListeners() != null;
   }

   protected boolean isExcludeSuperclassListeners() {
      return this.entity.getExcludeSuperclassListeners() != null;
   }

   protected JaxbIdClass getIdClass() {
      return this.entity.getIdClass();
   }

   protected JaxbEntityListeners getEntityListeners() {
      return this.entity.getEntityListeners();
   }

   protected JaxbAccessType getAccessType() {
      return this.entity.getAccess();
   }

   protected AnnotationInstance parserInheritance(JaxbInheritance inheritance) {
      return inheritance == null ? null : this.create(INHERITANCE, MockHelper.enumValueArray("strategy", INHERITANCE_TYPE, inheritance.getStrategy()));
   }

   protected AnnotationInstance parserDiscriminatorColumn(JaxbDiscriminatorColumn discriminatorColumn) {
      if (discriminatorColumn == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", discriminatorColumn.getName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", discriminatorColumn.getColumnDefinition(), annotationValueList);
         MockHelper.integerValue("length", discriminatorColumn.getLength(), annotationValueList);
         MockHelper.enumValue("discriminatorType", DISCRIMINATOR_TYPE, discriminatorColumn.getDiscriminatorType(), annotationValueList);
         return this.create(DISCRIMINATOR_COLUMN, annotationValueList);
      }
   }

   protected AnnotationInstance parserSecondaryTable(JaxbSecondaryTable secondaryTable, AnnotationTarget target) {
      if (secondaryTable == null) {
         return null;
      } else {
         DefaultConfigurationHelper.INSTANCE.applyDefaults((SchemaAware)(new SchemaAware.SecondaryTableSchemaAware(secondaryTable)), this.getDefaults());
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", secondaryTable.getName(), annotationValueList);
         MockHelper.stringValue("catalog", secondaryTable.getCatalog(), annotationValueList);
         MockHelper.stringValue("schema", secondaryTable.getSchema(), annotationValueList);
         this.nestedPrimaryKeyJoinColumnList("pkJoinColumns", secondaryTable.getPrimaryKeyJoinColumn(), annotationValueList);
         this.nestedUniqueConstraintList("uniqueConstraints", secondaryTable.getUniqueConstraint(), annotationValueList);
         return this.create(SECONDARY_TABLE, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserSecondaryTableList(List primaryKeyJoinColumnList, AnnotationTarget target) {
      if (MockHelper.isNotEmpty(primaryKeyJoinColumnList)) {
         return primaryKeyJoinColumnList.size() == 1 ? this.parserSecondaryTable((JaxbSecondaryTable)primaryKeyJoinColumnList.get(0), target) : this.create(SECONDARY_TABLES, target, this.nestedSecondaryTableList("value", primaryKeyJoinColumnList, (List)null));
      } else {
         return null;
      }
   }

   protected AnnotationValue[] nestedSecondaryTableList(String name, List secondaryTableList, List annotationValueList) {
      if (!MockHelper.isNotEmpty(secondaryTableList)) {
         return MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY;
      } else {
         AnnotationValue[] values = new AnnotationValue[secondaryTableList.size()];

         for(int i = 0; i < secondaryTableList.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserSecondaryTable((JaxbSecondaryTable)secondaryTableList.get(i), (AnnotationTarget)null);
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
         return values;
      }
   }
}
