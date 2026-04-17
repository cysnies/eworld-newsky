package com.earth2me.essentials;

import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Kit {
   public static String listKits(IEssentials ess, User user) throws Exception {
      try {
         ConfigurationSection kits = ess.getSettings().getKits();
         StringBuilder list = new StringBuilder();

         for(String kiteItem : kits.getKeys(false)) {
            if (user == null || user.isAuthorized("essentials.kits." + kiteItem.toLowerCase(Locale.ENGLISH))) {
               list.append(" ").append(I18n.capitalCase(kiteItem));
            }
         }

         return list.toString().trim();
      } catch (Exception ex) {
         throw new Exception(I18n._("kitError"), ex);
      }
   }

   public static void checkTime(User user, String kitName, Map els) throws Exception {
      if (!user.isAuthorized("essentials.kit.exemptdelay")) {
         Calendar time = new GregorianCalendar();
         double delay = els.containsKey("delay") ? ((Number)els.get("delay")).doubleValue() : (double)0.0F;
         Calendar earliestTime = new GregorianCalendar();
         earliestTime.add(13, -((int)delay));
         earliestTime.add(14, -((int)(delay * (double)1000.0F % (double)1000.0F)));
         long earliestLong = earliestTime.getTimeInMillis();
         long lastTime = user.getKitTimestamp(kitName);
         if (lastTime >= earliestLong && lastTime != 0L) {
            if (lastTime <= time.getTimeInMillis()) {
               if (earliestLong < 0L) {
                  user.sendMessage(I18n._("kitOnce"));
                  throw new NoChargeException();
               }

               time.setTimeInMillis(lastTime);
               time.add(13, (int)delay);
               time.add(14, (int)(delay * (double)1000.0F % (double)1000.0F));
               user.sendMessage(I18n._("kitTimed", Util.formatDateDiff(time.getTimeInMillis())));
               throw new NoChargeException();
            }

            user.setKitTimestamp(kitName, time.getTimeInMillis());
         } else {
            user.setKitTimestamp(kitName, time.getTimeInMillis());
         }

      }
   }

   public static List getItems(User user, Map kit) throws Exception {
      if (kit == null) {
         throw new Exception(I18n._("kitError2"));
      } else {
         try {
            return (List)kit.get("items");
         } catch (Exception e) {
            user.sendMessage(I18n._("kitError2"));
            throw new Exception(I18n._("kitErrorHelp"), e);
         }
      }
   }

   public static void expandItems(IEssentials ess, User user, List items) throws Exception {
      try {
         boolean spew = false;
         boolean allowUnsafe = ess.getSettings().allowUnsafeEnchantments();

         for(String d : items) {
            if (d.startsWith(ess.getSettings().getCurrencySymbol())) {
               Double value = Double.parseDouble(d.substring(ess.getSettings().getCurrencySymbol().length()).trim());
               Trade t = new Trade(value, ess);
               t.pay(user);
            } else {
               String[] parts = d.split(" ");
               ItemStack parseStack = ess.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
               MetaItemStack metaStack = new MetaItemStack(parseStack);
               if (parts.length > 2) {
                  metaStack.parseStringMeta((CommandSender)null, allowUnsafe, parts, 2, ess);
               }

               Map<Integer, ItemStack> overfilled;
               if (user.isAuthorized("essentials.oversizedstacks")) {
                  overfilled = InventoryWorkaround.addOversizedItems(user.getInventory(), ess.getSettings().getOversizedStackSize(), metaStack.getItemStack());
               } else {
                  overfilled = InventoryWorkaround.addItems(user.getInventory(), metaStack.getItemStack());
               }

               for(ItemStack itemStack : overfilled.values()) {
                  user.getWorld().dropItemNaturally(user.getLocation(), itemStack);
                  spew = true;
               }
            }
         }

         user.updateInventory();
         if (spew) {
            user.sendMessage(I18n._("kitInvFull"));
         }

      } catch (Exception var13) {
         user.updateInventory();
         if (ess.getSettings().isDebug()) {
            ess.getLogger().log(Level.WARNING, var13.getMessage());
         } else {
            ess.getLogger().log(Level.WARNING, var13.getMessage());
         }

         throw new Exception(I18n._("kitError2"), var13);
      }
   }
}
