package org.hibernate.internal.util.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;

public class BeanInfoHelper {
   private final Class beanClass;
   private final Class stopClass;

   public BeanInfoHelper(Class beanClass) {
      this(beanClass, Object.class);
   }

   public BeanInfoHelper(Class beanClass, Class stopClass) {
      this.beanClass = beanClass;
      this.stopClass = stopClass;
   }

   public void applyToBeanInfo(Object bean, BeanInfoDelegate delegate) {
      if (!this.beanClass.isInstance(bean)) {
         throw new BeanIntrospectionException("Bean [" + bean + "] was not of declared bean type [" + this.beanClass.getName() + "]");
      } else {
         visitBeanInfo(this.beanClass, this.stopClass, delegate);
      }
   }

   public static void visitBeanInfo(Class beanClass, BeanInfoDelegate delegate) {
      visitBeanInfo(beanClass, Object.class, delegate);
   }

   public static void visitBeanInfo(Class beanClass, Class stopClass, BeanInfoDelegate delegate) {
      try {
         BeanInfo info = Introspector.getBeanInfo(beanClass, stopClass);

         try {
            delegate.processBeanInfo(info);
         } catch (RuntimeException e) {
            throw e;
         } catch (InvocationTargetException e) {
            throw new BeanIntrospectionException("Error delegating bean info use", e.getTargetException());
         } catch (Exception e) {
            throw new BeanIntrospectionException("Error delegating bean info use", e);
         } finally {
            Introspector.flushFromCaches(beanClass);
         }

      } catch (IntrospectionException e) {
         throw new BeanIntrospectionException("Unable to determine bean info from class [" + beanClass.getName() + "]", e);
      }
   }

   public interface BeanInfoDelegate {
      void processBeanInfo(BeanInfo var1) throws Exception;
   }
}
