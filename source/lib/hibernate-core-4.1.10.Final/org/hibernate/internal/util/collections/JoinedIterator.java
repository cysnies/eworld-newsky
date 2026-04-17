package org.hibernate.internal.util.collections;

import java.util.Iterator;
import java.util.List;

public class JoinedIterator implements Iterator {
   private static final Iterator[] ITERATORS = new Iterator[0];
   private Iterator[] iterators;
   private int currentIteratorIndex;
   private Iterator currentIterator;
   private Iterator lastUsedIterator;

   public JoinedIterator(List iterators) {
      this((Iterator[])iterators.toArray(ITERATORS));
   }

   public JoinedIterator(Iterator[] iterators) {
      if (iterators == null) {
         throw new NullPointerException("Unexpected NULL iterators argument");
      } else {
         this.iterators = iterators;
      }
   }

   public JoinedIterator(Iterator first, Iterator second) {
      this(new Iterator[]{first, second});
   }

   public boolean hasNext() {
      this.updateCurrentIterator();
      return this.currentIterator.hasNext();
   }

   public Object next() {
      this.updateCurrentIterator();
      return this.currentIterator.next();
   }

   public void remove() {
      this.updateCurrentIterator();
      this.lastUsedIterator.remove();
   }

   protected void updateCurrentIterator() {
      if (this.currentIterator == null) {
         if (this.iterators.length == 0) {
            this.currentIterator = EmptyIterator.INSTANCE;
         } else {
            this.currentIterator = this.iterators[0];
         }

         this.lastUsedIterator = this.currentIterator;
      }

      while(!this.currentIterator.hasNext() && this.currentIteratorIndex < this.iterators.length - 1) {
         ++this.currentIteratorIndex;
         this.currentIterator = this.iterators[this.currentIteratorIndex];
      }

   }
}
