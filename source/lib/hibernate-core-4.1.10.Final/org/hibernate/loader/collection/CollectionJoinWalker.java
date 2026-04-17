package org.hibernate.loader.collection;

import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.loader.JoinWalker;

public abstract class CollectionJoinWalker extends JoinWalker {
   public CollectionJoinWalker(SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) {
      super(factory, loadQueryInfluencers);
   }

   protected StringBuilder whereString(String alias, String[] columnNames, String subselect, int batchSize) {
      if (subselect == null) {
         return super.whereString(alias, columnNames, batchSize);
      } else {
         StringBuilder buf = new StringBuilder();
         if (columnNames.length > 1) {
            buf.append('(');
         }

         buf.append(StringHelper.join(", ", StringHelper.qualify(alias, columnNames)));
         if (columnNames.length > 1) {
            buf.append(')');
         }

         buf.append(" in ").append('(').append(subselect).append(')');
         return buf;
      }
   }
}
