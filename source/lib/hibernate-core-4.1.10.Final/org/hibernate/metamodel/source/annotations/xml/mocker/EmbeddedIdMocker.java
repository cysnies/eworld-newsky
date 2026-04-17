package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddedId;
import org.jboss.jandex.ClassInfo;

class EmbeddedIdMocker extends PropertyMocker {
   private JaxbEmbeddedId embeddedId;

   EmbeddedIdMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbEmbeddedId embeddedId) {
      super(indexBuilder, classInfo, defaults);
      this.embeddedId = embeddedId;
   }

   protected String getFieldName() {
      return this.embeddedId.getName();
   }

   protected void processExtra() {
      this.create(EMBEDDED_ID);
   }

   protected JaxbAccessType getAccessType() {
      return this.embeddedId.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.embeddedId.setAccess(accessType);
   }
}
