package org.hibernate.criterion;

import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;

/** @deprecated */
@Deprecated
public final class Expression extends Restrictions {
   private Expression() {
   }

   /** @deprecated */
   @Deprecated
   public static Criterion sql(String sql, Object[] values, Type[] types) {
      return new SQLCriterion(sql, values, types);
   }

   /** @deprecated */
   @Deprecated
   public static Criterion sql(String sql, Object value, Type type) {
      return new SQLCriterion(sql, new Object[]{value}, new Type[]{type});
   }

   /** @deprecated */
   @Deprecated
   public static Criterion sql(String sql) {
      return new SQLCriterion(sql, ArrayHelper.EMPTY_OBJECT_ARRAY, ArrayHelper.EMPTY_TYPE_ARRAY);
   }
}
