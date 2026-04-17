package org.hibernate.metamodel.binding;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.metamodel.domain.SingularAttribute;

public class ManyToOneAttributeBinding extends BasicAttributeBinding implements SingularAssociationAttributeBinding {
   private String referencedEntityName;
   private String referencedAttributeName;
   private AttributeBinding referencedAttributeBinding;
   private boolean isLogicalOneToOne;
   private String foreignKeyName;
   private CascadeStyle cascadeStyle;
   private FetchTiming fetchTiming;
   private FetchStyle fetchStyle;

   ManyToOneAttributeBinding(AttributeBindingContainer container, SingularAttribute attribute) {
      super(container, attribute, false, false);
   }

   public boolean isAssociation() {
      return true;
   }

   public final boolean isPropertyReference() {
      return this.referencedAttributeName != null;
   }

   public final String getReferencedEntityName() {
      return this.referencedEntityName;
   }

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName;
   }

   public final String getReferencedAttributeName() {
      return this.referencedAttributeName;
   }

   public void setReferencedAttributeName(String referencedEntityAttributeName) {
      this.referencedAttributeName = referencedEntityAttributeName;
   }

   public CascadeStyle getCascadeStyle() {
      return this.cascadeStyle;
   }

   public void setCascadeStyles(Iterable cascadeStyles) {
      List<CascadeStyle> cascadeStyleList = new ArrayList();

      for(CascadeStyle style : cascadeStyles) {
         if (style != CascadeStyle.NONE) {
            cascadeStyleList.add(style);
         }
      }

      if (cascadeStyleList.isEmpty()) {
         this.cascadeStyle = CascadeStyle.NONE;
      } else if (cascadeStyleList.size() == 1) {
         this.cascadeStyle = (CascadeStyle)cascadeStyleList.get(0);
      } else {
         this.cascadeStyle = new CascadeStyle.MultipleCascadeStyle((CascadeStyle[])cascadeStyleList.toArray(new CascadeStyle[cascadeStyleList.size()]));
      }

   }

   public FetchTiming getFetchTiming() {
      return this.fetchTiming;
   }

   public void setFetchTiming(FetchTiming fetchTiming) {
      this.fetchTiming = fetchTiming;
   }

   public FetchStyle getFetchStyle() {
      return this.fetchStyle;
   }

   public void setFetchStyle(FetchStyle fetchStyle) {
      if (fetchStyle == FetchStyle.SUBSELECT) {
         throw new AssertionFailure("Subselect fetching not yet supported for singular associations");
      } else {
         this.fetchStyle = fetchStyle;
      }
   }

   public FetchMode getFetchMode() {
      if (this.fetchStyle == FetchStyle.JOIN) {
         return FetchMode.JOIN;
      } else if (this.fetchStyle == FetchStyle.SELECT) {
         return FetchMode.SELECT;
      } else if (this.fetchStyle == FetchStyle.BATCH) {
         return FetchMode.SELECT;
      } else {
         throw new AssertionFailure("Unexpected fetch style : " + this.fetchStyle.name());
      }
   }

   public final boolean isReferenceResolved() {
      return this.referencedAttributeBinding != null;
   }

   public final void resolveReference(AttributeBinding referencedAttributeBinding) {
      if (!EntityBinding.class.isInstance(referencedAttributeBinding.getContainer())) {
         throw new AssertionFailure("Illegal attempt to resolve many-to-one reference based on non-entity attribute");
      } else {
         EntityBinding entityBinding = (EntityBinding)referencedAttributeBinding.getContainer();
         if (!this.referencedEntityName.equals(entityBinding.getEntity().getName())) {
            throw new IllegalStateException("attempt to set EntityBinding with name: [" + entityBinding.getEntity().getName() + "; entity name should be: " + this.referencedEntityName);
         } else {
            if (this.referencedAttributeName == null) {
               this.referencedAttributeName = referencedAttributeBinding.getAttribute().getName();
            } else if (!this.referencedAttributeName.equals(referencedAttributeBinding.getAttribute().getName())) {
               throw new IllegalStateException("Inconsistent attribute name; expected: " + this.referencedAttributeName + "actual: " + referencedAttributeBinding.getAttribute().getName());
            }

            this.referencedAttributeBinding = referencedAttributeBinding;
         }
      }
   }

   public AttributeBinding getReferencedAttributeBinding() {
      if (!this.isReferenceResolved()) {
         throw new IllegalStateException("Referenced AttributeBiding has not been resolved.");
      } else {
         return this.referencedAttributeBinding;
      }
   }

   public final EntityBinding getReferencedEntityBinding() {
      return (EntityBinding)this.referencedAttributeBinding.getContainer();
   }
}
