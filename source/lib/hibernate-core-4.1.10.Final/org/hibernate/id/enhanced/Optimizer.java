package org.hibernate.id.enhanced;

import java.io.Serializable;
import org.hibernate.id.IntegralDataTypeHolder;

public interface Optimizer {
   Serializable generate(AccessCallback var1);

   IntegralDataTypeHolder getLastSourceValue();

   int getIncrementSize();

   boolean applyIncrementSizeToSourceValues();
}
