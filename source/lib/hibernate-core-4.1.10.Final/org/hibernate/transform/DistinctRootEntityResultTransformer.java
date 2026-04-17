package org.hibernate.transform;

import java.util.List;

public class DistinctRootEntityResultTransformer implements TupleSubsetResultTransformer {
   public static final DistinctRootEntityResultTransformer INSTANCE = new DistinctRootEntityResultTransformer();

   private DistinctRootEntityResultTransformer() {
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      return RootEntityResultTransformer.INSTANCE.transformTuple(tuple, aliases);
   }

   public List transformList(List list) {
      return DistinctResultTransformer.INSTANCE.transformList(list);
   }

   public boolean[] includeInTransform(String[] aliases, int tupleLength) {
      return RootEntityResultTransformer.INSTANCE.includeInTransform(aliases, tupleLength);
   }

   public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
      return RootEntityResultTransformer.INSTANCE.isTransformedValueATupleElement((String[])null, tupleLength);
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
