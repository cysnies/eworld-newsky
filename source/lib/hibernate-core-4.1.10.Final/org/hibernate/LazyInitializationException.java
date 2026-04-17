package org.hibernate;

import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class LazyInitializationException extends HibernateException {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, LazyInitializationException.class.getName());

   public LazyInitializationException(String msg) {
      super(msg);
      LOG.trace(msg, this);
   }
}
