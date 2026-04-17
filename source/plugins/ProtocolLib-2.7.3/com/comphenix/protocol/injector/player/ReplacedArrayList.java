package com.comphenix.protocol.injector.player;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ReplacedArrayList extends ArrayList {
   private static final long serialVersionUID = 1008492765999744804L;
   private BiMap replaceMap = HashBiMap.create();
   private List underlyingList;

   public ReplacedArrayList(List underlyingList) {
      this.underlyingList = underlyingList;
   }

   protected void onReplacing(Object inserting, Object replacement) {
   }

   protected void onInserting(Object inserting) {
   }

   protected void onRemoved(Object removing) {
   }

   public boolean add(Object element) {
      this.onInserting(element);
      if (this.replaceMap.containsKey(element)) {
         TKey replacement = (TKey)this.replaceMap.get(element);
         this.onReplacing(element, replacement);
         return this.delegate().add(replacement);
      } else {
         return this.delegate().add(element);
      }
   }

   public void add(int index, Object element) {
      this.onInserting(element);
      if (this.replaceMap.containsKey(element)) {
         TKey replacement = (TKey)this.replaceMap.get(element);
         this.onReplacing(element, replacement);
         this.delegate().add(index, replacement);
      } else {
         this.delegate().add(index, element);
      }

   }

   public boolean addAll(Collection collection) {
      int oldSize = this.size();

      for(Object element : collection) {
         this.add(element);
      }

      return this.size() != oldSize;
   }

   public boolean addAll(int index, Collection elements) {
      int oldSize = this.size();

      for(Object element : elements) {
         this.add(index++, element);
      }

      return this.size() != oldSize;
   }

   public boolean remove(Object object) {
      boolean success = this.delegate().remove(object);
      if (success) {
         this.onRemoved(object);
      }

      return success;
   }

   public Object remove(int index) {
      TKey removed = (TKey)this.delegate().remove(index);
      if (removed != null) {
         this.onRemoved(removed);
      }

      return removed;
   }

   public boolean removeAll(Collection collection) {
      int oldSize = this.size();

      for(Object element : collection) {
         this.remove(element);
      }

      return this.size() != oldSize;
   }

   protected List delegate() {
      return this.underlyingList;
   }

   public void clear() {
      for(Object element : this.delegate()) {
         this.onRemoved(element);
      }

      this.delegate().clear();
   }

   public boolean contains(Object o) {
      return this.delegate().contains(o);
   }

   public boolean containsAll(Collection c) {
      return this.delegate().containsAll(c);
   }

   public Object get(int index) {
      return this.delegate().get(index);
   }

   public int indexOf(Object o) {
      return this.delegate().indexOf(o);
   }

   public boolean isEmpty() {
      return this.delegate().isEmpty();
   }

   public Iterator iterator() {
      return this.delegate().iterator();
   }

   public int lastIndexOf(Object o) {
      return this.delegate().lastIndexOf(o);
   }

   public ListIterator listIterator() {
      return this.delegate().listIterator();
   }

   public ListIterator listIterator(int index) {
      return this.delegate().listIterator(index);
   }

   public boolean retainAll(Collection c) {
      int oldSize = this.size();
      Iterator<TKey> it = this.delegate().iterator();

      while(it.hasNext()) {
         TKey current = (TKey)it.next();
         if (!c.contains(current)) {
            it.remove();
            this.onRemoved(current);
         }
      }

      return this.size() != oldSize;
   }

   public Object set(int index, Object element) {
      if (this.replaceMap.containsKey(element)) {
         TKey replacement = (TKey)this.replaceMap.get(element);
         this.onReplacing(element, replacement);
         return this.delegate().set(index, replacement);
      } else {
         return this.delegate().set(index, element);
      }
   }

   public int size() {
      return this.delegate().size();
   }

   public List subList(int fromIndex, int toIndex) {
      return this.delegate().subList(fromIndex, toIndex);
   }

   public Object[] toArray() {
      return this.delegate().toArray();
   }

   public Object[] toArray(Object[] a) {
      return this.delegate().toArray(a);
   }

   public synchronized void addMapping(Object target, Object replacement) {
      this.addMapping(target, replacement, false);
   }

   public Object getMapping(Object target) {
      return this.replaceMap.get(target);
   }

   public synchronized void addMapping(Object target, Object replacement, boolean ignoreExisting) {
      this.replaceMap.put(target, replacement);
      if (!ignoreExisting) {
         this.replaceAll(target, replacement);
      }

   }

   public synchronized Object removeMapping(Object target) {
      if (this.replaceMap.containsKey(target)) {
         TKey replacement = (TKey)this.replaceMap.get(target);
         this.replaceMap.remove(target);
         this.replaceAll(replacement, target);
         return replacement;
      } else {
         return null;
      }
   }

   public synchronized Object swapMapping(Object target) {
      TKey replacement = (TKey)this.removeMapping(target);
      if (replacement != null) {
         this.replaceMap.put(replacement, target);
      }

      return replacement;
   }

   public synchronized void replaceAll(Object find, Object replace) {
      for(int i = 0; i < this.underlyingList.size(); ++i) {
         if (Objects.equal(this.underlyingList.get(i), find)) {
            this.onReplacing(find, replace);
            this.underlyingList.set(i, replace);
         }
      }

   }

   public synchronized void revertAll() {
      if (this.replaceMap.size() >= 1) {
         BiMap<TKey, TKey> inverse = this.replaceMap.inverse();

         for(int i = 0; i < this.underlyingList.size(); ++i) {
            TKey replaced = (TKey)this.underlyingList.get(i);
            if (inverse.containsKey(replaced)) {
               TKey original = (TKey)inverse.get(replaced);
               this.onReplacing(replaced, original);
               this.underlyingList.set(i, original);
            }
         }

         this.replaceMap.clear();
      }
   }

   protected void finalize() throws Throwable {
      this.revertAll();
      super.finalize();
   }
}
