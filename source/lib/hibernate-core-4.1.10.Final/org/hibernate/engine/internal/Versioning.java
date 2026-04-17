package org.hibernate.engine.internal;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.VersionType;
import org.jboss.logging.Logger;

public final class Versioning {
   public static final int OPTIMISTIC_LOCK_NONE = -1;
   public static final int OPTIMISTIC_LOCK_VERSION = 0;
   public static final int OPTIMISTIC_LOCK_ALL = 2;
   public static final int OPTIMISTIC_LOCK_DIRTY = 1;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Versioning.class.getName());

   private Versioning() {
   }

   private static Object seed(VersionType versionType, SessionImplementor session) {
      Object seed = versionType.seed(session);
      LOG.tracev("Seeding: {0}", seed);
      return seed;
   }

   public static boolean seedVersion(Object[] fields, int versionProperty, VersionType versionType, SessionImplementor session) {
      Object initialVersion = fields[versionProperty];
      if (initialVersion != null && (!(initialVersion instanceof Number) || ((Number)initialVersion).longValue() >= 0L)) {
         LOG.tracev("Using initial version: {0}", initialVersion);
         return false;
      } else {
         fields[versionProperty] = seed(versionType, session);
         return true;
      }
   }

   public static Object increment(Object version, VersionType versionType, SessionImplementor session) {
      Object next = versionType.next(version, session);
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Incrementing: {0} to {1}", versionType.toLoggableString(version, session.getFactory()), versionType.toLoggableString(next, session.getFactory()));
      }

      return next;
   }

   public static void setVersion(Object[] fields, Object version, EntityPersister persister) {
      if (persister.isVersioned()) {
         fields[persister.getVersionProperty()] = version;
      }
   }

   public static Object getVersion(Object[] fields, EntityPersister persister) {
      return !persister.isVersioned() ? null : fields[persister.getVersionProperty()];
   }

   public static boolean isVersionIncrementRequired(int[] dirtyProperties, boolean hasDirtyCollections, boolean[] propertyVersionability) {
      if (hasDirtyCollections) {
         return true;
      } else {
         for(int i = 0; i < dirtyProperties.length; ++i) {
            if (propertyVersionability[dirtyProperties[i]]) {
               return true;
            }
         }

         return false;
      }
   }
}
