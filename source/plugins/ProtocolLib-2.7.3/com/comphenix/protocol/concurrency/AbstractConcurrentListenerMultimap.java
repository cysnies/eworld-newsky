package com.comphenix.protocol.concurrency;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public abstract class AbstractConcurrentListenerMultimap {
   private AtomicReferenceArray arrayListeners;
   private ConcurrentMap mapListeners;

   public AbstractConcurrentListenerMultimap(int maximumPacketID) {
      this.arrayListeners = new AtomicReferenceArray(maximumPacketID + 1);
      this.mapListeners = new ConcurrentHashMap();
   }

   public void addListener(Object listener, ListeningWhitelist whitelist) {
      PrioritizedListener<TListener> prioritized = new PrioritizedListener(listener, whitelist.getPriority());

      for(Integer packetID : whitelist.getWhitelist()) {
         this.addListener(packetID, prioritized);
      }

   }

   private void addListener(Integer packetID, PrioritizedListener listener) {
      SortedCopyOnWriteArray<PrioritizedListener<TListener>> list = (SortedCopyOnWriteArray)this.arrayListeners.get(packetID);
      if (list == null) {
         SortedCopyOnWriteArray<PrioritizedListener<TListener>> value = new SortedCopyOnWriteArray();
         if (this.arrayListeners.compareAndSet(packetID, (Object)null, value)) {
            this.mapListeners.put(packetID, value);
            list = value;
         } else {
            list = (SortedCopyOnWriteArray)this.arrayListeners.get(packetID);
         }
      }

      list.add((Comparable)listener);
   }

   public List removeListener(Object listener, ListeningWhitelist whitelist) {
      List<Integer> removedPackets = new ArrayList();

      for(Integer packetID : whitelist.getWhitelist()) {
         SortedCopyOnWriteArray<PrioritizedListener<TListener>> list = (SortedCopyOnWriteArray)this.arrayListeners.get(packetID);
         if (list != null && list.size() > 0) {
            list.remove(new PrioritizedListener(listener, whitelist.getPriority()));
            if (list.size() == 0) {
               this.arrayListeners.set(packetID, (Object)null);
               this.mapListeners.remove(packetID);
               removedPackets.add(packetID);
            }
         }
      }

      return removedPackets;
   }

   public Collection getListener(int packetID) {
      return (Collection)this.arrayListeners.get(packetID);
   }

   public Iterable values() {
      return Iterables.concat(this.mapListeners.values());
   }

   public Set keySet() {
      return this.mapListeners.keySet();
   }

   protected void clearListeners() {
      this.arrayListeners = new AtomicReferenceArray(this.arrayListeners.length());
      this.mapListeners.clear();
   }
}
