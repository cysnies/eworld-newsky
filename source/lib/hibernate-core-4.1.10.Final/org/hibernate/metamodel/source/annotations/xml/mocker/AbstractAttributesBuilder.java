package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.List;
import org.hibernate.internal.jaxb.mapping.orm.JaxbBasic;
import org.hibernate.internal.jaxb.mapping.orm.JaxbElementCollection;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbedded;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddedId;
import org.hibernate.internal.jaxb.mapping.orm.JaxbId;
import org.hibernate.internal.jaxb.mapping.orm.JaxbManyToMany;
import org.hibernate.internal.jaxb.mapping.orm.JaxbManyToOne;
import org.hibernate.internal.jaxb.mapping.orm.JaxbOneToMany;
import org.hibernate.internal.jaxb.mapping.orm.JaxbOneToOne;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTransient;
import org.hibernate.internal.jaxb.mapping.orm.JaxbVersion;
import org.jboss.jandex.ClassInfo;

abstract class AbstractAttributesBuilder {
   private ClassInfo classInfo;
   private EntityMappingsMocker.Default defaults;
   private IndexBuilder indexBuilder;

   AbstractAttributesBuilder(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults) {
      this.indexBuilder = indexBuilder;
      this.classInfo = classInfo;
      this.defaults = defaults;
   }

   final void parser() {
      for(JaxbId id : this.getId()) {
         (new IdMocker(this.indexBuilder, this.classInfo, this.defaults, id)).process();
      }

      for(JaxbTransient transientObj : this.getTransient()) {
         (new TransientMocker(this.indexBuilder, this.classInfo, this.defaults, transientObj)).process();
      }

      for(JaxbVersion version : this.getVersion()) {
         (new VersionMocker(this.indexBuilder, this.classInfo, this.defaults, version)).process();
      }

      for(JaxbBasic basic : this.getBasic()) {
         (new BasicMocker(this.indexBuilder, this.classInfo, this.defaults, basic)).process();
      }

      for(JaxbElementCollection elementCollection : this.getElementCollection()) {
         (new ElementCollectionMocker(this.indexBuilder, this.classInfo, this.defaults, elementCollection)).process();
      }

      for(JaxbEmbedded embedded : this.getEmbedded()) {
         (new EmbeddedMocker(this.indexBuilder, this.classInfo, this.defaults, embedded)).process();
      }

      for(JaxbManyToMany manyToMany : this.getManyToMany()) {
         (new ManyToManyMocker(this.indexBuilder, this.classInfo, this.defaults, manyToMany)).process();
      }

      for(JaxbManyToOne manyToOne : this.getManyToOne()) {
         (new ManyToOneMocker(this.indexBuilder, this.classInfo, this.defaults, manyToOne)).process();
      }

      for(JaxbOneToMany oneToMany : this.getOneToMany()) {
         (new OneToManyMocker(this.indexBuilder, this.classInfo, this.defaults, oneToMany)).process();
      }

      for(JaxbOneToOne oneToOne : this.getOneToOne()) {
         (new OneToOneMocker(this.indexBuilder, this.classInfo, this.defaults, oneToOne)).process();
      }

      if (this.getEmbeddedId() != null) {
         (new EmbeddedIdMocker(this.indexBuilder, this.classInfo, this.defaults, this.getEmbeddedId())).process();
      }

   }

   abstract List getId();

   abstract List getTransient();

   abstract List getVersion();

   abstract List getBasic();

   abstract List getElementCollection();

   abstract List getEmbedded();

   abstract List getManyToMany();

   abstract List getManyToOne();

   abstract List getOneToMany();

   abstract List getOneToOne();

   abstract JaxbEmbeddedId getEmbeddedId();
}
