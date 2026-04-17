package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.EntityMode;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbCompositeElementElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbTuplizerElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.AttributeSource;
import org.hibernate.metamodel.source.binder.CompositePluralAttributeElementSource;
import org.hibernate.metamodel.source.binder.PluralAttributeElementNature;

public class CompositePluralAttributeElementSourceImpl implements CompositePluralAttributeElementSource {
   private final JaxbCompositeElementElement compositeElement;
   private final LocalBindingContext bindingContext;

   public CompositePluralAttributeElementSourceImpl(JaxbCompositeElementElement compositeElement, LocalBindingContext bindingContext) {
      this.compositeElement = compositeElement;
      this.bindingContext = bindingContext;
   }

   public PluralAttributeElementNature getNature() {
      return PluralAttributeElementNature.COMPONENT;
   }

   public String getClassName() {
      return this.bindingContext.qualifyClassName(this.compositeElement.getClazz());
   }

   public ValueHolder getClassReference() {
      return this.bindingContext.makeClassReference(this.getClassName());
   }

   public String getParentReferenceAttributeName() {
      return this.compositeElement.getParent() != null ? this.compositeElement.getParent().getName() : null;
   }

   public String getExplicitTuplizerClassName() {
      if (this.compositeElement.getTuplizer() == null) {
         return null;
      } else {
         EntityMode entityMode = StringHelper.isEmpty(this.compositeElement.getClazz()) ? EntityMode.MAP : EntityMode.POJO;

         for(JaxbTuplizerElement tuplizerElement : this.compositeElement.getTuplizer()) {
            if (entityMode == EntityMode.parse(tuplizerElement.getEntityMode())) {
               return tuplizerElement.getClazz();
            }
         }

         return null;
      }
   }

   public String getPath() {
      return null;
   }

   public Iterable attributeSources() {
      List<AttributeSource> attributeSources = new ArrayList();

      for(Object attribute : this.compositeElement.getPropertyOrManyToOneOrAny()) {
         ;
      }

      return attributeSources;
   }

   public LocalBindingContext getLocalBindingContext() {
      return this.bindingContext;
   }
}
