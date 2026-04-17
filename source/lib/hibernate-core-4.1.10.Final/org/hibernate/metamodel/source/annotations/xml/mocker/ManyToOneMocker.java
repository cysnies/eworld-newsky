package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbManyToOne;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class ManyToOneMocker extends PropertyMocker {
   private JaxbManyToOne manyToOne;

   ManyToOneMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbManyToOne manyToOne) {
      super(indexBuilder, classInfo, defaults);
      this.manyToOne = manyToOne;
   }

   protected String getFieldName() {
      return this.manyToOne.getName();
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.classValue("targetEntity", this.manyToOne.getTargetEntity(), annotationValueList, this.indexBuilder.getServiceRegistry());
      MockHelper.enumValue("fetch", FETCH_TYPE, this.manyToOne.getFetch(), annotationValueList);
      MockHelper.booleanValue("optional", this.manyToOne.isOptional(), annotationValueList);
      MockHelper.cascadeValue("cascade", this.manyToOne.getCascade(), this.isDefaultCascadePersist(), annotationValueList);
      this.create(MANY_TO_ONE, annotationValueList);
      this.parserJoinColumnList(this.manyToOne.getJoinColumn(), this.getTarget());
      this.parserJoinTable(this.manyToOne.getJoinTable(), this.getTarget());
      if (this.manyToOne.getMapsId() != null) {
         this.create(MAPS_ID, MockHelper.stringValueArray("value", this.manyToOne.getMapsId()));
      }

      if (this.manyToOne.isId() != null && this.manyToOne.isId()) {
         this.create(ID);
      }

   }

   protected JaxbAccessType getAccessType() {
      return this.manyToOne.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.manyToOne.setAccess(accessType);
   }
}
