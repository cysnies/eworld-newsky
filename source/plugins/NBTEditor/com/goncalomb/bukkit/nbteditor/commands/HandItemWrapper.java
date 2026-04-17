package com.goncalomb.bukkit.nbteditor.commands;

import com.goncalomb.bukkit.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.betterplugin.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

public abstract class HandItemWrapper {
   public final ItemStack item;
   public final ItemMeta meta;

   private HandItemWrapper(Class clazz, Player player, boolean throwEx) throws BetterCommandException {
      this.item = player.getItemInHand();
      if (this.item.getType() != Material.AIR) {
         ItemMeta m = this.item.getItemMeta();
         if (clazz.isInstance(m)) {
            this.meta = m;
            return;
         }
      }

      this.meta = null;
      if (throwEx) {
         throw new BetterCommandException(Lang._format("nbt.meta-error.format", Lang._("nbt.meta-error." + this.getClass().getSimpleName().toLowerCase())));
      }
   }

   public void save() {
      this.item.setItemMeta(this.meta);
   }

   // $FF: synthetic method
   HandItemWrapper(Class var1, Player var2, boolean var3, HandItemWrapper var4) throws BetterCommandException {
      this(var1, var2, var3);
   }

   public static final class Item extends HandItemWrapper {
      public Item(Player player) throws BetterCommandException {
         super(ItemMeta.class, player, true, (HandItemWrapper)null);
      }
   }

   public static final class Potion extends HandItemWrapper {
      public Potion(Player player) throws BetterCommandException {
         super(PotionMeta.class, player, true, (HandItemWrapper)null);
      }
   }

   public static final class Book extends HandItemWrapper {
      public Book(Player player, BookType bookType) throws BetterCommandException {
         super(BookMeta.class, player, bookType == HandItemWrapper.Book.BookType.BOTH, (HandItemWrapper)null);
         if (this.meta == null) {
            throw new BetterCommandException(Lang._format("nbt.meta-error.format", Lang._("nbt.meta-error.book-" + (bookType == HandItemWrapper.Book.BookType.BOOK_AND_QUILL ? "quill" : "written"))));
         }
      }

      public static enum BookType {
         BOTH,
         BOOK_AND_QUILL,
         WRITTEN;
      }
   }

   public static final class LeatherArmor extends HandItemWrapper {
      public LeatherArmor(Player player) throws BetterCommandException {
         super(LeatherArmorMeta.class, player, true, (HandItemWrapper)null);
      }
   }
}
