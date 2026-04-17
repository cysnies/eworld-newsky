package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Items extends Check {
   private static Items instance = null;

   public Items() {
      super(CheckType.INVENTORY_ITEMS);
      instance = this;
   }

   public static final boolean checkIllegalEnchantments(Player player, ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Material type = stack.getType();
         if (type == Material.WRITTEN_BOOK) {
            Map<Enchantment, Integer> enchantments = stack.getEnchantments();
            if (enchantments != null && !enchantments.isEmpty() && instance.isEnabled(player)) {
               for(Enchantment ench : new HashSet(enchantments.keySet())) {
                  stack.removeEnchantment(ench);
               }

               return true;
            }
         }

         return false;
      }
   }
}
