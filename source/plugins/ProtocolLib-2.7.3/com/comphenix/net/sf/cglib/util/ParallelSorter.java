package com.comphenix.net.sf.cglib.util;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassesKey;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import java.util.Comparator;

public abstract class ParallelSorter extends SorterTemplate {
   protected Object[] a;
   private Comparer comparer;
   // $FF: synthetic field
   static Class class$net$sf$cglib$util$ParallelSorter;

   protected ParallelSorter() {
   }

   public abstract ParallelSorter newInstance(Object[] var1);

   public static ParallelSorter create(Object[] arrays) {
      Generator gen = new Generator();
      gen.setArrays(arrays);
      return gen.create();
   }

   private int len() {
      return ((Object[])((Object[])this.a[0])).length;
   }

   public void quickSort(int index) {
      this.quickSort(index, 0, this.len(), (Comparator)null);
   }

   public void quickSort(int index, int lo, int hi) {
      this.quickSort(index, lo, hi, (Comparator)null);
   }

   public void quickSort(int index, Comparator cmp) {
      this.quickSort(index, 0, this.len(), cmp);
   }

   public void quickSort(int index, int lo, int hi, Comparator cmp) {
      this.chooseComparer(index, cmp);
      super.quickSort(lo, hi - 1);
   }

   public void mergeSort(int index) {
      this.mergeSort(index, 0, this.len(), (Comparator)null);
   }

   public void mergeSort(int index, int lo, int hi) {
      this.mergeSort(index, lo, hi, (Comparator)null);
   }

   public void mergeSort(int index, Comparator cmp) {
      this.mergeSort(index, 0, this.len(), cmp);
   }

   public void mergeSort(int index, int lo, int hi, Comparator cmp) {
      this.chooseComparer(index, cmp);
      super.mergeSort(lo, hi - 1);
   }

   private void chooseComparer(int index, Comparator cmp) {
      Object array = this.a[index];
      Class type = array.getClass().getComponentType();
      if (type.equals(Integer.TYPE)) {
         this.comparer = new IntComparer((int[])array);
      } else if (type.equals(Long.TYPE)) {
         this.comparer = new LongComparer((long[])array);
      } else if (type.equals(Double.TYPE)) {
         this.comparer = new DoubleComparer((double[])array);
      } else if (type.equals(Float.TYPE)) {
         this.comparer = new FloatComparer((float[])array);
      } else if (type.equals(Short.TYPE)) {
         this.comparer = new ShortComparer((short[])array);
      } else if (type.equals(Byte.TYPE)) {
         this.comparer = new ByteComparer((byte[])array);
      } else if (cmp != null) {
         this.comparer = new ComparatorComparer(array, cmp);
      } else {
         this.comparer = new ObjectComparer(array);
      }

   }

   protected int compare(int i, int j) {
      return this.comparer.compare(i, j);
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static class ComparatorComparer implements Comparer {
      private Object[] a;
      private Comparator cmp;

      public ComparatorComparer(Object[] a, Comparator cmp) {
         this.a = a;
         this.cmp = cmp;
      }

      public int compare(int i, int j) {
         return this.cmp.compare(this.a[i], this.a[j]);
      }
   }

   static class ObjectComparer implements Comparer {
      private Object[] a;

      public ObjectComparer(Object[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         return ((Comparable)this.a[i]).compareTo(this.a[j]);
      }
   }

   static class IntComparer implements Comparer {
      private int[] a;

      public IntComparer(int[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         return this.a[i] - this.a[j];
      }
   }

   static class LongComparer implements Comparer {
      private long[] a;

      public LongComparer(long[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         long vi = this.a[i];
         long vj = this.a[j];
         return vi == vj ? 0 : (vi > vj ? 1 : -1);
      }
   }

   static class FloatComparer implements Comparer {
      private float[] a;

      public FloatComparer(float[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         float vi = this.a[i];
         float vj = this.a[j];
         return vi == vj ? 0 : (vi > vj ? 1 : -1);
      }
   }

   static class DoubleComparer implements Comparer {
      private double[] a;

      public DoubleComparer(double[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         double vi = this.a[i];
         double vj = this.a[j];
         return vi == vj ? 0 : (vi > vj ? 1 : -1);
      }
   }

   static class ShortComparer implements Comparer {
      private short[] a;

      public ShortComparer(short[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         return this.a[i] - this.a[j];
      }
   }

   static class ByteComparer implements Comparer {
      private byte[] a;

      public ByteComparer(byte[] a) {
         this.a = a;
      }

      public int compare(int i, int j) {
         return this.a[i] - this.a[j];
      }
   }

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private Object[] arrays;

      public Generator() {
         super(SOURCE);
      }

      protected ClassLoader getDefaultClassLoader() {
         return null;
      }

      public void setArrays(Object[] arrays) {
         this.arrays = arrays;
      }

      public ParallelSorter create() {
         return (ParallelSorter)super.create(ClassesKey.create(this.arrays));
      }

      public void generateClass(ClassVisitor v) throws Exception {
         if (this.arrays.length == 0) {
            throw new IllegalArgumentException("No arrays specified to sort");
         } else {
            for(int i = 0; i < this.arrays.length; ++i) {
               if (!this.arrays[i].getClass().isArray()) {
                  throw new IllegalArgumentException(this.arrays[i].getClass() + " is not an array");
               }
            }

            new ParallelSorterEmitter(v, this.getClassName(), this.arrays);
         }
      }

      protected Object firstInstance(Class type) {
         return ((ParallelSorter)ReflectUtils.newInstance(type)).newInstance(this.arrays);
      }

      protected Object nextInstance(Object instance) {
         return ((ParallelSorter)instance).newInstance(this.arrays);
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((ParallelSorter.class$net$sf$cglib$util$ParallelSorter == null ? (ParallelSorter.class$net$sf$cglib$util$ParallelSorter = ParallelSorter.class$("com.comphenix.net.sf.cglib.util.ParallelSorter")) : ParallelSorter.class$net$sf$cglib$util$ParallelSorter).getName());
      }
   }

   interface Comparer {
      int compare(int var1, int var2);
   }
}
