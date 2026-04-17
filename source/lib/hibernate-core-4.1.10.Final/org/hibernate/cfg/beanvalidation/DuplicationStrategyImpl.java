package org.hibernate.cfg.beanvalidation;

import org.hibernate.event.service.spi.DuplicationStrategy;

public class DuplicationStrategyImpl implements DuplicationStrategy {
   public static final DuplicationStrategyImpl INSTANCE = new DuplicationStrategyImpl();

   public boolean areMatch(Object listener, Object original) {
      return listener.getClass().equals(original.getClass()) && BeanValidationEventListener.class.equals(listener.getClass());
   }

   public DuplicationStrategy.Action getAction() {
      return DuplicationStrategy.Action.KEEP_ORIGINAL;
   }
}
