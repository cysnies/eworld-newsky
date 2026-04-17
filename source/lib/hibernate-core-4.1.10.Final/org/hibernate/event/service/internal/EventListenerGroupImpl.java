package org.hibernate.event.service.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistrationException;
import org.hibernate.event.spi.EventType;

public class EventListenerGroupImpl implements EventListenerGroup {
   private EventType eventType;
   private final Set duplicationStrategies = new LinkedHashSet();
   private List listeners;

   public EventListenerGroupImpl(EventType eventType) {
      // $FF: Couldn't be decompiled
   }

   public EventType getEventType() {
      return this.eventType;
   }

   public boolean isEmpty() {
      return this.count() <= 0;
   }

   public int count() {
      return this.listeners == null ? 0 : this.listeners.size();
   }

   public void clear() {
      if (this.duplicationStrategies != null) {
         this.duplicationStrategies.clear();
      }

      if (this.listeners != null) {
         this.listeners.clear();
      }

   }

   public void addDuplicationStrategy(DuplicationStrategy strategy) {
      this.duplicationStrategies.add(strategy);
   }

   public Iterable listeners() {
      return this.listeners == null ? Collections.emptyList() : this.listeners;
   }

   public void appendListeners(Object... listeners) {
      for(Object listener : listeners) {
         this.appendListener(listener);
      }

   }

   public void appendListener(Object listener) {
      if (this.listenerShouldGetAdded(listener)) {
         this.internalAppend(listener);
      }

   }

   public void prependListeners(Object... listeners) {
      for(Object listener : listeners) {
         this.prependListener(listener);
      }

   }

   public void prependListener(Object listener) {
      if (this.listenerShouldGetAdded(listener)) {
         this.internalPrepend(listener);
      }

   }

   private boolean listenerShouldGetAdded(Object listener) {
      if (this.listeners == null) {
         this.listeners = new ArrayList();
         return true;
      } else {
         boolean doAdd = true;

         for(DuplicationStrategy strategy : this.duplicationStrategies) {
            ListIterator<T> itr = this.listeners.listIterator();

            while(itr.hasNext()) {
               T existingListener = (T)itr.next();
               if (strategy.areMatch(listener, existingListener)) {
                  switch (strategy.getAction()) {
                     case ERROR:
                        throw new EventListenerRegistrationException("Duplicate event listener found");
                     case KEEP_ORIGINAL:
                        doAdd = false;
                        return doAdd;
                     case REPLACE_ORIGINAL:
                        itr.set(listener);
                        doAdd = false;
                        return doAdd;
                  }
               }
            }
         }

         return doAdd;
      }
   }

   private void internalPrepend(Object listener) {
      this.checkAgainstBaseInterface(listener);
      this.listeners.add(0, listener);
   }

   private void checkAgainstBaseInterface(Object listener) {
      if (!this.eventType.baseListenerInterface().isInstance(listener)) {
         throw new EventListenerRegistrationException("Listener did not implement expected interface [" + this.eventType.baseListenerInterface().getName() + "]");
      }
   }

   private void internalAppend(Object listener) {
      this.checkAgainstBaseInterface(listener);
      this.listeners.add(listener);
   }
}
