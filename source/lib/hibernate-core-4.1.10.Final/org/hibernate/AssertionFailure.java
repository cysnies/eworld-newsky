package org.hibernate;

import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class AssertionFailure extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AssertionFailure.class.getName());

   public AssertionFailure(String s) {
      super(s);
      LOG.failed(this);
   }

   public AssertionFailure(String s, Throwable t) {
      super(s, t);
      LOG.failed(t);
   }
}
