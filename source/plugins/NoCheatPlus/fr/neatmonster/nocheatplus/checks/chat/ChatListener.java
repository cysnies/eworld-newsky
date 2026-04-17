package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class ChatListener extends CheckListener implements INotifyReload, JoinLeaveListener {
   private final Captcha captcha = (Captcha)this.addCheck(new Captcha());
   private final Color color = (Color)this.addCheck(new Color());
   private final Commands commands = (Commands)this.addCheck(new Commands());
   private final Logins logins = (Logins)this.addCheck(new Logins());
   private final Text text = (Text)this.addCheck(new Text());
   private final Relog relog = (Relog)this.addCheck(new Relog());
   private final SimpleCharPrefixTree commandExclusions = new SimpleCharPrefixTree();
   private final SimpleCharPrefixTree chatCommands = new SimpleCharPrefixTree();

   public ChatListener() {
      super(CheckType.CHAT);
      ConfigFile config = ConfigManager.getConfigFile();
      this.initFilters(config);
   }

   private void initFilters(ConfigFile config) {
      this.commandExclusions.clear();
      this.commandExclusions.feedAll(config.getStringList("checks.chat.commands.exclusions"), false, true);
      this.chatCommands.clear();
      this.chatCommands.feedAll(config.getStringList("checks.chat.commands.handleaschat"), false, true);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      TickTask.requestPermissionUpdate(event.getPlayer().getName(), CheckType.CHAT);
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
      if (this.color.isEnabled(player)) {
         event.setMessage(this.color.check(player, event.getMessage(), false));
      }

      if (this.text.isEnabled(player) && this.text.check(player, event.getMessage(), this.captcha, false)) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
      String lcMessage = event.getMessage().trim().toLowerCase();
      String alias = lcMessage.split(" ")[0].substring(1);
      String commandLabel = CommandUtil.getCommandLabel(alias, false);
      ChatConfig cc = ChatConfig.getConfig(player);
      if (cc.protectPlugins && (commandLabel.equals("plugins") || commandLabel.equals("version") || commandLabel.equals("icanhasbukkit")) && !player.hasPermission("nocheatplus.admin.plugins")) {
         player.sendMessage(cc.noCommandPermMessage);
         event.setCancelled(true);
      } else if (!cc.opInConsoleOnly || !commandLabel.equals("op") && !commandLabel.equals("deop")) {
         if (this.color.isEnabled(player)) {
            event.setMessage(this.color.check(player, event.getMessage(), true));
         }

         lcMessage = event.getMessage().trim().toLowerCase();
         boolean handleAsChat = this.chatCommands.hasPrefixWords(lcMessage);
         if (handleAsChat) {
            if (this.text.isEnabled(player) && this.text.check(player, event.getMessage(), this.captcha, true)) {
               event.setCancelled(true);
            }
         } else if (!this.commandExclusions.hasPrefixWords(lcMessage) && this.commands.isEnabled(player) && this.commands.check(player, event.getMessage(), this.captcha)) {
            event.setCancelled(true);
         }

      } else {
         player.sendMessage(ChatColor.RED + "I'm sorry, but this command can't be executed in chat. Use the console instead!");
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      if (event.getResult() == Result.ALLOWED) {
         Player player = event.getPlayer();
         ChatConfig cc = ChatConfig.getConfig(player);
         ChatData data = ChatData.getData(player);
         TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
         TickTask.updatePermissions();
         synchronized(data) {
            this.captcha.resetCaptcha(cc, data);
         }

         if (this.relog.isEnabled(player) && this.relog.unsafeLoginCheck(player, cc, data)) {
            event.disallow(Result.KICK_OTHER, cc.relogKickMessage);
         } else if (this.logins.isEnabled(player) && this.logins.check(player, cc, data)) {
            event.disallow(Result.KICK_OTHER, cc.loginsKickMessage);
         }

      }
   }

   public void onReload() {
      ConfigFile config = ConfigManager.getConfigFile();
      this.initFilters(config);
      this.text.onReload();
      this.logins.onReload();
   }

   public void playerJoins(Player player) {
      ChatConfig cc = ChatConfig.getConfig(player);
      ChatData data = ChatData.getData(player);
      synchronized(data) {
         if (this.captcha.isEnabled(player) && this.captcha.shouldCheckCaptcha(cc, data)) {
            this.captcha.sendNewCaptcha(player, cc, data);
         }

      }
   }

   public void playerLeaves(Player player) {
   }
}
