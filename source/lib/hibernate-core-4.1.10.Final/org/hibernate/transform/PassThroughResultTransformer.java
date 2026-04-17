package org.hibernate.transform;

import java.util.Arrays;
import java.util.List;

public class PassThroughResultTransformer extends BasicTransformerAdapter implements TupleSubsetResultTransformer {
   public static final PassThroughResultTransformer INSTANCE = new PassThroughResultTransformer();

   private PassThroughResultTransformer() {
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      return tuple.length == 1 ? tuple[0] : tuple;
   }

   public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
      return tupleLength == 1;
   }

   public boolean[] includeInTransform(String[] aliases, int tupleLength) {
      boolean[] includeInTransformedResult = new boolean[tupleLength];
      Arrays.fill(includeInTransformedResult, true);
      return includeInTransformedResult;
   }

   List untransformToTuples(List results, boolean isSingleResult) {
      if (isSingleResult) {
         for(int i = 0; i < results.size(); ++i) {
            Object[] tuple = this.untransformToTuple(results.get(i), isSingleResult);
            results.set(i, tuple);
         }
      }

      return results;
   }

   Object[] untransformToTuple(Object transformed, boolean isSingleResult) {
      return isSingleResult ? new Object[]{transformed} : (Object[])((Object[])transformed);
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
