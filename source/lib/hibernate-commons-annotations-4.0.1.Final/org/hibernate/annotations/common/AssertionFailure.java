package org.hibernate.annotations.common;

import org.hibernate.annotations.common.util.impl.Log;
import org.hibernate.annotations.common.util.impl.LoggerFactory;

public class AssertionFailure extends RuntimeException {
   private static final Log log = LoggerFactory.make(AssertionFailure.class.getName());

   public AssertionFailure(String s) {
      super(s);
      log.assertionFailure(this);
   }

   public AssertionFailure(String s, Throwable t) {
      super(s, t);
      log.assertionFailure(this);
   }
}
