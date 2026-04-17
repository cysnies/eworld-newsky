package org.hibernate.transform;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;

public class CacheableResultTransformer implements ResultTransformer {
   private static final PassThroughResultTransformer ACTUAL_TRANSFORMER;
   private final int tupleLength;
   private final int tupleSubsetLength;
   private final boolean[] includeInTuple;
   private final int[] includeInTransformIndex;

   public static CacheableResultTransformer create(ResultTransformer transformer, String[] aliases, boolean[] includeInTuple) {
      return transformer instanceof TupleSubsetResultTransformer ? create((TupleSubsetResultTransformer)transformer, aliases, includeInTuple) : create(includeInTuple);
   }

   private static CacheableResultTransformer create(TupleSubsetResultTransformer transformer, String[] aliases, boolean[] includeInTuple) {
      if (transformer == null) {
         throw new IllegalArgumentException("transformer cannot be null");
      } else {
         int tupleLength = ArrayHelper.countTrue(includeInTuple);
         if (aliases != null && aliases.length != tupleLength) {
            throw new IllegalArgumentException("if aliases is not null, then the length of aliases[] must equal the number of true elements in includeInTuple; aliases.length=" + aliases.length + "tupleLength=" + tupleLength);
         } else {
            return new CacheableResultTransformer(includeInTuple, transformer.includeInTransform(aliases, tupleLength));
         }
      }
   }

   private static CacheableResultTransformer create(boolean[] includeInTuple) {
      return new CacheableResultTransformer(includeInTuple, (boolean[])null);
   }

   private CacheableResultTransformer(boolean[] includeInTuple, boolean[] includeInTransform) {
      if (includeInTuple == null) {
         throw new IllegalArgumentException("includeInTuple cannot be null");
      } else {
         this.includeInTuple = includeInTuple;
         this.tupleLength = ArrayHelper.countTrue(includeInTuple);
         this.tupleSubsetLength = includeInTransform == null ? this.tupleLength : ArrayHelper.countTrue(includeInTransform);
         if (this.tupleSubsetLength == this.tupleLength) {
            this.includeInTransformIndex = null;
         } else {
            this.includeInTransformIndex = new int[this.tupleSubsetLength];
            int i = 0;

            for(int j = 0; i < includeInTransform.length; ++i) {
               if (includeInTransform[i]) {
                  this.includeInTransformIndex[j] = i;
                  ++j;
               }
            }
         }

      }
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      if (aliases != null && aliases.length != this.tupleLength) {
         throw new IllegalStateException("aliases expected length is " + this.tupleLength + "; actual length is " + aliases.length);
      } else {
         return ACTUAL_TRANSFORMER.transformTuple(this.index(tuple.getClass(), tuple), (String[])null);
      }
   }

   public List retransformResults(List transformedResults, String[] aliases, ResultTransformer transformer, boolean[] includeInTuple) {
      if (transformer == null) {
         throw new IllegalArgumentException("transformer cannot be null");
      } else if (!this.equals(create(transformer, aliases, includeInTuple))) {
         throw new IllegalStateException("this CacheableResultTransformer is inconsistent with specified arguments; cannot re-transform");
      } else {
         boolean requiresRetransform = true;
         String[] aliasesToUse = aliases == null ? null : (String[])this.index(aliases.getClass(), aliases);
         if (transformer == ACTUAL_TRANSFORMER) {
            requiresRetransform = false;
         } else if (transformer instanceof TupleSubsetResultTransformer) {
            requiresRetransform = !((TupleSubsetResultTransformer)transformer).isTransformedValueATupleElement(aliasesToUse, this.tupleLength);
         }

         if (requiresRetransform) {
            for(int i = 0; i < transformedResults.size(); ++i) {
               Object[] tuple = ACTUAL_TRANSFORMER.untransformToTuple(transformedResults.get(i), this.tupleSubsetLength == 1);
               transformedResults.set(i, transformer.transformTuple(tuple, aliasesToUse));
            }
         }

         return transformedResults;
      }
   }

   public List untransformToTuples(List results) {
      if (this.includeInTransformIndex == null) {
         results = ACTUAL_TRANSFORMER.untransformToTuples(results, this.tupleSubsetLength == 1);
      } else {
         for(int i = 0; i < results.size(); ++i) {
            Object[] tuple = ACTUAL_TRANSFORMER.untransformToTuple(results.get(i), this.tupleSubsetLength == 1);
            results.set(i, this.unindex(tuple.getClass(), tuple));
         }
      }

      return results;
   }

   public Type[] getCachedResultTypes(Type[] tupleResultTypes) {
      return this.tupleLength != this.tupleSubsetLength ? (Type[])this.index(tupleResultTypes.getClass(), tupleResultTypes) : tupleResultTypes;
   }

   public List transformList(List list) {
      return list;
   }

   private Object[] index(Class clazz, Object[] objects) {
      T[] objectsIndexed = objects;
      if (objects != null && this.includeInTransformIndex != null && objects.length != this.tupleSubsetLength) {
         objectsIndexed = (T[])((Object[])clazz.cast(Array.newInstance(clazz.getComponentType(), this.tupleSubsetLength)));

         for(int i = 0; i < this.tupleSubsetLength; ++i) {
            objectsIndexed[i] = objects[this.includeInTransformIndex[i]];
         }
      }

      return objectsIndexed;
   }

   private Object[] unindex(Class clazz, Object[] objects) {
      T[] objectsUnindexed = objects;
      if (objects != null && this.includeInTransformIndex != null && objects.length != this.tupleLength) {
         objectsUnindexed = (T[])((Object[])clazz.cast(Array.newInstance(clazz.getComponentType(), this.tupleLength)));

         for(int i = 0; i < this.tupleSubsetLength; ++i) {
            objectsUnindexed[this.includeInTransformIndex[i]] = objects[i];
         }
      }

      return objectsUnindexed;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CacheableResultTransformer that = (CacheableResultTransformer)o;
         return this.tupleLength == that.tupleLength && this.tupleSubsetLength == that.tupleSubsetLength && Arrays.equals(this.includeInTuple, that.includeInTuple) && Arrays.equals(this.includeInTransformIndex, that.includeInTransformIndex);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.tupleLength;
      result = 31 * result + this.tupleSubsetLength;
      result = 31 * result + (this.includeInTuple != null ? Arrays.hashCode(this.includeInTuple) : 0);
      result = 31 * result + (this.includeInTransformIndex != null ? Arrays.hashCode(this.includeInTransformIndex) : 0);
      return result;
   }

   static {
      ACTUAL_TRANSFORMER = PassThroughResultTransformer.INSTANCE;
   }
}
