package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Server;
import org.bukkit.entity.Player;

class NetLoginInjector {
   public static final ReportType REPORT_CANNOT_HOOK_LOGIN_HANDLER = new ReportType("Unable to hook %s.");
   public static final ReportType REPORT_CANNOT_CLEANUP_LOGIN_HANDLER = new ReportType("Cannot cleanup %s.");
   private ConcurrentMap injectedLogins = Maps.newConcurrentMap();
   private ProxyPlayerInjectionHandler injectionHandler;
   private TemporaryPlayerFactory playerFactory = new TemporaryPlayerFactory();
   private ErrorReporter reporter;
   private Server server;

   public NetLoginInjector(ErrorReporter reporter, Server server, ProxyPlayerInjectionHandler injectionHandler) {
      this.reporter = reporter;
      this.server = server;
      this.injectionHandler = injectionHandler;
   }

   public Object onNetLoginCreated(Object inserting) {
      try {
         if (!this.injectionHandler.isInjectionNecessary(GamePhase.LOGIN)) {
            return inserting;
         } else {
            Player temporary = this.playerFactory.createTemporaryPlayer(this.server);
            PlayerInjector injector = this.injectionHandler.injectPlayer(temporary, inserting, PlayerInjectionHandler.ConflictStrategy.BAIL_OUT, GamePhase.LOGIN);
            if (injector != null) {
               TemporaryPlayerFactory.setInjectorInPlayer(temporary, injector);
               injector.updateOnLogin = true;
               this.injectedLogins.putIfAbsent(inserting, injector);
            }

            return inserting;
         }
      } catch (Throwable e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_HOOK_LOGIN_HANDLER).messageParam(MinecraftReflection.getNetLoginHandlerName()).callerParam(inserting, this.injectionHandler).error(e));
         return inserting;
      }
   }

   public synchronized void cleanup(Object removing) {
      PlayerInjector injected = (PlayerInjector)this.injectedLogins.get(removing);
      if (injected != null) {
         try {
            PlayerInjector newInjector = null;
            Player player = injected.getPlayer();
            this.injectedLogins.remove(removing);
            if (injected.isClean()) {
               return;
            }

            newInjector = this.injectionHandler.getInjectorByNetworkHandler(injected.getNetworkManager());
            this.injectionHandler.uninjectPlayer(player);
            if (newInjector != null && injected instanceof NetworkObjectInjector) {
               newInjector.setNetworkManager(injected.getNetworkManager(), true);
            }
         } catch (Throwable e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_CLEANUP_LOGIN_HANDLER).messageParam(MinecraftReflection.getNetLoginHandlerName()).callerParam(removing).error(e));
         }
      }

   }

   public void cleanupAll() {
      for(PlayerInjector injector : this.injectedLogins.values()) {
         injector.cleanupAll();
      }

      this.injectedLogins.clear();
   }
}
