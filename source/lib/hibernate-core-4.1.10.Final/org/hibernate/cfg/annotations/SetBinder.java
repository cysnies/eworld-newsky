package org.hibernate.cfg.annotations;

import org.hibernate.annotations.OrderBy;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Set;
import org.jboss.logging.Logger;

public class SetBinder extends CollectionBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SetBinder.class.getName());

   public SetBinder() {
   }

   public SetBinder(boolean sorted) {
      super(sorted);
   }

   protected Collection createCollection(PersistentClass persistentClass) {
      return new Set(this.getMappings(), persistentClass);
   }

   public void setSqlOrderBy(OrderBy orderByAnn) {
      if (orderByAnn != null) {
         super.setSqlOrderBy(orderByAnn);
      }

   }
}
