package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.BookSerialize;
import com.goncalomb.bukkit.EntityTypeMap;
import com.goncalomb.bukkit.customitems.api.CustomItem;
import com.goncalomb.bukkit.customitems.api.CustomItemManager;
import com.goncalomb.bukkit.nbteditor.nbt.DroppedItemNBT;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.FallingBlockNBT;
import com.goncalomb.bukkit.nbteditor.nbt.FireworkNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MinecartContainerNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MinecartSpawnerNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import com.goncalomb.bukkit.nbteditor.nbt.ThrownPotionNBT;
import com.goncalomb.bukkit.nbteditor.nbt.VillagerNBT;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.Attribute;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.Modifier;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariableContainer;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.iharder.Base64;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class BookOfSouls {
   private static final String _author;
   private static final String _dataTitle;
   private static final String _dataTitleOLD;
   private static CustomItem _bosEmptyCustomItem;
   private static CustomItem _bosCustomItem;
   private static Plugin _plugin;
   private static final String[] _mobEquipSlotName;
   private ItemStack _book;
   private EntityNBT _entityNbt;

   static {
      _author = ChatColor.GOLD + "The Creator";
      _dataTitle = "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Soul Data v0.2" + ChatColor.BLACK + "\n";
      _dataTitleOLD = "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Soul Data v0.1" + ChatColor.BLACK + "\n";
      _plugin = null;
      _mobEquipSlotName = new String[]{"Head Equipment", "Chest Equipment", "Legs Equipment", "Feet Equipment", "Hand Item"};
   }

   public static void initialize(Plugin plugin, CustomItemManager itemManager) {
      if (_plugin == null) {
         _plugin = plugin;
         _bosEmptyCustomItem = new BookOfSoulsEmptyCI();
         itemManager.registerNew(_bosEmptyCustomItem, plugin);
         _bosCustomItem = new BookOfSoulsCI();
         itemManager.registerNew(_bosCustomItem, plugin);
      }
   }

   static EntityNBT bookToEntityNBT(ItemStack book) {
      if (isValidBook(book)) {
         try {
            String data = BookSerialize.loadData((BookMeta)book.getItemMeta(), _dataTitle);
            if (data == null) {
               data = BookSerialize.loadData((BookMeta)book.getItemMeta(), _dataTitleOLD);
               if (data != null) {
                  int i = data.indexOf(44);
                  NBTTagCompoundWrapper nbtData = NBTTagCompoundWrapper.unserialize(Base64.decode(data.substring(i + 1)));
                  nbtData.setString("id", data.substring(0, i));
                  data = Base64.encodeBytes(nbtData.serialize(), 2);
               }
            }

            if (data != null) {
               return EntityNBT.unserialize(data);
            }
         } catch (Throwable var4) {
            return null;
         }
      }

      return null;
   }

   public static BookOfSouls getFromBook(ItemStack book) {
      EntityNBT entityNbt = bookToEntityNBT(book);
      return entityNbt != null ? new BookOfSouls(book, entityNbt) : null;
   }

   public static ItemStack getEmpty() {
      return _bosEmptyCustomItem.getItem();
   }

   public BookOfSouls(EntityNBT entityNBT) {
      this((ItemStack)null, entityNBT);
   }

   private BookOfSouls(ItemStack book, EntityNBT entityNBT) {
      this._book = book;
      this._entityNbt = entityNBT;
   }

   public static boolean isValidBook(ItemStack book) {
      if (book != null && book.getType() == Material.WRITTEN_BOOK) {
         ItemMeta meta = book.getItemMeta();
         String title = ((BookMeta)meta).getTitle();
         if (meta != null && title != null && title.equals(_bosCustomItem.getName())) {
            return true;
         }
      }

      return false;
   }

   public boolean openInventory(Player player) {
      if (this._entityNbt instanceof MobNBT) {
         (new InventoryForMobs(this, player)).openInventory(player, _plugin);
         return true;
      } else if (this._entityNbt instanceof DroppedItemNBT) {
         (new InventoryForDroppedItems(this, player)).openInventory(player, _plugin);
         return true;
      } else if (this._entityNbt instanceof ThrownPotionNBT) {
         (new InventoryForThownPotion(this, player)).openInventory(player, _plugin);
         return true;
      } else if (this._entityNbt instanceof FireworkNBT) {
         (new InventoryForFirework(this, player)).openInventory(player, _plugin);
         return true;
      } else {
         return false;
      }
   }

   public boolean openOffersInventory(Player player) {
      if (this._entityNbt instanceof VillagerNBT) {
         (new InventoryForVillagers(this, player)).openInventory(player, _plugin);
         return true;
      } else {
         return false;
      }
   }

   public void openRidingInventory(Player player) {
      (new InventoryForRiding(this, player)).openInventory(player, _plugin);
   }

   public boolean setMobDropChance(float head, float chest, float legs, float feet, float hand) {
      if (this._entityNbt instanceof MobNBT) {
         ((MobNBT)this._entityNbt).setDropChances(hand, feet, legs, chest, head);
         return true;
      } else {
         return false;
      }
   }

   public boolean clearMobDropChance() {
      if (this._entityNbt instanceof MobNBT) {
         ((MobNBT)this._entityNbt).clearDropChances();
         return true;
      } else {
         return false;
      }
   }

   public EntityNBT getEntityNBT() {
      return this._entityNbt;
   }

   public ItemStack getBook() {
      if (this._book == null) {
         this._book = new ItemStack(Material.WRITTEN_BOOK);
         this.saveBook(true);
      }

      return this._book;
   }

   public void saveBook() {
      this.saveBook(false);
   }

   public void saveBook(boolean resetName) {
      BookMeta meta = (BookMeta)this._book.getItemMeta();
      String entityName = EntityTypeMap.getName(this._entityNbt.getEntityType());
      if (resetName) {
         meta.setDisplayName(_bosCustomItem.getName() + ChatColor.RESET + " - " + ChatColor.RED + entityName);
         meta.setTitle(_bosCustomItem.getName());
         meta.setAuthor(_author);
      }

      meta.setPages((List)null);
      StringBuilder sb = new StringBuilder();
      sb.append("This book contains the soul of a " + ChatColor.RED + ChatColor.BOLD + entityName + "\n\n");
      int x = 7;
      if (this._entityNbt instanceof MinecartSpawnerNBT) {
         sb.append(ChatColor.BLACK + "Left-click a existing spawner to copy the entities and variables from the spawner, left-click while sneaking to copy them back to the spawner.");
         meta.addPage(new String[]{sb.toString()});
         sb = new StringBuilder();
         x = 11;
      } else if (this._entityNbt instanceof MinecartContainerNBT) {
         sb.append(ChatColor.BLACK + "Left-click a chest to copy the items from it, left-click while sneaking to copy them back to the chest.");
         meta.addPage(new String[]{sb.toString()});
         sb = new StringBuilder();
         x = 11;
      } else if (this._entityNbt instanceof FallingBlockNBT) {
         sb.append(ChatColor.BLACK + "Left-click a block while sneaking to copy block data.\n\n");
         x = 5;
      }

      NBTVariableContainer[] var9;
      for(NBTVariableContainer vc : var9 = this._entityNbt.getAllVariables()) {
         if (x == 1) {
            meta.addPage(new String[]{sb.toString()});
            sb = new StringBuilder();
            x = 11;
         }

         sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + vc.getName() + ":\n");

         for(NBTVariable var : vc) {
            --x;
            if (x == 0) {
               meta.addPage(new String[]{sb.toString()});
               sb = new StringBuilder();
               x = 10;
            }

            String value = var.getValue();
            sb.append("  " + ChatColor.DARK_BLUE + var.getName() + ": " + ChatColor.BLACK + (value != null ? value : ChatColor.ITALIC + "-") + "\n");
         }
      }

      meta.addPage(new String[]{sb.toString()});
      if (this._entityNbt instanceof MobNBT) {
         MobNBT mob = (MobNBT)this._entityNbt;
         sb = new StringBuilder();
         sb.append("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Attributes:\n");
         Collection<Attribute> attributes = mob.getAttributes().values();
         if (attributes.size() == 0) {
            sb.append("  " + ChatColor.BLACK + ChatColor.ITALIC + "none\n");
         } else {
            x = 11;

            for(Attribute attribute : attributes) {
               if (x <= 3) {
                  meta.addPage(new String[]{sb.toString()});
                  sb = new StringBuilder();
                  x = 11;
               }

               sb.append("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + attribute.getType().getName() + ":\n");
               sb.append("  " + ChatColor.DARK_BLUE + "Base: " + ChatColor.BLACK + attribute.getBase() + "\n");
               sb.append("  " + ChatColor.DARK_BLUE + "Modifiers:\n");
               x -= 3;
               List<Modifier> modifiers = attribute.getModifiers();
               if (modifiers.size() == 0) {
                  sb.append("    " + ChatColor.BLACK + ChatColor.ITALIC + "none\n");
               } else {
                  for(Modifier modifier : modifiers) {
                     if (x <= 3) {
                        meta.addPage(new String[]{sb.toString()});
                        sb = new StringBuilder();
                        x = 11;
                     }

                     sb.append("    " + ChatColor.RED + modifier.getName() + ChatColor.DARK_GREEN + " Op: " + ChatColor.BLACK + modifier.getOperation() + "\n");
                     sb.append("      " + ChatColor.DARK_GREEN + "Amount: " + ChatColor.BLACK + modifier.getAmount() + "\n");
                     x -= 3;
                  }
               }

               sb.append("\n");
               --x;
            }
         }

         meta.addPage(new String[]{sb.toString()});
         sb = new StringBuilder();
         sb.append("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Equipment:\n");
         ItemStack[] items = mob.getEquipment();

         for(int i = 0; i < 5; ++i) {
            sb.append(ChatColor.DARK_BLUE + _mobEquipSlotName[i] + ":\n");
            if (items[4 - i] != null) {
               sb.append("  " + ChatColor.BLACK + items[4 - i].getType().name() + ":" + items[4 - i].getDurability() + "(" + items[4 - i].getAmount() + ")" + "\n");
            } else {
               sb.append("  " + ChatColor.BLACK + ChatColor.ITALIC + "none\n");
            }
         }

         meta.addPage(new String[]{sb.toString()});
         sb = new StringBuilder();
         sb.append("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Drop chance:\n");
         float[] chances = mob.getDropChances();
         if (chances != null) {
            for(int i = 0; i < 5; ++i) {
               sb.append(ChatColor.DARK_BLUE + _mobEquipSlotName[i] + ":\n");
               sb.append("  " + ChatColor.BLACK + chances[4 - i] + "\n");
            }
         } else {
            sb.append("" + ChatColor.BLACK + ChatColor.ITALIC + "not defined,\ndefault 0.85");
         }

         meta.addPage(new String[]{sb.toString()});
      }

      BookSerialize.saveToBook(meta, this._entityNbt.serialize(), _dataTitle);
      meta.addPage(new String[]{"RandomId: " + Integer.toHexString((new Random()).nextInt()) + "\n\n\n" + ChatColor.DARK_BLUE + ChatColor.BOLD + "      The END."});
      this._book.setItemMeta(meta);
   }
}
