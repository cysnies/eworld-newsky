package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbBasic;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class BasicMocker extends PropertyMocker {
   private JaxbBasic basic;

   BasicMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbBasic basic) {
      super(indexBuilder, classInfo, defaults);
      this.basic = basic;
   }

   protected String getFieldName() {
      return this.basic.getName();
   }

   protected void processExtra() {
      List<AnnotationValue> annotationValueList = new ArrayList();
      MockHelper.booleanValue("optional", this.basic.isOptional(), annotationValueList);
      MockHelper.enumValue("fetch", FETCH_TYPE, this.basic.getFetch(), annotationValueList);
      this.create(BASIC, annotationValueList);
      this.parserColumn(this.basic.getColumn(), this.getTarget());
      this.parserEnumType(this.basic.getEnumerated(), this.getTarget());
      this.parserLob(this.basic.getLob(), this.getTarget());
      this.parserTemporalType(this.basic.getTemporal(), this.getTarget());
   }

   protected JaxbAccessType getAccessType() {
      return this.basic.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.basic.setAccess(accessType);
   }
}
