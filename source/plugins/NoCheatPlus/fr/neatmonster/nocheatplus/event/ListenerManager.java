package fr.neatmonster.nocheatplus.event;

import fr.neatmonster.nocheatplus.components.ComponentWithName;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ListenerManager {
   protected Map map;
   private final Plugin plugin;
   private boolean registerDirectly;

   public ListenerManager(Plugin plugin) {
      this(plugin, false);
   }

   public ListenerManager(Plugin plugin, boolean registerDirectly) {
      this.map = new HashMap();
      this.plugin = plugin;
      this.registerDirectly = true;
   }

   public GenericListener getListener(Class clazz, EventPriority priority) {
      EnumMap<EventPriority, GenericListener<?>> prioMap = (EnumMap)this.map.get(clazz);
      if (prioMap == null) {
         prioMap = new EnumMap(EventPriority.class);
         this.map.put(clazz, prioMap);
      }

      GenericListener<E> listener = (GenericListener)prioMap.get(priority);
      if (listener == null) {
         listener = new GenericListener(clazz, priority);
         prioMap.put(priority, listener);
      }

      if (this.registerDirectly && !listener.isRegistered()) {
         listener.register(this.plugin);
      }

      return listener;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public boolean isRegisterDirectly() {
      return this.registerDirectly;
   }

   public void setRegisterDirectly(boolean registerDirectly) {
      this.registerDirectly = registerDirectly;
   }

   public void registerAllWithBukkit() {
      for(EnumMap prioMap : this.map.values()) {
         for(GenericListener listener : prioMap.values()) {
            if (!listener.isRegistered()) {
               listener.register(this.plugin);
            }
         }
      }

   }

   public void clear() {
      for(Map prioMap : this.map.values()) {
         for(GenericListener listener : prioMap.values()) {
            listener.clear();
         }
      }

      this.map.clear();
   }

   public void registerAllEventHandlers(Listener listener, String tag) {
      this.registerAllEventHandlers(listener, tag, (GenericListener.MethodEntry.MethodOrder)null);
   }

   public void registerAllEventHandlers(Listener listener, String tag, GenericListener.MethodEntry.MethodOrder order) {
      if (listener instanceof IHaveMethodOrder) {
         order = ((IHaveMethodOrder)listener).getMethodOrder();
      }

      if (listener instanceof ComponentWithName) {
         tag = ((ComponentWithName)listener).getComponentName();
      }

      Class<?> clazz = listener.getClass();
      Set<Method> allMethods = new HashSet();

      for(Method method : clazz.getMethods()) {
         allMethods.add(method);
      }

      for(Method method : clazz.getDeclaredMethods()) {
         allMethods.add(method);
      }

      for(Method method : allMethods) {
         EventHandler anno = (EventHandler)method.getAnnotation(EventHandler.class);
         if (anno != null) {
            if (!method.isAccessible()) {
               try {
                  method.setAccessible(true);
               } catch (SecurityException var16) {
                  LogUtil.logWarning("[ListenerManager]  Can not set method accessible: " + method.toGenericString() + " registered in " + clazz.getName() + ", ignoring it!");
               }
            }

            Class<?>[] argTypes = method.getParameterTypes();
            if (argTypes.length != 1) {
               LogUtil.logWarning("[ListenerManager] Bad method signature (number of arguments not 1): " + method.toGenericString() + " registered in " + clazz.getName() + ", ignoring it!");
            } else {
               Class<?> eventType = argTypes[0];
               if (!Event.class.isAssignableFrom(eventType)) {
                  LogUtil.logWarning("[ListenerManager] Bad method signature (argument does not extend Event): " + method.toGenericString() + " registered in " + clazz.getName() + ", ignoring it!");
               } else {
                  Class<? extends Event> checkedEventType = eventType.asSubclass(Event.class);
                  GenericListener.MethodEntry.MethodOrder tempOrder = order;
                  String tempTag = tag;
                  MethodOrder orderAnno = (MethodOrder)method.getAnnotation(MethodOrder.class);
                  if (orderAnno != null) {
                     GenericListener.MethodEntry.MethodOrder veryTempOrder = tempOrder = GenericListener.MethodEntry.MethodOrder.getMethodOrder(orderAnno);
                     if (veryTempOrder != null) {
                        tempOrder = veryTempOrder;
                     }

                     if (!orderAnno.tag().isEmpty()) {
                        tempTag = orderAnno.tag();
                     }
                  }

                  this.getListener(checkedEventType, anno.priority()).addMethodEntry(new GenericListener.MethodEntry(listener, method, anno.ignoreCancelled(), tempTag, tempOrder));
               }
            }
         }
      }

   }

   public void remove(Listener listener) {
      for(Map prioMap : this.map.values()) {
         for(GenericListener gl : prioMap.values()) {
            gl.remove(listener);
         }
      }

   }

   public boolean hasListeners() {
      return !this.map.isEmpty();
   }

   public boolean hasRegisteredListeners() {
      for(Map prioMap : this.map.values()) {
         for(GenericListener gl : prioMap.values()) {
            if (gl.isRegistered()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasPendingListeners() {
      for(Map prioMap : this.map.values()) {
         for(GenericListener gl : prioMap.values()) {
            if (!gl.isRegistered()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasListenerMethods() {
      for(Map prioMap : this.map.values()) {
         for(GenericListener gl : prioMap.values()) {
            if (gl.hasListenerMethods()) {
               return true;
            }
         }
      }

      return false;
   }
}
