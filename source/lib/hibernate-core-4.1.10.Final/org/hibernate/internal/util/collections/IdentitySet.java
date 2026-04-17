package org.hibernate.internal.util.collections;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public class IdentitySet implements Set {
   private static final Object DUMP_VALUE = new Object();
   private final IdentityHashMap map;

   public IdentitySet() {
      this.map = new IdentityHashMap();
   }

   public IdentitySet(int sizing) {
      this.map = new IdentityHashMap(sizing);
   }

   public int size() {
      return this.map.size();
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   public boolean contains(Object o) {
      return this.map.get(o) == DUMP_VALUE;
   }

   public Iterator iterator() {
      return this.map.keySet().iterator();
   }

   public Object[] toArray() {
      return this.map.keySet().toArray();
   }

   public Object[] toArray(Object[] a) {
      return this.map.keySet().toArray(a);
   }

   public boolean add(Object o) {
      return this.map.put(o, DUMP_VALUE) == null;
   }

   public boolean remove(Object o) {
      return this.map.remove(o) == DUMP_VALUE;
   }

   public boolean containsAll(Collection c) {
      Iterator it = c.iterator();

      while(it.hasNext()) {
         if (!this.map.containsKey(it.next())) {
            return false;
         }
      }

      return true;
   }

   public boolean addAll(Collection c) {
      Iterator it = c.iterator();
      boolean changed = false;

      while(it.hasNext()) {
         if (this.add(it.next())) {
            changed = true;
         }
      }

      return changed;
   }

   public boolean retainAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean removeAll(Collection c) {
      Iterator it = c.iterator();
      boolean changed = false;

      while(it.hasNext()) {
         if (this.remove(it.next())) {
            changed = true;
         }
      }

      return changed;
   }

   public void clear() {
      this.map.clear();
   }
}
