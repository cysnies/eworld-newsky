package org.hibernate.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DistinctResultTransformer extends BasicTransformerAdapter {
   public static final DistinctResultTransformer INSTANCE = new DistinctResultTransformer();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DistinctResultTransformer.class.getName());

   private DistinctResultTransformer() {
   }

   public List transformList(List list) {
      List result = new ArrayList(list.size());
      Set distinct = new HashSet();

      for(int i = 0; i < list.size(); ++i) {
         Object entity = list.get(i);
         if (distinct.add(new Identity(entity))) {
            result.add(entity);
         }
      }

      LOG.debugf("Transformed: %s rows to: %s distinct results", list.size(), result.size());
      return result;
   }

   private Object readResolve() {
      return INSTANCE;
   }

   private static final class Identity {
      final Object entity;

      private Identity(Object entity) {
         this.entity = entity;
      }

      public boolean equals(Object other) {
         return Identity.class.isInstance(other) && this.entity == ((Identity)other).entity;
      }

      public int hashCode() {
         return System.identityHashCode(this.entity);
      }
   }
}
