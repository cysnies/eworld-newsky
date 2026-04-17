package org.hibernate.criterion;

import org.hibernate.Criteria;

public interface EnhancedProjection extends Projection {
   String[] getColumnAliases(int var1, Criteria var2, CriteriaQuery var3);

   String[] getColumnAliases(String var1, int var2, Criteria var3, CriteriaQuery var4);
}
