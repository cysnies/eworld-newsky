package com.comphenix.executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class BukkitFutures {
   private static Listener EMPTY_LISTENER = new Listener() {
   };

   public static ListenableFuture nextEvent(Plugin plugin, Class eventClass) {
      return nextEvent(plugin, eventClass, EventPriority.NORMAL, false);
   }

   public static ListenableFuture nextEvent(Plugin plugin, Class eventClass, EventPriority priority, boolean ignoreCancelled) {
      final HandlerList list = getHandlerList(eventClass);
      final SettableFuture<TEvent> future = SettableFuture.create();
      EventExecutor executor = new EventExecutor() {
         private final AtomicBoolean once = new AtomicBoolean();

         public void execute(Listener listener, Event event) throws EventException {
            if (!future.isCancelled() && !this.once.getAndSet(true)) {
               future.set(event);
            }

         }
      };
      RegisteredListener listener = new RegisteredListener(EMPTY_LISTENER, executor, priority, plugin, ignoreCancelled) {
         public void callEvent(Event event) throws EventException {
            super.callEvent(event);
            list.unregister(this);
         }
      };
      PluginDisabledListener.getListener(plugin).addFuture(future);
      list.register(listener);
      return future;
   }

   public static void registerEventExecutor(Plugin plugin, Class eventClass, EventPriority priority, EventExecutor executor) {
      getHandlerList(eventClass).register(new RegisteredListener(EMPTY_LISTENER, executor, priority, plugin, false));
   }

   private static HandlerList getHandlerList(Class clazz) {
      while(clazz.getSuperclass() != null && Event.class.isAssignableFrom(clazz.getSuperclass())) {
         try {
            Method method = clazz.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList)method.invoke((Object)null);
         } catch (NoSuchMethodException var2) {
            clazz = clazz.getSuperclass().asSubclass(Event.class);
         } catch (Exception e) {
            throw new IllegalPluginAccessException(e.getMessage());
         }
      }

      throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
   }
}
