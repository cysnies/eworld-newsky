package org.hibernate.internal.util.collections;

import java.util.Iterator;
import java.util.Map;

public final class LazyIterator implements Iterator {
   private final Map map;
   private Iterator iterator;

   private Iterator getIterator() {
      if (this.iterator == null) {
         this.iterator = this.map.values().iterator();
      }

      return this.iterator;
   }

   public LazyIterator(Map map) {
      this.map = map;
   }

   public boolean hasNext() {
      return this.getIterator().hasNext();
   }

   public Object next() {
      return this.getIterator().next();
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
