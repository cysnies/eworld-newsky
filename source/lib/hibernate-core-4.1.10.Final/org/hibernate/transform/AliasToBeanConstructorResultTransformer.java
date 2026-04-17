package org.hibernate.transform;

import java.lang.reflect.Constructor;
import java.util.List;
import org.hibernate.QueryException;

public class AliasToBeanConstructorResultTransformer implements ResultTransformer {
   private final Constructor constructor;

   public AliasToBeanConstructorResultTransformer(Constructor constructor) {
      this.constructor = constructor;
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      try {
         return this.constructor.newInstance(tuple);
      } catch (Exception e) {
         throw new QueryException("could not instantiate class [" + this.constructor.getDeclaringClass().getName() + "] from tuple", e);
      }
   }

   public List transformList(List collection) {
      return collection;
   }

   public int hashCode() {
      return this.constructor.hashCode();
   }

   public boolean equals(Object other) {
      return other instanceof AliasToBeanConstructorResultTransformer && this.constructor.equals(((AliasToBeanConstructorResultTransformer)other).constructor);
   }
}
