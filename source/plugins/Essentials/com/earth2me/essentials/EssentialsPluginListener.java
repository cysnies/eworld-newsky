package com.earth2me.essentials;

import com.earth2me.essentials.register.payment.Methods;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class EssentialsPluginListener implements Listener, IConf {
   private final transient IEssentials ess;

   public EssentialsPluginListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginEnable(PluginEnableEvent event) {
      if (event.getPlugin().getName().equals("EssentialsChat")) {
         this.ess.getSettings().setEssentialsChatActive(true);
      }

      this.ess.getPermissionsHandler().checkPermissions();
      this.ess.getAlternativeCommandsHandler().addPlugin(event.getPlugin());
      this.ess.getPaymentMethod();
      if (!Methods.hasMethod()) {
         this.ess.getPaymentMethod();
         if (Methods.setMethod(this.ess.getServer().getPluginManager())) {
            Logger var10000 = this.ess.getLogger();
            Level var10001 = Level.INFO;
            StringBuilder var10002 = (new StringBuilder()).append("Payment method found (");
            this.ess.getPaymentMethod();
            var10002 = var10002.append(Methods.getMethod().getLongName()).append(" version: ");
            this.ess.getPaymentMethod();
            var10000.log(var10001, var10002.append(Methods.getMethod().getVersion()).append(")").toString());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin().getName().equals("EssentialsChat")) {
         this.ess.getSettings().setEssentialsChatActive(false);
      }

      this.ess.getPermissionsHandler().checkPermissions();
      this.ess.getAlternativeCommandsHandler().removePlugin(event.getPlugin());
      if (this.ess.getPaymentMethod() != null) {
         this.ess.getPaymentMethod();
         if (Methods.hasMethod()) {
            this.ess.getPaymentMethod();
            if (Methods.checkDisabled(event.getPlugin())) {
               this.ess.getPaymentMethod();
               Methods.reset();
               this.ess.getLogger().log(Level.INFO, "Payment method was disabled. No longer accepting payments.");
            }
         }
      }

   }

   public void reloadConfig() {
      this.ess.getPermissionsHandler().setUseSuperperms(this.ess.getSettings().useBukkitPermissions());
      this.ess.getPermissionsHandler().checkPermissions();
   }
}
