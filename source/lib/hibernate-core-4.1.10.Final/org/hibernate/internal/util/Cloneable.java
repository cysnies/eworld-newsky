package org.hibernate.internal.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.hibernate.HibernateException;

public class Cloneable {
   private static final Object[] READER_METHOD_ARGS = new Object[0];

   public Object shallowCopy() {
      return AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return Cloneable.this.copyListeners();
         }
      });
   }

   public void validate() throws HibernateException {
      AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            Cloneable.this.checkListeners();
            return null;
         }
      });
   }

   private Object copyListeners() {
      Object copy = null;
      BeanInfo beanInfo = null;

      try {
         beanInfo = Introspector.getBeanInfo(this.getClass(), Object.class);
         this.internalCheckListeners(beanInfo);
         copy = this.getClass().newInstance();
         PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
         int i = 0;

         for(int max = pds.length; i < max; ++i) {
            try {
               pds[i].getWriteMethod().invoke(copy, pds[i].getReadMethod().invoke(this, READER_METHOD_ARGS));
            } catch (Throwable var11) {
               throw new HibernateException("Unable copy copy listener [" + pds[i].getName() + "]");
            }
         }
      } catch (Exception t) {
         throw new HibernateException("Unable to copy listeners", t);
      } finally {
         if (beanInfo != null) {
            Introspector.flushFromCaches(this.getClass());
         }

      }

      return copy;
   }

   private void checkListeners() {
      BeanInfo beanInfo = null;

      try {
         beanInfo = Introspector.getBeanInfo(this.getClass(), Object.class);
         this.internalCheckListeners(beanInfo);
      } catch (IntrospectionException t) {
         throw new HibernateException("Unable to validate listener config", t);
      } finally {
         if (beanInfo != null) {
            Introspector.flushFromCaches(this.getClass());
         }

      }

   }

   private void internalCheckListeners(BeanInfo beanInfo) {
      PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

      try {
         int i = 0;

         for(int max = pds.length; i < max; ++i) {
            Object listener = pds[i].getReadMethod().invoke(this, READER_METHOD_ARGS);
            if (listener == null) {
               throw new HibernateException("Listener [" + pds[i].getName() + "] was null");
            }

            if (listener.getClass().isArray()) {
               Object[] listenerArray = listener;
               int length = listenerArray.length;

               for(int index = 0; index < length; ++index) {
                  if (listenerArray[index] == null) {
                     throw new HibernateException("Listener in [" + pds[i].getName() + "] was null");
                  }
               }
            }
         }

      } catch (HibernateException e) {
         throw e;
      } catch (Throwable var10) {
         throw new HibernateException("Unable to validate listener config");
      }
   }
}
