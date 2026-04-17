package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.EntityMode;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbAnyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbComponentElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbManyToManyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbManyToOneElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbOneToManyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbOneToOneElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbPropertyElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbTuplizerElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.AttributeSource;
import org.hibernate.metamodel.source.binder.AttributeSourceContainer;
import org.hibernate.metamodel.source.binder.ComponentAttributeSource;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;

public class ComponentAttributeSourceImpl implements ComponentAttributeSource {
   private final JaxbComponentElement componentElement;
   private final AttributeSourceContainer parentContainer;
   private final ValueHolder componentClassReference;
   private final String path;

   public ComponentAttributeSourceImpl(JaxbComponentElement componentElement, AttributeSourceContainer parentContainer, LocalBindingContext bindingContext) {
      this.componentElement = componentElement;
      this.parentContainer = parentContainer;
      this.componentClassReference = bindingContext.makeClassReference(bindingContext.qualifyClassName(componentElement.getClazz()));
      this.path = parentContainer.getPath() + '.' + componentElement.getName();
   }

   public String getClassName() {
      return this.componentElement.getClazz();
   }

   public ValueHolder getClassReference() {
      return this.componentClassReference;
   }

   public String getPath() {
      return this.path;
   }

   public LocalBindingContext getLocalBindingContext() {
      return this.parentContainer.getLocalBindingContext();
   }

   public String getParentReferenceAttributeName() {
      return this.componentElement.getParent() == null ? null : this.componentElement.getParent().getName();
   }

   public String getExplicitTuplizerClassName() {
      if (this.componentElement.getTuplizer() == null) {
         return null;
      } else {
         EntityMode entityMode = StringHelper.isEmpty(this.componentElement.getClazz()) ? EntityMode.MAP : EntityMode.POJO;

         for(JaxbTuplizerElement tuplizerElement : this.componentElement.getTuplizer()) {
            if (entityMode == EntityMode.parse(tuplizerElement.getEntityMode())) {
               return tuplizerElement.getClazz();
            }
         }

         return null;
      }
   }

   public Iterable attributeSources() {
      List<AttributeSource> attributeSources = new ArrayList();

      for(Object attributeElement : this.componentElement.getPropertyOrManyToOneOrOneToOne()) {
         if (JaxbPropertyElement.class.isInstance(attributeElement)) {
            attributeSources.add(new PropertyAttributeSourceImpl((JaxbPropertyElement)JaxbPropertyElement.class.cast(attributeElement), this.getLocalBindingContext()));
         } else if (JaxbComponentElement.class.isInstance(attributeElement)) {
            attributeSources.add(new ComponentAttributeSourceImpl((JaxbComponentElement)attributeElement, this, this.getLocalBindingContext()));
         } else if (JaxbManyToOneElement.class.isInstance(attributeElement)) {
            attributeSources.add(new ManyToOneAttributeSourceImpl((JaxbManyToOneElement)JaxbManyToOneElement.class.cast(attributeElement), this.getLocalBindingContext()));
         } else if (!JaxbOneToOneElement.class.isInstance(attributeElement) && !JaxbAnyElement.class.isInstance(attributeElement) && !JaxbOneToManyElement.class.isInstance(attributeElement) && JaxbManyToManyElement.class.isInstance(attributeElement)) {
         }
      }

      return attributeSources;
   }

   public boolean isVirtualAttribute() {
      return false;
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.COMPONENT;
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return null;
   }

   public String getName() {
      return this.componentElement.getName();
   }

   public boolean isSingular() {
      return true;
   }

   public String getPropertyAccessorName() {
      return this.componentElement.getAccess();
   }

   public boolean isInsertable() {
      return this.componentElement.isInsert();
   }

   public boolean isUpdatable() {
      return this.componentElement.isUpdate();
   }

   public PropertyGeneration getGeneration() {
      return null;
   }

   public boolean isLazy() {
      return this.componentElement.isLazy();
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.componentElement.isOptimisticLock();
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.componentElement.getMeta());
   }

   public boolean areValuesIncludedInInsertByDefault() {
      return this.isInsertable();
   }

   public boolean areValuesIncludedInUpdateByDefault() {
      return this.isUpdatable();
   }

   public boolean areValuesNullableByDefault() {
      return true;
   }

   public List relationalValueSources() {
      return null;
   }
}
