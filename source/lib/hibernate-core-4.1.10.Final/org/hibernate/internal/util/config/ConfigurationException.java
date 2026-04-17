package org.hibernate.internal.util.config;

import org.hibernate.HibernateException;

public class ConfigurationException extends HibernateException {
   public ConfigurationException(String string, Throwable root) {
      super(string, root);
   }

   public ConfigurationException(String s) {
      super(s);
   }
}
