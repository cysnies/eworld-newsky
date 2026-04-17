package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEnumType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMapKey;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMapKeyClass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMapKeyColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMapKeyJoinColumn;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTemporalType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

abstract class PropertyMocker extends AnnotationMocker {
   protected ClassInfo classInfo;
   private AnnotationTarget target;

   PropertyMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults) {
      super(indexBuilder, defaults);
      this.classInfo = classInfo;
   }

   protected abstract void processExtra();

   protected abstract String getFieldName();

   protected abstract JaxbAccessType getAccessType();

   protected abstract void setAccessType(JaxbAccessType var1);

   protected DotName getTargetName() {
      return this.classInfo.name();
   }

   protected void resolveTarget() {
      JaxbAccessType accessType = this.getAccessType();
      if (accessType == null) {
         accessType = AccessHelper.getAccessFromAttributeAnnotation(this.getTargetName(), this.getFieldName(), this.indexBuilder);
         if (accessType == null) {
            accessType = AccessHelper.getEntityAccess(this.getTargetName(), this.indexBuilder);
         }

         if (accessType == null) {
            accessType = AccessHelper.getAccessFromIdPosition(this.getTargetName(), this.indexBuilder);
         }

         if (accessType == null) {
            accessType = AccessHelper.getAccessFromDefault(this.indexBuilder);
         }

         if (accessType == null) {
            accessType = JaxbAccessType.PROPERTY;
         }

         this.setAccessType(accessType);
      }

   }

   protected AnnotationTarget getTarget() {
      if (this.target == null) {
         this.target = this.getTargetFromAttributeAccessType(this.getAccessType());
      }

      return this.target;
   }

   protected AnnotationTarget getTargetFromAttributeAccessType(JaxbAccessType accessType) {
      if (accessType == null) {
         throw new IllegalArgumentException("access type can't be null.");
      } else {
         switch (accessType) {
            case FIELD:
               return MockHelper.getTarget(this.indexBuilder.getServiceRegistry(), this.classInfo, this.getFieldName(), MockHelper.TargetType.FIELD);
            case PROPERTY:
               return MockHelper.getTarget(this.indexBuilder.getServiceRegistry(), this.classInfo, this.getFieldName(), MockHelper.TargetType.PROPERTY);
            default:
               throw new HibernateException("can't determin access type [" + accessType + "]");
         }
      }
   }

   final void process() {
      this.resolveTarget();
      this.processExtra();
   }

   protected AnnotationInstance parserMapKeyColumn(JaxbMapKeyColumn mapKeyColumn, AnnotationTarget target) {
      if (mapKeyColumn == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", mapKeyColumn.getName(), annotationValueList);
         MockHelper.stringValue("columnDefinition", mapKeyColumn.getColumnDefinition(), annotationValueList);
         MockHelper.stringValue("table", mapKeyColumn.getTable(), annotationValueList);
         MockHelper.booleanValue("nullable", mapKeyColumn.isNullable(), annotationValueList);
         MockHelper.booleanValue("insertable", mapKeyColumn.isInsertable(), annotationValueList);
         MockHelper.booleanValue("updatable", mapKeyColumn.isUpdatable(), annotationValueList);
         MockHelper.booleanValue("unique", mapKeyColumn.isUnique(), annotationValueList);
         MockHelper.integerValue("length", mapKeyColumn.getLength(), annotationValueList);
         MockHelper.integerValue("precision", mapKeyColumn.getPrecision(), annotationValueList);
         MockHelper.integerValue("scale", mapKeyColumn.getScale(), annotationValueList);
         return this.create(MAP_KEY_COLUMN, target, annotationValueList);
      }
   }

   protected AnnotationInstance parserMapKeyClass(JaxbMapKeyClass mapKeyClass, AnnotationTarget target) {
      return mapKeyClass == null ? null : this.create(MAP_KEY_CLASS, target, MockHelper.classValueArray("value", mapKeyClass.getClazz(), this.indexBuilder.getServiceRegistry()));
   }

   protected AnnotationInstance parserMapKeyTemporal(JaxbTemporalType temporalType, AnnotationTarget target) {
      return temporalType == null ? null : this.create(MAP_KEY_TEMPORAL, target, MockHelper.enumValueArray("value", TEMPORAL_TYPE, temporalType));
   }

   protected AnnotationInstance parserMapKeyEnumerated(JaxbEnumType enumType, AnnotationTarget target) {
      return enumType == null ? null : this.create(MAP_KEY_ENUMERATED, target, MockHelper.enumValueArray("value", ENUM_TYPE, enumType));
   }

   protected AnnotationInstance parserMapKey(JaxbMapKey mapKey, AnnotationTarget target) {
      return mapKey == null ? null : this.create(MAP_KEY, target, MockHelper.stringValueArray("name", mapKey.getName()));
   }

   private AnnotationValue[] nestedMapKeyJoinColumnList(String name, List columns, List annotationValueList) {
      if (!MockHelper.isNotEmpty(columns)) {
         return MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY;
      } else {
         AnnotationValue[] values = new AnnotationValue[columns.size()];

         for(int i = 0; i < columns.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserMapKeyJoinColumn((JaxbMapKeyJoinColumn)columns.get(i), (AnnotationTarget)null);
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
         return values;
      }
   }

   protected AnnotationInstance parserMapKeyJoinColumnList(List joinColumnList, AnnotationTarget target) {
      if (MockHelper.isNotEmpty(joinColumnList)) {
         if (joinColumnList.size() == 1) {
            return this.parserMapKeyJoinColumn((JaxbMapKeyJoinColumn)joinColumnList.get(0), target);
         } else {
            AnnotationValue[] values = this.nestedMapKeyJoinColumnList("value", joinColumnList, (List)null);
            return this.create(MAP_KEY_JOIN_COLUMNS, target, values);
         }
      } else {
         return null;
      }
   }

   private AnnotationInstance parserMapKeyJoinColumn(JaxbMapKeyJoinColumn column, AnnotationTarget target) {
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
         return this.create(MAP_KEY_JOIN_COLUMN, target, annotationValueList);
      }
   }
}
