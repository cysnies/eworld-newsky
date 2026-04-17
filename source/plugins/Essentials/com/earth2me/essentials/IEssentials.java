package com.earth2me.essentials;

import com.earth2me.essentials.api.IJails;
import com.earth2me.essentials.metrics.Metrics;
import com.earth2me.essentials.perm.PermissionsHandler;
import com.earth2me.essentials.register.payment.Methods;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public interface IEssentials extends Plugin {
   void addReloadListener(IConf var1);

   void reload();

   boolean onCommandEssentials(CommandSender var1, Command var2, String var3, String[] var4, ClassLoader var5, String var6, String var7, IEssentialsModule var8);

   User getUser(Object var1);

   I18n getI18n();

   User getOfflineUser(String var1);

   World getWorld(String var1);

   int broadcastMessage(IUser var1, String var2);

   ISettings getSettings();

   BukkitScheduler getScheduler();

   IJails getJails();

   Warps getWarps();

   Worth getWorth();

   Backup getBackup();

   Methods getPaymentMethod();

   BukkitTask runTaskAsynchronously(Runnable var1);

   BukkitTask runTaskLaterAsynchronously(Runnable var1, long var2);

   int scheduleSyncDelayedTask(Runnable var1);

   int scheduleSyncDelayedTask(Runnable var1, long var2);

   int scheduleSyncRepeatingTask(Runnable var1, long var2, long var4);

   TNTExplodeListener getTNTListener();

   PermissionsHandler getPermissionsHandler();

   AlternativeCommandsHandler getAlternativeCommandsHandler();

   void showError(CommandSender var1, Throwable var2, String var3);

   ItemDb getItemDb();

   UserMap getUserMap();

   Metrics getMetrics();

   void setMetrics(Metrics var1);

   EssentialsTimer getTimer();

   List getVanishedPlayers();
}
