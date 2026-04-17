package com.comphenix.protocol.wrappers.collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class CachedCollection implements Collection {
   protected Set delegate;
   protected Object[] cache;

   public CachedCollection(Set delegate) {
      this.delegate = (Set)Preconditions.checkNotNull(delegate, "delegate cannot be NULL.");
   }

   private void initializeCache() {
      if (this.cache == null) {
         this.cache = new Object[this.delegate.size()];
      }

   }

   private void growCache() {
      if (this.cache != null) {
         int newLength;
         for(newLength = this.cache.length; newLength < this.delegate.size(); newLength *= 2) {
         }

         if (newLength != this.cache.length) {
            this.cache = Arrays.copyOf(this.cache, newLength);
         }

      }
   }

   public int size() {
      return this.delegate.size();
   }

   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }

   public boolean contains(Object o) {
      return this.delegate.contains(o);
   }

   public Iterator iterator() {
      // $FF: Couldn't be decompiled
   }

   public Object[] toArray() {
      Iterators.size(this.iterator());
      return this.cache.clone();
   }

   public Object[] toArray(Object[] a) {
      Iterators.size(this.iterator());
      return Arrays.copyOf(this.cache, this.size(), a.getClass().getComponentType());
   }

   public boolean add(Object e) {
      boolean result = this.delegate.add(e);
      this.growCache();
      return result;
   }

   public boolean addAll(Collection c) {
      boolean result = this.delegate.addAll(c);
      this.growCache();
      return result;
   }

   public boolean containsAll(Collection c) {
      return this.delegate.containsAll(c);
   }

   public boolean remove(Object o) {
      this.cache = null;
      return this.delegate.remove(o);
   }

   public boolean removeAll(Collection c) {
      this.cache = null;
      return this.delegate.removeAll(c);
   }

   public boolean retainAll(Collection c) {
      this.cache = null;
      return this.delegate.retainAll(c);
   }

   public void clear() {
      this.cache = null;
      this.delegate.clear();
   }

   public int hashCode() {
      int result = 1;

      for(Object element : this) {
         result = 31 * result + (element == null ? 0 : element.hashCode());
      }

      return result;
   }

   public String toString() {
      Iterators.size(this.iterator());
      StringBuilder result = new StringBuilder("[");

      for(Object element : this) {
         if (result.length() > 1) {
            result.append(", ");
         }

         result.append(element);
      }

      return result.append("]").toString();
   }
}
