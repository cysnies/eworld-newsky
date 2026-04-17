package org.hibernate.transform;

import java.util.HashMap;
import java.util.Map;

public class AliasToEntityMapResultTransformer extends AliasedTupleSubsetResultTransformer {
   public static final AliasToEntityMapResultTransformer INSTANCE = new AliasToEntityMapResultTransformer();

   private AliasToEntityMapResultTransformer() {
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      Map result = new HashMap(tuple.length);

      for(int i = 0; i < tuple.length; ++i) {
         String alias = aliases[i];
         if (alias != null) {
            result.put(alias, tuple[i]);
         }
      }

      return result;
   }

   public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
      return false;
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
