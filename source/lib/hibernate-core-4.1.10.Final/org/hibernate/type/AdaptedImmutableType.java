package org.hibernate.type;

import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlan;

public class AdaptedImmutableType extends AbstractSingleColumnStandardBasicType {
   private final AbstractStandardBasicType baseMutableType;

   public AdaptedImmutableType(AbstractStandardBasicType baseMutableType) {
      super(baseMutableType.getSqlTypeDescriptor(), baseMutableType.getJavaTypeDescriptor());
      this.baseMutableType = baseMutableType;
   }

   protected MutabilityPlan getMutabilityPlan() {
      return ImmutableMutabilityPlan.INSTANCE;
   }

   public String getName() {
      return "imm_" + this.baseMutableType.getName();
   }
}
