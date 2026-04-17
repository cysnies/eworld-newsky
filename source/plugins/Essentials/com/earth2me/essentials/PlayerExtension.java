package com.earth2me.essentials;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayerExtension implements Player {
   protected Player base;

   public PlayerExtension(Player base) {
      this.base = base;
   }

   public final Player getBase() {
      return this.base;
   }

   public final Player setBase(Player base) {
      return this.base = base;
   }

   public String getDisplayName() {
      return this.base.getDisplayName();
   }

   public void setDisplayName(String arg0) {
      this.base.setDisplayName(arg0);
   }

   public String getPlayerListName() {
      return this.base.getPlayerListName();
   }

   public void setPlayerListName(String arg0) {
      this.base.setPlayerListName(arg0);
   }

   public void setCompassTarget(Location arg0) {
      this.base.setCompassTarget(arg0);
   }

   public Location getCompassTarget() {
      return this.base.getCompassTarget();
   }

   public InetSocketAddress getAddress() {
      return this.base.getAddress();
   }

   public void sendRawMessage(String arg0) {
      this.base.sendRawMessage(arg0);
   }

   public void kickPlayer(String arg0) {
      this.base.kickPlayer(arg0);
   }

   public void chat(String arg0) {
      this.base.chat(arg0);
   }

   public boolean performCommand(String arg0) {
      return this.base.performCommand(arg0);
   }

   public boolean isSneaking() {
      return this.base.isSneaking();
   }

   public void setSneaking(boolean arg0) {
      this.base.setSneaking(arg0);
   }

   public boolean isSprinting() {
      return this.base.isSprinting();
   }

   public void setSprinting(boolean arg0) {
      this.base.setSprinting(arg0);
   }

   public void saveData() {
      this.base.saveData();
   }

   public void loadData() {
      this.base.loadData();
   }

   public void setSleepingIgnored(boolean arg0) {
      this.base.setSleepingIgnored(arg0);
   }

   public boolean isSleepingIgnored() {
      return this.base.isSleepingIgnored();
   }

   public void playNote(Location arg0, byte arg1, byte arg2) {
      this.base.playNote(arg0, arg1, arg2);
   }

   public void playNote(Location arg0, Instrument arg1, Note arg2) {
      this.base.playNote(arg0, arg1, arg2);
   }

   public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
      this.base.playSound(arg0, arg1, arg2, arg3);
   }

   public void playEffect(Location arg0, Effect arg1, int arg2) {
      this.base.playEffect(arg0, arg1, arg2);
   }

   public void playEffect(Location arg0, Effect arg1, Object arg2) {
      this.base.playEffect(arg0, arg1, arg2);
   }

   public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
      this.base.sendBlockChange(arg0, arg1, arg2);
   }

   public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
      return this.base.sendChunkChange(arg0, arg1, arg2, arg3, arg4);
   }

   public void sendBlockChange(Location arg0, int arg1, byte arg2) {
      this.base.sendBlockChange(arg0, arg1, arg2);
   }

   public void sendMap(MapView arg0) {
      this.base.sendMap(arg0);
   }

   /** @deprecated */
   @Deprecated
   public void updateInventory() {
      this.base.updateInventory();
   }

   public void awardAchievement(Achievement arg0) {
      this.base.awardAchievement(arg0);
   }

   public void incrementStatistic(Statistic arg0) {
      this.base.incrementStatistic(arg0);
   }

   public void incrementStatistic(Statistic arg0, int arg1) {
      this.base.incrementStatistic(arg0, arg1);
   }

   public void incrementStatistic(Statistic arg0, Material arg1) {
      this.base.incrementStatistic(arg0, arg1);
   }

   public void incrementStatistic(Statistic arg0, Material arg1, int arg2) {
      this.base.incrementStatistic(arg0, arg1, arg2);
   }

   public void setPlayerTime(long arg0, boolean arg1) {
      this.base.setPlayerTime(arg0, arg1);
   }

   public long getPlayerTime() {
      return this.base.getPlayerTime();
   }

   public long getPlayerTimeOffset() {
      return this.base.getPlayerTimeOffset();
   }

   public boolean isPlayerTimeRelative() {
      return this.base.isPlayerTimeRelative();
   }

   public void resetPlayerTime() {
      this.base.resetPlayerTime();
   }

   public void setPlayerWeather(WeatherType arg0) {
      this.base.setPlayerWeather(arg0);
   }

   public WeatherType getPlayerWeather() {
      return this.base.getPlayerWeather();
   }

   public void resetPlayerWeather() {
      this.base.resetPlayerWeather();
   }

   public void giveExp(int arg0) {
      this.base.giveExp(arg0);
   }

   public void giveExpLevels(int arg0) {
      this.base.giveExpLevels(arg0);
   }

   public float getExp() {
      return this.base.getExp();
   }

   public void setExp(float arg0) {
      this.base.setExp(arg0);
   }

   public int getLevel() {
      return this.base.getLevel();
   }

   public void setLevel(int arg0) {
      this.base.setLevel(arg0);
   }

   public int getTotalExperience() {
      return this.base.getTotalExperience();
   }

   public void setTotalExperience(int arg0) {
      this.base.setTotalExperience(arg0);
   }

   public float getExhaustion() {
      return this.base.getExhaustion();
   }

   public void setExhaustion(float arg0) {
      this.base.setExhaustion(arg0);
   }

   public float getSaturation() {
      return this.base.getSaturation();
   }

   public void setSaturation(float arg0) {
      this.base.setSaturation(arg0);
   }

   public int getFoodLevel() {
      return this.base.getFoodLevel();
   }

   public void setFoodLevel(int arg0) {
      this.base.setFoodLevel(arg0);
   }

   public Location getBedSpawnLocation() {
      return this.base.getBedSpawnLocation();
   }

   public void setBedSpawnLocation(Location arg0) {
      this.base.setBedSpawnLocation(arg0);
   }

   public void setBedSpawnLocation(Location arg0, boolean arg1) {
      this.base.setBedSpawnLocation(arg0, arg1);
   }

   public boolean getAllowFlight() {
      return this.base.getAllowFlight();
   }

   public void setAllowFlight(boolean arg0) {
      this.base.setAllowFlight(arg0);
   }

   public void hidePlayer(Player arg0) {
      this.base.hidePlayer(arg0);
   }

   public void showPlayer(Player arg0) {
      this.base.showPlayer(arg0);
   }

   public boolean canSee(Player arg0) {
      return this.base.canSee(arg0);
   }

   /** @deprecated */
   @Deprecated
   public boolean isOnGround() {
      return this.base.isOnGround();
   }

   public boolean isFlying() {
      return this.base.isFlying();
   }

   public void setFlying(boolean arg0) {
      this.base.setFlying(arg0);
   }

   public void setFlySpeed(float arg0) throws IllegalArgumentException {
      this.base.setFlySpeed(arg0);
   }

   public void setWalkSpeed(float arg0) throws IllegalArgumentException {
      this.base.setWalkSpeed(arg0);
   }

   public float getFlySpeed() {
      return this.base.getFlySpeed();
   }

   public float getWalkSpeed() {
      return this.base.getWalkSpeed();
   }

   public void setTexturePack(String arg0) {
      this.base.setTexturePack(arg0);
   }

   public String getName() {
      return this.base.getName();
   }

   public PlayerInventory getInventory() {
      return this.base.getInventory();
   }

   public Inventory getEnderChest() {
      return this.base.getEnderChest();
   }

   public boolean setWindowProperty(InventoryView.Property arg0, int arg1) {
      return this.base.setWindowProperty(arg0, arg1);
   }

   public InventoryView getOpenInventory() {
      return this.base.getOpenInventory();
   }

   public InventoryView openInventory(Inventory arg0) {
      return this.base.openInventory(arg0);
   }

   public InventoryView openWorkbench(Location arg0, boolean arg1) {
      return this.base.openWorkbench(arg0, arg1);
   }

   public InventoryView openEnchanting(Location arg0, boolean arg1) {
      return this.base.openEnchanting(arg0, arg1);
   }

   public void openInventory(InventoryView arg0) {
      this.base.openInventory(arg0);
   }

   public void closeInventory() {
      this.base.closeInventory();
   }

   public ItemStack getItemInHand() {
      return this.base.getItemInHand();
   }

   public void setItemInHand(ItemStack arg0) {
      this.base.setItemInHand(arg0);
   }

   public ItemStack getItemOnCursor() {
      return this.base.getItemOnCursor();
   }

   public void setItemOnCursor(ItemStack arg0) {
      this.base.setItemOnCursor(arg0);
   }

   public boolean isSleeping() {
      return this.base.isSleeping();
   }

   public int getSleepTicks() {
      return this.base.getSleepTicks();
   }

   public GameMode getGameMode() {
      return this.base.getGameMode();
   }

   public void setGameMode(GameMode arg0) {
      this.base.setGameMode(arg0);
   }

   public boolean isBlocking() {
      return this.base.isBlocking();
   }

   public int getExpToLevel() {
      return this.base.getExpToLevel();
   }

   public boolean isConversing() {
      return this.base.isConversing();
   }

   public void acceptConversationInput(String arg0) {
      this.base.acceptConversationInput(arg0);
   }

   public boolean beginConversation(Conversation arg0) {
      return this.base.beginConversation(arg0);
   }

   public void abandonConversation(Conversation arg0) {
      this.base.abandonConversation(arg0);
   }

   public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
      this.base.abandonConversation(arg0, arg1);
   }

   public void sendMessage(String arg0) {
      this.base.sendMessage(arg0);
   }

   public void sendMessage(String[] arg0) {
      this.base.sendMessage(arg0);
   }

   public Server getServer() {
      return this.base.getServer();
   }

   public boolean isOnline() {
      return this.base.isOnline();
   }

   public boolean isBanned() {
      return this.base.isBanned();
   }

   public void setBanned(boolean arg0) {
      this.base.setBanned(arg0);
   }

   public boolean isWhitelisted() {
      return this.base.isWhitelisted();
   }

   public void setWhitelisted(boolean arg0) {
      this.base.setWhitelisted(arg0);
   }

   public Player getPlayer() {
      return this.base.getPlayer();
   }

   public long getFirstPlayed() {
      return this.base.getFirstPlayed();
   }

   public long getLastPlayed() {
      return this.base.getLastPlayed();
   }

   public boolean hasPlayedBefore() {
      return this.base.hasPlayedBefore();
   }

   public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
      this.base.sendPluginMessage(arg0, arg1, arg2);
   }

   public Set getListeningPluginChannels() {
      return this.base.getListeningPluginChannels();
   }

   public Location getLocation() {
      return this.base.getLocation();
   }

   public Location getLocation(Location arg0) {
      return this.base.getLocation(arg0);
   }

   public void setVelocity(Vector arg0) {
      this.base.setVelocity(arg0);
   }

   public Vector getVelocity() {
      return this.base.getVelocity();
   }

   public World getWorld() {
      return this.base.getWorld();
   }

   public boolean teleport(Location arg0) {
      return this.base.teleport(arg0);
   }

   public boolean teleport(Location arg0, PlayerTeleportEvent.TeleportCause arg1) {
      return this.base.teleport(arg0, arg1);
   }

   public boolean teleport(Entity arg0) {
      return this.base.teleport(arg0);
   }

   public boolean teleport(Entity arg0, PlayerTeleportEvent.TeleportCause arg1) {
      return this.base.teleport(arg0, arg1);
   }

   public List getNearbyEntities(double arg0, double arg1, double arg2) {
      return this.base.getNearbyEntities(arg0, arg1, arg2);
   }

   public int getEntityId() {
      return this.base.getEntityId();
   }

   public int getFireTicks() {
      return this.base.getFireTicks();
   }

   public int getMaxFireTicks() {
      return this.base.getMaxFireTicks();
   }

   public void setFireTicks(int arg0) {
      this.base.setFireTicks(arg0);
   }

   public void remove() {
      this.base.remove();
   }

   public boolean isDead() {
      return this.base.isDead();
   }

   public boolean isValid() {
      return this.base.isValid();
   }

   public Entity getPassenger() {
      return this.base.getPassenger();
   }

   public boolean setPassenger(Entity arg0) {
      return this.base.setPassenger(arg0);
   }

   public boolean isEmpty() {
      return this.base.isEmpty();
   }

   public boolean eject() {
      return this.base.eject();
   }

   public float getFallDistance() {
      return this.base.getFallDistance();
   }

   public void setFallDistance(float arg0) {
      this.base.setFallDistance(arg0);
   }

   public void setLastDamageCause(EntityDamageEvent arg0) {
      this.base.setLastDamageCause(arg0);
   }

   public EntityDamageEvent getLastDamageCause() {
      return this.base.getLastDamageCause();
   }

   public UUID getUniqueId() {
      return this.base.getUniqueId();
   }

   public int getTicksLived() {
      return this.base.getTicksLived();
   }

   public void setTicksLived(int arg0) {
      this.base.setTicksLived(arg0);
   }

   public void playEffect(EntityEffect arg0) {
      this.base.playEffect(arg0);
   }

   public EntityType getType() {
      return this.base.getType();
   }

   public boolean isInsideVehicle() {
      return this.base.isInsideVehicle();
   }

   public boolean leaveVehicle() {
      return this.base.leaveVehicle();
   }

   public Entity getVehicle() {
      return this.base.getVehicle();
   }

   public void setMetadata(String arg0, MetadataValue arg1) {
      this.base.setMetadata(arg0, arg1);
   }

   public List getMetadata(String arg0) {
      return this.base.getMetadata(arg0);
   }

   public boolean hasMetadata(String arg0) {
      return this.base.hasMetadata(arg0);
   }

   public void removeMetadata(String arg0, Plugin arg1) {
      this.base.removeMetadata(arg0, arg1);
   }

   public boolean isPermissionSet(String arg0) {
      return this.base.isPermissionSet(arg0);
   }

   public boolean isPermissionSet(Permission arg0) {
      return this.base.isPermissionSet(arg0);
   }

   public boolean hasPermission(String arg0) {
      return this.base.hasPermission(arg0);
   }

   public boolean hasPermission(Permission arg0) {
      return this.base.hasPermission(arg0);
   }

   public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
      return this.base.addAttachment(arg0, arg1, arg2);
   }

   public PermissionAttachment addAttachment(Plugin arg0) {
      return this.base.addAttachment(arg0);
   }

   public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
      return this.base.addAttachment(arg0, arg1, arg2, arg3);
   }

   public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
      return this.base.addAttachment(arg0, arg1);
   }

   public void removeAttachment(PermissionAttachment arg0) {
      this.base.removeAttachment(arg0);
   }

   public void recalculatePermissions() {
      this.base.recalculatePermissions();
   }

   public Set getEffectivePermissions() {
      return this.base.getEffectivePermissions();
   }

   public boolean isOp() {
      return this.base.isOp();
   }

   public void setOp(boolean arg0) {
      this.base.setOp(arg0);
   }

   public double getEyeHeight() {
      return this.base.getEyeHeight();
   }

   public double getEyeHeight(boolean arg0) {
      return this.base.getEyeHeight(arg0);
   }

   public Location getEyeLocation() {
      return this.base.getEyeLocation();
   }

   public List getLineOfSight(HashSet arg0, int arg1) {
      return this.base.getLineOfSight(arg0, arg1);
   }

   public Block getTargetBlock(HashSet arg0, int arg1) {
      return this.base.getTargetBlock(arg0, arg1);
   }

   public List getLastTwoTargetBlocks(HashSet arg0, int arg1) {
      return this.base.getLastTwoTargetBlocks(arg0, arg1);
   }

   /** @deprecated */
   @Deprecated
   public Egg throwEgg() {
      return this.base.throwEgg();
   }

   /** @deprecated */
   @Deprecated
   public Snowball throwSnowball() {
      return this.base.throwSnowball();
   }

   /** @deprecated */
   @Deprecated
   public Arrow shootArrow() {
      return this.base.shootArrow();
   }

   public Projectile launchProjectile(Class arg0) {
      return this.base.launchProjectile(arg0);
   }

   public int getRemainingAir() {
      return this.base.getRemainingAir();
   }

   public void setRemainingAir(int arg0) {
      this.base.setRemainingAir(arg0);
   }

   public int getMaximumAir() {
      return this.base.getMaximumAir();
   }

   public void setMaximumAir(int arg0) {
      this.base.setMaximumAir(arg0);
   }

   public int getMaximumNoDamageTicks() {
      return this.base.getMaximumNoDamageTicks();
   }

   public void setMaximumNoDamageTicks(int arg0) {
      this.base.setMaximumNoDamageTicks(arg0);
   }

   public int getLastDamage() {
      return this.base.getLastDamage();
   }

   public void setLastDamage(int arg0) {
      this.base.setLastDamage(arg0);
   }

   public int getNoDamageTicks() {
      return this.base.getNoDamageTicks();
   }

   public void setNoDamageTicks(int arg0) {
      this.base.setNoDamageTicks(arg0);
   }

   public Player getKiller() {
      return this.base.getKiller();
   }

   public boolean addPotionEffect(PotionEffect arg0) {
      return this.base.addPotionEffect(arg0);
   }

   public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
      return this.base.addPotionEffect(arg0, arg1);
   }

   public boolean addPotionEffects(Collection arg0) {
      return this.base.addPotionEffects(arg0);
   }

   public boolean hasPotionEffect(PotionEffectType arg0) {
      return this.base.hasPotionEffect(arg0);
   }

   public void removePotionEffect(PotionEffectType arg0) {
      this.base.removePotionEffect(arg0);
   }

   public Collection getActivePotionEffects() {
      return this.base.getActivePotionEffects();
   }

   public boolean hasLineOfSight(Entity arg0) {
      return this.base.hasLineOfSight(arg0);
   }

   public boolean getRemoveWhenFarAway() {
      return this.base.getRemoveWhenFarAway();
   }

   public void setRemoveWhenFarAway(boolean arg0) {
      this.base.setRemoveWhenFarAway(arg0);
   }

   public EntityEquipment getEquipment() {
      return this.base.getEquipment();
   }

   public void setCanPickupItems(boolean arg0) {
      this.base.setCanPickupItems(arg0);
   }

   public boolean getCanPickupItems() {
      return this.base.getCanPickupItems();
   }

   public void setCustomName(String arg0) {
      this.base.setCustomName(arg0);
   }

   public String getCustomName() {
      return this.base.getCustomName();
   }

   public void setCustomNameVisible(boolean arg0) {
      this.base.setCustomNameVisible(arg0);
   }

   public boolean isCustomNameVisible() {
      return this.base.isCustomNameVisible();
   }

   public Map serialize() {
      return this.base.serialize();
   }

   public void damage(int arg0) {
      this.base.damage(arg0);
   }

   public void damage(int arg0, Entity arg1) {
      this.base.damage(arg0, arg1);
   }

   public int getHealth() {
      return this.base.getHealth();
   }

   public void setHealth(int arg0) {
      this.base.setHealth(arg0);
   }

   public int getMaxHealth() {
      return this.base.getMaxHealth();
   }

   public void setMaxHealth(int arg0) {
      this.base.setMaxHealth(arg0);
   }

   public void resetMaxHealth() {
      this.base.resetMaxHealth();
   }
}
