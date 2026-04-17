package com.comphenix.protocol.async;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import javax.annotation.Nullable;

class Synchronization {
   public static Queue queue(Queue queue, @Nullable Object mutex) {
      return (Queue)(queue instanceof SynchronizedQueue ? queue : new SynchronizedQueue(queue, mutex));
   }

   private static class SynchronizedObject implements Serializable {
      private static final long serialVersionUID = -4408866092364554628L;
      final Object delegate;
      final Object mutex;

      SynchronizedObject(Object delegate, @Nullable Object mutex) {
         this.delegate = Preconditions.checkNotNull(delegate);
         this.mutex = mutex == null ? this : mutex;
      }

      Object delegate() {
         return this.delegate;
      }

      public String toString() {
         synchronized(this.mutex) {
            return this.delegate.toString();
         }
      }
   }

   private static class SynchronizedCollection extends SynchronizedObject implements Collection {
      private static final long serialVersionUID = 5440572373531285692L;

      private SynchronizedCollection(Collection delegate, @Nullable Object mutex) {
         super(delegate, mutex);
      }

      Collection delegate() {
         return (Collection)super.delegate();
      }

      public boolean add(Object e) {
         synchronized(this.mutex) {
            return this.delegate().add(e);
         }
      }

      public boolean addAll(Collection c) {
         synchronized(this.mutex) {
            return this.delegate().addAll(c);
         }
      }

      public void clear() {
         synchronized(this.mutex) {
            this.delegate().clear();
         }
      }

      public boolean contains(Object o) {
         synchronized(this.mutex) {
            return this.delegate().contains(o);
         }
      }

      public boolean containsAll(Collection c) {
         synchronized(this.mutex) {
            return this.delegate().containsAll(c);
         }
      }

      public boolean isEmpty() {
         synchronized(this.mutex) {
            return this.delegate().isEmpty();
         }
      }

      public Iterator iterator() {
         return this.delegate().iterator();
      }

      public boolean remove(Object o) {
         synchronized(this.mutex) {
            return this.delegate().remove(o);
         }
      }

      public boolean removeAll(Collection c) {
         synchronized(this.mutex) {
            return this.delegate().removeAll(c);
         }
      }

      public boolean retainAll(Collection c) {
         synchronized(this.mutex) {
            return this.delegate().retainAll(c);
         }
      }

      public int size() {
         synchronized(this.mutex) {
            return this.delegate().size();
         }
      }

      public Object[] toArray() {
         synchronized(this.mutex) {
            return this.delegate().toArray();
         }
      }

      public Object[] toArray(Object[] a) {
         synchronized(this.mutex) {
            return this.delegate().toArray(a);
         }
      }
   }

   private static class SynchronizedQueue extends SynchronizedCollection implements Queue {
      private static final long serialVersionUID = 1961791630386791902L;

      SynchronizedQueue(Queue delegate, @Nullable Object mutex) {
         super(delegate, mutex, null);
      }

      Queue delegate() {
         return (Queue)super.delegate();
      }

      public Object element() {
         synchronized(this.mutex) {
            return this.delegate().element();
         }
      }

      public boolean offer(Object e) {
         synchronized(this.mutex) {
            return this.delegate().offer(e);
         }
      }

      public Object peek() {
         synchronized(this.mutex) {
            return this.delegate().peek();
         }
      }

      public Object poll() {
         synchronized(this.mutex) {
            return this.delegate().poll();
         }
      }

      public Object remove() {
         synchronized(this.mutex) {
            return this.delegate().remove();
         }
      }
   }
}
