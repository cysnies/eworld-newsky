package com.comphenix.protocol.injector;

import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

public class PrioritizedListener implements Comparable {
   private Object listener;
   private ListenerPriority priority;

   public PrioritizedListener(Object listener, ListenerPriority priority) {
      this.listener = listener;
      this.priority = priority;
   }

   public int compareTo(PrioritizedListener other) {
      return Ints.compare(this.getPriority().getSlot(), other.getPriority().getSlot());
   }

   public boolean equals(Object obj) {
      if (obj instanceof PrioritizedListener) {
         PrioritizedListener<TListener> other = (PrioritizedListener)obj;
         return Objects.equal(this.listener, other.listener);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.listener});
   }

   public Object getListener() {
      return this.listener;
   }

   public ListenerPriority getPriority() {
      return this.priority;
   }
}
