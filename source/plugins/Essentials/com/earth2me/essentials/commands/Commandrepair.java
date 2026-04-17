package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandrepair extends EssentialsCommand {
   public Commandrepair() {
      super("repair");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args[0].equalsIgnoreCase("hand")) {
            ItemStack item = user.getItemInHand();
            if (item == null || item.getType().isBlock() || item.getDurability() == 0) {
               throw new Exception(I18n._("repairInvalidType"));
            }

            if (!item.getEnchantments().isEmpty() && !this.ess.getSettings().getRepairEnchanted() && !user.isAuthorized("essentials.repair.enchanted")) {
               throw new Exception(I18n._("repairEnchanted"));
            }

            String itemName = item.getType().toString().toLowerCase(Locale.ENGLISH);
            Trade charge = new Trade("repair-" + itemName.replace('_', '-'), new Trade("repair-" + item.getTypeId(), new Trade("repair-item", this.ess), this.ess), this.ess);
            charge.isAffordableFor(user);
            this.repairItem(item);
            charge.charge(user);
            user.sendMessage(I18n._("repair", itemName.replace('_', ' ')));
         } else {
            if (!args[0].equalsIgnoreCase("all")) {
               throw new NotEnoughArgumentsException();
            }

            Trade charge = new Trade("repair-all", this.ess);
            charge.isAffordableFor(user);
            List<String> repaired = new ArrayList();
            this.repairItems(user.getInventory().getContents(), user, repaired);
            if (user.isAuthorized("essentials.repair.armor")) {
               this.repairItems(user.getInventory().getArmorContents(), user, repaired);
            }

            if (repaired.isEmpty()) {
               throw new Exception(I18n._("repairNone"));
            }

            user.sendMessage(I18n._("repair", Util.joinList(repaired)));
            charge.charge(user);
         }

      }
   }

   private void repairItem(ItemStack item) throws Exception {
      Material material = Material.getMaterial(item.getTypeId());
      if (!material.isBlock() && material.getMaxDurability() >= 1) {
         if (item.getDurability() == 0) {
            throw new Exception(I18n._("repairAlreadyFixed"));
         } else {
            item.setDurability((short)0);
         }
      } else {
         throw new Exception(I18n._("repairInvalidType"));
      }
   }

   private void repairItems(ItemStack[] items, IUser user, List repaired) {
      for(ItemStack item : items) {
         if (item != null && !item.getType().isBlock() && item.getDurability() != 0) {
            String itemName = item.getType().toString().toLowerCase(Locale.ENGLISH);
            Trade charge = new Trade("repair-" + itemName.replace('_', '-'), new Trade("repair-" + item.getTypeId(), new Trade("repair-item", this.ess), this.ess), this.ess);

            try {
               charge.isAffordableFor(user);
            } catch (ChargeException ex) {
               user.sendMessage(ex.getMessage());
               continue;
            }

            if (item.getEnchantments().isEmpty() || this.ess.getSettings().getRepairEnchanted() || user.isAuthorized("essentials.repair.enchanted")) {
               try {
                  this.repairItem(item);
               } catch (Exception var12) {
                  continue;
               }

               try {
                  charge.charge(user);
               } catch (ChargeException ex) {
                  user.sendMessage(ex.getMessage());
               }

               repaired.add(itemName.replace('_', ' '));
            }
         }
      }

   }
}
