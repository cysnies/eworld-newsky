package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.error.ErrorReporter;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public abstract class AbstractInputStreamLookup {
   protected final ErrorReporter reporter;
   protected final Server server;

   protected AbstractInputStreamLookup(ErrorReporter reporter, Server server) {
      this.reporter = reporter;
      this.server = server;
   }

   public abstract void inject(Object var1);

   public abstract SocketInjector waitSocketInjector(InputStream var1);

   public abstract SocketInjector waitSocketInjector(Socket var1);

   public abstract SocketInjector waitSocketInjector(SocketAddress var1);

   public abstract SocketInjector peekSocketInjector(SocketAddress var1);

   public abstract void setSocketInjector(SocketAddress var1, SocketInjector var2);

   protected void onPreviousSocketOverwritten(SocketInjector previous, SocketInjector current) {
      Player player = previous.getPlayer();
      if (player instanceof InjectorContainer) {
         TemporaryPlayerFactory.setInjectorInPlayer(player, current);
      }

   }

   public abstract void cleanupAll();
}
