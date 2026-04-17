package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddedId;
import org.jboss.jandex.ClassInfo;

class AttributesBuilder extends AbstractAttributesBuilder {
   private JaxbAttributes attributes;

   AttributesBuilder(IndexBuilder indexBuilder, ClassInfo classInfo, JaxbAccessType accessType, EntityMappingsMocker.Default defaults, JaxbAttributes attributes) {
      super(indexBuilder, classInfo, defaults);
      this.attributes = attributes;
   }

   List getBasic() {
      return this.attributes.getBasic();
   }

   List getId() {
      return this.attributes.getId();
   }

   List getTransient() {
      return this.attributes.getTransient();
   }

   List getVersion() {
      return this.attributes.getVersion();
   }

   List getElementCollection() {
      return this.attributes.getElementCollection();
   }

   List getEmbedded() {
      return this.attributes.getEmbedded();
   }

   List getManyToMany() {
      return this.attributes.getManyToMany();
   }

   List getManyToOne() {
      return this.attributes.getManyToOne();
   }

   List getOneToMany() {
      return this.attributes.getOneToMany();
   }

   List getOneToOne() {
      return this.attributes.getOneToOne();
   }

   JaxbEmbeddedId getEmbeddedId() {
      return this.attributes.getEmbeddedId();
   }
}
