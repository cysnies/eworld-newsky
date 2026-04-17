package org.hibernate.metamodel.source.annotations.attribute;

import java.util.Set;
import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.source.annotations.EnumConversionHelper;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.ToOneAttributeSource;

public class ToOneAttributeSourceImpl extends SingularAttributeSourceImpl implements ToOneAttributeSource {
   private final AssociationAttribute associationAttribute;
   private final Set cascadeStyles;

   public ToOneAttributeSourceImpl(AssociationAttribute associationAttribute) {
      super(associationAttribute);
      this.associationAttribute = associationAttribute;
      this.cascadeStyles = EnumConversionHelper.cascadeTypeToCascadeStyleSet(associationAttribute.getCascadeTypes());
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.MANY_TO_ONE;
   }

   public String getReferencedEntityName() {
      return this.associationAttribute.getReferencedEntityType();
   }

   public String getReferencedEntityAttributeName() {
      return this.associationAttribute.getMappedBy();
   }

   public Iterable getCascadeStyles() {
      return this.cascadeStyles;
   }

   public FetchMode getFetchMode() {
      return this.associationAttribute.getFetchMode();
   }

   public FetchTiming getFetchTiming() {
      return FetchTiming.IMMEDIATE;
   }

   public FetchStyle getFetchStyle() {
      return FetchStyle.JOIN;
   }
}
