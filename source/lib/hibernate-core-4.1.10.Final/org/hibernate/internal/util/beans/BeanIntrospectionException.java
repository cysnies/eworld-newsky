package org.hibernate.internal.util.beans;

import org.hibernate.HibernateException;

public class BeanIntrospectionException extends HibernateException {
   public BeanIntrospectionException(String string, Throwable root) {
      super(string, root);
   }

   public BeanIntrospectionException(String s) {
      super(s);
   }
}
