package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public abstract class ConvertedList extends ConvertedCollection implements List {
   private List inner;

   public ConvertedList(List inner) {
      super(inner);
      this.inner = inner;
   }

   public void add(int index, Object element) {
      this.inner.add(index, this.toInner(element));
   }

   public boolean addAll(int index, Collection c) {
      return this.inner.addAll(index, this.getInnerCollection(c));
   }

   public Object get(int index) {
      return this.toOuter(this.inner.get(index));
   }

   public int indexOf(Object o) {
      return this.inner.indexOf(this.toInner(o));
   }

   public int lastIndexOf(Object o) {
      return this.inner.lastIndexOf(this.toInner(o));
   }

   public ListIterator listIterator() {
      return this.listIterator(0);
   }

   public ListIterator listIterator(int index) {
      // $FF: Couldn't be decompiled
   }

   public Object remove(int index) {
      return this.toOuter(this.inner.remove(index));
   }

   public Object set(int index, Object element) {
      return this.toOuter(this.inner.set(index, this.toInner(element)));
   }

   public List subList(int fromIndex, int toIndex) {
      // $FF: Couldn't be decompiled
   }

   private ConvertedCollection getInnerCollection(Collection c) {
      // $FF: Couldn't be decompiled
   }
}
