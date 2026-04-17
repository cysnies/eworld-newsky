package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryListener extends CheckListener {
   private final Drop drop = (Drop)this.addCheck(new Drop());
   private final FastClick fastClick = (FastClick)this.addCheck(new FastClick());
   private final InstantBow instantBow = (InstantBow)this.addCheck(new InstantBow());
   private final InstantEat instantEat = (InstantEat)this.addCheck(new InstantEat());
   protected final Items items = (Items)this.addCheck(new Items());

   public InventoryListener() {
      super(CheckType.INVENTORY);
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (this.instantBow.isEnabled(player)) {
            long now = System.currentTimeMillis();
            Location loc = player.getLocation();
            if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName())) {
               event.setCancelled(true);
            }

            if (this.instantBow.check(player, event.getForce(), now)) {
               event.setCancelled(true);
            } else if (Improbable.check(player, 0.6F, now, "inventory.instantbow")) {
               event.setCancelled(true);
            }
         }
      }

   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (this.instantEat.isEnabled(player) && this.instantEat.check(player, event.getFoodLevel())) {
            event.setCancelled(true);
         }
      }

   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         long now = System.currentTimeMillis();
         HumanEntity entity = event.getWhoClicked();
         if (!(entity instanceof Player)) {
            return;
         }

         Player player = (Player)entity;
         int slot = event.getSlot();
         if (slot == -999 || slot < 0) {
            InventoryData.getData(player).lastClickTime = now;
            return;
         }

         ItemStack cursor = event.getCursor();
         ItemStack clicked = event.getCurrentItem();

         try {
            if (Items.checkIllegalEnchantments(player, clicked)) {
               event.setCancelled(true);
            }
         } catch (ArrayIndexOutOfBoundsException var12) {
         }

         try {
            if (Items.checkIllegalEnchantments(player, cursor)) {
               event.setCancelled(true);
            }
         } catch (ArrayIndexOutOfBoundsException var11) {
         }

         InventoryData data = InventoryData.getData(player);
         if (this.fastClick.isEnabled(player)) {
            InventoryConfig cc = InventoryConfig.getConfig(player);
            if (player.getGameMode() != GameMode.CREATIVE || !cc.fastClickSpareCreative) {
               if (this.fastClick.check(player, now, event.getView(), slot, cursor, clicked, event.isShiftClick(), data, cc)) {
                  event.setCancelled(true);
               }

               Improbable.feed(player, 0.7F, System.currentTimeMillis());
            }
         }

         data.lastClickTime = now;
      }

   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   protected void onPlayerDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      Item item = event.getItemDrop();
      if (item != null) {
         Items.checkIllegalEnchantments(player, item.getItemStack());
      }

      if (!event.getPlayer().isDead()) {
         if (this.drop.isEnabled(event.getPlayer()) && this.drop.check(event.getPlayer())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.LOWEST
   )
   public final void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         InventoryData data = InventoryData.getData(player);
         boolean resetAll = false;
         if (event.hasItem()) {
            ItemStack item = event.getItem();
            Material type = item.getType();
            if (type == Material.BOW) {
               long now = System.currentTimeMillis();
               data.instantBowInteract = data.instantBowInteract > 0L && now - data.instantBowInteract < 800L ? Math.min(System.currentTimeMillis(), data.instantBowInteract) : System.currentTimeMillis();
            } else if (!type.isEdible() && type != Material.POTION) {
               resetAll = true;
            } else {
               long now = System.currentTimeMillis();
               data.instantEatFood = type;
               data.instantEatInteract = data.instantEatInteract > 0L && now - data.instantEatInteract < 800L ? Math.min(System.currentTimeMillis(), data.instantEatInteract) : System.currentTimeMillis();
               data.instantBowInteract = 0L;
            }

            if (Items.checkIllegalEnchantments(player, item)) {
               event.setCancelled(true);
            }
         } else {
            resetAll = true;
         }

         if (resetAll) {
            data.instantBowInteract = 0L;
            data.instantEatInteract = 0L;
            data.instantEatFood = null;
         }

      }
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.LOWEST
   )
   public final void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      Player player = event.getPlayer();
      if (player.getGameMode() != GameMode.CREATIVE) {
         ItemStack stack = player.getItemInHand();
         if (stack != null && stack.getTypeId() == Material.MONSTER_EGG.getId() && this.items.isEnabled(player)) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onItemHeldChange(PlayerItemHeldEvent event) {
      Player player = event.getPlayer();
      InventoryData data = InventoryData.getData(player);
      data.instantBowInteract = 0L;
      data.instantEatInteract = 0L;
      data.instantEatFood = null;
      PlayerInventory inv = player.getInventory();
      Items.checkIllegalEnchantments(player, inv.getItem(event.getNewSlot()));
      Items.checkIllegalEnchantments(player, inv.getItem(event.getPreviousSlot()));
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      if (shouldEnsureCloseInventories()) {
         closePlayerInventoryRecursively(event.getPlayer());
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerPortal(PlayerPortalEvent event) {
      if (shouldEnsureCloseInventories()) {
         closePlayerInventoryRecursively(event.getPlayer());
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityPortal(EntityPortalEnterEvent event) {
      if (shouldEnsureCloseInventories()) {
         closePlayerInventoryRecursively(event.getEntity());
      }

   }

   public static boolean shouldEnsureCloseInventories() {
      return ConfigManager.getConfigFile().getBoolean("checks.inventory..ensureclose", true);
   }

   public static void closePlayerInventoryRecursively(Entity entity) {
      while(true) {
         if (entity != null) {
            if (entity instanceof Player) {
               closeInventory((Player)entity);
            }

            Entity passenger = entity.getPassenger();
            if (!entity.equals(passenger)) {
               entity = passenger;
               continue;
            }
         }

         return;
      }
   }

   public static void closeInventory(Player player) {
      player.closeInventory();
   }
}
