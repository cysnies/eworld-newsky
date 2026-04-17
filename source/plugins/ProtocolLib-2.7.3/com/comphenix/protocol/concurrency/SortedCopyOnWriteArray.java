package com.comphenix.protocol.concurrency;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SortedCopyOnWriteArray implements Iterable, Collection {
   private volatile List list;

   public SortedCopyOnWriteArray() {
      this.list = new ArrayList();
   }

   public SortedCopyOnWriteArray(Collection wrapped) {
      this.list = new ArrayList(wrapped);
   }

   public SortedCopyOnWriteArray(Collection wrapped, boolean sort) {
      this.list = new ArrayList(wrapped);
      if (sort) {
         Collections.sort(this.list);
      }

   }

   public synchronized boolean add(Comparable value) {
      if (value == null) {
         throw new IllegalArgumentException("value cannot be NULL");
      } else {
         List<T> copy = new ArrayList();

         for(Comparable element : this.list) {
            if (value != null && value.compareTo(element) < 0) {
               copy.add(value);
               value = (T)null;
            }

            copy.add(element);
         }

         if (value != null) {
            copy.add(value);
         }

         this.list = copy;
         return true;
      }
   }

   public synchronized boolean addAll(Collection values) {
      if (values == null) {
         throw new IllegalArgumentException("values cannot be NULL");
      } else if (values.size() == 0) {
         return false;
      } else {
         List<T> copy = new ArrayList();
         copy.addAll(this.list);
         copy.addAll(values);
         Collections.sort(copy);
         this.list = copy;
         return true;
      }
   }

   public synchronized boolean remove(Object value) {
      List<T> copy = new ArrayList();

      for(Comparable element : this.list) {
         if (value != null && !Objects.equal(value, element)) {
            copy.add(element);
            value = null;
         }
      }

      this.list = copy;
      return value == null;
   }

   public boolean removeAll(Collection values) {
      if (values == null) {
         throw new IllegalArgumentException("values cannot be NULL");
      } else if (values.size() == 0) {
         return false;
      } else {
         List<T> copy = new ArrayList();
         copy.addAll(this.list);
         copy.removeAll(values);
         this.list = copy;
         return true;
      }
   }

   public boolean retainAll(Collection values) {
      if (values == null) {
         throw new IllegalArgumentException("values cannot be NULL");
      } else if (values.size() == 0) {
         return false;
      } else {
         List<T> copy = new ArrayList();
         copy.addAll(this.list);
         copy.removeAll(values);
         this.list = copy;
         return true;
      }
   }

   public synchronized void remove(int index) {
      List<T> copy = new ArrayList(this.list);
      copy.remove(index);
      this.list = copy;
   }

   public Comparable get(int index) {
      return (Comparable)this.list.get(index);
   }

   public int size() {
      return this.list.size();
   }

   public Iterator iterator() {
      return Iterables.unmodifiableIterable(this.list).iterator();
   }

   public void clear() {
      this.list = new ArrayList();
   }

   public boolean contains(Object value) {
      return this.list.contains(value);
   }

   public boolean containsAll(Collection values) {
      return this.list.containsAll(values);
   }

   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public Object[] toArray() {
      return this.list.toArray();
   }

   public Object[] toArray(Object[] a) {
      return this.list.toArray(a);
   }
}
