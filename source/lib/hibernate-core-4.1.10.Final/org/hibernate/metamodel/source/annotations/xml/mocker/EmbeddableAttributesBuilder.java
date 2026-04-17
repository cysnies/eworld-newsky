package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.Collections;
import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddableAttributes;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddedId;
import org.jboss.jandex.ClassInfo;

class EmbeddableAttributesBuilder extends AbstractAttributesBuilder {
   private JaxbEmbeddableAttributes attributes;

   EmbeddableAttributesBuilder(IndexBuilder indexBuilder, ClassInfo classInfo, JaxbAccessType accessType, EntityMappingsMocker.Default defaults, JaxbEmbeddableAttributes embeddableAttributes) {
      super(indexBuilder, classInfo, defaults);
      this.attributes = embeddableAttributes;
   }

   List getBasic() {
      return this.attributes.getBasic();
   }

   List getId() {
      return Collections.emptyList();
   }

   List getTransient() {
      return this.attributes.getTransient();
   }

   List getVersion() {
      return Collections.emptyList();
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
      return null;
   }
}
