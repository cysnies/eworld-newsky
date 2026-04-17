package com.comphenix.protocol.wrappers.collection;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class ConvertedCollection extends AbstractConverted implements Collection {
   private Collection inner;

   public ConvertedCollection(Collection inner) {
      this.inner = inner;
   }

   public boolean add(Object e) {
      return this.inner.add(this.toInner(e));
   }

   public boolean addAll(Collection c) {
      boolean modified = false;

      for(Object outer : c) {
         modified |= this.add(outer);
      }

      return modified;
   }

   public void clear() {
      this.inner.clear();
   }

   public boolean contains(Object o) {
      return this.inner.contains(this.toInner(o));
   }

   public boolean containsAll(Collection c) {
      for(Object outer : c) {
         if (!this.contains(outer)) {
            return false;
         }
      }

      return true;
   }

   public boolean isEmpty() {
      return this.inner.isEmpty();
   }

   public Iterator iterator() {
      return Iterators.transform(this.inner.iterator(), this.getOuterConverter());
   }

   public boolean remove(Object o) {
      return this.inner.remove(this.toInner(o));
   }

   public boolean removeAll(Collection c) {
      boolean modified = false;

      for(Object outer : c) {
         modified |= this.remove(outer);
      }

      return modified;
   }

   public boolean retainAll(Collection c) {
      List<VInner> innerCopy = Lists.newArrayList();

      for(Object outer : c) {
         innerCopy.add(this.toInner(outer));
      }

      return this.inner.retainAll(innerCopy);
   }

   public int size() {
      return this.inner.size();
   }

   public Object[] toArray() {
      Object[] array = this.inner.toArray();

      for(int i = 0; i < array.length; ++i) {
         array[i] = this.toOuter(array[i]);
      }

      return array;
   }

   public Object[] toArray(Object[] a) {
      T[] array = a;
      int index = 0;
      if (a.length < this.size()) {
         array = (T[])((Object[])((Object[])Array.newInstance(a.getClass().getComponentType(), this.size())));
      }

      for(Object innerValue : this.inner) {
         array[index++] = this.toOuter(innerValue);
      }

      return array;
   }
}
