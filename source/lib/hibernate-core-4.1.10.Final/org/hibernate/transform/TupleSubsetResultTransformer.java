package org.hibernate.transform;

public interface TupleSubsetResultTransformer extends ResultTransformer {
   boolean isTransformedValueATupleElement(String[] var1, int var2);

   boolean[] includeInTransform(String[] var1, int var2);
}
