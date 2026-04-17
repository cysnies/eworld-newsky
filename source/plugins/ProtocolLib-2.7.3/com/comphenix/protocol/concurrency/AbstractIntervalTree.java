package com.comphenix.protocol.concurrency;

import com.google.common.base.Objects;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public abstract class AbstractIntervalTree {
   protected NavigableMap bounds = new TreeMap();

   public Set remove(Comparable lowerBound, Comparable upperBound) {
      return this.remove(lowerBound, upperBound, false);
   }

   public Set remove(Comparable lowerBound, Comparable upperBound, boolean preserveDifference) {
      this.checkBounds(lowerBound, upperBound);
      NavigableMap<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> range = this.bounds.subMap(lowerBound, true, upperBound, true);
      AbstractIntervalTree<TKey, TValue>.EndPoint first = this.getNextEndPoint(lowerBound, true);
      AbstractIntervalTree<TKey, TValue>.EndPoint last = this.getPreviousEndPoint(upperBound, true);
      AbstractIntervalTree<TKey, TValue>.EndPoint previous = null;
      AbstractIntervalTree<TKey, TValue>.EndPoint next = null;
      Set<AbstractIntervalTree<TKey, TValue>.Entry> resized = new HashSet();
      Set<AbstractIntervalTree<TKey, TValue>.Entry> removed = new HashSet();
      if (first != null && first.state == AbstractIntervalTree.State.CLOSE) {
         previous = this.getPreviousEndPoint(first.key, false);
         if (previous != null) {
            removed.add(this.getEntry(previous, first));
         }
      }

      if (last != null && last.state == AbstractIntervalTree.State.OPEN) {
         next = this.getNextEndPoint(last.key, false);
         if (next != null) {
            removed.add(this.getEntry(last, next));
         }
      }

      this.removeEntrySafely(previous, first);
      this.removeEntrySafely(last, next);
      if (preserveDifference) {
         if (previous != null) {
            resized.add(this.putUnsafe(previous.key, this.decrementKey(lowerBound), previous.value));
         }

         if (next != null) {
            resized.add(this.putUnsafe(this.incrementKey(upperBound), next.key, next.value));
         }
      }

      this.getEntries(removed, range);
      this.invokeEntryRemoved(removed);
      if (preserveDifference) {
         this.invokeEntryAdded(resized);
      }

      range.clear();
      return removed;
   }

   protected Entry getEntry(EndPoint left, EndPoint right) {
      if (left == null) {
         throw new IllegalArgumentException("left endpoint cannot be NULL.");
      } else if (right == null) {
         throw new IllegalArgumentException("right endpoint cannot be NULL.");
      } else {
         return right.key.compareTo(left.key) < 0 ? this.getEntry(right, left) : new Entry(left, right);
      }
   }

   private void removeEntrySafely(EndPoint left, EndPoint right) {
      if (left != null && right != null) {
         this.bounds.remove(left.key);
         this.bounds.remove(right.key);
      }

   }

   protected EndPoint addEndPoint(Comparable key, Object value, State state) {
      AbstractIntervalTree<TKey, TValue>.EndPoint endPoint = (EndPoint)this.bounds.get(key);
      if (endPoint != null) {
         endPoint.state = AbstractIntervalTree.State.BOTH;
      } else {
         endPoint = new EndPoint(state, key, value);
         this.bounds.put(key, endPoint);
      }

      return endPoint;
   }

   public void put(Comparable lowerBound, Comparable upperBound, Object value) {
      this.remove(lowerBound, upperBound, true);
      this.invokeEntryAdded(this.putUnsafe(lowerBound, upperBound, value));
   }

   private Entry putUnsafe(Comparable lowerBound, Comparable upperBound, Object value) {
      if (value != null) {
         AbstractIntervalTree<TKey, TValue>.EndPoint left = this.addEndPoint(lowerBound, value, AbstractIntervalTree.State.OPEN);
         AbstractIntervalTree<TKey, TValue>.EndPoint right = this.addEndPoint(upperBound, value, AbstractIntervalTree.State.CLOSE);
         return new Entry(left, right);
      } else {
         return null;
      }
   }

   private void checkBounds(Comparable lowerBound, Comparable upperBound) {
      if (lowerBound == null) {
         throw new IllegalAccessError("lowerbound cannot be NULL.");
      } else if (upperBound == null) {
         throw new IllegalAccessError("upperBound cannot be NULL.");
      } else if (upperBound.compareTo(lowerBound) < 0) {
         throw new IllegalArgumentException("upperBound cannot be less than lowerBound.");
      }
   }

   public boolean containsKey(Comparable key) {
      return this.getEndPoint(key) != null;
   }

   public Set entrySet() {
      Set<AbstractIntervalTree<TKey, TValue>.Entry> result = new HashSet();
      this.getEntries(result, this.bounds);
      return result;
   }

   public void clear() {
      if (!this.bounds.isEmpty()) {
         this.remove((Comparable)this.bounds.firstKey(), (Comparable)this.bounds.lastKey());
      }

   }

   private void getEntries(Set destination, NavigableMap map) {
      Map.Entry<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> last = null;

      for(Map.Entry entry : map.entrySet()) {
         switch (((EndPoint)entry.getValue()).state) {
            case BOTH:
               AbstractIntervalTree<TKey, TValue>.EndPoint point = (EndPoint)entry.getValue();
               destination.add(new Entry(point, point));
               break;
            case CLOSE:
               if (last != null) {
                  destination.add(new Entry((EndPoint)last.getValue(), (EndPoint)entry.getValue()));
               }
               break;
            case OPEN:
               last = entry;
               break;
            default:
               throw new IllegalStateException("Illegal open/close state detected.");
         }
      }

   }

   public void putAll(AbstractIntervalTree other) {
      for(Entry entry : other.entrySet()) {
         this.put(entry.left.key, entry.right.key, entry.getValue());
      }

   }

   public Object get(Comparable key) {
      AbstractIntervalTree<TKey, TValue>.EndPoint point = this.getEndPoint(key);
      return point != null ? point.value : null;
   }

   protected EndPoint getEndPoint(Comparable key) {
      AbstractIntervalTree<TKey, TValue>.EndPoint ends = (EndPoint)this.bounds.get(key);
      if (ends != null) {
         if (ends.state == AbstractIntervalTree.State.CLOSE) {
            Map.Entry<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> left = this.bounds.floorEntry(this.decrementKey(key));
            return left != null ? (EndPoint)left.getValue() : null;
         } else {
            return ends;
         }
      } else {
         Map.Entry<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> left = this.bounds.floorEntry(key);
         return left != null && ((EndPoint)left.getValue()).state == AbstractIntervalTree.State.OPEN ? (EndPoint)left.getValue() : null;
      }
   }

   protected EndPoint getPreviousEndPoint(Comparable point, boolean inclusive) {
      if (point != null) {
         Map.Entry<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> previous = this.bounds.floorEntry(inclusive ? point : this.decrementKey(point));
         if (previous != null) {
            return (EndPoint)previous.getValue();
         }
      }

      return null;
   }

   protected EndPoint getNextEndPoint(Comparable point, boolean inclusive) {
      if (point != null) {
         Map.Entry<TKey, AbstractIntervalTree<TKey, TValue>.EndPoint> next = this.bounds.ceilingEntry(inclusive ? point : this.incrementKey(point));
         if (next != null) {
            return (EndPoint)next.getValue();
         }
      }

      return null;
   }

   private void invokeEntryAdded(Entry added) {
      if (added != null) {
         this.onEntryAdded(added);
      }

   }

   private void invokeEntryAdded(Set added) {
      for(Entry entry : added) {
         this.onEntryAdded(entry);
      }

   }

   private void invokeEntryRemoved(Set removed) {
      for(Entry entry : removed) {
         this.onEntryRemoved(entry);
      }

   }

   protected void onEntryAdded(Entry added) {
   }

   protected void onEntryRemoved(Entry removed) {
   }

   protected abstract Comparable decrementKey(Comparable var1);

   protected abstract Comparable incrementKey(Comparable var1);

   protected static enum State {
      OPEN,
      CLOSE,
      BOTH;
   }

   public class Entry implements Map.Entry {
      private EndPoint left;
      private EndPoint right;

      Entry(EndPoint left, EndPoint right) {
         if (left == null) {
            throw new IllegalAccessError("left cannot be NUll");
         } else if (right == null) {
            throw new IllegalAccessError("right cannot be NUll");
         } else if (left.key.compareTo(right.key) > 0) {
            throw new IllegalArgumentException("Left key (" + left.key + ") cannot be greater than the right key (" + right.key + ")");
         } else {
            this.left = left;
            this.right = right;
         }
      }

      public Range getKey() {
         return Ranges.closed(this.left.key, this.right.key);
      }

      public Object getValue() {
         return this.left.value;
      }

      public Object setValue(Object value) {
         TValue old = (TValue)this.left.value;
         this.left.value = value;
         this.right.value = value;
         return old;
      }

      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof Entry)) {
            return false;
         } else {
            return Objects.equal(this.left.key, ((Entry)obj).left.key) && Objects.equal(this.right.key, ((Entry)obj).right.key) && Objects.equal(this.left.value, ((Entry)obj).left.value);
         }
      }

      public int hashCode() {
         return Objects.hashCode(new Object[]{this.left.key, this.right.key, this.left.value});
      }

      public String toString() {
         return String.format("Value %s at [%s, %s]", this.left.value, this.left.key, this.right.key);
      }
   }

   protected class EndPoint {
      public State state;
      public Object value;
      public Comparable key;

      public EndPoint(State state, Comparable key, Object value) {
         this.state = state;
         this.key = key;
         this.value = value;
      }
   }
}
