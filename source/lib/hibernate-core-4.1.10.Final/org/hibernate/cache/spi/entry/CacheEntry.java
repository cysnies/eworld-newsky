package org.hibernate.cache.spi.entry;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.TypeHelper;

public final class CacheEntry implements Serializable {
   private final Serializable[] disassembledState;
   private final String subclass;
   private final boolean lazyPropertiesAreUnfetched;
   private final Object version;

   public String getSubclass() {
      return this.subclass;
   }

   public boolean areLazyPropertiesUnfetched() {
      return this.lazyPropertiesAreUnfetched;
   }

   public CacheEntry(Object[] state, EntityPersister persister, boolean unfetched, Object version, SessionImplementor session, Object owner) throws HibernateException {
      this.disassembledState = TypeHelper.disassemble(state, persister.getPropertyTypes(), persister.isLazyPropertiesCacheable() ? null : persister.getPropertyLaziness(), session, owner);
      this.subclass = persister.getEntityName();
      this.lazyPropertiesAreUnfetched = unfetched || !persister.isLazyPropertiesCacheable();
      this.version = version;
   }

   public Object getVersion() {
      return this.version;
   }

   CacheEntry(Serializable[] state, String subclass, boolean unfetched, Object version) {
      this.disassembledState = state;
      this.subclass = subclass;
      this.lazyPropertiesAreUnfetched = unfetched;
      this.version = version;
   }

   public Object[] assemble(Object instance, Serializable id, EntityPersister persister, Interceptor interceptor, EventSource session) throws HibernateException {
      if (!persister.getEntityName().equals(this.subclass)) {
         throw new AssertionFailure("Tried to assemble a different subclass instance");
      } else {
         return assemble(this.disassembledState, instance, id, persister, interceptor, session);
      }
   }

   private static Object[] assemble(Serializable[] values, Object result, Serializable id, EntityPersister persister, Interceptor interceptor, EventSource session) throws HibernateException {
      Object[] assembledProps = TypeHelper.assemble(values, persister.getPropertyTypes(), session, result);
      PreLoadEvent preLoadEvent = (new PreLoadEvent(session)).setEntity(result).setState(assembledProps).setId(id).setPersister(persister);
      EventListenerGroup<PreLoadEventListener> listenerGroup = ((EventListenerRegistry)session.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.PRE_LOAD);

      for(PreLoadEventListener listener : listenerGroup.listeners()) {
         listener.onPreLoad(preLoadEvent);
      }

      persister.setPropertyValues(result, assembledProps);
      return assembledProps;
   }

   public Serializable[] getDisassembledState() {
      return this.disassembledState;
   }

   public String toString() {
      return "CacheEntry(" + this.subclass + ')' + ArrayHelper.toString(this.disassembledState);
   }
}
