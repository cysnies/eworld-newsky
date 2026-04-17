package org.hibernate.internal.util.collections;

import java.util.Iterator;

public final class SingletonIterator implements Iterator {
   private Object value;
   private boolean hasNext = true;

   public boolean hasNext() {
      return this.hasNext;
   }

   public Object next() {
      if (this.hasNext) {
         this.hasNext = false;
         return this.value;
      } else {
         throw new IllegalStateException();
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   public SingletonIterator(Object value) {
      this.value = value;
   }
}
