package org.hibernate.internal.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.type.Type;

public final class ArrayHelper {
   public static final boolean[] TRUE = new boolean[]{true};
   public static final boolean[] FALSE = new boolean[]{false};
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   public static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
   public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   private static int SEED = 23;
   private static int PRIME_NUMER = 37;

   public static int indexOf(Object[] array, Object object) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i].equals(object)) {
            return i;
         }
      }

      return -1;
   }

   public static String[] toStringArray(Object[] objects) {
      int length = objects.length;
      String[] result = new String[length];

      for(int i = 0; i < length; ++i) {
         result[i] = objects[i].toString();
      }

      return result;
   }

   public static String[] fillArray(String value, int length) {
      String[] result = new String[length];
      Arrays.fill(result, value);
      return result;
   }

   public static int[] fillArray(int value, int length) {
      int[] result = new int[length];
      Arrays.fill(result, value);
      return result;
   }

   public static LockMode[] fillArray(LockMode lockMode, int length) {
      LockMode[] array = new LockMode[length];
      Arrays.fill(array, lockMode);
      return array;
   }

   public static LockOptions[] fillArray(LockOptions lockOptions, int length) {
      LockOptions[] array = new LockOptions[length];
      Arrays.fill(array, lockOptions);
      return array;
   }

   public static String[] toStringArray(Collection coll) {
      return (String[])coll.toArray(new String[coll.size()]);
   }

   public static String[][] to2DStringArray(Collection coll) {
      return (String[][])coll.toArray(new String[coll.size()][]);
   }

   public static int[][] to2DIntArray(Collection coll) {
      return (int[][])coll.toArray(new int[coll.size()][]);
   }

   public static Type[] toTypeArray(Collection coll) {
      return (Type[])coll.toArray(new Type[coll.size()]);
   }

   public static int[] toIntArray(Collection coll) {
      Iterator iter = coll.iterator();
      int[] arr = new int[coll.size()];

      for(int i = 0; iter.hasNext(); arr[i++] = (Integer)iter.next()) {
      }

      return arr;
   }

   public static boolean[] toBooleanArray(Collection coll) {
      Iterator iter = coll.iterator();
      boolean[] arr = new boolean[coll.size()];

      for(int i = 0; iter.hasNext(); arr[i++] = (Boolean)iter.next()) {
      }

      return arr;
   }

   public static Object[] typecast(Object[] array, Object[] to) {
      return Arrays.asList(array).toArray(to);
   }

   public static List toList(Object array) {
      if (array instanceof Object[]) {
         return Arrays.asList(array);
      } else {
         int size = Array.getLength(array);
         ArrayList list = new ArrayList(size);

         for(int i = 0; i < size; ++i) {
            list.add(Array.get(array, i));
         }

         return list;
      }
   }

   public static String[] slice(String[] strings, int begin, int length) {
      String[] result = new String[length];
      System.arraycopy(strings, begin, result, 0, length);
      return result;
   }

   public static Object[] slice(Object[] objects, int begin, int length) {
      Object[] result = new Object[length];
      System.arraycopy(objects, begin, result, 0, length);
      return result;
   }

   public static List toList(Iterator iter) {
      List list = new ArrayList();

      while(iter.hasNext()) {
         list.add(iter.next());
      }

      return list;
   }

   public static String[] join(String[] x, String[] y) {
      String[] result = new String[x.length + y.length];
      System.arraycopy(x, 0, result, 0, x.length);
      System.arraycopy(y, 0, result, x.length, y.length);
      return result;
   }

   public static String[] join(String[] x, String[] y, boolean[] use) {
      String[] result = new String[x.length + countTrue(use)];
      System.arraycopy(x, 0, result, 0, x.length);
      int k = x.length;

      for(int i = 0; i < y.length; ++i) {
         if (use[i]) {
            result[k++] = y[i];
         }
      }

      return result;
   }

   public static int[] join(int[] x, int[] y) {
      int[] result = new int[x.length + y.length];
      System.arraycopy(x, 0, result, 0, x.length);
      System.arraycopy(y, 0, result, x.length, y.length);
      return result;
   }

   public static Object[] join(Object[] x, Object[] y) {
      T[] result = (T[])((Object[])((Object[])Array.newInstance(x.getClass().getComponentType(), x.length + y.length)));
      System.arraycopy(x, 0, result, 0, x.length);
      System.arraycopy(y, 0, result, x.length, y.length);
      return result;
   }

   private ArrayHelper() {
   }

   public static String toString(Object[] array) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");

      for(int i = 0; i < array.length; ++i) {
         sb.append(array[i]);
         if (i < array.length - 1) {
            sb.append(",");
         }
      }

      sb.append("]");
      return sb.toString();
   }

   public static boolean isAllNegative(int[] array) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i] >= 0) {
            return false;
         }
      }

      return true;
   }

   public static boolean isAllTrue(boolean[] array) {
      for(int i = 0; i < array.length; ++i) {
         if (!array[i]) {
            return false;
         }
      }

      return true;
   }

   public static int countTrue(boolean[] array) {
      int result = 0;

      for(int i = 0; i < array.length; ++i) {
         if (array[i]) {
            ++result;
         }
      }

      return result;
   }

   public static boolean isAllFalse(boolean[] array) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i]) {
            return false;
         }
      }

      return true;
   }

   public static void addAll(Collection collection, Object[] array) {
      collection.addAll(Arrays.asList(array));
   }

   public static int[] getBatchSizes(int maxBatchSize) {
      int batchSize = maxBatchSize;

      int n;
      for(n = 1; batchSize > 1; ++n) {
         batchSize = getNextBatchSize(batchSize);
      }

      int[] result = new int[n];
      batchSize = maxBatchSize;

      for(int i = 0; i < n; ++i) {
         result[i] = batchSize;
         batchSize = getNextBatchSize(batchSize);
      }

      return result;
   }

   private static int getNextBatchSize(int batchSize) {
      if (batchSize <= 10) {
         return batchSize - 1;
      } else {
         return batchSize / 2 < 10 ? 10 : batchSize / 2;
      }
   }

   public static int hash(Object[] array) {
      int length = array.length;
      int seed = SEED;

      for(int index = 0; index < length; ++index) {
         seed = hash(seed, array[index] == null ? 0 : array[index].hashCode());
      }

      return seed;
   }

   public static int hash(char[] array) {
      int length = array.length;
      int seed = SEED;

      for(int index = 0; index < length; ++index) {
         seed = hash(seed, array[index]);
      }

      return seed;
   }

   public static int hash(byte[] bytes) {
      int length = bytes.length;
      int seed = SEED;

      for(int index = 0; index < length; ++index) {
         seed = hash(seed, bytes[index]);
      }

      return seed;
   }

   private static int hash(int seed, int i) {
      return PRIME_NUMER * seed + i;
   }

   public static boolean isEquals(Object[] o1, Object[] o2) {
      if (o1 == o2) {
         return true;
      } else if (o1 != null && o2 != null) {
         int length = o1.length;
         if (length != o2.length) {
            return false;
         } else {
            for(int index = 0; index < length; ++index) {
               if (!o1[index].equals(o2[index])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isEquals(char[] o1, char[] o2) {
      if (o1 == o2) {
         return true;
      } else if (o1 != null && o2 != null) {
         int length = o1.length;
         if (length != o2.length) {
            return false;
         } else {
            for(int index = 0; index < length; ++index) {
               if (o1[index] != o2[index]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isEquals(byte[] b1, byte[] b2) {
      if (b1 == b2) {
         return true;
      } else if (b1 != null && b2 != null) {
         int length = b1.length;
         if (length != b2.length) {
            return false;
         } else {
            for(int index = 0; index < length; ++index) {
               if (b1[index] != b2[index]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
