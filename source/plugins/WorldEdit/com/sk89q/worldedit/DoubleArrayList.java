package com.sk89q.worldedit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class DoubleArrayList implements Iterable {
   private List listA = new ArrayList();
   private List listB = new ArrayList();
   private boolean isReversed = false;

   public DoubleArrayList(boolean isReversed) {
      this.isReversed = isReversed;
   }

   public void put(Object a, Object b) {
      this.listA.add(a);
      this.listB.add(b);
   }

   public int size() {
      return this.listA.size();
   }

   public void clear() {
      this.listA.clear();
      this.listB.clear();
   }

   public Iterator iterator() {
      return (Iterator)(this.isReversed ? new ReverseEntryIterator(this.listA.listIterator(this.listA.size()), this.listB.listIterator(this.listB.size())) : new ForwardEntryIterator(this.listA.iterator(), this.listB.iterator()));
   }

   public class ForwardEntryIterator implements Iterator {
      private Iterator keyIterator;
      private Iterator valueIterator;

      public ForwardEntryIterator(Iterator keyIterator, Iterator valueIterator) {
         this.keyIterator = keyIterator;
         this.valueIterator = valueIterator;
      }

      public boolean hasNext() {
         return this.keyIterator.hasNext();
      }

      public Map.Entry next() throws NoSuchElementException {
         return DoubleArrayList.this.new Entry(this.keyIterator.next(), this.valueIterator.next());
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public class ReverseEntryIterator implements Iterator {
      private ListIterator keyIterator;
      private ListIterator valueIterator;

      public ReverseEntryIterator(ListIterator keyIterator, ListIterator valueIterator) {
         this.keyIterator = keyIterator;
         this.valueIterator = valueIterator;
      }

      public boolean hasNext() {
         return this.keyIterator.hasPrevious();
      }

      public Map.Entry next() throws NoSuchElementException {
         return DoubleArrayList.this.new Entry(this.keyIterator.previous(), this.valueIterator.previous());
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public class Entry implements Map.Entry {
      private Object key;
      private Object value;

      private Entry(Object key, Object value) {
         this.key = key;
         this.value = value;
      }

      public Object getKey() {
         return this.key;
      }

      public Object getValue() {
         return this.value;
      }

      public Object setValue(Object value) {
         throw new UnsupportedOperationException();
      }
   }
}
