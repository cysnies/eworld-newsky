package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.register.payment.Method;
import com.earth2me.essentials.register.payment.Methods;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class User extends UserData implements Comparable, IReplyTo, IUser {
   private CommandSender replyTo = null;
   private transient User teleportRequester;
   private transient boolean teleportRequestHere;
   private transient boolean vanished;
   private final transient Teleport teleport;
   private transient long teleportRequestTime;
   private transient long lastOnlineActivity;
   private transient long lastThrottledAction;
   private transient long lastActivity = System.currentTimeMillis();
   private boolean hidden = false;
   private boolean rightClickJump = false;
   private transient Location afkPosition = null;
   private boolean invSee = false;
   private boolean recipeSee = false;
   private boolean enderSee = false;
   private static final Logger logger = Logger.getLogger("Minecraft");
   private transient long teleportInvulnerabilityTimestamp = 0L;

   User(Player base, IEssentials ess) {
      super(base, ess);
      this.teleport = new Teleport(this, ess);
      if (this.isAfk()) {
         this.afkPosition = this.getLocation();
      }

   }

   User update(Player base) {
      this.setBase(base);
      return this;
   }

   public boolean isAuthorized(IEssentialsCommand cmd) {
      return this.isAuthorized(cmd, "essentials.");
   }

   public boolean isAuthorized(IEssentialsCommand cmd, String permissionPrefix) {
      return this.isAuthorized(permissionPrefix + (cmd.getName().equals("r") ? "msg" : cmd.getName()));
   }

   public boolean isAuthorized(String node) {
      boolean result = this.isAuthorizedCheck(node);
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "checking if " + this.base.getName() + " has " + node + " - " + result);
      }

      return result;
   }

   private boolean isAuthorizedCheck(String node) {
      if (this.base instanceof OfflinePlayer) {
         return false;
      } else {
         try {
            return this.ess.getPermissionsHandler().hasPermission(this.base, node);
         } catch (Exception ex) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
               this.ess.getLogger().log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
         }
      }
   }

   public void healCooldown() throws Exception {
      Calendar now = new GregorianCalendar();
      if (this.getLastHealTimestamp() > 0L) {
         double cooldown = this.ess.getSettings().getHealCooldown();
         Calendar cooldownTime = new GregorianCalendar();
         cooldownTime.setTimeInMillis(this.getLastHealTimestamp());
         cooldownTime.add(13, (int)cooldown);
         cooldownTime.add(14, (int)(cooldown * (double)1000.0F % (double)1000.0F));
         if (cooldownTime.after(now) && !this.isAuthorized("essentials.heal.cooldown.bypass")) {
            throw new Exception(I18n._("timeBeforeHeal", Util.formatDateDiff(cooldownTime.getTimeInMillis())));
         }
      }

      this.setLastHealTimestamp(now.getTimeInMillis());
   }

   public void giveMoney(double value) {
      this.giveMoney(value, (CommandSender)null);
   }

   public void giveMoney(double value, CommandSender initiator) {
      if (value != (double)0.0F) {
         this.setMoney(this.getMoney() + value);
         this.sendMessage(I18n._("addedToAccount", Util.displayCurrency(value, this.ess)));
         if (initiator != null) {
            initiator.sendMessage(I18n._("addedToOthersAccount", Util.displayCurrency(value, this.ess), this.getDisplayName(), Util.displayCurrency(this.getMoney(), this.ess)));
         }

      }
   }

   public void payUser(User reciever, double value) throws Exception {
      if (value != (double)0.0F) {
         if (this.canAfford(value)) {
            this.setMoney(this.getMoney() - value);
            reciever.setMoney(reciever.getMoney() + value);
            this.sendMessage(I18n._("moneySentTo", Util.displayCurrency(value, this.ess), reciever.getDisplayName()));
            reciever.sendMessage(I18n._("moneyRecievedFrom", Util.displayCurrency(value, this.ess), this.getDisplayName()));
         } else {
            throw new Exception(I18n._("notEnoughMoney"));
         }
      }
   }

   public void takeMoney(double value) {
      this.takeMoney(value, (CommandSender)null);
   }

   public void takeMoney(double value, CommandSender initiator) {
      if (value != (double)0.0F) {
         this.setMoney(this.getMoney() - value);
         this.sendMessage(I18n._("takenFromAccount", Util.displayCurrency(value, this.ess)));
         if (initiator != null) {
            initiator.sendMessage(I18n._("takenFromOthersAccount", Util.displayCurrency(value, this.ess), this.getDisplayName(), Util.displayCurrency(this.getMoney(), this.ess)));
         }

      }
   }

   public boolean canAfford(double cost) {
      return this.canAfford(cost, true);
   }

   public boolean canAfford(double cost, boolean permcheck) {
      if (cost <= (double)0.0F) {
         return true;
      } else {
         double mon = this.getMoney();
         if (permcheck && !this.isAuthorized("essentials.eco.loan")) {
            return cost <= mon;
         } else {
            return mon - cost >= this.ess.getSettings().getMinMoney();
         }
      }
   }

   public void dispose() {
      this.base = new OfflinePlayer(this.getName(), this.ess);
   }

   public void setReplyTo(CommandSender user) {
      this.replyTo = user;
   }

   public CommandSender getReplyTo() {
      return this.replyTo;
   }

   public int compareTo(User other) {
      return Util.stripFormat(this.getDisplayName()).compareToIgnoreCase(Util.stripFormat(other.getDisplayName()));
   }

   public boolean equals(Object object) {
      return !(object instanceof User) ? false : this.getName().equalsIgnoreCase(((User)object).getName());
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public Boolean canSpawnItem(int itemId) {
      return !this.ess.getSettings().itemSpawnBlacklist().contains(itemId);
   }

   public Location getHome() throws Exception {
      return this.getHome((String)this.getHomes().get(0));
   }

   public void setLastLocation() {
      this.setLastLocation(this.getLocation());
   }

   public void setLogoutLocation() {
      this.setLogoutLocation(this.getLocation());
   }

   public void requestTeleport(User player, boolean here) {
      this.teleportRequestTime = System.currentTimeMillis();
      this.teleportRequester = player;
      this.teleportRequestHere = here;
   }

   public User getTeleportRequest() {
      return this.teleportRequester;
   }

   public boolean isTpRequestHere() {
      return this.teleportRequestHere;
   }

   public String getNick(boolean longnick) {
      StringBuilder prefix = new StringBuilder();
      String suffix = "";
      String nick = this.getNickname();
      String nickname;
      if (!this.ess.getSettings().isCommandDisabled("nick") && nick != null && !nick.isEmpty() && !nick.equalsIgnoreCase(this.getName())) {
         nickname = this.ess.getSettings().getNicknamePrefix() + nick;
      } else {
         nickname = this.getName();
      }

      if (this.isOp()) {
         try {
            ChatColor opPrefix = this.ess.getSettings().getOperatorColor();
            if (opPrefix != null && opPrefix.toString().length() > 0) {
               prefix.insert(0, opPrefix.toString());
               suffix = "§r";
            }
         } catch (Exception var8) {
         }
      }

      if (this.ess.getSettings().addPrefixSuffix()) {
         if (!this.ess.getSettings().disablePrefix()) {
            String ptext = this.ess.getPermissionsHandler().getPrefix(this.base).replace('&', '§');
            prefix.insert(0, ptext);
            suffix = "§r";
         }

         if (!this.ess.getSettings().disableSuffix()) {
            String stext = this.ess.getPermissionsHandler().getSuffix(this.base).replace('&', '§');
            suffix = stext + "§r";
            suffix = suffix.replace("§f§f", "§f").replace("§f§r", "§r").replace("§r§r", "§r");
         }
      }

      String strPrefix = prefix.toString();
      String output = strPrefix + nickname + suffix;
      if (!longnick && output.length() > 16) {
         output = strPrefix + nickname;
      }

      if (!longnick && output.length() > 16) {
         output = Util.lastCode(strPrefix) + nickname;
      }

      if (!longnick && output.length() > 16) {
         output = Util.lastCode(strPrefix) + nickname.substring(0, 14);
      }

      if (output.charAt(output.length() - 1) == 167) {
         output = output.substring(0, output.length() - 1);
      }

      return output;
   }

   public void setDisplayNick() {
      if (this.base.isOnline() && this.ess.getSettings().changeDisplayName()) {
         this.setDisplayName(this.getNick(true));
         if (this.ess.getSettings().changePlayerListName()) {
            String name = this.getNick(false);

            try {
               this.setPlayerListName(name);
            } catch (IllegalArgumentException var3) {
               if (this.ess.getSettings().isDebug()) {
                  logger.log(Level.INFO, "Playerlist for " + name + " was not updated. Name clashed with another online player.");
               }
            }
         }
      }

   }

   public String getDisplayName() {
      return super.getDisplayName() == null ? super.getName() : super.getDisplayName();
   }

   public Teleport getTeleport() {
      return this.teleport;
   }

   public long getLastOnlineActivity() {
      return this.lastOnlineActivity;
   }

   public void setLastOnlineActivity(long timestamp) {
      this.lastOnlineActivity = timestamp;
   }

   public double getMoney() {
      this.ess.getPaymentMethod();
      if (Methods.hasMethod()) {
         try {
            this.ess.getPaymentMethod();
            Method method = Methods.getMethod();
            if (!method.hasAccount(this.getName())) {
               throw new Exception();
            }

            this.ess.getPaymentMethod();
            Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
            return account.balance();
         } catch (Throwable var3) {
         }
      }

      return super.getMoney();
   }

   public void setMoney(double value) {
      this.ess.getPaymentMethod();
      if (Methods.hasMethod()) {
         try {
            this.ess.getPaymentMethod();
            Method method = Methods.getMethod();
            if (!method.hasAccount(this.getName())) {
               throw new Exception();
            }

            this.ess.getPaymentMethod();
            Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
            account.set(value);
         } catch (Throwable var5) {
         }
      }

      super.setMoney(value);
      Trade.log("Update", "Set", "API", this.getName(), new Trade(value, this.ess), (String)null, (Trade)null, (Location)null, this.ess);
   }

   public void updateMoneyCache(double value) {
      this.ess.getPaymentMethod();
      if (Methods.hasMethod() && super.getMoney() != value) {
         super.setMoney(value);
      }

   }

   public void setAfk(boolean set) {
      this.setSleepingIgnored(this.isAuthorized("essentials.sleepingignored") ? true : set);
      if (set && !this.isAfk()) {
         this.afkPosition = this.getLocation();
      } else if (!set && this.isAfk()) {
         this.afkPosition = null;
      }

      super.setAfk(set);
   }

   public boolean toggleAfk() {
      boolean now = super.toggleAfk();
      this.setSleepingIgnored(this.isAuthorized("essentials.sleepingignored") ? true : now);
      return now;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
      if (hidden) {
         this.setLastLogout(this.getLastOnlineActivity());
      }

   }

   public boolean checkJailTimeout(long currentTime) {
      if (this.getJailTimeout() > 0L && this.getJailTimeout() < currentTime && this.isJailed()) {
         this.setJailTimeout(0L);
         this.setJailed(false);
         this.sendMessage(I18n._("haveBeenReleased"));
         this.setJail((String)null);

         try {
            this.getTeleport().back();
         } catch (Exception var6) {
            try {
               this.getTeleport().respawn((Trade)null, TeleportCause.PLUGIN);
            } catch (Exception var5) {
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean checkMuteTimeout(long currentTime) {
      if (this.getMuteTimeout() > 0L && this.getMuteTimeout() < currentTime && this.isMuted()) {
         this.setMuteTimeout(0L);
         this.sendMessage(I18n._("canTalkAgain"));
         this.setMuted(false);
         return true;
      } else {
         return false;
      }
   }

   public boolean checkBanTimeout(long currentTime) {
      if (this.getBanTimeout() > 0L && this.getBanTimeout() < currentTime && this.isBanned()) {
         this.setBanTimeout(0L);
         this.setBanned(false);
         return true;
      } else {
         return false;
      }
   }

   public void updateActivity(boolean broadcast) {
      if (this.isAfk() && this.ess.getSettings().cancelAfkOnInteract()) {
         this.setAfk(false);
         if (broadcast && !this.isHidden()) {
            this.setDisplayNick();
            String msg = I18n._("userIsNotAway", this.getDisplayName());
            if (!msg.isEmpty()) {
               this.ess.broadcastMessage(this, msg);
            }
         }
      }

      this.lastActivity = System.currentTimeMillis();
   }

   public void checkActivity() {
      long autoafkkick = this.ess.getSettings().getAutoAfkKick();
      if (autoafkkick > 0L && this.lastActivity > 0L && this.lastActivity + autoafkkick * 1000L < System.currentTimeMillis() && !this.isHidden() && !this.isAuthorized("essentials.kick.exempt") && !this.isAuthorized("essentials.afk.kickexempt")) {
         String kickReason = I18n._("autoAfkKickReason", (double)autoafkkick / (double)60.0F);
         this.lastActivity = 0L;
         this.kickPlayer(kickReason);

         for(Player player : this.ess.getServer().getOnlinePlayers()) {
            User user = this.ess.getUser(player);
            if (user.isAuthorized("essentials.kick.notify")) {
               player.sendMessage(I18n._("playerKicked", "Console", this.getName(), kickReason));
            }
         }
      }

      long autoafk = this.ess.getSettings().getAutoAfk();
      if (!this.isAfk() && autoafk > 0L && this.lastActivity + autoafk * 1000L < System.currentTimeMillis() && this.isAuthorized("essentials.afk.auto")) {
         this.setAfk(true);
         if (!this.isHidden()) {
            this.setDisplayNick();
            String msg = I18n._("userIsAway", this.getDisplayName());
            if (!msg.isEmpty()) {
               this.ess.broadcastMessage(this, msg);
            }
         }
      }

   }

   public Location getAfkPosition() {
      return this.afkPosition;
   }

   public boolean isGodModeEnabled() {
      return super.isGodModeEnabled() && !this.ess.getSettings().getNoGodWorlds().contains(this.getLocation().getWorld().getName()) || this.isAfk() && this.ess.getSettings().getFreezeAfkPlayers();
   }

   public boolean isGodModeEnabledRaw() {
      return super.isGodModeEnabled();
   }

   public String getGroup() {
      return this.ess.getPermissionsHandler().getGroup(this.base);
   }

   public boolean inGroup(String group) {
      return this.ess.getPermissionsHandler().inGroup(this.base, group);
   }

   public boolean canBuild() {
      return this.isOp() ? true : this.ess.getPermissionsHandler().canBuild(this.base, this.getGroup());
   }

   public long getTeleportRequestTime() {
      return this.teleportRequestTime;
   }

   public boolean isInvSee() {
      return this.invSee;
   }

   public void setInvSee(boolean set) {
      this.invSee = set;
   }

   public boolean isEnderSee() {
      return this.enderSee;
   }

   public void setEnderSee(boolean set) {
      this.enderSee = set;
   }

   public void enableInvulnerabilityAfterTeleport() {
      long time = this.ess.getSettings().getTeleportInvulnerability();
      if (time > 0L) {
         this.teleportInvulnerabilityTimestamp = System.currentTimeMillis() + time;
      }

   }

   public void resetInvulnerabilityAfterTeleport() {
      if (this.teleportInvulnerabilityTimestamp != 0L && this.teleportInvulnerabilityTimestamp < System.currentTimeMillis()) {
         this.teleportInvulnerabilityTimestamp = 0L;
      }

   }

   public boolean hasInvulnerabilityAfterTeleport() {
      return this.teleportInvulnerabilityTimestamp != 0L && this.teleportInvulnerabilityTimestamp >= System.currentTimeMillis();
   }

   public boolean isVanished() {
      return this.vanished;
   }

   public void setVanished(boolean set) {
      this.vanished = set;
      if (set) {
         for(Player p : this.ess.getServer().getOnlinePlayers()) {
            if (!this.ess.getUser(p).isAuthorized("essentials.vanish.see")) {
               p.hidePlayer(this.getBase());
            }
         }

         this.setHidden(true);
         this.ess.getVanishedPlayers().add(this.getName());
         if (this.isAuthorized("essentials.vanish.effect")) {
            this.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false));
         }
      } else {
         for(Player p : this.ess.getServer().getOnlinePlayers()) {
            p.showPlayer(this.getBase());
         }

         this.setHidden(false);
         this.ess.getVanishedPlayers().remove(this.getName());
         if (this.isAuthorized("essentials.vanish.effect")) {
            this.removePotionEffect(PotionEffectType.INVISIBILITY);
         }
      }

   }

   public void toggleVanished() {
      boolean set = !this.vanished;
      this.setVanished(set);
   }

   public boolean checkSignThrottle() {
      if (this.isSignThrottled()) {
         return true;
      } else {
         this.updateThrottle();
         return false;
      }
   }

   public boolean isSignThrottled() {
      long minTime = this.lastThrottledAction + (long)(1000 / this.ess.getSettings().getSignUsePerSecond());
      return System.currentTimeMillis() < minTime;
   }

   public void updateThrottle() {
      this.lastThrottledAction = System.currentTimeMillis();
   }

   public boolean isFlyClickJump() {
      return this.rightClickJump;
   }

   public void setRightClickJump(boolean rightClickJump) {
      this.rightClickJump = rightClickJump;
   }

   public boolean isIgnoreExempt() {
      return this.isAuthorized("essentials.chat.ignoreexempt");
   }

   public boolean isRecipeSee() {
      return this.recipeSee;
   }

   public void setRecipeSee(boolean recipeSee) {
      this.recipeSee = recipeSee;
   }
}
