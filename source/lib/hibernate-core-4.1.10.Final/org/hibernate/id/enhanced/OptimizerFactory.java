package org.hibernate.id.enhanced;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import org.hibernate.HibernateException;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class OptimizerFactory {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, OptimizerFactory.class.getName());
   private static Class[] CTOR_SIG;
   /** @deprecated */
   @Deprecated
   public static final String NONE;
   /** @deprecated */
   @Deprecated
   public static final String HILO;
   /** @deprecated */
   @Deprecated
   public static final String LEGACY_HILO = "legacy-hilo";
   /** @deprecated */
   @Deprecated
   public static final String POOL = "pooled";
   /** @deprecated */
   @Deprecated
   public static final String POOL_LO = "pooled-lo";

   public static boolean isPooledOptimizer(String type) {
      StandardOptimizerDescriptor standardDescriptor = OptimizerFactory.StandardOptimizerDescriptor.fromExternalName(type);
      return standardDescriptor != null && standardDescriptor.isPooled();
   }

   /** @deprecated */
   @Deprecated
   public static Optimizer buildOptimizer(String type, Class returnClass, int incrementSize) {
      StandardOptimizerDescriptor standardDescriptor = OptimizerFactory.StandardOptimizerDescriptor.fromExternalName(type);
      Class<? extends Optimizer> optimizerClass;
      if (standardDescriptor != null) {
         optimizerClass = standardDescriptor.getOptimizerClass();
      } else {
         try {
            optimizerClass = ReflectHelper.classForName(type);
         } catch (Throwable var7) {
            LOG.unableToLocateCustomOptimizerClass(type);
            return buildFallbackOptimizer(returnClass, incrementSize);
         }
      }

      try {
         Constructor ctor = optimizerClass.getConstructor(CTOR_SIG);
         return (Optimizer)ctor.newInstance(returnClass, incrementSize);
      } catch (Throwable var6) {
         LOG.unableToInstantiateOptimizer(type);
         return buildFallbackOptimizer(returnClass, incrementSize);
      }
   }

   private static Optimizer buildFallbackOptimizer(Class returnClass, int incrementSize) {
      return new NoopOptimizer(returnClass, incrementSize);
   }

   public static Optimizer buildOptimizer(String type, Class returnClass, int incrementSize, long explicitInitialValue) {
      Optimizer optimizer = buildOptimizer(type, returnClass, incrementSize);
      if (InitialValueAwareOptimizer.class.isInstance(optimizer)) {
         ((InitialValueAwareOptimizer)optimizer).injectInitialValue(explicitInitialValue);
      }

      return optimizer;
   }

   static {
      CTOR_SIG = new Class[]{Class.class, Integer.TYPE};
      NONE = OptimizerFactory.StandardOptimizerDescriptor.NONE.getExternalName();
      HILO = OptimizerFactory.StandardOptimizerDescriptor.HILO.getExternalName();
   }

   public static enum StandardOptimizerDescriptor {
      NONE("none", NoopOptimizer.class),
      HILO("hilo", HiLoOptimizer.class),
      LEGACY_HILO("legacy-hilo", LegacyHiLoAlgorithmOptimizer.class),
      POOLED("pooled", PooledOptimizer.class, true),
      POOLED_LO("pooled-lo", PooledLoOptimizer.class, true);

      private final String externalName;
      private final Class optimizerClass;
      private final boolean isPooled;

      private StandardOptimizerDescriptor(String externalName, Class optimizerClass) {
         this(externalName, optimizerClass, false);
      }

      private StandardOptimizerDescriptor(String externalName, Class optimizerClass, boolean pooled) {
         this.externalName = externalName;
         this.optimizerClass = optimizerClass;
         this.isPooled = pooled;
      }

      public String getExternalName() {
         return this.externalName;
      }

      public Class getOptimizerClass() {
         return this.optimizerClass;
      }

      public boolean isPooled() {
         return this.isPooled;
      }

      public static StandardOptimizerDescriptor fromExternalName(String externalName) {
         if (StringHelper.isEmpty(externalName)) {
            OptimizerFactory.LOG.debug("No optimizer specified, using NONE as default");
            return NONE;
         } else if (NONE.externalName.equals(externalName)) {
            return NONE;
         } else if (HILO.externalName.equals(externalName)) {
            return HILO;
         } else if (LEGACY_HILO.externalName.equals(externalName)) {
            return LEGACY_HILO;
         } else if (POOLED.externalName.equals(externalName)) {
            return POOLED;
         } else if (POOLED_LO.externalName.equals(externalName)) {
            return POOLED_LO;
         } else {
            OptimizerFactory.LOG.debugf("Unknown optimizer key [%s]; returning null assuming Optimizer impl class name", externalName);
            return null;
         }
      }
   }

   public abstract static class OptimizerSupport implements Optimizer {
      protected final Class returnClass;
      protected final int incrementSize;

      protected OptimizerSupport(Class returnClass, int incrementSize) {
         if (returnClass == null) {
            throw new HibernateException("return class is required");
         } else {
            this.returnClass = returnClass;
            this.incrementSize = incrementSize;
         }
      }

      public final Class getReturnClass() {
         return this.returnClass;
      }

      public final int getIncrementSize() {
         return this.incrementSize;
      }
   }

   public static class NoopOptimizer extends OptimizerSupport {
      private IntegralDataTypeHolder lastSourceValue;

      public NoopOptimizer(Class returnClass, int incrementSize) {
         super(returnClass, incrementSize);
      }

      public Serializable generate(AccessCallback callback) {
         IntegralDataTypeHolder value;
         for(value = null; value == null || value.lt(1L); value = callback.getNextValue()) {
         }

         this.lastSourceValue = value;
         return value.makeValue();
      }

      public IntegralDataTypeHolder getLastSourceValue() {
         return this.lastSourceValue;
      }

      public boolean applyIncrementSizeToSourceValues() {
         return false;
      }
   }

   public static class HiLoOptimizer extends OptimizerSupport {
      private IntegralDataTypeHolder lastSourceValue;
      private IntegralDataTypeHolder upperLimit;
      private IntegralDataTypeHolder value;

      public HiLoOptimizer(Class returnClass, int incrementSize) {
         super(returnClass, incrementSize);
         if (incrementSize < 1) {
            throw new HibernateException("increment size cannot be less than 1");
         } else {
            if (OptimizerFactory.LOG.isTraceEnabled()) {
               OptimizerFactory.LOG.tracev("Creating hilo optimizer with [incrementSize={0}; returnClass={1}]", incrementSize, returnClass.getName());
            }

         }
      }

      public synchronized Serializable generate(AccessCallback callback) {
         if (this.lastSourceValue == null) {
            for(this.lastSourceValue = callback.getNextValue(); this.lastSourceValue.lt(1L); this.lastSourceValue = callback.getNextValue()) {
            }

            this.upperLimit = this.lastSourceValue.copy().multiplyBy((long)this.incrementSize).increment();
            this.value = this.upperLimit.copy().subtract((long)this.incrementSize);
         } else if (!this.upperLimit.gt(this.value)) {
            this.lastSourceValue = callback.getNextValue();
            this.upperLimit = this.lastSourceValue.copy().multiplyBy((long)this.incrementSize).increment();
         }

         return this.value.makeValueThenIncrement();
      }

      public IntegralDataTypeHolder getLastSourceValue() {
         return this.lastSourceValue;
      }

      public boolean applyIncrementSizeToSourceValues() {
         return false;
      }

      public IntegralDataTypeHolder getLastValue() {
         return this.value.copy().decrement();
      }

      public IntegralDataTypeHolder getHiValue() {
         return this.upperLimit;
      }
   }

   public static class LegacyHiLoAlgorithmOptimizer extends OptimizerSupport {
      private long maxLo;
      private long lo;
      private IntegralDataTypeHolder hi;
      private IntegralDataTypeHolder lastSourceValue;
      private IntegralDataTypeHolder value;

      public LegacyHiLoAlgorithmOptimizer(Class returnClass, int incrementSize) {
         super(returnClass, incrementSize);
         if (incrementSize < 1) {
            throw new HibernateException("increment size cannot be less than 1");
         } else {
            if (OptimizerFactory.LOG.isTraceEnabled()) {
               OptimizerFactory.LOG.tracev("Creating hilo optimizer (legacy) with [incrementSize={0}; returnClass={1}]", incrementSize, returnClass.getName());
            }

            this.maxLo = (long)incrementSize;
            this.lo = this.maxLo + 1L;
         }
      }

      public synchronized Serializable generate(AccessCallback callback) {
         if (this.lo > this.maxLo) {
            this.lastSourceValue = callback.getNextValue();
            this.lo = this.lastSourceValue.eq(0L) ? 1L : 0L;
            this.hi = this.lastSourceValue.copy().multiplyBy(this.maxLo + 1L);
         }

         this.value = this.hi.copy().add((long)(this.lo++));
         return this.value.makeValue();
      }

      public IntegralDataTypeHolder getLastSourceValue() {
         return this.lastSourceValue.copy();
      }

      public boolean applyIncrementSizeToSourceValues() {
         return false;
      }

      public IntegralDataTypeHolder getLastValue() {
         return this.value;
      }
   }

   public static class PooledOptimizer extends OptimizerSupport implements InitialValueAwareOptimizer {
      private IntegralDataTypeHolder hiValue;
      private IntegralDataTypeHolder value;
      private long initialValue = -1L;

      public PooledOptimizer(Class returnClass, int incrementSize) {
         super(returnClass, incrementSize);
         if (incrementSize < 1) {
            throw new HibernateException("increment size cannot be less than 1");
         } else {
            if (OptimizerFactory.LOG.isTraceEnabled()) {
               OptimizerFactory.LOG.tracev("Creating pooled optimizer with [incrementSize={0}; returnClass={1}]", incrementSize, returnClass.getName());
            }

         }
      }

      public synchronized Serializable generate(AccessCallback callback) {
         if (this.hiValue == null) {
            this.value = callback.getNextValue();
            if (this.value.lt(1L)) {
               OptimizerFactory.LOG.pooledOptimizerReportedInitialValue(this.value);
            }

            if ((this.initialValue != -1L || !this.value.lt((long)this.incrementSize)) && !this.value.eq(this.initialValue)) {
               this.hiValue = this.value;
               this.value = this.hiValue.copy().subtract((long)this.incrementSize);
            } else {
               this.hiValue = callback.getNextValue();
            }
         } else if (!this.hiValue.gt(this.value)) {
            this.hiValue = callback.getNextValue();
            this.value = this.hiValue.copy().subtract((long)this.incrementSize);
         }

         return this.value.makeValueThenIncrement();
      }

      public IntegralDataTypeHolder getLastSourceValue() {
         return this.hiValue;
      }

      public boolean applyIncrementSizeToSourceValues() {
         return true;
      }

      public IntegralDataTypeHolder getLastValue() {
         return this.value.copy().decrement();
      }

      public void injectInitialValue(long initialValue) {
         this.initialValue = initialValue;
      }
   }

   public static class PooledLoOptimizer extends OptimizerSupport {
      private IntegralDataTypeHolder lastSourceValue;
      private IntegralDataTypeHolder value;

      public PooledLoOptimizer(Class returnClass, int incrementSize) {
         super(returnClass, incrementSize);
         if (incrementSize < 1) {
            throw new HibernateException("increment size cannot be less than 1");
         } else {
            if (OptimizerFactory.LOG.isTraceEnabled()) {
               OptimizerFactory.LOG.tracev("Creating pooled optimizer (lo) with [incrementSize={0}; returnClass=]", incrementSize, returnClass.getName());
            }

         }
      }

      public synchronized Serializable generate(AccessCallback callback) {
         if (this.lastSourceValue == null || !this.value.lt(this.lastSourceValue.copy().add((long)this.incrementSize))) {
            this.lastSourceValue = callback.getNextValue();
            this.value = this.lastSourceValue.copy();

            while(this.value.lt(1L)) {
               this.value.increment();
            }
         }

         return this.value.makeValueThenIncrement();
      }

      public IntegralDataTypeHolder getLastSourceValue() {
         return this.lastSourceValue;
      }

      public boolean applyIncrementSizeToSourceValues() {
         return true;
      }
   }

   public interface InitialValueAwareOptimizer {
      void injectInitialValue(long var1);
   }
}
