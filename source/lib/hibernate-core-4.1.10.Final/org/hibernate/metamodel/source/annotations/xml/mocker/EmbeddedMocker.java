package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbedded;
import org.jboss.jandex.ClassInfo;

class EmbeddedMocker extends PropertyMocker {
   private JaxbEmbedded embedded;

   EmbeddedMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbEmbedded embedded) {
      super(indexBuilder, classInfo, defaults);
      this.embedded = embedded;
   }

   protected void processExtra() {
      this.create(EMBEDDED);
      this.parserAttributeOverrides(this.embedded.getAttributeOverride(), this.getTarget());
      this.parserAssociationOverrides(this.embedded.getAssociationOverride(), this.getTarget());
   }

   protected String getFieldName() {
      return this.embedded.getName();
   }

   protected JaxbAccessType getAccessType() {
      return this.embedded.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.embedded.setAccess(accessType);
   }
}
