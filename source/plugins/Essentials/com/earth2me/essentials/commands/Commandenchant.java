package com.earth2me.essentials.commands;

import com.earth2me.essentials.Enchantments;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class Commandenchant extends EssentialsCommand {
   public Commandenchant() {
      super("enchant");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (stack != null && stack.getType() != Material.AIR) {
         if (args.length == 0) {
            Set<String> enchantmentslist = new TreeSet();

            for(Map.Entry entry : Enchantments.entrySet()) {
               String enchantmentName = ((Enchantment)entry.getValue()).getName().toLowerCase(Locale.ENGLISH);
               if (enchantmentslist.contains(enchantmentName) || user.isAuthorized("essentials.enchantments." + enchantmentName) && ((Enchantment)entry.getValue()).canEnchantItem(stack)) {
                  enchantmentslist.add(entry.getKey());
               }
            }

            throw new NotEnoughArgumentsException(I18n._("enchantments", Util.joinList(enchantmentslist.toArray())));
         } else {
            int level = -1;
            if (args.length > 1) {
               try {
                  level = Integer.parseInt(args[1]);
               } catch (NumberFormatException var11) {
                  level = -1;
               }
            }

            boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments() && user.isAuthorized("essentials.enchantments.allowunsafe");
            MetaItemStack metaStack = new MetaItemStack(stack);
            Enchantment enchantment = metaStack.getEnchantment(user, args[0]);
            metaStack.addEnchantment(user, allowUnsafe, enchantment, level);
            user.getInventory().setItemInHand(metaStack.getItemStack());
            user.updateInventory();
            String enchantmentName = enchantment.getName().toLowerCase(Locale.ENGLISH);
            if (level == 0) {
               user.sendMessage(I18n._("enchantmentRemoved", enchantmentName.replace('_', ' ')));
            } else {
               user.sendMessage(I18n._("enchantmentApplied", enchantmentName.replace('_', ' ')));
            }

         }
      } else {
         throw new Exception(I18n._("nothingInHand"));
      }
   }
}
