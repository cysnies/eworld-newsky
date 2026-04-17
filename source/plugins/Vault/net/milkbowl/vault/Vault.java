package net.milkbowl.vault;

import com.nijikokun.register.payment.Methods;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.chat.plugins.Chat_DroxPerms;
import net.milkbowl.vault.chat.plugins.Chat_GroupManager;
import net.milkbowl.vault.chat.plugins.Chat_Permissions3;
import net.milkbowl.vault.chat.plugins.Chat_PermissionsEx;
import net.milkbowl.vault.chat.plugins.Chat_bPermissions;
import net.milkbowl.vault.chat.plugins.Chat_bPermissions2;
import net.milkbowl.vault.chat.plugins.Chat_iChat;
import net.milkbowl.vault.chat.plugins.Chat_mChat;
import net.milkbowl.vault.chat.plugins.Chat_mChatSuite;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.plugins.Economy_3co;
import net.milkbowl.vault.economy.plugins.Economy_AEco;
import net.milkbowl.vault.economy.plugins.Economy_BOSE6;
import net.milkbowl.vault.economy.plugins.Economy_BOSE7;
import net.milkbowl.vault.economy.plugins.Economy_CommandsEX;
import net.milkbowl.vault.economy.plugins.Economy_Craftconomy;
import net.milkbowl.vault.economy.plugins.Economy_Craftconomy3;
import net.milkbowl.vault.economy.plugins.Economy_CurrencyCore;
import net.milkbowl.vault.economy.plugins.Economy_Dosh;
import net.milkbowl.vault.economy.plugins.Economy_EconXP;
import net.milkbowl.vault.economy.plugins.Economy_Essentials;
import net.milkbowl.vault.economy.plugins.Economy_GoldIsMoney;
import net.milkbowl.vault.economy.plugins.Economy_GoldIsMoney2;
import net.milkbowl.vault.economy.plugins.Economy_Gringotts;
import net.milkbowl.vault.economy.plugins.Economy_McMoney;
import net.milkbowl.vault.economy.plugins.Economy_MineConomy;
import net.milkbowl.vault.economy.plugins.Economy_MultiCurrency;
import net.milkbowl.vault.economy.plugins.Economy_SDFEconomy;
import net.milkbowl.vault.economy.plugins.Economy_XPBank;
import net.milkbowl.vault.economy.plugins.Economy_eWallet;
import net.milkbowl.vault.economy.plugins.Economy_iConomy4;
import net.milkbowl.vault.economy.plugins.Economy_iConomy5;
import net.milkbowl.vault.economy.plugins.Economy_iConomy6;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.permission.plugins.Permission_DroxPerms;
import net.milkbowl.vault.permission.plugins.Permission_GroupManager;
import net.milkbowl.vault.permission.plugins.Permission_Permissions3;
import net.milkbowl.vault.permission.plugins.Permission_PermissionsBukkit;
import net.milkbowl.vault.permission.plugins.Permission_PermissionsEx;
import net.milkbowl.vault.permission.plugins.Permission_Privileges;
import net.milkbowl.vault.permission.plugins.Permission_SimplyPerms;
import net.milkbowl.vault.permission.plugins.Permission_Starburst;
import net.milkbowl.vault.permission.plugins.Permission_SuperPerms;
import net.milkbowl.vault.permission.plugins.Permission_Xperms;
import net.milkbowl.vault.permission.plugins.Permission_bPermissions;
import net.milkbowl.vault.permission.plugins.Permission_bPermissions2;
import net.milkbowl.vault.permission.plugins.Permission_zPermissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Vault extends JavaPlugin {
   private static final Logger log = Logger.getLogger("Minecraft");
   private Permission perms;
   private double newVersion;
   private double currentVersion;
   private ServicesManager sm;
   private Metrics metrics;

   public void onDisable() {
      this.getServer().getServicesManager().unregisterAll(this);
      Bukkit.getScheduler().cancelTasks(this);
   }

   public void onEnable() {
      this.currentVersion = Double.valueOf(this.getDescription().getVersion().split("-")[0].replaceFirst("\\.", ""));
      this.sm = this.getServer().getServicesManager();
      this.loadEconomy();
      this.loadPermission();
      this.loadChat();
      this.getCommand("vault-info").setExecutor(this);
      this.getCommand("vault-convert").setExecutor(this);
      this.getServer().getPluginManager().registerEvents(new VaultListener(), this);
      this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
         public void run() {
            try {
               Vault.this.newVersion = Vault.this.updateCheck(Vault.this.currentVersion);
               if (Vault.this.newVersion > Vault.this.currentVersion) {
                  Vault.log.warning("Vault " + Vault.this.newVersion + " is out! You are running: Vault " + Vault.this.currentVersion);
                  Vault.log.warning("Update Vault at: http://dev.bukkit.org/server-mods/vault");
               }
            } catch (Exception var2) {
            }

         }
      }, 0L, 432000L);

      try {
         String authors = "";

         for(String author : this.getDescription().getAuthors()) {
            authors = authors + author + ", ";
         }

         if (!authors.isEmpty()) {
            authors = authors.substring(0, authors.length() - 2);
         }

         this.metrics = new Metrics(this.getDescription().getVersion(), authors);
         this.metrics.findCustomData(this);
         this.metrics.beginMeasuringPlugin(this);
      } catch (IOException var4) {
      }

      log.info(String.format("[%s] Enabled Version %s", this.getDescription().getName(), this.getDescription().getVersion()));
   }

   private void loadChat() {
      this.hookChat("PermissionsEx", Chat_PermissionsEx.class, ServicePriority.Highest, "ru.tehkode.permissions.bukkit.PermissionsEx");
      this.hookChat("mChatSuite", Chat_mChatSuite.class, ServicePriority.Highest, "in.mDev.MiracleM4n.mChatSuite.mChatSuite");
      this.hookChat("mChat", Chat_mChat.class, ServicePriority.Highest, "net.D3GN.MiracleM4n.mChat");
      this.hookChat("DroxPerms", Chat_DroxPerms.class, ServicePriority.Lowest, "de.hydrox.bukkit.DroxPerms.DroxPerms");
      this.hookChat("bPermssions2", Chat_bPermissions2.class, ServicePriority.Highest, "de.bananaco.bpermissions.api.ApiLayer");
      this.hookChat("bPermissions", Chat_bPermissions.class, ServicePriority.Normal, "de.bananaco.permissions.info.InfoReader");
      this.hookChat("GroupManager", Chat_GroupManager.class, ServicePriority.Normal, "org.anjocaido.groupmanager.GroupManager");
      this.hookChat("Permissions3", Chat_Permissions3.class, ServicePriority.Normal, "com.nijiko.permissions.ModularControl");
      this.hookChat("iChat", Chat_iChat.class, ServicePriority.Low, "net.TheDgtl.iChat.iChat");
   }

   private void loadEconomy() {
      this.hookEconomy("MultiCurrency", Economy_MultiCurrency.class, ServicePriority.Normal, "me.ashtheking.currency.Currency", "me.ashtheking.currency.CurrencyList");
      this.hookEconomy("MineConomy", Economy_MineConomy.class, ServicePriority.Normal, "me.mjolnir.mineconomy.MineConomy");
      this.hookEconomy("AEco", Economy_AEco.class, ServicePriority.Normal, "org.neocraft.AEco.AEco");
      this.hookEconomy("McMoney", Economy_McMoney.class, ServicePriority.Normal, "boardinggamer.mcmoney.McMoneyAPI");
      this.hookEconomy("CraftConomy", Economy_Craftconomy.class, ServicePriority.Normal, "me.greatman.Craftconomy.Craftconomy");
      this.hookEconomy("CraftConomy3", Economy_Craftconomy3.class, ServicePriority.Normal, "com.greatmancode.craftconomy3.BukkitLoader");
      this.hookEconomy("eWallet", Economy_eWallet.class, ServicePriority.Normal, "me.ethan.eWallet.ECO");
      this.hookEconomy("3co", Economy_3co.class, ServicePriority.Normal, "me.ic3d.eco.ECO");
      this.hookEconomy("BOSEconomy6", Economy_BOSE6.class, ServicePriority.Normal, "cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandManager");
      this.hookEconomy("BOSEconomy7", Economy_BOSE7.class, ServicePriority.Normal, "cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandHandler");
      this.hookEconomy("CurrencyCore", Economy_CurrencyCore.class, ServicePriority.Normal, "is.currency.Currency");
      this.hookEconomy("Gringotts", Economy_Gringotts.class, ServicePriority.Normal, "org.gestern.gringotts.Gringotts");
      this.hookEconomy("Essentials Economy", Economy_Essentials.class, ServicePriority.Low, "com.earth2me.essentials.api.Economy", "com.earth2me.essentials.api.NoLoanPermittedException", "com.earth2me.essentials.api.UserDoesNotExistException");
      this.hookEconomy("iConomy 4", Economy_iConomy4.class, ServicePriority.High, "com.nijiko.coelho.iConomy.iConomy", "com.nijiko.coelho.iConomy.system.Account");
      this.hookEconomy("iConomy 5", Economy_iConomy5.class, ServicePriority.High, "com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings");
      this.hookEconomy("iConomy 6", Economy_iConomy6.class, ServicePriority.High, "com.iCo6.iConomy");
      this.hookEconomy("EconXP", Economy_EconXP.class, ServicePriority.Normal, "ca.agnate.EconXP.EconXP");
      this.hookEconomy("GoldIsMoney", Economy_GoldIsMoney.class, ServicePriority.Normal, "com.flobi.GoldIsMoney.GoldIsMoney");
      this.hookEconomy("GoldIsMoney2", Economy_GoldIsMoney2.class, ServicePriority.Normal, "com.flobi.GoldIsMoney2.GoldIsMoney");
      this.hookEconomy("Dosh", Economy_Dosh.class, ServicePriority.Normal, "com.gravypod.Dosh.Dosh");
      this.hookEconomy("CommandsEX", Economy_CommandsEX.class, ServicePriority.Normal, "com.github.zathrus_writer.commandsex.api.EconomyAPI");
      this.hookEconomy("SDFEconomy", Economy_SDFEconomy.class, ServicePriority.Normal, "com.github.omwah.SDFEconomy.SDFEconomy");
      this.hookEconomy("XPBank", Economy_XPBank.class, ServicePriority.Normal, "com.gmail.mirelatrue.xpbank.XPBank");
   }

   private void loadPermission() {
      this.hookPermission("Starburst", Permission_Starburst.class, ServicePriority.Highest, "com.dthielke.starburst.StarburstPlugin");
      this.hookPermission("PermissionsEx", Permission_PermissionsEx.class, ServicePriority.Highest, "ru.tehkode.permissions.bukkit.PermissionsEx");
      this.hookPermission("PermissionsBukkit", Permission_PermissionsBukkit.class, ServicePriority.Normal, "com.platymuus.bukkit.permissions.PermissionsPlugin");
      this.hookPermission("DroxPerms", Permission_DroxPerms.class, ServicePriority.High, "de.hydrox.bukkit.DroxPerms.DroxPerms");
      this.hookPermission("SimplyPerms", Permission_SimplyPerms.class, ServicePriority.Highest, "net.crystalyx.bukkit.simplyperms.SimplyPlugin");
      this.hookPermission("bPermissions 2", Permission_bPermissions2.class, ServicePriority.Highest, "de.bananaco.bpermissions.api.WorldManager");
      this.hookPermission("zPermissions", Permission_zPermissions.class, ServicePriority.Highest, "org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsPlugin");
      this.hookPermission("Privileges", Permission_Privileges.class, ServicePriority.Highest, "net.krinsoft.privileges.Privileges");
      this.hookPermission("bPermissions", Permission_bPermissions.class, ServicePriority.High, "de.bananaco.permissions.SuperPermissionHandler");
      this.hookPermission("GroupManager", Permission_GroupManager.class, ServicePriority.High, "org.anjocaido.groupmanager.GroupManager");
      this.hookPermission("Permissions 3 (Yeti)", Permission_Permissions3.class, ServicePriority.Normal, "com.nijiko.permissions.ModularControl");
      this.hookPermission("Xperms", Permission_Xperms.class, ServicePriority.Low, "com.github.sebc722.Xperms");
      Permission perms = new Permission_SuperPerms(this);
      this.sm.register(Permission.class, perms, this, ServicePriority.Lowest);
      log.info(String.format("[%s][Permission] SuperPermissions loaded as backup permission system.", this.getDescription().getName()));
      this.perms = (Permission)this.sm.getRegistration(Permission.class).getProvider();
   }

   private void hookChat(String name, Class hookClass, ServicePriority priority, String... packages) {
      try {
         if (packagesExists(packages)) {
            Chat chat = (Chat)hookClass.getConstructor(Plugin.class, Permission.class).newInstance(this, this.perms);
            this.sm.register(Chat.class, chat, this, priority);
            log.info(String.format("[%s][Chat] %s found: %s", this.getDescription().getName(), name, chat.isEnabled() ? "Loaded" : "Waiting"));
         }
      } catch (Exception var6) {
         log.severe(String.format("[%s][Chat] There was an error hooking %s - check to make sure you're using a compatible version!", this.getDescription().getName(), name));
      }

   }

   private void hookEconomy(String name, Class hookClass, ServicePriority priority, String... packages) {
      try {
         if (packagesExists(packages)) {
            Economy econ = (Economy)hookClass.getConstructor(Plugin.class).newInstance(this);
            this.sm.register(Economy.class, econ, this, priority);
            log.info(String.format("[%s][Economy] %s found: %s", this.getDescription().getName(), name, econ.isEnabled() ? "Loaded" : "Waiting"));
         }
      } catch (Exception var6) {
         log.severe(String.format("[%s][Economy] There was an error hooking %s - check to make sure you're using a compatible version!", this.getDescription().getName(), name));
      }

   }

   private void hookPermission(String name, Class hookClass, ServicePriority priority, String... packages) {
      try {
         if (packagesExists(packages)) {
            Permission perms = (Permission)hookClass.getConstructor(Plugin.class).newInstance(this);
            this.sm.register(Permission.class, perms, this, priority);
            log.info(String.format("[%s][Permission] %s found: %s", this.getDescription().getName(), name, perms.isEnabled() ? "Loaded" : "Waiting"));
         }
      } catch (Exception var6) {
         log.severe(String.format("[%s][Permission] There was an error hooking %s - check to make sure you're using a compatible version!", this.getDescription().getName(), name));
      }

   }

   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         if (!p.isOp()) {
            return true;
         }
      }

      if (command.getName().equalsIgnoreCase("vault-info")) {
         this.infoCommand(sender);
         return true;
      } else if (command.getName().equalsIgnoreCase("vault-convert")) {
         this.convertCommand(sender, args);
         return true;
      } else {
         sender.sendMessage("Vault Commands:");
         sender.sendMessage("  /vault-info - Displays information about Vault");
         sender.sendMessage("  /vault-convert [economy1] [economy2] - Converts from one Economy to another");
         return true;
      }
   }

   private void convertCommand(CommandSender sender, String[] args) {
      Collection<RegisteredServiceProvider<Economy>> econs = this.getServer().getServicesManager().getRegistrations(Economy.class);
      if (econs != null && econs.size() >= 2) {
         if (args.length != 2) {
            sender.sendMessage("You must specify only the economy to convert from and the economy to convert to. (names should not contain spaces)");
         } else {
            Economy econ1 = null;
            Economy econ2 = null;

            for(RegisteredServiceProvider econ : econs) {
               String econName = ((Economy)econ.getProvider()).getName().replace(" ", "");
               if (econName.equalsIgnoreCase(args[0])) {
                  econ1 = (Economy)econ.getProvider();
               } else if (econName.equalsIgnoreCase(args[1])) {
                  econ2 = (Economy)econ.getProvider();
               }
            }

            if (econ1 == null) {
               sender.sendMessage("Could not find " + args[0] + " loaded on the server, check your spelling");
            } else if (econ2 == null) {
               sender.sendMessage("Could not find " + args[1] + " loaded on the server, check your spelling");
            } else {
               sender.sendMessage("This may take some time to convert, expect server lag.");

               for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                  String pName = op.getName();
                  if (econ1.hasAccount(pName) && !econ2.hasAccount(pName)) {
                     econ2.createPlayerAccount(pName);
                     econ2.depositPlayer(pName, econ1.getBalance(pName));
                  }
               }

               sender.sendMessage("Converson complete, please verify the data before using it.");
            }
         }
      } else {
         sender.sendMessage("You must have at least 2 economies loaded to convert.");
      }
   }

   private void infoCommand(CommandSender sender) {
      String registeredEcons = null;

      for(RegisteredServiceProvider econ : this.getServer().getServicesManager().getRegistrations(Economy.class)) {
         Economy e = (Economy)econ.getProvider();
         if (registeredEcons == null) {
            registeredEcons = e.getName();
         } else {
            registeredEcons = registeredEcons + ", " + e.getName();
         }
      }

      String registeredPerms = null;

      for(RegisteredServiceProvider perm : this.getServer().getServicesManager().getRegistrations(Permission.class)) {
         Permission p = (Permission)perm.getProvider();
         if (registeredPerms == null) {
            registeredPerms = p.getName();
         } else {
            registeredPerms = registeredPerms + ", " + p.getName();
         }
      }

      String registeredChats = null;

      for(RegisteredServiceProvider chat : this.getServer().getServicesManager().getRegistrations(Chat.class)) {
         Chat c = (Chat)chat.getProvider();
         if (registeredChats == null) {
            registeredChats = c.getName();
         } else {
            registeredChats = registeredChats + ", " + c.getName();
         }
      }

      RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
      Economy econ = null;
      if (rsp != null) {
         econ = (Economy)rsp.getProvider();
      }

      Permission perm = null;
      RegisteredServiceProvider<Permission> rspp = this.getServer().getServicesManager().getRegistration(Permission.class);
      if (rspp != null) {
         perm = (Permission)rspp.getProvider();
      }

      Chat chat = null;
      RegisteredServiceProvider<Chat> rspc = this.getServer().getServicesManager().getRegistration(Chat.class);
      if (rspc != null) {
         chat = (Chat)rspc.getProvider();
      }

      sender.sendMessage(String.format("[%s] Vault v%s Information", this.getDescription().getName(), this.getDescription().getVersion()));
      sender.sendMessage(String.format("[%s] Economy: %s [%s]", this.getDescription().getName(), econ == null ? "None" : econ.getName(), registeredEcons));
      sender.sendMessage(String.format("[%s] Permission: %s [%s]", this.getDescription().getName(), perm == null ? "None" : perm.getName(), registeredPerms));
      sender.sendMessage(String.format("[%s] Chat: %s [%s]", this.getDescription().getName(), chat == null ? "None" : chat.getName(), registeredChats));
   }

   private static boolean packagesExists(String... packages) {
      try {
         for(String pkg : packages) {
            Class.forName(pkg);
         }

         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public double updateCheck(double currentVersion) throws Exception {
      String pluginUrlString = "http://dev.bukkit.org/server-mods/vault/files.rss";

      try {
         URL url = new URL(pluginUrlString);
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
         doc.getDocumentElement().normalize();
         NodeList nodes = doc.getElementsByTagName("item");
         Node firstNode = nodes.item(0);
         if (firstNode.getNodeType() == 1) {
            Element firstElement = (Element)firstNode;
            NodeList firstElementTagName = firstElement.getElementsByTagName("title");
            Element firstNameElement = (Element)firstElementTagName.item(0);
            NodeList firstNodes = firstNameElement.getChildNodes();
            return Double.valueOf(firstNodes.item(0).getNodeValue().replace("Vault", "").replaceFirst(".", "").trim());
         }
      } catch (Exception var12) {
      }

      return currentVersion;
   }

   public class VaultListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         if (Vault.this.perms.has(player, "vault.admin")) {
            try {
               if (Vault.this.newVersion > Vault.this.currentVersion) {
                  player.sendMessage(Vault.this.newVersion + " is out! You are running " + Vault.this.currentVersion);
                  player.sendMessage("Update Vault at: http://dev.bukkit.org/server-mods/vault");
               }
            } catch (Exception var4) {
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("Register") && Vault.packagesExists("com.nijikokun.register.payment.Methods") && !Methods.hasMethod()) {
            try {
               Method m = Methods.class.getMethod("addMethod", Methods.class);
               m.setAccessible(true);
               m.invoke((Object)null, "Vault", new VaultEco());
               if (!Methods.setPreferred("Vault")) {
                  Vault.log.info("Unable to hook register");
               } else {
                  Vault.log.info("[Vault] - Successfully injected Vault methods into Register.");
               }
            } catch (SecurityException var3) {
               Vault.log.info("Unable to hook register");
            } catch (NoSuchMethodException var4) {
               Vault.log.info("Unable to hook register");
            } catch (IllegalArgumentException var5) {
               Vault.log.info("Unable to hook register");
            } catch (IllegalAccessException var6) {
               Vault.log.info("Unable to hook register");
            } catch (InvocationTargetException var7) {
               Vault.log.info("Unable to hook register");
            }
         }

      }
   }
}
