package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.Potions;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Commandpotion extends EssentialsCommand {
   public Commandpotion() {
      super("potion");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (args.length == 0) {
         Set<String> potionslist = new TreeSet();

         for(Map.Entry entry : Potions.entrySet()) {
            String potionName = ((PotionEffectType)entry.getValue()).getName().toLowerCase(Locale.ENGLISH);
            if (potionslist.contains(potionName) || user.isAuthorized("essentials.potion." + potionName)) {
               potionslist.add(entry.getKey());
            }
         }

         throw new NotEnoughArgumentsException(I18n._("potions", Util.joinList(potionslist.toArray())));
      } else if (stack.getType() != Material.POTION) {
         throw new Exception(I18n._("holdPotion"));
      } else {
         PotionMeta pmeta = (PotionMeta)stack.getItemMeta();
         if (args.length > 0) {
            if (args[0].equalsIgnoreCase("clear")) {
               pmeta.clearCustomEffects();
               stack.setItemMeta(pmeta);
            } else if (args[0].equalsIgnoreCase("apply") && user.isAuthorized("essentials.potion.apply")) {
               for(PotionEffect effect : pmeta.getCustomEffects()) {
                  effect.apply(user);
               }
            } else {
               if (args.length < 3) {
                  throw new NotEnoughArgumentsException();
               }

               MetaItemStack mStack = new MetaItemStack(stack);

               for(String arg : args) {
                  mStack.addPotionMeta(user, true, arg, this.ess);
               }

               if (!mStack.completePotion()) {
                  user.sendMessage(I18n._("invalidPotion"));
                  throw new NotEnoughArgumentsException();
               }

               pmeta = (PotionMeta)mStack.getItemStack().getItemMeta();
               stack.setItemMeta(pmeta);
            }
         }

      }
   }
}
