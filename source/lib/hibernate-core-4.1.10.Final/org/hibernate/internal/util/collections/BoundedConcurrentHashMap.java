package org.hibernate.internal.util.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedConcurrentHashMap extends AbstractMap implements ConcurrentMap, Serializable {
   private static final long serialVersionUID = 7249069246763182397L;
   static final int DEFAULT_MAXIMUM_CAPACITY = 512;
   static final float DEFAULT_LOAD_FACTOR = 0.75F;
   static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int RETRIES_BEFORE_LOCK = 2;
   final int segmentMask;
   final int segmentShift;
   final Segment[] segments;
   transient Set keySet;
   transient Set entrySet;
   transient Collection values;

   private static int hash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   final Segment segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   public BoundedConcurrentHashMap(int capacity, int concurrencyLevel, Eviction evictionStrategy, EvictionListener evictionListener) {
      if (capacity >= 0 && concurrencyLevel > 0) {
         concurrencyLevel = Math.min(capacity / 2, concurrencyLevel);
         concurrencyLevel = Math.max(concurrencyLevel, 1);
         if (capacity < concurrencyLevel * 2 && capacity != 1) {
            throw new IllegalArgumentException("Maximum capacity has to be at least twice the concurrencyLevel");
         } else if (evictionStrategy != null && evictionListener != null) {
            if (concurrencyLevel > 65536) {
               concurrencyLevel = 65536;
            }

            int sshift = 0;

            int ssize;
            for(ssize = 1; ssize < concurrencyLevel; ssize <<= 1) {
               ++sshift;
            }

            this.segmentShift = 32 - sshift;
            this.segmentMask = ssize - 1;
            this.segments = BoundedConcurrentHashMap.Segment.newArray(ssize);
            if (capacity > 1073741824) {
               capacity = 1073741824;
            }

            int c = capacity / ssize;

            int cap;
            for(cap = 1; cap < c; cap <<= 1) {
            }

            for(int i = 0; i < this.segments.length; ++i) {
               this.segments[i] = new Segment(cap, c, 0.75F, evictionStrategy, evictionListener);
            }

         } else {
            throw new IllegalArgumentException();
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   public BoundedConcurrentHashMap(int capacity, int concurrencyLevel) {
      this(capacity, concurrencyLevel, BoundedConcurrentHashMap.Eviction.LRU);
   }

   public BoundedConcurrentHashMap(int capacity, int concurrencyLevel, Eviction evictionStrategy) {
      this(capacity, concurrencyLevel, evictionStrategy, new NullEvictionListener());
   }

   public BoundedConcurrentHashMap(int capacity) {
      this(capacity, 16);
   }

   public BoundedConcurrentHashMap() {
      this(512, 16);
   }

   public boolean isEmpty() {
      Segment<K, V>[] segments = this.segments;
      int[] mc = new int[segments.length];
      int mcsum = 0;

      for(int i = 0; i < segments.length; ++i) {
         if (segments[i].count != 0) {
            return false;
         }

         mcsum += mc[i] = segments[i].modCount;
      }

      if (mcsum != 0) {
         for(int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0 || mc[i] != segments[i].modCount) {
               return false;
            }
         }
      }

      return true;
   }

   public int size() {
      Segment<K, V>[] segments = this.segments;
      long sum = 0L;
      long check = 0L;
      int[] mc = new int[segments.length];

      for(int k = 0; k < 2; ++k) {
         check = 0L;
         sum = 0L;
         int mcsum = 0;

         for(int i = 0; i < segments.length; ++i) {
            sum += (long)segments[i].count;
            mcsum += mc[i] = segments[i].modCount;
         }

         if (mcsum != 0) {
            for(int i = 0; i < segments.length; ++i) {
               check += (long)segments[i].count;
               if (mc[i] != segments[i].modCount) {
                  check = -1L;
                  break;
               }
            }
         }

         if (check == sum) {
            break;
         }
      }

      if (check != sum) {
         sum = 0L;

         for(int i = 0; i < segments.length; ++i) {
            segments[i].lock();
         }

         boolean var13 = false;

         try {
            var13 = true;

            for(int var16 = 0; var16 < segments.length; ++var16) {
               sum += (long)segments[var16].count;
            }

            var13 = false;
         } finally {
            if (var13) {
               for(int i = 0; i < segments.length; ++i) {
                  segments[i].unlock();
               }

            }
         }

         for(int i = 0; i < segments.length; ++i) {
            segments[i].unlock();
         }
      }

      return sum > 2147483647L ? Integer.MAX_VALUE : (int)sum;
   }

   public Object get(Object key) {
      int hash = hash(key.hashCode());
      return this.segmentFor(hash).get(key, hash);
   }

   public boolean containsKey(Object key) {
      int hash = hash(key.hashCode());
      return this.segmentFor(hash).containsKey(key, hash);
   }

   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         Segment<K, V>[] segments = this.segments;
         int[] mc = new int[segments.length];

         for(int k = 0; k < 2; ++k) {
            int mcsum = 0;

            for(int i = 0; i < segments.length; ++i) {
               int c = segments[i].count;
               mcsum += mc[i] = segments[i].modCount;
               if (segments[i].containsValue(value)) {
                  return true;
               }
            }

            boolean cleanSweep = true;
            if (mcsum != 0) {
               for(int i = 0; i < segments.length; ++i) {
                  int c = segments[i].count;
                  if (mc[i] != segments[i].modCount) {
                     cleanSweep = false;
                     break;
                  }
               }
            }

            if (cleanSweep) {
               return false;
            }
         }

         for(int i = 0; i < segments.length; ++i) {
            segments[i].lock();
         }

         boolean found = false;
         boolean var12 = false;

         try {
            var12 = true;
            int i = 0;

            while(true) {
               if (i >= segments.length) {
                  var12 = false;
                  break;
               }

               if (segments[i].containsValue(value)) {
                  found = true;
                  var12 = false;
                  break;
               }

               ++i;
            }
         } finally {
            if (var12) {
               for(int i = 0; i < segments.length; ++i) {
                  segments[i].unlock();
               }

            }
         }

         for(int i = 0; i < segments.length; ++i) {
            segments[i].unlock();
         }

         return found;
      }
   }

   public boolean contains(Object value) {
      return this.containsValue(value);
   }

   public Object put(Object key, Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key.hashCode());
         return this.segmentFor(hash).put(key, hash, value, false);
      }
   }

   public Object putIfAbsent(Object key, Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key.hashCode());
         return this.segmentFor(hash).put(key, hash, value, true);
      }
   }

   public void putAll(Map m) {
      for(Map.Entry e : m.entrySet()) {
         this.put(e.getKey(), e.getValue());
      }

   }

   public Object remove(Object key) {
      int hash = hash(key.hashCode());
      return this.segmentFor(hash).remove(key, hash, (Object)null);
   }

   public boolean remove(Object key, Object value) {
      int hash = hash(key.hashCode());
      if (value == null) {
         return false;
      } else {
         return this.segmentFor(hash).remove(key, hash, value) != null;
      }
   }

   public boolean replace(Object key, Object oldValue, Object newValue) {
      if (oldValue != null && newValue != null) {
         int hash = hash(key.hashCode());
         return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
      } else {
         throw new NullPointerException();
      }
   }

   public Object replace(Object key, Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = hash(key.hashCode());
         return this.segmentFor(hash).replace(key, hash, value);
      }
   }

   public void clear() {
      for(int i = 0; i < this.segments.length; ++i) {
         this.segments[i].clear();
      }

   }

   public Set keySet() {
      Set<K> ks = this.keySet;
      return ks != null ? ks : (this.keySet = new KeySet());
   }

   public Collection values() {
      Collection<V> vs = this.values;
      return vs != null ? vs : (this.values = new Values());
   }

   public Set entrySet() {
      Set<Map.Entry<K, V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new EntrySet());
   }

   public Enumeration keys() {
      return new KeyIterator();
   }

   public Enumeration elements() {
      return new ValueIterator();
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();

      for(int k = 0; k < this.segments.length; ++k) {
         Segment<K, V> seg = this.segments[k];
         seg.lock();

         try {
            HashEntry<K, V>[] tab = seg.table;

            for(int i = 0; i < tab.length; ++i) {
               for(HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                  s.writeObject(e.key);
                  s.writeObject(e.value);
               }
            }
         } finally {
            seg.unlock();
         }
      }

      s.writeObject((Object)null);
      s.writeObject((Object)null);
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();

      for(int i = 0; i < this.segments.length; ++i) {
         this.segments[i].setTable(new HashEntry[1]);
      }

      while(true) {
         K key = (K)s.readObject();
         V value = (V)s.readObject();
         if (key == null) {
            return;
         }

         this.put(key, value);
      }
   }

   private static class HashEntry {
      final Object key;
      final int hash;
      volatile Object value;
      final HashEntry next;

      HashEntry(Object key, int hash, HashEntry next, Object value) {
         this.key = key;
         this.hash = hash;
         this.next = next;
         this.value = value;
      }

      public int hashCode() {
         int result = 17;
         result = result * 31 + this.hash;
         result = result * 31 + this.key.hashCode();
         return result;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o == null) {
            return false;
         } else {
            HashEntry<?, ?> other = (HashEntry)o;
            return this.hash == other.hash && this.key.equals(other.key);
         }
      }

      static HashEntry[] newArray(int i) {
         return new HashEntry[i];
      }
   }

   private static enum Recency {
      HIR_RESIDENT,
      LIR_RESIDENT,
      HIR_NONRESIDENT;
   }

   public static enum Eviction {
      NONE {
         public EvictionPolicy make(Segment s, int capacity, float lf) {
            return new NullEvictionPolicy();
         }
      },
      LRU {
         public EvictionPolicy make(Segment s, int capacity, float lf) {
            return new LRU(s, capacity, lf, capacity * 10, lf);
         }
      },
      LIRS {
         public EvictionPolicy make(Segment s, int capacity, float lf) {
            return new LIRS(s, capacity, capacity * 10, lf);
         }
      };

      private Eviction() {
      }

      abstract EvictionPolicy make(Segment var1, int var2, float var3);
   }

   static final class NullEvictionListener implements EvictionListener {
      public void onEntryEviction(Map evicted) {
      }

      public void onEntryChosenForEviction(Object internalCacheEntry) {
      }
   }

   static class NullEvictionPolicy implements EvictionPolicy {
      public void clear() {
      }

      public Set execute() {
         return Collections.emptySet();
      }

      public boolean onEntryHit(HashEntry e) {
         return false;
      }

      public Set onEntryMiss(HashEntry e) {
         return Collections.emptySet();
      }

      public void onEntryRemove(HashEntry e) {
      }

      public boolean thresholdExpired() {
         return false;
      }

      public Eviction strategy() {
         return BoundedConcurrentHashMap.Eviction.NONE;
      }

      public HashEntry createNewEntry(Object key, int hash, HashEntry next, Object value) {
         return new HashEntry(key, hash, next, value);
      }
   }

   static final class LRU extends LinkedHashMap implements EvictionPolicy {
      private static final long serialVersionUID = -7645068174197717838L;
      private final ConcurrentLinkedQueue accessQueue;
      private final Segment segment;
      private final int maxBatchQueueSize;
      private final int trimDownSize;
      private final float batchThresholdFactor;
      private final Set evicted;

      public LRU(Segment s, int capacity, float lf, int maxBatchSize, float batchThresholdFactor) {
         super(capacity, lf, true);
         this.segment = s;
         this.trimDownSize = capacity;
         this.maxBatchQueueSize = maxBatchSize > 64 ? 64 : maxBatchSize;
         this.batchThresholdFactor = batchThresholdFactor;
         this.accessQueue = new ConcurrentLinkedQueue();
         this.evicted = new HashSet();
      }

      public Set execute() {
         Set<HashEntry<K, V>> evictedCopy = new HashSet();

         for(HashEntry e : this.accessQueue) {
            this.put(e, e.value);
         }

         evictedCopy.addAll(this.evicted);
         this.accessQueue.clear();
         this.evicted.clear();
         return evictedCopy;
      }

      public Set onEntryMiss(HashEntry e) {
         this.put(e, e.value);
         if (!this.evicted.isEmpty()) {
            Set<HashEntry<K, V>> evictedCopy = new HashSet();
            evictedCopy.addAll(this.evicted);
            this.evicted.clear();
            return evictedCopy;
         } else {
            return Collections.emptySet();
         }
      }

      public boolean onEntryHit(HashEntry e) {
         this.accessQueue.add(e);
         return (float)this.accessQueue.size() >= (float)this.maxBatchQueueSize * this.batchThresholdFactor;
      }

      public boolean thresholdExpired() {
         return this.accessQueue.size() >= this.maxBatchQueueSize;
      }

      public void onEntryRemove(HashEntry e) {
         this.remove(e);

         while(this.accessQueue.remove(e)) {
         }

      }

      public void clear() {
         super.clear();
         this.accessQueue.clear();
      }

      public Eviction strategy() {
         return BoundedConcurrentHashMap.Eviction.LRU;
      }

      protected boolean isAboveThreshold() {
         return this.size() > this.trimDownSize;
      }

      protected boolean removeEldestEntry(Map.Entry eldest) {
         boolean aboveThreshold = this.isAboveThreshold();
         if (aboveThreshold) {
            HashEntry<K, V> evictedEntry = (HashEntry)eldest.getKey();
            this.segment.evictionListener.onEntryChosenForEviction(evictedEntry.value);
            this.segment.remove(evictedEntry.key, evictedEntry.hash, (Object)null);
            this.evicted.add(evictedEntry);
         }

         return aboveThreshold;
      }

      public HashEntry createNewEntry(Object key, int hash, HashEntry next, Object value) {
         return new HashEntry(key, hash, next, value);
      }
   }

   private static final class LIRSHashEntry extends HashEntry {
      private LIRSHashEntry previousInStack;
      private LIRSHashEntry nextInStack;
      private LIRSHashEntry previousInQueue;
      private LIRSHashEntry nextInQueue;
      volatile Recency state;
      LIRS owner;

      LIRSHashEntry(LIRS owner, Object key, int hash, HashEntry next, Object value) {
         super(key, hash, next, value);
         this.owner = owner;
         this.state = BoundedConcurrentHashMap.Recency.HIR_RESIDENT;
         this.previousInStack = this;
         this.nextInStack = this;
         this.previousInQueue = this;
         this.nextInQueue = this;
      }

      public int hashCode() {
         int result = 17;
         result = result * 31 + this.hash;
         result = result * 31 + this.key.hashCode();
         return result;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o == null) {
            return false;
         } else {
            HashEntry<?, ?> other = (HashEntry)o;
            return this.hash == other.hash && this.key.equals(other.key);
         }
      }

      public boolean inStack() {
         return this.nextInStack != null;
      }

      public boolean inQueue() {
         return this.nextInQueue != null;
      }

      public void hit(Set evicted) {
         switch (this.state) {
            case LIR_RESIDENT:
               this.hotHit(evicted);
               break;
            case HIR_RESIDENT:
               this.coldHit(evicted);
               break;
            case HIR_NONRESIDENT:
               throw new IllegalStateException("Can't hit a non-resident entry!");
            default:
               throw new AssertionError("Hit with unknown status: " + this.state);
         }

      }

      private void hotHit(Set evicted) {
         boolean onBottom = this.owner.stackBottom() == this;
         this.moveToStackTop();
         if (onBottom) {
            this.owner.pruneStack(evicted);
         }

      }

      private void coldHit(Set evicted) {
         boolean inStack = this.inStack();
         this.moveToStackTop();
         if (inStack) {
            this.hot();
            this.removeFromQueue();
            this.owner.stackBottom().migrateToQueue();
            this.owner.pruneStack(evicted);
         } else {
            this.moveToQueueEnd();
         }

      }

      private Set miss() {
         Set<HashEntry<K, V>> evicted = Collections.emptySet();
         if (this.owner.hotSize < this.owner.maximumHotSize) {
            this.warmupMiss();
         } else {
            evicted = new HashSet();
            this.fullMiss(evicted);
         }

         this.owner.size++;
         return evicted;
      }

      private void warmupMiss() {
         this.hot();
         this.moveToStackTop();
      }

      private void fullMiss(Set evicted) {
         if (this.owner.size >= this.owner.maximumSize) {
            LIRSHashEntry<K, V> evictedNode = this.owner.queueFront();
            evicted.add(evictedNode);
         }

         boolean inStack = this.inStack();
         this.moveToStackTop();
         if (inStack) {
            this.hot();
            this.owner.stackBottom().migrateToQueue();
            this.owner.pruneStack(evicted);
         } else {
            this.cold();
         }

      }

      private void hot() {
         if (this.state != BoundedConcurrentHashMap.Recency.LIR_RESIDENT) {
            this.owner.hotSize++;
         }

         this.state = BoundedConcurrentHashMap.Recency.LIR_RESIDENT;
      }

      private void cold() {
         if (this.state == BoundedConcurrentHashMap.Recency.LIR_RESIDENT) {
            this.owner.hotSize--;
         }

         this.state = BoundedConcurrentHashMap.Recency.HIR_RESIDENT;
         this.moveToQueueEnd();
      }

      private void nonResident() {
         switch (this.state) {
            case LIR_RESIDENT:
               this.owner.hotSize--;
            case HIR_RESIDENT:
               this.owner.size--;
            default:
               this.state = BoundedConcurrentHashMap.Recency.HIR_NONRESIDENT;
         }
      }

      public boolean isResident() {
         return this.state != BoundedConcurrentHashMap.Recency.HIR_NONRESIDENT;
      }

      private void tempRemoveFromStack() {
         if (this.inStack()) {
            this.previousInStack.nextInStack = this.nextInStack;
            this.nextInStack.previousInStack = this.previousInStack;
         }

      }

      private void removeFromStack() {
         this.tempRemoveFromStack();
         this.previousInStack = null;
         this.nextInStack = null;
      }

      private void addToStackBefore(LIRSHashEntry existingEntry) {
         this.previousInStack = existingEntry.previousInStack;
         this.nextInStack = existingEntry;
         this.previousInStack.nextInStack = this;
         this.nextInStack.previousInStack = this;
      }

      private void moveToStackTop() {
         this.tempRemoveFromStack();
         this.addToStackBefore(this.owner.header.nextInStack);
      }

      private void moveToStackBottom() {
         this.tempRemoveFromStack();
         this.addToStackBefore(this.owner.header);
      }

      private void tempRemoveFromQueue() {
         if (this.inQueue()) {
            this.previousInQueue.nextInQueue = this.nextInQueue;
            this.nextInQueue.previousInQueue = this.previousInQueue;
         }

      }

      private void removeFromQueue() {
         this.tempRemoveFromQueue();
         this.previousInQueue = null;
         this.nextInQueue = null;
      }

      private void addToQueueBefore(LIRSHashEntry existingEntry) {
         this.previousInQueue = existingEntry.previousInQueue;
         this.nextInQueue = existingEntry;
         this.previousInQueue.nextInQueue = this;
         this.nextInQueue.previousInQueue = this;
      }

      private void moveToQueueEnd() {
         this.tempRemoveFromQueue();
         this.addToQueueBefore(this.owner.header);
      }

      private void migrateToQueue() {
         this.removeFromStack();
         this.cold();
      }

      private void migrateToStack() {
         this.removeFromQueue();
         if (!this.inStack()) {
            this.moveToStackBottom();
         }

         this.hot();
      }

      private void evict() {
         this.removeFromQueue();
         this.removeFromStack();
         this.nonResident();
         this.owner = null;
      }

      private Object remove() {
         boolean wasHot = this.state == BoundedConcurrentHashMap.Recency.LIR_RESIDENT;
         V result = (V)this.value;
         LIRSHashEntry<K, V> end = this.owner != null ? this.owner.queueEnd() : null;
         this.evict();
         if (wasHot && end != null) {
            end.migrateToStack();
         }

         return result;
      }
   }

   static final class LIRS implements EvictionPolicy {
      private static final float L_LIRS = 0.95F;
      private final Segment segment;
      private final ConcurrentLinkedQueue accessQueue;
      private final int maxBatchQueueSize;
      private int size;
      private final float batchThresholdFactor;
      private final LIRSHashEntry header = new LIRSHashEntry((LIRS)null, (Object)null, 0, (HashEntry)null, (Object)null);
      private final int maximumHotSize;
      private final int maximumSize;
      private int hotSize = 0;

      public LIRS(Segment s, int capacity, int maxBatchSize, float batchThresholdFactor) {
         this.segment = s;
         this.maximumSize = capacity;
         this.maximumHotSize = calculateLIRSize(capacity);
         this.maxBatchQueueSize = maxBatchSize > 64 ? 64 : maxBatchSize;
         this.batchThresholdFactor = batchThresholdFactor;
         this.accessQueue = new ConcurrentLinkedQueue();
      }

      private static int calculateLIRSize(int maximumSize) {
         int result = (int)(0.95F * (float)maximumSize);
         return result == maximumSize ? maximumSize - 1 : result;
      }

      public Set execute() {
         Set<HashEntry<K, V>> evicted = new HashSet();

         try {
            for(LIRSHashEntry e : this.accessQueue) {
               if (e.isResident()) {
                  e.hit(evicted);
               }
            }

            this.removeFromSegment(evicted);
         } finally {
            this.accessQueue.clear();
         }

         return evicted;
      }

      private void pruneStack(Set evicted) {
         for(LIRSHashEntry<K, V> bottom = this.stackBottom(); bottom != null && bottom.state != BoundedConcurrentHashMap.Recency.LIR_RESIDENT; bottom = this.stackBottom()) {
            bottom.removeFromStack();
            if (bottom.state == BoundedConcurrentHashMap.Recency.HIR_NONRESIDENT) {
               evicted.add(bottom);
            }
         }

      }

      public Set onEntryMiss(HashEntry en) {
         LIRSHashEntry<K, V> e = (LIRSHashEntry)en;
         Set<HashEntry<K, V>> evicted = e.miss();
         this.removeFromSegment(evicted);
         return evicted;
      }

      private void removeFromSegment(Set evicted) {
         for(HashEntry e : evicted) {
            ((LIRSHashEntry)e).evict();
            this.segment.evictionListener.onEntryChosenForEviction(e.value);
            this.segment.remove(e.key, e.hash, (Object)null);
         }

      }

      public boolean onEntryHit(HashEntry e) {
         this.accessQueue.add(e);
         return (float)this.accessQueue.size() >= (float)this.maxBatchQueueSize * this.batchThresholdFactor;
      }

      public boolean thresholdExpired() {
         return this.accessQueue.size() >= this.maxBatchQueueSize;
      }

      public void onEntryRemove(HashEntry e) {
         ((LIRSHashEntry)e).remove();

         while(this.accessQueue.remove(e)) {
         }

      }

      public void clear() {
         this.accessQueue.clear();
      }

      public Eviction strategy() {
         return BoundedConcurrentHashMap.Eviction.LIRS;
      }

      private LIRSHashEntry stackBottom() {
         LIRSHashEntry<K, V> bottom = this.header.previousInStack;
         return bottom == this.header ? null : bottom;
      }

      private LIRSHashEntry queueFront() {
         LIRSHashEntry<K, V> front = this.header.nextInQueue;
         return front == this.header ? null : front;
      }

      private LIRSHashEntry queueEnd() {
         LIRSHashEntry<K, V> end = this.header.previousInQueue;
         return end == this.header ? null : end;
      }

      public HashEntry createNewEntry(Object key, int hash, HashEntry next, Object value) {
         return new LIRSHashEntry(this, key, hash, next, value);
      }
   }

   static final class Segment extends ReentrantLock {
      private static final long serialVersionUID = 2249069246763182397L;
      transient volatile int count;
      transient int modCount;
      transient int threshold;
      transient volatile HashEntry[] table;
      final float loadFactor;
      final int evictCap;
      final transient EvictionPolicy eviction;
      final transient EvictionListener evictionListener;

      Segment(int cap, int evictCap, float lf, Eviction es, EvictionListener listener) {
         this.loadFactor = lf;
         this.evictCap = evictCap;
         this.eviction = es.make(this, evictCap, lf);
         this.evictionListener = listener;
         this.setTable(BoundedConcurrentHashMap.HashEntry.newArray(cap));
      }

      static Segment[] newArray(int i) {
         return new Segment[i];
      }

      EvictionListener getEvictionListener() {
         return this.evictionListener;
      }

      void setTable(HashEntry[] newTable) {
         this.threshold = (int)((float)newTable.length * this.loadFactor);
         this.table = newTable;
      }

      HashEntry getFirst(int hash) {
         HashEntry<K, V>[] tab = this.table;
         return tab[hash & tab.length - 1];
      }

      Object readValueUnderLock(HashEntry e) {
         this.lock();

         Object var2;
         try {
            var2 = e.value;
         } finally {
            this.unlock();
         }

         return var2;
      }

      Object get(Object key, int hash) {
         int c = this.count;
         if (c == 0) {
            return null;
         } else {
            V result = (V)null;

            HashEntry<K, V> e;
            for(e = this.getFirst(hash); e != null; e = e.next) {
               if (e.hash == hash && key.equals(e.key)) {
                  V v = (V)e.value;
                  if (v != null) {
                     result = v;
                  } else {
                     result = (V)this.readValueUnderLock(e);
                  }
                  break;
               }
            }

            if (result != null && this.eviction.onEntryHit(e)) {
               Set<HashEntry<K, V>> evicted = this.attemptEviction(false);
               this.notifyEvictionListener(evicted);
            }

            return result;
         }
      }

      boolean containsKey(Object key, int hash) {
         if (this.count != 0) {
            for(HashEntry<K, V> e = this.getFirst(hash); e != null; e = e.next) {
               if (e.hash == hash && key.equals(e.key)) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean containsValue(Object value) {
         if (this.count != 0) {
            HashEntry<K, V>[] tab = this.table;
            int len = tab.length;

            for(int i = 0; i < len; ++i) {
               for(HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                  V v = (V)e.value;
                  if (v == null) {
                     v = (V)this.readValueUnderLock(e);
                  }

                  if (value.equals(v)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      boolean replace(Object key, int hash, Object oldValue, Object newValue) {
         this.lock();
         Set<HashEntry<K, V>> evicted = null;

         boolean var8;
         try {
            HashEntry<K, V> e;
            for(e = this.getFirst(hash); e != null && (e.hash != hash || !key.equals(e.key)); e = e.next) {
            }

            boolean replaced = false;
            if (e != null && oldValue.equals(e.value)) {
               replaced = true;
               e.value = newValue;
               if (this.eviction.onEntryHit(e)) {
                  evicted = this.attemptEviction(true);
               }
            }

            var8 = replaced;
         } finally {
            this.unlock();
            this.notifyEvictionListener(evicted);
         }

         return var8;
      }

      Object replace(Object key, int hash, Object newValue) {
         this.lock();
         Set<HashEntry<K, V>> evicted = null;

         Object var7;
         try {
            HashEntry<K, V> e;
            for(e = this.getFirst(hash); e != null && (e.hash != hash || !key.equals(e.key)); e = e.next) {
            }

            V oldValue = (V)null;
            if (e != null) {
               oldValue = (V)e.value;
               e.value = newValue;
               if (this.eviction.onEntryHit(e)) {
                  evicted = this.attemptEviction(true);
               }
            }

            var7 = oldValue;
         } finally {
            this.unlock();
            this.notifyEvictionListener(evicted);
         }

         return var7;
      }

      Object put(Object key, int hash, Object value, boolean onlyIfAbsent) {
         this.lock();
         Set<HashEntry<K, V>> evicted = null;

         Object var17;
         try {
            int c = this.count;
            if (c++ > this.threshold && this.eviction.strategy() == BoundedConcurrentHashMap.Eviction.NONE) {
               this.rehash();
            }

            HashEntry<K, V>[] tab = this.table;
            int index = hash & tab.length - 1;
            HashEntry<K, V> first = tab[index];

            HashEntry<K, V> e;
            for(e = first; e != null && (e.hash != hash || !key.equals(e.key)); e = e.next) {
            }

            V oldValue;
            if (e != null) {
               oldValue = (V)e.value;
               if (!onlyIfAbsent) {
                  e.value = value;
                  this.eviction.onEntryHit(e);
               }
            } else {
               oldValue = (V)null;
               ++this.modCount;
               this.count = c;
               if (this.eviction.strategy() != BoundedConcurrentHashMap.Eviction.NONE) {
                  if (c > this.evictCap) {
                     evicted = this.eviction.execute();
                     first = tab[index];
                  }

                  tab[index] = this.eviction.createNewEntry(key, hash, first, value);
                  Set<HashEntry<K, V>> newlyEvicted = this.eviction.onEntryMiss(tab[index]);
                  if (!newlyEvicted.isEmpty()) {
                     if (evicted != null) {
                        evicted.addAll(newlyEvicted);
                     } else {
                        evicted = newlyEvicted;
                     }
                  }
               } else {
                  tab[index] = this.eviction.createNewEntry(key, hash, first, value);
               }
            }

            var17 = oldValue;
         } finally {
            this.unlock();
            this.notifyEvictionListener(evicted);
         }

         return var17;
      }

      void rehash() {
         HashEntry<K, V>[] oldTable = this.table;
         int oldCapacity = oldTable.length;
         if (oldCapacity < 1073741824) {
            HashEntry<K, V>[] newTable = BoundedConcurrentHashMap.HashEntry.newArray(oldCapacity << 1);
            this.threshold = (int)((float)newTable.length * this.loadFactor);
            int sizeMask = newTable.length - 1;

            for(int i = 0; i < oldCapacity; ++i) {
               HashEntry<K, V> e = oldTable[i];
               if (e != null) {
                  HashEntry<K, V> next = e.next;
                  int idx = e.hash & sizeMask;
                  if (next == null) {
                     newTable[idx] = e;
                  } else {
                     HashEntry<K, V> lastRun = e;
                     int lastIdx = idx;

                     for(HashEntry<K, V> last = next; last != null; last = last.next) {
                        int k = last.hash & sizeMask;
                        if (k != lastIdx) {
                           lastIdx = k;
                           lastRun = last;
                        }
                     }

                     newTable[lastIdx] = lastRun;

                     for(HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                        int k = p.hash & sizeMask;
                        HashEntry<K, V> n = newTable[k];
                        newTable[k] = this.eviction.createNewEntry(p.key, p.hash, n, p.value);
                     }
                  }
               }
            }

            this.table = newTable;
         }
      }

      Object remove(Object key, int hash, Object value) {
         this.lock();

         V v;
         try {
            int c = this.count - 1;
            HashEntry<K, V>[] tab = this.table;
            int index = hash & tab.length - 1;
            HashEntry<K, V> first = tab[index];

            HashEntry<K, V> e;
            for(e = first; e != null && (e.hash != hash || !key.equals(e.key)); e = e.next) {
            }

            V oldValue = (V)null;
            if (e != null) {
               v = (V)e.value;
               if (value == null || value.equals(v)) {
                  oldValue = v;
                  ++this.modCount;
                  this.eviction.onEntryRemove(e);
                  HashEntry<K, V> newFirst = e.next;

                  for(HashEntry<K, V> p = first; p != e; p = p.next) {
                     this.eviction.onEntryRemove(p);
                     newFirst = this.eviction.createNewEntry(p.key, p.hash, newFirst, p.value);
                     this.eviction.onEntryMiss(newFirst);
                  }

                  tab[index] = newFirst;
                  this.count = c;
               }
            }

            v = oldValue;
         } finally {
            this.unlock();
         }

         return v;
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               HashEntry<K, V>[] tab = this.table;

               for(int i = 0; i < tab.length; ++i) {
                  tab[i] = null;
               }

               ++this.modCount;
               this.eviction.clear();
               this.count = 0;
            } finally {
               this.unlock();
            }
         }

      }

      private Set attemptEviction(boolean lockedAlready) {
         Set<HashEntry<K, V>> evicted = null;
         boolean obtainedLock = lockedAlready || this.tryLock();
         if (!obtainedLock && this.eviction.thresholdExpired()) {
            this.lock();
            obtainedLock = true;
         }

         if (obtainedLock) {
            try {
               if (this.eviction.thresholdExpired()) {
                  evicted = this.eviction.execute();
               }
            } finally {
               if (!lockedAlready) {
                  this.unlock();
               }

            }
         }

         return evicted;
      }

      private void notifyEvictionListener(Set evicted) {
         if (evicted != null) {
            Map<K, V> evictedCopy;
            if (evicted.size() == 1) {
               HashEntry<K, V> evictedEntry = (HashEntry)evicted.iterator().next();
               evictedCopy = Collections.singletonMap(evictedEntry.key, evictedEntry.value);
            } else {
               evictedCopy = new HashMap(evicted.size());

               for(HashEntry he : evicted) {
                  evictedCopy.put(he.key, he.value);
               }

               evictedCopy = Collections.unmodifiableMap(evictedCopy);
            }

            this.evictionListener.onEntryEviction(evictedCopy);
         }

      }
   }

   abstract class HashIterator {
      int nextSegmentIndex;
      int nextTableIndex;
      HashEntry[] currentTable;
      HashEntry nextEntry;
      HashEntry lastReturned;

      HashIterator() {
         this.nextSegmentIndex = BoundedConcurrentHashMap.this.segments.length - 1;
         this.nextTableIndex = -1;
         this.advance();
      }

      public boolean hasMoreElements() {
         return this.hasNext();
      }

      final void advance() {
         if (this.nextEntry == null || (this.nextEntry = this.nextEntry.next) == null) {
            while(this.nextTableIndex >= 0) {
               if ((this.nextEntry = this.currentTable[this.nextTableIndex--]) != null) {
                  return;
               }
            }

            while(this.nextSegmentIndex >= 0) {
               Segment<K, V> seg = BoundedConcurrentHashMap.this.segments[this.nextSegmentIndex--];
               if (seg.count != 0) {
                  this.currentTable = seg.table;

                  for(int j = this.currentTable.length - 1; j >= 0; --j) {
                     if ((this.nextEntry = this.currentTable[j]) != null) {
                        this.nextTableIndex = j - 1;
                        return;
                     }
                  }
               }
            }

         }
      }

      public boolean hasNext() {
         return this.nextEntry != null;
      }

      HashEntry nextEntry() {
         if (this.nextEntry == null) {
            throw new NoSuchElementException();
         } else {
            this.lastReturned = this.nextEntry;
            this.advance();
            return this.lastReturned;
         }
      }

      public void remove() {
         if (this.lastReturned == null) {
            throw new IllegalStateException();
         } else {
            BoundedConcurrentHashMap.this.remove(this.lastReturned.key);
            this.lastReturned = null;
         }
      }
   }

   final class KeyIterator extends HashIterator implements Iterator, Enumeration {
      public Object next() {
         return super.nextEntry().key;
      }

      public Object nextElement() {
         return super.nextEntry().key;
      }
   }

   final class ValueIterator extends HashIterator implements Iterator, Enumeration {
      public Object next() {
         return super.nextEntry().value;
      }

      public Object nextElement() {
         return super.nextEntry().value;
      }
   }

   final class WriteThroughEntry extends AbstractMap.SimpleEntry {
      private static final long serialVersionUID = -7041346694785573824L;

      WriteThroughEntry(Object k, Object v) {
         super(k, v);
      }

      public Object setValue(Object value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            V v = (V)super.setValue(value);
            BoundedConcurrentHashMap.this.put(this.getKey(), value);
            return v;
         }
      }
   }

   final class EntryIterator extends HashIterator implements Iterator {
      public Map.Entry next() {
         HashEntry<K, V> e = super.nextEntry();
         return BoundedConcurrentHashMap.this.new WriteThroughEntry(e.key, e.value);
      }
   }

   final class KeySet extends AbstractSet {
      public Iterator iterator() {
         return BoundedConcurrentHashMap.this.new KeyIterator();
      }

      public int size() {
         return BoundedConcurrentHashMap.this.size();
      }

      public boolean isEmpty() {
         return BoundedConcurrentHashMap.this.isEmpty();
      }

      public boolean contains(Object o) {
         return BoundedConcurrentHashMap.this.containsKey(o);
      }

      public boolean remove(Object o) {
         return BoundedConcurrentHashMap.this.remove(o) != null;
      }

      public void clear() {
         BoundedConcurrentHashMap.this.clear();
      }
   }

   final class Values extends AbstractCollection {
      public Iterator iterator() {
         return BoundedConcurrentHashMap.this.new ValueIterator();
      }

      public int size() {
         return BoundedConcurrentHashMap.this.size();
      }

      public boolean isEmpty() {
         return BoundedConcurrentHashMap.this.isEmpty();
      }

      public boolean contains(Object o) {
         return BoundedConcurrentHashMap.this.containsValue(o);
      }

      public void clear() {
         BoundedConcurrentHashMap.this.clear();
      }
   }

   final class EntrySet extends AbstractSet {
      public Iterator iterator() {
         return BoundedConcurrentHashMap.this.new EntryIterator();
      }

      public boolean contains(Object o) {
         if (!(o instanceof Map.Entry)) {
            return false;
         } else {
            Map.Entry<?, ?> e = (Map.Entry)o;
            V v = (V)BoundedConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
         }
      }

      public boolean remove(Object o) {
         if (!(o instanceof Map.Entry)) {
            return false;
         } else {
            Map.Entry<?, ?> e = (Map.Entry)o;
            return BoundedConcurrentHashMap.this.remove(e.getKey(), e.getValue());
         }
      }

      public int size() {
         return BoundedConcurrentHashMap.this.size();
      }

      public boolean isEmpty() {
         return BoundedConcurrentHashMap.this.isEmpty();
      }

      public void clear() {
         BoundedConcurrentHashMap.this.clear();
      }
   }

   public interface EvictionListener {
      void onEntryEviction(Map var1);

      void onEntryChosenForEviction(Object var1);
   }

   public interface EvictionPolicy {
      int MAX_BATCH_SIZE = 64;

      HashEntry createNewEntry(Object var1, int var2, HashEntry var3, Object var4);

      Set execute();

      Set onEntryMiss(HashEntry var1);

      boolean onEntryHit(HashEntry var1);

      void onEntryRemove(HashEntry var1);

      void clear();

      Eviction strategy();

      boolean thresholdExpired();
   }
}
