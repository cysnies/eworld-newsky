package org.hibernate.engine.loading.internal;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class EntityLoadContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityLoadContext.class.getName());
   private final LoadContexts loadContexts;
   private final ResultSet resultSet;
   private final List hydratingEntities = new ArrayList(20);

   public EntityLoadContext(LoadContexts loadContexts, ResultSet resultSet) {
      this.loadContexts = loadContexts;
      this.resultSet = resultSet;
   }

   void cleanup() {
      if (!this.hydratingEntities.isEmpty()) {
         LOG.hydratingEntitiesCount(this.hydratingEntities.size());
      }

      this.hydratingEntities.clear();
   }

   public String toString() {
      return super.toString() + "<rs=" + this.resultSet + ">";
   }
}
