package com.comphenix.protocol.injector;

import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedTracker;
import java.util.Collection;

public final class SortedPacketListenerList extends AbstractConcurrentListenerMultimap {
   private TimedListenerManager timedManager = TimedListenerManager.getInstance();

   public SortedPacketListenerList() {
      super(255);
   }

   public void invokePacketRecieving(ErrorReporter reporter, PacketEvent event) {
      Collection<PrioritizedListener<PacketListener>> list = this.getListener(event.getPacketID());
      if (list != null) {
         if (this.timedManager.isTiming()) {
            for(PrioritizedListener element : list) {
               TimedTracker tracker = this.timedManager.getTracker((PacketListener)element.getListener(), TimedListenerManager.ListenerType.SYNC_CLIENT_SIDE);
               long token = tracker.beginTracking();
               this.invokeReceivingListener(reporter, event, element);
               tracker.endTracking(token, event.getPacketID());
            }
         } else {
            for(PrioritizedListener element : list) {
               this.invokeReceivingListener(reporter, event, element);
            }
         }

      }
   }

   public void invokePacketRecieving(ErrorReporter reporter, PacketEvent event, ListenerPriority priorityFilter) {
      Collection<PrioritizedListener<PacketListener>> list = this.getListener(event.getPacketID());
      if (list != null) {
         if (this.timedManager.isTiming()) {
            for(PrioritizedListener element : list) {
               if (element.getPriority() == priorityFilter) {
                  TimedTracker tracker = this.timedManager.getTracker((PacketListener)element.getListener(), TimedListenerManager.ListenerType.SYNC_CLIENT_SIDE);
                  long token = tracker.beginTracking();
                  this.invokeReceivingListener(reporter, event, element);
                  tracker.endTracking(token, event.getPacketID());
               }
            }
         } else {
            for(PrioritizedListener element : list) {
               if (element.getPriority() == priorityFilter) {
                  this.invokeReceivingListener(reporter, event, element);
               }
            }
         }

      }
   }

   private final void invokeReceivingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener element) {
      try {
         event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
         ((PacketListener)element.getListener()).onPacketReceiving(event);
      } catch (OutOfMemoryError e) {
         throw e;
      } catch (ThreadDeath e) {
         throw e;
      } catch (Throwable e) {
         reporter.reportMinimal(((PacketListener)element.getListener()).getPlugin(), "onPacketReceiving(PacketEvent)", e, event.getPacket().getHandle());
      }

   }

   public void invokePacketSending(ErrorReporter reporter, PacketEvent event) {
      Collection<PrioritizedListener<PacketListener>> list = this.getListener(event.getPacketID());
      if (list != null) {
         if (this.timedManager.isTiming()) {
            for(PrioritizedListener element : list) {
               TimedTracker tracker = this.timedManager.getTracker((PacketListener)element.getListener(), TimedListenerManager.ListenerType.SYNC_SERVER_SIDE);
               long token = tracker.beginTracking();
               this.invokeSendingListener(reporter, event, element);
               tracker.endTracking(token, event.getPacketID());
            }
         } else {
            for(PrioritizedListener element : list) {
               this.invokeSendingListener(reporter, event, element);
            }
         }

      }
   }

   public void invokePacketSending(ErrorReporter reporter, PacketEvent event, ListenerPriority priorityFilter) {
      Collection<PrioritizedListener<PacketListener>> list = this.getListener(event.getPacketID());
      if (list != null) {
         if (this.timedManager.isTiming()) {
            for(PrioritizedListener element : list) {
               if (element.getPriority() == priorityFilter) {
                  TimedTracker tracker = this.timedManager.getTracker((PacketListener)element.getListener(), TimedListenerManager.ListenerType.SYNC_SERVER_SIDE);
                  long token = tracker.beginTracking();
                  this.invokeSendingListener(reporter, event, element);
                  tracker.endTracking(token, event.getPacketID());
               }
            }
         } else {
            for(PrioritizedListener element : list) {
               if (element.getPriority() == priorityFilter) {
                  this.invokeSendingListener(reporter, event, element);
               }
            }
         }

      }
   }

   private final void invokeSendingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener element) {
      try {
         event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
         ((PacketListener)element.getListener()).onPacketSending(event);
      } catch (OutOfMemoryError e) {
         throw e;
      } catch (ThreadDeath e) {
         throw e;
      } catch (Throwable e) {
         reporter.reportMinimal(((PacketListener)element.getListener()).getPlugin(), "onPacketSending(PacketEvent)", e, event.getPacket().getHandle());
      }

   }
}
