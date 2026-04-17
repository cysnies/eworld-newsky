package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbVersion;
import org.jboss.jandex.ClassInfo;

class VersionMocker extends PropertyMocker {
   private JaxbVersion version;

   VersionMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbVersion version) {
      super(indexBuilder, classInfo, defaults);
      this.version = version;
   }

   protected String getFieldName() {
      return this.version.getName();
   }

   protected void processExtra() {
      this.create(VERSION);
      this.parserColumn(this.version.getColumn(), this.getTarget());
      this.parserTemporalType(this.version.getTemporal(), this.getTarget());
   }

   protected JaxbAccessType getAccessType() {
      return this.version.getAccess();
   }

   protected void setAccessType(JaxbAccessType accessType) {
      this.version.setAccess(accessType);
   }
}
