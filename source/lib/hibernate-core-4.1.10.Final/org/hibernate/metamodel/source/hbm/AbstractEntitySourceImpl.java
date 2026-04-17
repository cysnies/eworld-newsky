package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbAnyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbBagElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbComponentElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbIdbagElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbListElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbManyToOneElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbMapElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbOneToOneElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbPropertyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSetElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSynchronizeElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbTuplizerElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.AttributeSource;
import org.hibernate.metamodel.source.binder.EntitySource;
import org.hibernate.metamodel.source.binder.SubclassEntitySource;

public abstract class AbstractEntitySourceImpl implements EntitySource {
   private final MappingDocument sourceMappingDocument;
   private final EntityElement entityElement;
   private List subclassEntitySources = new ArrayList();
   private EntityHierarchyImpl entityHierarchy;

   protected AbstractEntitySourceImpl(MappingDocument sourceMappingDocument, EntityElement entityElement) {
      this.sourceMappingDocument = sourceMappingDocument;
      this.entityElement = entityElement;
   }

   protected EntityElement entityElement() {
      return this.entityElement;
   }

   protected MappingDocument sourceMappingDocument() {
      return this.sourceMappingDocument;
   }

   public Origin getOrigin() {
      return this.sourceMappingDocument.getOrigin();
   }

   public LocalBindingContext getLocalBindingContext() {
      return this.sourceMappingDocument.getMappingLocalBindingContext();
   }

   public String getEntityName() {
      return StringHelper.isNotEmpty(this.entityElement.getEntityName()) ? this.entityElement.getEntityName() : this.getClassName();
   }

   public String getClassName() {
      return this.getLocalBindingContext().qualifyClassName(this.entityElement.getName());
   }

   public String getJpaEntityName() {
      return null;
   }

   public boolean isAbstract() {
      return Helper.getBooleanValue(this.entityElement.isAbstract(), false);
   }

   public boolean isLazy() {
      return Helper.getBooleanValue(this.entityElement.isAbstract(), true);
   }

   public String getProxy() {
      return this.entityElement.getProxy();
   }

   public int getBatchSize() {
      return Helper.getIntValue(this.entityElement.getBatchSize(), -1);
   }

   public boolean isDynamicInsert() {
      return this.entityElement.isDynamicInsert();
   }

   public boolean isDynamicUpdate() {
      return this.entityElement.isDynamicUpdate();
   }

   public boolean isSelectBeforeUpdate() {
      return this.entityElement.isSelectBeforeUpdate();
   }

   protected EntityMode determineEntityMode() {
      return StringHelper.isNotEmpty(this.getClassName()) ? EntityMode.POJO : EntityMode.MAP;
   }

   public String getCustomTuplizerClassName() {
      if (this.entityElement.getTuplizer() == null) {
         return null;
      } else {
         EntityMode entityMode = this.determineEntityMode();

         for(JaxbTuplizerElement tuplizerElement : this.entityElement.getTuplizer()) {
            if (entityMode == EntityMode.parse(tuplizerElement.getEntityMode())) {
               return tuplizerElement.getClazz();
            }
         }

         return null;
      }
   }

   public String getCustomPersisterClassName() {
      return this.getLocalBindingContext().qualifyClassName(this.entityElement.getPersister());
   }

   public String getCustomLoaderName() {
      return this.entityElement.getLoader() != null ? this.entityElement.getLoader().getQueryRef() : null;
   }

   public CustomSQL getCustomSqlInsert() {
      return Helper.buildCustomSql(this.entityElement.getSqlInsert());
   }

   public CustomSQL getCustomSqlUpdate() {
      return Helper.buildCustomSql(this.entityElement.getSqlUpdate());
   }

   public CustomSQL getCustomSqlDelete() {
      return Helper.buildCustomSql(this.entityElement.getSqlDelete());
   }

   public List getSynchronizedTableNames() {
      List<String> tableNames = new ArrayList();

      for(JaxbSynchronizeElement synchronizeElement : this.entityElement.getSynchronize()) {
         tableNames.add(synchronizeElement.getTable());
      }

      return tableNames;
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.entityElement.getMeta());
   }

   public String getPath() {
      return this.sourceMappingDocument.getMappingLocalBindingContext().determineEntityName(this.entityElement);
   }

   public Iterable attributeSources() {
      List<AttributeSource> attributeSources = new ArrayList();

      for(Object attributeElement : this.entityElement.getPropertyOrManyToOneOrOneToOne()) {
         if (JaxbPropertyElement.class.isInstance(attributeElement)) {
            attributeSources.add(new PropertyAttributeSourceImpl((JaxbPropertyElement)JaxbPropertyElement.class.cast(attributeElement), this.sourceMappingDocument().getMappingLocalBindingContext()));
         } else if (JaxbComponentElement.class.isInstance(attributeElement)) {
            attributeSources.add(new ComponentAttributeSourceImpl((JaxbComponentElement)attributeElement, this, this.sourceMappingDocument.getMappingLocalBindingContext()));
         } else if (JaxbManyToOneElement.class.isInstance(attributeElement)) {
            attributeSources.add(new ManyToOneAttributeSourceImpl((JaxbManyToOneElement)JaxbManyToOneElement.class.cast(attributeElement), this.sourceMappingDocument().getMappingLocalBindingContext()));
         } else if (!JaxbOneToOneElement.class.isInstance(attributeElement) && !JaxbAnyElement.class.isInstance(attributeElement)) {
            if (JaxbBagElement.class.isInstance(attributeElement)) {
               attributeSources.add(new BagAttributeSourceImpl((JaxbBagElement)JaxbBagElement.class.cast(attributeElement), this));
            } else if (!JaxbIdbagElement.class.isInstance(attributeElement)) {
               if (JaxbSetElement.class.isInstance(attributeElement)) {
                  attributeSources.add(new SetAttributeSourceImpl((JaxbSetElement)JaxbSetElement.class.cast(attributeElement), this));
               } else if (!JaxbListElement.class.isInstance(attributeElement) && !JaxbMapElement.class.isInstance(attributeElement)) {
                  throw new AssertionFailure("Unexpected attribute element type encountered : " + attributeElement.getClass());
               }
            }
         }
      }

      return attributeSources;
   }

   public void injectHierarchy(EntityHierarchyImpl entityHierarchy) {
      this.entityHierarchy = entityHierarchy;
   }

   public void add(SubclassEntitySource subclassEntitySource) {
      this.add((SubclassEntitySourceImpl)subclassEntitySource);
   }

   public void add(SubclassEntitySourceImpl subclassEntitySource) {
      this.entityHierarchy.processSubclass(subclassEntitySource);
      this.subclassEntitySources.add(subclassEntitySource);
   }

   public Iterable subclassEntitySources() {
      return this.subclassEntitySources;
   }

   public String getDiscriminatorMatchValue() {
      return null;
   }

   public Iterable getConstraints() {
      return Collections.emptySet();
   }

   public Iterable getSecondaryTables() {
      return Collections.emptySet();
   }

   public List getJpaCallbackClasses() {
      return Collections.EMPTY_LIST;
   }
}
