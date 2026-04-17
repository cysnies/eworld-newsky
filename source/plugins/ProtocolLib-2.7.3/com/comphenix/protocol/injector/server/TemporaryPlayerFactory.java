package com.comphenix.protocol.injector.server;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.CallbackFilter;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.net.sf.cglib.proxy.NoOp;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.FieldAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class TemporaryPlayerFactory {
   private final PacketConstructor chatPacket;
   private static CallbackFilter callbackFilter;

   public TemporaryPlayerFactory() {
      this.chatPacket = PacketConstructor.DEFAULT.withPacket(3, new Object[]{"DEMO"});
   }

   public static SocketInjector getInjectorFromPlayer(Player player) {
      return player instanceof InjectorContainer ? ((InjectorContainer)player).getInjector() : null;
   }

   public static void setInjectorInPlayer(Player player, SocketInjector injector) {
      ((InjectorContainer)player).setInjector(injector);
   }

   public Player createTemporaryPlayer(final Server server) {
      Callback implementation = new MethodInterceptor() {
         public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            String methodName = method.getName();
            SocketInjector injector = ((InjectorContainer)obj).getInjector();
            if (injector == null) {
               throw new IllegalStateException("Unable to find injector.");
            } else if (methodName.equalsIgnoreCase("isOnline")) {
               return injector.getSocket() != null && injector.getSocket().isConnected();
            } else if (methodName.equalsIgnoreCase("getName")) {
               return "UNKNOWN[" + injector.getSocket().getRemoteSocketAddress() + "]";
            } else if (methodName.equalsIgnoreCase("getPlayer")) {
               return injector.getUpdatedPlayer();
            } else if (methodName.equalsIgnoreCase("getAddress")) {
               return injector.getAddress();
            } else if (methodName.equalsIgnoreCase("getServer")) {
               return server;
            } else {
               try {
                  if (methodName.equalsIgnoreCase("chat") || methodName.equalsIgnoreCase("sendMessage")) {
                     Object argument = args[0];
                     if (argument instanceof String) {
                        return TemporaryPlayerFactory.this.sendMessage(injector, (String)argument);
                     }

                     if (argument instanceof String[]) {
                        for(String message : (String[])argument) {
                           TemporaryPlayerFactory.this.sendMessage(injector, message);
                        }

                        return null;
                     }
                  }
               } catch (InvocationTargetException e) {
                  throw e.getCause();
               }

               if (methodName.equalsIgnoreCase("kickPlayer")) {
                  injector.disconnect((String)args[0]);
                  return null;
               } else {
                  throw new UnsupportedOperationException("The method " + method.getName() + " is not supported for temporary players.");
               }
            }
         }
      };
      if (callbackFilter == null) {
         callbackFilter = new CallbackFilter() {
            public int accept(Method method) {
               return !method.getDeclaringClass().equals(Object.class) && !method.getDeclaringClass().equals(InjectorContainer.class) ? 1 : 0;
            }
         };
      }

      Enhancer ex = new Enhancer();
      ex.setSuperclass(InjectorContainer.class);
      ex.setInterfaces(new Class[]{Player.class});
      ex.setCallbacks(new Callback[]{NoOp.INSTANCE, implementation});
      ex.setCallbackFilter(callbackFilter);
      return (Player)ex.create();
   }

   public Player createTemporaryPlayer(Server server, SocketInjector injector) {
      Player temporary = this.createTemporaryPlayer(server);
      ((InjectorContainer)temporary).setInjector(injector);
      return temporary;
   }

   private Object sendMessage(SocketInjector injector, String message) throws InvocationTargetException, FieldAccessException {
      injector.sendServerPacket(this.chatPacket.createPacket(message).getHandle(), (NetworkMarker)null, false);
      return null;
   }
}
