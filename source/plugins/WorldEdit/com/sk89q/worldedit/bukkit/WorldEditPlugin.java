package com.sk89q.worldedit.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditOperation;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.zip.ZipEntry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldEditPlugin extends JavaPlugin {
   public static final String CUI_PLUGIN_CHANNEL = "WECUI";
   private BukkitServerInterface server;
   private WorldEdit controller;
   private WorldEditAPI api;
   private BukkitConfiguration config;

   public void onEnable() {
      String pluginYmlVersion = this.getDescription().getVersion();
      String manifestVersion = WorldEdit.getVersion();
      if (!manifestVersion.equalsIgnoreCase(pluginYmlVersion)) {
         WorldEdit.setVersion(manifestVersion + " (" + pluginYmlVersion + ")");
      }

      this.getDataFolder().mkdirs();
      File targetDir = new File(this.getDataFolder() + File.separator + "nmsblocks");
      targetDir.mkdir();
      this.copyNmsBlockClasses(targetDir);
      this.createDefaultConfiguration("config.yml");
      this.config = new BukkitConfiguration(new YAMLProcessor(new File(this.getDataFolder(), "config.yml"), true), this);
      PermissionsResolverManager.initialize(this);
      this.config.load();
      this.server = new BukkitServerInterface(this, this.getServer());
      this.controller = new WorldEdit(this.server, this.config);
      WorldEdit.logger.setParent(Bukkit.getLogger());
      this.api = new WorldEditAPI(this);
      this.getServer().getMessenger().registerIncomingPluginChannel(this, "WECUI", new CUIChannelListener(this));
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "WECUI");
      this.getServer().getPluginManager().registerEvents(new WorldEditListener(this), this);
      this.getServer().getScheduler().runTaskTimerAsynchronously(this, new SessionTimer(this.controller, this.getServer()), 120L, 120L);
   }

   private void copyNmsBlockClasses(File target) {
      try {
         JarFile jar = new JarFile(this.getFile());
         Enumeration entries = jar.entries();

         while(entries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)entries.nextElement();
            if (jarEntry.getName().startsWith("nmsblocks") && !jarEntry.isDirectory()) {
               File file = new File(target + File.separator + jarEntry.getName().replace("nmsblocks", ""));
               if (!file.exists()) {
                  InputStream is = jar.getInputStream(jarEntry);
                  new FileOutputStream(file);
                  FileOutputStream fos = new FileOutputStream(file);
                  byte[] buf = new byte[8192];
                  int length = 0;

                  while((length = is.read(buf)) > 0) {
                     fos.write(buf, 0, length);
                  }

                  fos.close();
                  is.close();
               }
            }
         }
      } catch (Throwable var10) {
      }

   }

   public void onDisable() {
      this.controller.clearSessions();

      for(Handler h : this.controller.commandLogger.getHandlers()) {
         h.close();
      }

      this.config.unload();
      this.server.unregisterCommands();
      this.getServer().getScheduler().cancelTasks(this);
   }

   protected void loadConfiguration() {
      this.config.unload();
      this.config.load();
      this.getPermissionsResolver().load();
   }

   protected void createDefaultConfiguration(String name) {
      File actual = new File(this.getDataFolder(), name);
      if (!actual.exists()) {
         InputStream input = null;

         try {
            JarFile file = new JarFile(this.getFile());
            ZipEntry copy = file.getEntry("defaults/" + name);
            if (copy == null) {
               throw new FileNotFoundException();
            }

            input = file.getInputStream(copy);
         } catch (IOException var20) {
            this.getLogger().severe("Unable to read default configuration: " + name);
         }

         if (input != null) {
            FileOutputStream output = null;

            try {
               output = new FileOutputStream(actual);
               byte[] buf = new byte[8192];
               int length = 0;

               while((length = input.read(buf)) > 0) {
                  output.write(buf, 0, length);
               }

               this.getLogger().info("Default configuration file written: " + name);
            } catch (IOException e) {
               e.printStackTrace();
            } finally {
               try {
                  if (input != null) {
                     input.close();
                  }
               } catch (IOException var19) {
               }

               try {
                  if (output != null) {
                     output.close();
                  }
               } catch (IOException var18) {
               }

            }
         }
      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      String[] split = new String[args.length + 1];
      System.arraycopy(args, 0, split, 1, args.length);
      split[0] = "/" + cmd.getName();
      this.controller.handleCommand(this.wrapCommandSender(sender), split);
      return true;
   }

   public LocalSession getSession(Player player) {
      return this.controller.getSession((LocalPlayer)this.wrapPlayer(player));
   }

   public EditSession createEditSession(Player player) {
      LocalPlayer wePlayer = this.wrapPlayer(player);
      LocalSession session = this.controller.getSession(wePlayer);
      BlockBag blockBag = session.getBlockBag(wePlayer);
      EditSession editSession = this.controller.getEditSessionFactory().getEditSession(wePlayer.getWorld(), session.getBlockChangeLimit(), blockBag, wePlayer);
      editSession.enableQueue();
      return editSession;
   }

   public void remember(Player player, EditSession editSession) {
      LocalPlayer wePlayer = this.wrapPlayer(player);
      LocalSession session = this.controller.getSession(wePlayer);
      session.remember(editSession);
      editSession.flushQueue();
      this.controller.flushBlockBag(wePlayer, editSession);
   }

   public void perform(Player player, WorldEditOperation op) throws Throwable {
      LocalPlayer wePlayer = this.wrapPlayer(player);
      LocalSession session = this.controller.getSession(wePlayer);
      EditSession editSession = this.createEditSession(player);

      try {
         op.run(session, wePlayer, editSession);
      } finally {
         this.remember(player, editSession);
      }

   }

   /** @deprecated */
   @Deprecated
   public WorldEditAPI getAPI() {
      return this.api;
   }

   public BukkitConfiguration getLocalConfiguration() {
      return this.config;
   }

   public PermissionsResolverManager getPermissionsResolver() {
      return PermissionsResolverManager.getInstance();
   }

   public BukkitPlayer wrapPlayer(Player player) {
      return new BukkitPlayer(this, this.server, player);
   }

   public LocalPlayer wrapCommandSender(CommandSender sender) {
      return (LocalPlayer)(sender instanceof Player ? this.wrapPlayer((Player)sender) : new BukkitCommandSender(this, this.server, sender));
   }

   public ServerInterface getServerInterface() {
      return this.server;
   }

   public WorldEdit getWorldEdit() {
      return this.controller;
   }

   public Selection getSelection(Player player) {
      if (player == null) {
         throw new IllegalArgumentException("Null player not allowed");
      } else if (!player.isOnline()) {
         throw new IllegalArgumentException("Offline player not allowed");
      } else {
         LocalSession session = this.controller.getSession((LocalPlayer)this.wrapPlayer(player));
         RegionSelector selector = session.getRegionSelector(BukkitUtil.getLocalWorld(player.getWorld()));

         try {
            Region region = selector.getRegion();
            World world = ((BukkitWorld)session.getSelectionWorld()).getWorld();
            if (region instanceof CuboidRegion) {
               return new CuboidSelection(world, selector, (CuboidRegion)region);
            } else {
               return region instanceof Polygonal2DRegion ? new Polygonal2DSelection(world, selector, (Polygonal2DRegion)region) : null;
            }
         } catch (IncompleteRegionException var6) {
            return null;
         }
      }
   }

   public void setSelection(Player player, Selection selection) {
      if (player == null) {
         throw new IllegalArgumentException("Null player not allowed");
      } else if (!player.isOnline()) {
         throw new IllegalArgumentException("Offline player not allowed");
      } else if (selection == null) {
         throw new IllegalArgumentException("Null selection not allowed");
      } else {
         LocalSession session = this.controller.getSession((LocalPlayer)this.wrapPlayer(player));
         RegionSelector sel = selection.getRegionSelector();
         session.setRegionSelector(BukkitUtil.getLocalWorld(player.getWorld()), sel);
         session.dispatchCUISelection(this.wrapPlayer(player));
      }
   }
}
