package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandunlimited extends EssentialsCommand {
   public Commandunlimited() {
      super("unlimited");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = user;
         if (args.length > 1 && user.isAuthorized("essentials.unlimited.others")) {
            target = this.getPlayer(server, user, args, 1);
         }

         if (args[0].equalsIgnoreCase("list")) {
            String list = this.getList(target);
            user.sendMessage(list);
         } else if (args[0].equalsIgnoreCase("clear")) {
            List<Integer> itemList = target.getUnlimited();
            int index = 0;

            while(itemList.size() > index) {
               Integer item = (Integer)itemList.get(index);
               if (!this.toggleUnlimited(user, target, item.toString())) {
                  ++index;
               }
            }
         } else {
            this.toggleUnlimited(user, target, args[0]);
         }

      }
   }

   private String getList(User target) {
      StringBuilder output = new StringBuilder();
      output.append(I18n._("unlimitedItems")).append(" ");
      boolean first = true;
      List<Integer> items = target.getUnlimited();
      if (items.isEmpty()) {
         output.append(I18n._("none"));
      }

      for(Integer integer : items) {
         if (!first) {
            output.append(", ");
         }

         first = false;
         String matname = Material.getMaterial(integer).toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         output.append(matname);
      }

      return output.toString();
   }

   private Boolean toggleUnlimited(User user, User target, String item) throws Exception {
      ItemStack stack = this.ess.getItemDb().get(item, 1);
      stack.setAmount(Math.min(stack.getType().getMaxStackSize(), 2));
      String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
      if (!this.ess.getSettings().permissionBasedItemSpawn() || user.isAuthorized("essentials.unlimited.item-all") || user.isAuthorized("essentials.unlimited.item-" + itemname) || user.isAuthorized("essentials.unlimited.item-" + stack.getTypeId()) || (stack.getType() == Material.WATER_BUCKET || stack.getType() == Material.LAVA_BUCKET) && user.isAuthorized("essentials.unlimited.item-bucket")) {
         String message = "disableUnlimited";
         boolean enableUnlimited = false;
         if (!target.hasUnlimited(stack)) {
            message = "enableUnlimited";
            enableUnlimited = true;
            if (!target.getInventory().containsAtLeast(stack, stack.getAmount())) {
               target.getInventory().addItem(new ItemStack[]{stack});
            }
         }

         if (user != target) {
            user.sendMessage(I18n._(message, itemname, target.getDisplayName()));
         }

         target.sendMessage(I18n._(message, itemname, target.getDisplayName()));
         target.setUnlimited(stack, enableUnlimited);
         return true;
      } else {
         throw new Exception(I18n._("unlimitedItemPermission", itemname));
      }
   }
}
