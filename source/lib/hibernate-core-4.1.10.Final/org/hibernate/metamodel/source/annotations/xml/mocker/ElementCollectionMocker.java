package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbElementCollection;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class ElementCollectionMocker extends PropertyMocker {
   private JaxbElementCollection elementCollection;

   ElementCollectionMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbElementCollection elementCollection) {
      super(indexBuilder, classInfo, defaults);
      this.elementCollection = elementCollection;
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.classValue("targetClass", this.elementCollection.getTargetClass(), annotationValueList, this.indexBuilder.getServiceRegistry());
      MockHelper.enumValue("fetch", FETCH_TYPE, this.elementCollection.getFetch(), annotationValueList);
      this.create(ELEMENT_COLLECTION, annotationValueList);
      this.parserLob(this.elementCollection.getLob(), this.getTarget());
      this.parserEnumType(this.elementCollection.getEnumerated(), this.getTarget());
      this.parserColumn(this.elementCollection.getColumn(), this.getTarget());
      this.parserTemporalType(this.elementCollection.getTemporal(), this.getTarget());
      this.parserCollectionTable(this.elementCollection.getCollectionTable(), this.getTarget());
      this.parserAssociationOverrides(this.elementCollection.getAssociationOverride(), this.getTarget());
      this.parserAttributeOverrides(this.elementCollection.getAttributeOverride(), this.getTarget());
      if (this.elementCollection.getOrderBy() != null) {
         this.create(ORDER_BY, MockHelper.stringValueArray("value", this.elementCollection.getOrderBy()));
      }

      this.parserAttributeOverrides(this.elementCollection.getMapKeyAttributeOverride(), this.getTarget());
      this.parserMapKeyJoinColumnList(this.elementCollection.getMapKeyJoinColumn(), this.getTarget());
      this.parserMapKey(this.elementCollection.getMapKey(), this.getTarget());
      this.parserMapKeyColumn(this.elementCollection.getMapKeyColumn(), this.getTarget());
      this.parserMapKeyClass(this.elementCollection.getMapKeyClass(), this.getTarget());
      this.parserMapKeyEnumerated(this.elementCollection.getMapKeyEnumerated(), this.getTarget());
      this.parserMapKeyTemporal(this.elementCollection.getMapKeyTemporal(), this.getTarget());
   }

   protected String getFieldName() {
      return this.elementCollection.getName();
   }

   protected JaxbAccessType getAccessType() {
      return this.elementCollection.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.elementCollection.setAccess(accessType);
   }
}
