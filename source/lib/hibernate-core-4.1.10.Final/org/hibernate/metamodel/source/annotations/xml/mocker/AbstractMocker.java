package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbUniqueConstraint;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

abstract class AbstractMocker implements JPADotNames {
   protected final IndexBuilder indexBuilder;

   AbstractMocker(IndexBuilder indexBuilder) {
      this.indexBuilder = indexBuilder;
   }

   protected abstract AnnotationInstance push(AnnotationInstance var1);

   protected AnnotationInstance create(DotName name, AnnotationTarget target) {
      return this.create(name, target, MockHelper.EMPTY_ANNOTATION_VALUE_ARRAY);
   }

   protected AnnotationInstance create(DotName name, AnnotationTarget target, List annotationValueList) {
      return this.create(name, target, MockHelper.toArray(annotationValueList));
   }

   protected AnnotationInstance create(DotName name, AnnotationTarget target, AnnotationValue[] annotationValues) {
      AnnotationInstance annotationInstance = MockHelper.create(name, target, annotationValues);
      this.push(annotationInstance);
      return annotationInstance;
   }

   protected AnnotationInstance parserAccessType(JaxbAccessType accessType, AnnotationTarget target) {
      return accessType == null ? null : this.create(ACCESS, target, MockHelper.enumValueArray("value", ACCESS_TYPE, accessType));
   }

   protected void nestedUniqueConstraintList(String name, List constraints, List annotationValueList) {
      if (MockHelper.isNotEmpty(constraints)) {
         AnnotationValue[] values = new AnnotationValue[constraints.size()];

         for(int i = 0; i < constraints.size(); ++i) {
            AnnotationInstance annotationInstance = this.parserUniqueConstraint((JaxbUniqueConstraint)constraints.get(i), (AnnotationTarget)null);
            values[i] = MockHelper.nestedAnnotationValue("", annotationInstance);
         }

         MockHelper.addToCollectionIfNotNull(annotationValueList, AnnotationValue.createArrayValue(name, values));
      }

   }

   protected AnnotationInstance parserUniqueConstraint(JaxbUniqueConstraint uniqueConstraint, AnnotationTarget target) {
      if (uniqueConstraint == null) {
         return null;
      } else {
         List<AnnotationValue> annotationValueList = new ArrayList();
         MockHelper.stringValue("name", uniqueConstraint.getName(), annotationValueList);
         MockHelper.stringArrayValue("columnNames", uniqueConstraint.getColumnName(), annotationValueList);
         return this.create(UNIQUE_CONSTRAINT, target, annotationValueList);
      }
   }
}
