package org.hibernate.stat.internal;

import java.util.Collections;
import java.util.Set;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.stat.SessionStatistics;

public class SessionStatisticsImpl implements SessionStatistics {
   private final SessionImplementor session;

   public SessionStatisticsImpl(SessionImplementor session) {
      this.session = session;
   }

   public int getEntityCount() {
      return this.session.getPersistenceContext().getEntityEntries().size();
   }

   public int getCollectionCount() {
      return this.session.getPersistenceContext().getCollectionEntries().size();
   }

   public Set getEntityKeys() {
      return Collections.unmodifiableSet(this.session.getPersistenceContext().getEntitiesByKey().keySet());
   }

   public Set getCollectionKeys() {
      return Collections.unmodifiableSet(this.session.getPersistenceContext().getCollectionsByKey().keySet());
   }

   public String toString() {
      return "SessionStatistics[" + "entity count=" + this.getEntityCount() + ",collection count=" + this.getCollectionCount() + ']';
   }
}
