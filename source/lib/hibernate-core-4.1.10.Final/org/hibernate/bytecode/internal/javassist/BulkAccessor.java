package org.hibernate.bytecode.internal.javassist;

import java.io.Serializable;

public abstract class BulkAccessor implements Serializable {
   protected Class target;
   protected String[] getters;
   protected String[] setters;
   protected Class[] types;

   protected BulkAccessor() {
   }

   public abstract void getPropertyValues(Object var1, Object[] var2);

   public abstract void setPropertyValues(Object var1, Object[] var2);

   public Object[] getPropertyValues(Object bean) {
      Object[] values = new Object[this.getters.length];
      this.getPropertyValues(bean, values);
      return values;
   }

   public Class[] getPropertyTypes() {
      return (Class[])this.types.clone();
   }

   public String[] getGetters() {
      return (String[])this.getters.clone();
   }

   public String[] getSetters() {
      return (String[])this.setters.clone();
   }

   public static BulkAccessor create(Class beanClass, String[] getters, String[] setters, Class[] types) {
      BulkAccessorFactory factory = new BulkAccessorFactory(beanClass, getters, setters, types);
      return factory.create();
   }
}
