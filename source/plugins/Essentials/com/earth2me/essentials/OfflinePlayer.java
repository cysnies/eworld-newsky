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
import org.bukkit.entity.Vehicle;
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

public class OfflinePlayer implements Player {
   private final transient IEssentials ess;
   private transient Location location = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F, 0.0F, 0.0F);
   private transient World world;
   private final transient UUID uniqueId = UUID.randomUUID();
   private transient org.bukkit.OfflinePlayer base;

   public OfflinePlayer(String name, IEssentials ess) {
      this.ess = ess;
      this.world = (World)ess.getServer().getWorlds().get(0);
      this.base = ess.getServer().getOfflinePlayer(name);
   }

   public void sendMessage(String string) {
   }

   public String getDisplayName() {
      return this.base.getName();
   }

   public void setDisplayName(String string) {
   }

   public void setCompassTarget(Location lctn) {
   }

   public InetSocketAddress getAddress() {
      return null;
   }

   public void kickPlayer(String string) {
   }

   public PlayerInventory getInventory() {
      return null;
   }

   public ItemStack getItemInHand() {
      return null;
   }

   public void setItemInHand(ItemStack is) {
   }

   public int getHealth() {
      return 0;
   }

   public void setHealth(int i) {
   }

   public Egg throwEgg() {
      return null;
   }

   public Snowball throwSnowball() {
      return null;
   }

   public Arrow shootArrow() {
      return null;
   }

   public boolean isInsideVehicle() {
      return false;
   }

   public boolean leaveVehicle() {
      return false;
   }

   public Vehicle getVehicle() {
      return null;
   }

   public Location getLocation() {
      return this.location;
   }

   public World getWorld() {
      return this.world;
   }

   public void setLocation(Location loc) {
      this.location = loc;
      this.world = loc.getWorld();
   }

   public void teleportTo(Location lctn) {
   }

   public void teleportTo(Entity entity) {
   }

   public int getEntityId() {
      return -1;
   }

   public boolean performCommand(String string) {
      return false;
   }

   public int getRemainingAir() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setRemainingAir(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getMaximumAir() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setMaximumAir(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean isSneaking() {
      return false;
   }

   public void setSneaking(boolean bln) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void updateInventory() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void chat(String string) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public double getEyeHeight() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public double getEyeHeight(boolean bln) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public List getLineOfSight(HashSet hs, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Block getTargetBlock(HashSet hs, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public List getLastTwoTargetBlocks(HashSet hs, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getFireTicks() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getMaxFireTicks() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setFireTicks(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void remove() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Server getServer() {
      return this.ess == null ? null : this.ess.getServer();
   }

   public Vector getMomentum() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setMomentum(Vector vector) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setVelocity(Vector vector) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Vector getVelocity() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void damage(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void damage(int i, Entity entity) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Location getEyeLocation() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void sendRawMessage(String string) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Location getCompassTarget() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getMaximumNoDamageTicks() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setMaximumNoDamageTicks(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getLastDamage() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setLastDamage(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getNoDamageTicks() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setNoDamageTicks(int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean teleport(Location lctn) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean teleport(Entity entity) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public Entity getPassenger() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean setPassenger(Entity entity) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean isEmpty() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean eject() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void saveData() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void loadData() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean isSleeping() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public int getSleepTicks() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public List getNearbyEntities(double d, double d1, double d2) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean isDead() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public float getFallDistance() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setFallDistance(float f) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setSleepingIgnored(boolean bln) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean isSleepingIgnored() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void awardAchievement(Achievement a) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void incrementStatistic(Statistic ststc) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void incrementStatistic(Statistic ststc, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void incrementStatistic(Statistic ststc, Material mtrl) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void incrementStatistic(Statistic ststc, Material mtrl, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void playNote(Location lctn, byte b, byte b1) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void sendBlockChange(Location lctn, Material mtrl, byte b) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void sendBlockChange(Location lctn, int i, byte b) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void setLastDamageCause(EntityDamageEvent ede) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public EntityDamageEvent getLastDamageCause() {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public void playEffect(Location lctn, Effect effect, int i) {
      throw new UnsupportedOperationException(I18n._("notSupportedYet"));
   }

   public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes) {
      return true;
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public void playNote(Location lctn, Instrument i, Note note) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setPlayerTime(long l, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getPlayerTime() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getPlayerTimeOffset() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isPlayerTimeRelative() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void resetPlayerTime() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isPermissionSet(String string) {
      return false;
   }

   public boolean isPermissionSet(Permission prmsn) {
      return false;
   }

   public boolean hasPermission(String string) {
      return false;
   }

   public boolean hasPermission(Permission prmsn) {
      return false;
   }

   public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public PermissionAttachment addAttachment(Plugin plugin) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public PermissionAttachment addAttachment(Plugin plugin, int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void removeAttachment(PermissionAttachment pa) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void recalculatePermissions() {
   }

   public Set getEffectivePermissions() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void sendMap(MapView mv) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public GameMode getGameMode() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setGameMode(GameMode gm) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getLevel() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setLevel(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getTotalExperience() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTotalExperience(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public float getExhaustion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setExhaustion(float f) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public float getSaturation() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setSaturation(float f) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getFoodLevel() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setFoodLevel(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isSprinting() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setSprinting(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setPlayerListName(String name) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String getPlayerListName() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getTicksLived() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTicksLived(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getMaxHealth() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void giveExp(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public float getExp() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setExp(float f) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean teleport(Location lctn, PlayerTeleportEvent.TeleportCause tc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause tc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Player getKiller() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   void setName(String name) {
      if (!this.base.getName().equalsIgnoreCase(name)) {
         this.base = this.ess.getServer().getOfflinePlayer(name);
      }

   }

   public void sendPluginMessage(Plugin plugin, String string, byte[] bytes) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Set getListeningPluginChannels() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setAllowFlight(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getAllowFlight() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setBedSpawnLocation(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setBedSpawnLocation(Location lctn, boolean force) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playEffect(EntityEffect ee) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void hidePlayer(Player player) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void showPlayer(Player player) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean canSee(Player player) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean addPotionEffect(PotionEffect pe) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean addPotionEffect(PotionEffect pe, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean addPotionEffects(Collection clctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasPotionEffect(PotionEffectType pet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void removePotionEffect(PotionEffectType pet) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Collection getActivePotionEffects() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Projectile launchProjectile(Class arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public EntityType getType() {
      return EntityType.PLAYER;
   }

   public void playEffect(Location lctn, Effect effect, Object t) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean setWindowProperty(InventoryView.Property prprt, int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public InventoryView getOpenInventory() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public InventoryView openInventory(Inventory invntr) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public InventoryView openWorkbench(Location lctn, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public InventoryView openEnchanting(Location lctn, boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void openInventory(InventoryView iv) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void closeInventory() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public ItemStack getItemOnCursor() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setItemOnCursor(ItemStack is) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setMetadata(String string, MetadataValue mv) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public List getMetadata(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasMetadata(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void removeMetadata(String string, Plugin plugin) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isConversing() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void acceptConversationInput(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean beginConversation(Conversation c) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void abandonConversation(Conversation c) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void sendMessage(String[] strings) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isBlocking() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isFlying() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setFlying(boolean arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getExpToLevel() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasLineOfSight(Entity entity) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isValid() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setFlySpeed(float value) throws IllegalArgumentException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setWalkSpeed(float value) throws IllegalArgumentException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public float getFlySpeed() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public float getWalkSpeed() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Inventory getEnderChest() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void giveExpLevels(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getRemoveWhenFarAway() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setRemoveWhenFarAway(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public EntityEquipment getEquipment() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setCanPickupItems(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean getCanPickupItems() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Location getLocation(Location lctn) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setTexturePack(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setMaxHealth(int i) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void resetMaxHealth() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setCustomName(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String getCustomName() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setCustomNameVisible(boolean bln) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isCustomNameVisible() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setPlayerWeather(WeatherType arg0) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public WeatherType getPlayerWeather() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void resetPlayerWeather() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isOnGround() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isOnline() {
      return this.base.isOnline();
   }

   public String getName() {
      return this.base.getName();
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

   public Location getBedSpawnLocation() {
      return this.base.getBedSpawnLocation();
   }

   public boolean isOp() {
      return this.base.isOp();
   }

   public void setOp(boolean arg0) {
      this.base.setOp(arg0);
   }

   public Map serialize() {
      return this.base.serialize();
   }
}
