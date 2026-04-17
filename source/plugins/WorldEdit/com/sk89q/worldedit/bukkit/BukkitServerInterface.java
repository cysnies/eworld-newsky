package com.sk89q.worldedit.bukkit;

import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class BukkitServerInterface extends ServerInterface {
   public Server server;
   public WorldEditPlugin plugin;
   private CommandRegistration dynamicCommands;
   private BukkitBiomeTypes biomes;

   public BukkitServerInterface(WorldEditPlugin plugin, Server server) {
      this.plugin = plugin;
      this.server = server;
      this.biomes = new BukkitBiomeTypes();
      this.dynamicCommands = new CommandRegistration(plugin);
   }

   public int resolveItem(String name) {
      Material mat = Material.matchMaterial(name);
      return mat == null ? 0 : mat.getId();
   }

   public boolean isValidMobType(String type) {
      EntityType entityType = EntityType.fromName(type);
      return entityType != null && entityType.isAlive();
   }

   public void reload() {
      this.plugin.loadConfiguration();
   }

   public BiomeTypes getBiomes() {
      return this.biomes;
   }

   public int schedule(long delay, long period, Runnable task) {
      return Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, task, delay, period);
   }

   public List getWorlds() {
      List<World> worlds = this.server.getWorlds();
      List<LocalWorld> ret = new ArrayList(worlds.size());

      for(World world : worlds) {
         ret.add(BukkitUtil.getLocalWorld(world));
      }

      return ret;
   }

   public void onCommandRegistration(List commands, CommandsManager manager) {
      List<CommandInfo> toRegister = new ArrayList();

      for(Command command : commands) {
         List<String> permissions = null;
         Method cmdMethod = (Method)((Map)manager.getMethods().get((Object)null)).get(command.aliases()[0]);
         Map<String, Method> childMethods = (Map)manager.getMethods().get(cmdMethod);
         if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
            permissions = Arrays.asList(((CommandPermissions)cmdMethod.getAnnotation(CommandPermissions.class)).value());
         } else if (cmdMethod != null && childMethods != null && childMethods.size() > 0) {
            permissions = new ArrayList();

            for(Method m : childMethods.values()) {
               if (m.isAnnotationPresent(CommandPermissions.class)) {
                  permissions.addAll(Arrays.asList(((CommandPermissions)m.getAnnotation(CommandPermissions.class)).value()));
               }
            }
         }

         toRegister.add(new CommandInfo(command.usage(), command.desc(), command.aliases(), commands, permissions == null ? null : (String[])permissions.toArray(new String[permissions.size()])));
      }

      this.dynamicCommands.register(toRegister);
   }

   public void unregisterCommands() {
      this.dynamicCommands.unregisterCommands();
   }
}
