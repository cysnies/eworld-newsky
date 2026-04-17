package org.hibernate.transform;

import org.hibernate.internal.util.collections.ArrayHelper;

public final class RootEntityResultTransformer extends BasicTransformerAdapter implements TupleSubsetResultTransformer {
   public static final RootEntityResultTransformer INSTANCE = new RootEntityResultTransformer();

   private RootEntityResultTransformer() {
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      return tuple[tuple.length - 1];
   }

   public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
      return true;
   }

   public boolean[] includeInTransform(String[] aliases, int tupleLength) {
      boolean[] includeInTransform;
      if (tupleLength == 1) {
         includeInTransform = ArrayHelper.TRUE;
      } else {
         includeInTransform = new boolean[tupleLength];
         includeInTransform[tupleLength - 1] = true;
      }

      return includeInTransform;
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
