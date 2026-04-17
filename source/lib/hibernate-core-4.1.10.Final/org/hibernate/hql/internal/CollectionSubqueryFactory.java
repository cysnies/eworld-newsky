package org.hibernate.hql.internal;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.JoinFragment;

public final class CollectionSubqueryFactory {
   private CollectionSubqueryFactory() {
   }

   public static String createCollectionSubquery(JoinSequence joinSequence, Map enabledFilters, String[] columns) {
      try {
         JoinFragment join = joinSequence.toJoinFragment(enabledFilters, true);
         return "select " + StringHelper.join(", ", columns) + " from " + join.toFromFragmentString().substring(2) + " where " + join.toWhereFragmentString().substring(5);
      } catch (MappingException me) {
         throw new QueryException(me);
      }
   }
}
