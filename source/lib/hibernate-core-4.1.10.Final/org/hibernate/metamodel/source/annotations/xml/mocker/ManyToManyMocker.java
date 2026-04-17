package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbManyToMany;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class ManyToManyMocker extends PropertyMocker {
   private JaxbManyToMany manyToMany;

   ManyToManyMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbManyToMany manyToMany) {
      super(indexBuilder, classInfo, defaults);
      this.manyToMany = manyToMany;
   }

   protected String getFieldName() {
      return this.manyToMany.getName();
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.classValue("targetEntity", this.manyToMany.getTargetEntity(), annotationValueList, this.indexBuilder.getServiceRegistry());
      MockHelper.enumValue("fetch", FETCH_TYPE, this.manyToMany.getFetch(), annotationValueList);
      MockHelper.stringValue("mappedBy", this.manyToMany.getMappedBy(), annotationValueList);
      MockHelper.cascadeValue("cascade", this.manyToMany.getCascade(), this.isDefaultCascadePersist(), annotationValueList);
      this.create(MANY_TO_MANY, annotationValueList);
      this.parserMapKeyClass(this.manyToMany.getMapKeyClass(), this.getTarget());
      this.parserMapKeyTemporal(this.manyToMany.getMapKeyTemporal(), this.getTarget());
      this.parserMapKeyEnumerated(this.manyToMany.getMapKeyEnumerated(), this.getTarget());
      this.parserMapKey(this.manyToMany.getMapKey(), this.getTarget());
      this.parserAttributeOverrides(this.manyToMany.getMapKeyAttributeOverride(), this.getTarget());
      this.parserMapKeyJoinColumnList(this.manyToMany.getMapKeyJoinColumn(), this.getTarget());
      this.parserOrderColumn(this.manyToMany.getOrderColumn(), this.getTarget());
      this.parserJoinTable(this.manyToMany.getJoinTable(), this.getTarget());
      if (this.manyToMany.getOrderBy() != null) {
         this.create(ORDER_BY, MockHelper.stringValueArray("value", this.manyToMany.getOrderBy()));
      }

   }

   protected JaxbAccessType getAccessType() {
      return this.manyToMany.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.manyToMany.setAccess(accessType);
   }
}
