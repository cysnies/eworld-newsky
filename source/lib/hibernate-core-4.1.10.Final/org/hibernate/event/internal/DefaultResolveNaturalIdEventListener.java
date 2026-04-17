package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.ResolveNaturalIdEvent;
import org.hibernate.event.spi.ResolveNaturalIdEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class DefaultResolveNaturalIdEventListener extends AbstractLockUpgradeEventListener implements ResolveNaturalIdEventListener {
   public static final Object REMOVED_ENTITY_MARKER = new Object();
   public static final Object INCONSISTENT_RTN_CLASS_MARKER = new Object();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultResolveNaturalIdEventListener.class.getName());

   public void onResolveNaturalId(ResolveNaturalIdEvent event) throws HibernateException {
      Serializable entityId = this.resolveNaturalId(event);
      event.setEntityId(entityId);
   }

   protected Serializable resolveNaturalId(ResolveNaturalIdEvent event) {
      EntityPersister persister = event.getEntityPersister();
      boolean traceEnabled = LOG.isTraceEnabled();
      if (traceEnabled) {
         LOG.tracev("Attempting to resolve: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getNaturalIdValues(), (SessionFactoryImplementor)event.getSession().getFactory()));
      }

      Serializable entityId = this.resolveFromCache(event);
      if (entityId != null) {
         if (traceEnabled) {
            LOG.tracev("Resolved object in cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getNaturalIdValues(), (SessionFactoryImplementor)event.getSession().getFactory()));
         }

         return entityId;
      } else {
         if (traceEnabled) {
            LOG.tracev("Object not resolved in any cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getNaturalIdValues(), (SessionFactoryImplementor)event.getSession().getFactory()));
         }

         return this.loadFromDatasource(event);
      }
   }

   protected Serializable resolveFromCache(ResolveNaturalIdEvent event) {
      return event.getSession().getPersistenceContext().getNaturalIdHelper().findCachedNaturalIdResolution(event.getEntityPersister(), event.getOrderedNaturalIdValues());
   }

   protected Serializable loadFromDatasource(ResolveNaturalIdEvent event) {
      SessionFactoryImplementor factory = event.getSession().getFactory();
      boolean stats = factory.getStatistics().isStatisticsEnabled();
      long startTime = 0L;
      if (stats) {
         startTime = System.currentTimeMillis();
      }

      Serializable pk = event.getEntityPersister().loadEntityIdByNaturalId(event.getOrderedNaturalIdValues(), event.getLockOptions(), event.getSession());
      if (stats) {
         NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy = event.getEntityPersister().getNaturalIdCacheAccessStrategy();
         String regionName = naturalIdCacheAccessStrategy == null ? null : naturalIdCacheAccessStrategy.getRegion().getName();
         factory.getStatisticsImplementor().naturalIdQueryExecuted(regionName, System.currentTimeMillis() - startTime);
      }

      if (pk != null) {
         event.getSession().getPersistenceContext().getNaturalIdHelper().cacheNaturalIdCrossReferenceFromLoad(event.getEntityPersister(), pk, event.getOrderedNaturalIdValues());
      }

      return pk;
   }
}
