package org.hibernate.bytecode.internal.javassist;

import java.io.Serializable;
import org.hibernate.PropertyAccessException;
import org.hibernate.bytecode.spi.ReflectionOptimizer;

public class AccessOptimizerAdapter implements ReflectionOptimizer.AccessOptimizer, Serializable {
   public static final String PROPERTY_GET_EXCEPTION = "exception getting property value with Javassist (set hibernate.bytecode.use_reflection_optimizer=false for more info)";
   public static final String PROPERTY_SET_EXCEPTION = "exception setting property value with Javassist (set hibernate.bytecode.use_reflection_optimizer=false for more info)";
   private final BulkAccessor bulkAccessor;
   private final Class mappedClass;

   public AccessOptimizerAdapter(BulkAccessor bulkAccessor, Class mappedClass) {
      this.bulkAccessor = bulkAccessor;
      this.mappedClass = mappedClass;
   }

   public String[] getPropertyNames() {
      return this.bulkAccessor.getGetters();
   }

   public Object[] getPropertyValues(Object object) {
      try {
         return this.bulkAccessor.getPropertyValues(object);
      } catch (Throwable t) {
         throw new PropertyAccessException(t, "exception getting property value with Javassist (set hibernate.bytecode.use_reflection_optimizer=false for more info)", false, this.mappedClass, getterName(t, this.bulkAccessor));
      }
   }

   public void setPropertyValues(Object object, Object[] values) {
      try {
         this.bulkAccessor.setPropertyValues(object, values);
      } catch (Throwable t) {
         throw new PropertyAccessException(t, "exception setting property value with Javassist (set hibernate.bytecode.use_reflection_optimizer=false for more info)", true, this.mappedClass, setterName(t, this.bulkAccessor));
      }
   }

   private static String setterName(Throwable t, BulkAccessor accessor) {
      return t instanceof BulkAccessorException ? accessor.getSetters()[((BulkAccessorException)t).getIndex()] : "?";
   }

   private static String getterName(Throwable t, BulkAccessor accessor) {
      return t instanceof BulkAccessorException ? accessor.getGetters()[((BulkAccessorException)t).getIndex()] : "?";
   }
}
