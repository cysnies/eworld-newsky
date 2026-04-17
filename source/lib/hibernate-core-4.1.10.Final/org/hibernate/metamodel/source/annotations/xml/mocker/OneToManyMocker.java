package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbOneToMany;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class OneToManyMocker extends PropertyMocker {
   private JaxbOneToMany oneToMany;

   OneToManyMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbOneToMany oneToMany) {
      super(indexBuilder, classInfo, defaults);
      this.oneToMany = oneToMany;
   }

   protected String getFieldName() {
      return this.oneToMany.getName();
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.classValue("targetEntity", this.oneToMany.getTargetEntity(), annotationValueList, this.indexBuilder.getServiceRegistry());
      MockHelper.enumValue("fetch", FETCH_TYPE, this.oneToMany.getFetch(), annotationValueList);
      MockHelper.stringValue("mappedBy", this.oneToMany.getMappedBy(), annotationValueList);
      MockHelper.booleanValue("orphanRemoval", this.oneToMany.isOrphanRemoval(), annotationValueList);
      MockHelper.cascadeValue("cascade", this.oneToMany.getCascade(), this.isDefaultCascadePersist(), annotationValueList);
      this.create(ONE_TO_MANY, this.getTarget(), annotationValueList);
      this.parserAttributeOverrides(this.oneToMany.getMapKeyAttributeOverride(), this.getTarget());
      this.parserMapKeyJoinColumnList(this.oneToMany.getMapKeyJoinColumn(), this.getTarget());
      this.parserMapKey(this.oneToMany.getMapKey(), this.getTarget());
      this.parserMapKeyColumn(this.oneToMany.getMapKeyColumn(), this.getTarget());
      this.parserMapKeyClass(this.oneToMany.getMapKeyClass(), this.getTarget());
      this.parserMapKeyTemporal(this.oneToMany.getMapKeyTemporal(), this.getTarget());
      this.parserMapKeyEnumerated(this.oneToMany.getMapKeyEnumerated(), this.getTarget());
      this.parserJoinColumnList(this.oneToMany.getJoinColumn(), this.getTarget());
      this.parserOrderColumn(this.oneToMany.getOrderColumn(), this.getTarget());
      this.parserJoinTable(this.oneToMany.getJoinTable(), this.getTarget());
      if (this.oneToMany.getOrderBy() != null) {
         this.create(ORDER_BY, this.getTarget(), MockHelper.stringValueArray("value", this.oneToMany.getOrderBy()));
      }

   }

   protected JaxbAccessType getAccessType() {
      return this.oneToMany.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.oneToMany.setAccess(accessType);
   }
}
