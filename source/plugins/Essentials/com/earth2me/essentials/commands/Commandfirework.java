package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.regex.Pattern;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class Commandfirework extends EssentialsCommand {
   private final transient Pattern splitPattern = Pattern.compile("[:+',;.]");

   public Commandfirework() {
      super("firework");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (stack.getType() == Material.FIREWORK) {
         if (args.length <= 0) {
            throw new NotEnoughArgumentsException();
         } else {
            if (args[0].equalsIgnoreCase("clear")) {
               FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();
               fmeta.clearEffects();
               stack.setItemMeta(fmeta);
               user.sendMessage(I18n._("fireworkEffectsCleared"));
            } else if (args.length <= 1 || !args[0].equalsIgnoreCase("power") && !args[0].equalsIgnoreCase("p")) {
               if ((args[0].equalsIgnoreCase("fire") || args[0].equalsIgnoreCase("f")) && user.isAuthorized("essentials.firework.fire")) {
                  int amount = 1;
                  boolean direction = false;
                  if (args.length > 1) {
                     if (Util.isInt(args[1])) {
                        int serverLimit = this.ess.getSettings().getSpawnMobLimit();
                        amount = Integer.parseInt(args[1]);
                        if (amount > serverLimit) {
                           amount = serverLimit;
                           user.sendMessage(I18n._("mobSpawnLimit"));
                        }
                     } else {
                        direction = true;
                     }
                  }

                  for(int i = 0; i < amount; ++i) {
                     Firework firework = (Firework)user.getWorld().spawnEntity(user.getLocation(), EntityType.FIREWORK);
                     FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();
                     if (direction) {
                        Vector vector = user.getEyeLocation().getDirection().multiply(0.07);
                        if (fmeta.getPower() > 1) {
                           fmeta.setPower(1);
                        }

                        firework.setVelocity(vector);
                     }

                     firework.setFireworkMeta(fmeta);
                  }
               } else {
                  MetaItemStack mStack = new MetaItemStack(stack);

                  for(String arg : args) {
                     try {
                        mStack.addFireworkMeta(user, true, arg, this.ess);
                     } catch (Exception e) {
                        user.sendMessage(I18n._("fireworkSyntax"));
                        throw e;
                     }
                  }

                  if (!mStack.isValidFirework()) {
                     user.sendMessage(I18n._("fireworkSyntax"));
                     throw new Exception(I18n._("fireworkColor"));
                  }

                  FireworkMeta fmeta = (FireworkMeta)mStack.getItemStack().getItemMeta();
                  FireworkEffect effect = mStack.getFireworkBuilder().build();
                  if (fmeta.getEffects().size() > 0 && !user.isAuthorized("essentials.firework.multiple")) {
                     throw new Exception(I18n._("multipleCharges"));
                  }

                  fmeta.addEffect(effect);
                  stack.setItemMeta(fmeta);
               }
            } else {
               FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();

               try {
                  int power = Integer.parseInt(args[1]);
                  fmeta.setPower(power > 3 ? 4 : power);
               } catch (NumberFormatException var12) {
                  throw new Exception(I18n._("invalidFireworkFormat", args[1], args[0]));
               }

               stack.setItemMeta(fmeta);
            }

         }
      } else {
         throw new Exception(I18n._("holdFirework"));
      }
   }
}
