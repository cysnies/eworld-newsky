package fr.neatmonster.nocheatplus.event;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public class GenericListener implements Listener, EventExecutor {
   protected final Class clazz;
   protected MethodEntry[] entries = new MethodEntry[0];
   protected final boolean isCancellable;
   protected final EventPriority priority;
   private boolean registered = false;

   public GenericListener(Class clazz, EventPriority priority) {
      this.clazz = clazz;
      this.priority = priority;
      this.isCancellable = clazz.isInstance(Cancellable.class);
   }

   public void execute(Listener listener, Event event) {
      if (this.clazz.isAssignableFrom(event.getClass())) {
         Cancellable cancellable = this.isCancellable ? (Cancellable)event : null;
         MethodEntry[] entries = this.entries;

         for(int i = 0; i < entries.length; ++i) {
            MethodEntry entry = entries[i];

            try {
               if (!this.isCancellable || !entry.ignoreCancelled || !cancellable.isCancelled()) {
                  entry.method.invoke(entry.listener, event);
               }
            } catch (Throwable t) {
               this.onError(entry, event, t);
            }
         }

      }
   }

   private void onError(MethodEntry entry, Event event, Throwable t) {
      String descr = "GenericListener<" + this.clazz.getName() + "> @" + this.priority + " encountered an exception for " + entry.listener.getClass().getName() + " with method " + entry.method.toGenericString();

      try {
         EventException e = new EventException(t, descr);
         if (event.isAsynchronous()) {
            LogUtil.scheduleLogSevere((Throwable)e);
         } else {
            LogUtil.logSevere((Throwable)e);
         }
      } catch (Throwable var6) {
         LogUtil.scheduleLogSevere("Could not log exception: " + descr);
      }

   }

   public void register(Plugin plugin) {
      if (!this.registered) {
         Bukkit.getPluginManager().registerEvent(this.clazz, this, this.priority, this, plugin, false);
         this.registered = true;
      }
   }

   public boolean isRegistered() {
      return this.registered;
   }

   public void addMethodEntry(MethodEntry entry) {
      MethodEntry[] entries = this.entries;
      int insertion = -1;
      if (entry.order != null && entry.order.beforeTag != null) {
         if ("*".equals(entry.order.beforeTag)) {
            insertion = 0;
         } else {
            for(int i = 0; i < entries.length; ++i) {
               MethodEntry other = entries[i];
               if (other.order != null && other.tag.matches(entry.order.beforeTag)) {
                  insertion = i;
                  break;
               }
            }
         }
      }

      MethodEntry[] newEntries;
      if (insertion != entries.length && insertion != -1) {
         newEntries = new MethodEntry[entries.length + 1];

         for(int i = 0; i < newEntries.length; ++i) {
            if (i < insertion) {
               newEntries[i] = entries[i];
            } else if (i == insertion) {
               newEntries[i] = entry;
            } else {
               newEntries[i] = entries[i - 1];
            }
         }
      } else {
         newEntries = (MethodEntry[])Arrays.copyOf(entries, entries.length + 1);
         newEntries[newEntries.length - 1] = entry;
      }

      Arrays.fill(entries, (Object)null);
      this.entries = newEntries;
   }

   public void remove(Listener listener) {
      MethodEntry[] entries = this.entries;
      List<MethodEntry> keep = new ArrayList(entries.length);

      for(MethodEntry entry : entries) {
         if (entry.listener != listener) {
            keep.add(entry);
         }
      }

      if (keep.size() != entries.length) {
         MethodEntry[] newEntries = new MethodEntry[keep.size()];
         keep.toArray(newEntries);
         Arrays.fill(entries, (Object)null);
         this.entries = newEntries;
      }

   }

   public void clear() {
      MethodEntry[] oldEntries = this.entries;
      this.entries = new MethodEntry[0];

      for(int i = 0; i < oldEntries.length; ++i) {
         oldEntries[i] = null;
      }

   }

   public boolean hasListenerMethods() {
      return this.entries.length > 0;
   }

   public static class MethodEntry {
      public final Object listener;
      public final Method method;
      public final boolean ignoreCancelled;
      public final String tag;
      public final MethodOrder order;

      public MethodEntry(Object listener, Method method, boolean ignoreCancelled, String tag, MethodOrder order) {
         this.listener = listener;
         this.method = method;
         this.ignoreCancelled = ignoreCancelled;
         this.tag = tag;
         this.order = order;
      }

      public static class MethodOrder {
         public final String beforeTag;

         public static final MethodOrder getMethodOrder(MethodOrder anno) {
            return anno.beforeTag().isEmpty() ? null : new MethodOrder(anno.beforeTag());
         }

         public MethodOrder(String beforeTag) {
            this.beforeTag = beforeTag;
         }
      }
   }
}
