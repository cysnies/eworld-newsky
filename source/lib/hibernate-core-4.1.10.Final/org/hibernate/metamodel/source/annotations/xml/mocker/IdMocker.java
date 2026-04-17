package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbGeneratedValue;
import org.hibernate.internal.jaxb.mapping.orm.JaxbId;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

class IdMocker extends PropertyMocker {
   private JaxbId id;

   IdMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbId id) {
      super(indexBuilder, classInfo, defaults);
      this.id = id;
   }

   protected void processExtra() {
      this.create(ID);
      this.parserColumn(this.id.getColumn(), this.getTarget());
      this.parserGeneratedValue(this.id.getGeneratedValue(), this.getTarget());
      this.parserTemporalType(this.id.getTemporal(), this.getTarget());
   }

   private AnnotationInstance parserGeneratedValue(JaxbGeneratedValue generatedValue, AnnotationTarget target) {
      if (generatedValue == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("generator", generatedValue.getGenerator(), annotationValueList);
         MockHelper.enumValue("strategy", GENERATION_TYPE, generatedValue.getStrategy(), annotationValueList);
         return this.create(GENERATED_VALUE, target, annotationValueList);
      }
   }

   protected String getFieldName() {
      return this.id.getName();
   }

   protected JaxbAccessType getAccessType() {
      return this.id.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.id.setAccess(accessType);
   }
}
