package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTransient;
import org.jboss.jandex.ClassInfo;

class TransientMocker extends PropertyMocker {
   private JaxbTransient transientObj;

   TransientMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbTransient transientObj) {
      super(indexBuilder, classInfo, defaults);
      this.transientObj = transientObj;
   }

   protected void processExtra() {
      this.create(TRANSIENT);
   }

   protected String getFieldName() {
      return this.transientObj.getName();
   }

   protected JaxbAccessType getAccessType() {
      return JaxbAccessType.FIELD;
   }

   protected void setAccessType(JaxbAccessType accessType) {
   }
}
