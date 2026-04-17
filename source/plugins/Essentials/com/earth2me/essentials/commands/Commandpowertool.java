package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandpowertool extends EssentialsCommand {
   public Commandpowertool() {
      super("powertool");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      String command = getFinalArg(args, 0);
      if (command != null && command.equalsIgnoreCase("d:")) {
         user.clearAllPowertools();
         user.sendMessage(I18n._("powerToolClearAll"));
      } else {
         ItemStack itemStack = user.getItemInHand();
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            String itemName = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
            List<String> powertools = user.getPowertool(itemStack);
            if (command != null && !command.isEmpty()) {
               if (command.equalsIgnoreCase("l:")) {
                  if (powertools != null && !powertools.isEmpty()) {
                     user.sendMessage(I18n._("powerToolList", Util.joinList(powertools), itemName));
                     throw new NoChargeException();
                  }

                  throw new Exception(I18n._("powerToolListEmpty", itemName));
               }

               if (command.startsWith("r:")) {
                  command = command.substring(2);
                  if (!powertools.contains(command)) {
                     throw new Exception(I18n._("powerToolNoSuchCommandAssigned", command, itemName));
                  }

                  powertools.remove(command);
                  user.sendMessage(I18n._("powerToolRemove", command, itemName));
               } else {
                  if (command.startsWith("a:")) {
                     if (!user.isAuthorized("essentials.powertool.append")) {
                        throw new Exception(I18n._("noPerm", "essentials.powertool.append"));
                     }

                     command = command.substring(2);
                     if (powertools.contains(command)) {
                        throw new Exception(I18n._("powerToolAlreadySet", command, itemName));
                     }
                  } else if (powertools != null && !powertools.isEmpty()) {
                     powertools.clear();
                  } else {
                     powertools = new ArrayList();
                  }

                  powertools.add(command);
                  user.sendMessage(I18n._("powerToolAttach", Util.joinList(powertools), itemName));
               }
            } else {
               if (powertools != null) {
                  powertools.clear();
               }

               user.sendMessage(I18n._("powerToolRemoveAll", itemName));
            }

            if (!user.arePowerToolsEnabled()) {
               user.setPowerToolsEnabled(true);
               user.sendMessage(I18n._("powerToolsEnabled"));
            }

            user.setPowertool(itemStack, powertools);
         } else {
            throw new Exception(I18n._("powerToolAir"));
         }
      }
   }
}
