package org.hibernate.type.descriptor.java;

import java.io.Serializable;

public interface MutabilityPlan extends Serializable {
   boolean isMutable();

   Object deepCopy(Object var1);

   Serializable disassemble(Object var1);

   Object assemble(Serializable var1);
}
