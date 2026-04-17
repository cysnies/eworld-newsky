package com.comphenix.protocol.concurrency;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IntegerSet {
   private final boolean[] array;

   public IntegerSet(int maximumCount) {
      this.array = new boolean[maximumCount];
   }

   public IntegerSet(int maximumCount, Collection values) {
      this.array = new boolean[maximumCount];
      this.addAll(values);
   }

   public boolean contains(int element) {
      return this.array[element];
   }

   public void add(int element) {
      this.array[element] = true;
   }

   public void addAll(Collection packets) {
      for(Integer id : packets) {
         this.add(id);
      }

   }

   public void remove(int element) {
      if (element >= 0 && element < this.array.length) {
         this.array[element] = false;
      }

   }

   public void clear() {
      Arrays.fill(this.array, false);
   }

   public Set toSet() {
      Set<Integer> elements = new HashSet();

      for(int i = 0; i < this.array.length; ++i) {
         if (this.array[i]) {
            elements.add(i);
         }
      }

      return elements;
   }
}
