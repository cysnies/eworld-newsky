package com.earth2me.essentials.spawn;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsSpawn extends JavaPlugin {
   private static final Logger LOGGER = Bukkit.getLogger();
   private transient IEssentials ess;
   private transient SpawnStorage spawns;

   public void onEnable() {
      PluginManager pluginManager = this.getServer().getPluginManager();
      this.ess = (IEssentials)pluginManager.getPlugin("Essentials");
      if (!this.getDescription().getVersion().equals(this.ess.getDescription().getVersion())) {
         LOGGER.log(Level.WARNING, I18n._("versionMismatchAll", new Object[0]));
      }

      if (!this.ess.isEnabled()) {
         this.setEnabled(false);
      } else {
         this.spawns = new SpawnStorage(this.ess);
         this.ess.addReloadListener(this.spawns);
         EssentialsSpawnPlayerListener playerListener = new EssentialsSpawnPlayerListener(this.ess, this.spawns);
         pluginManager.registerEvent(PlayerRespawnEvent.class, playerListener, this.ess.getSettings().getRespawnPriority(), new EventExecutor() {
            public void execute(Listener ll, Event event) throws EventException {
               ((EssentialsSpawnPlayerListener)ll).onPlayerRespawn((PlayerRespawnEvent)event);
            }
         }, this);
         pluginManager.registerEvent(PlayerJoinEvent.class, playerListener, this.ess.getSettings().getRespawnPriority(), new EventExecutor() {
            public void execute(Listener ll, Event event) throws EventException {
               ((EssentialsSpawnPlayerListener)ll).onPlayerJoin((PlayerJoinEvent)event);
            }
         }, this);
      }
   }

   public void onDisable() {
   }

   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      return this.ess.onCommandEssentials(sender, command, commandLabel, args, EssentialsSpawn.class.getClassLoader(), "com.earth2me.essentials.spawn.Command", "essentials.", this.spawns);
   }
}
