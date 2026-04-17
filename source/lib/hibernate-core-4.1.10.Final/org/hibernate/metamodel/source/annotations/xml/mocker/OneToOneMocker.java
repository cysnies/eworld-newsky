package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbOneToOne;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class OneToOneMocker extends PropertyMocker {
   private JaxbOneToOne oneToOne;

   OneToOneMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbOneToOne oneToOne) {
      super(indexBuilder, classInfo, defaults);
      this.oneToOne = oneToOne;
   }

   protected String getFieldName() {
      return this.oneToOne.getName();
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.classValue("targetEntity", this.oneToOne.getTargetEntity(), annotationValueList, this.indexBuilder.getServiceRegistry());
      MockHelper.enumValue("fetch", FETCH_TYPE, this.oneToOne.getFetch(), annotationValueList);
      MockHelper.booleanValue("optional", this.oneToOne.isOptional(), annotationValueList);
      MockHelper.booleanValue("orphanRemoval", this.oneToOne.isOrphanRemoval(), annotationValueList);
      MockHelper.stringValue("mappedBy", this.oneToOne.getMappedBy(), annotationValueList);
      MockHelper.cascadeValue("cascade", this.oneToOne.getCascade(), this.isDefaultCascadePersist(), annotationValueList);
      this.create(ONE_TO_ONE, annotationValueList);
      this.parserPrimaryKeyJoinColumnList(this.oneToOne.getPrimaryKeyJoinColumn(), this.getTarget());
      this.parserJoinColumnList(this.oneToOne.getJoinColumn(), this.getTarget());
      this.parserJoinTable(this.oneToOne.getJoinTable(), this.getTarget());
      if (this.oneToOne.getMapsId() != null) {
         this.create(MAPS_ID, MockHelper.stringValueArray("value", this.oneToOne.getMapsId()));
      }

      if (this.oneToOne.isId() != null && this.oneToOne.isId()) {
         this.create(ID);
      }

   }

   protected JaxbAccessType getAccessType() {
      return this.oneToOne.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.oneToOne.setAccess(accessType);
   }
}
