package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Trade {
   private final transient String command;
   private final transient Trade fallbackTrade;
   private final transient Double money;
   private final transient ItemStack itemStack;
   private final transient Integer exp;
   private final transient IEssentials ess;
   private static FileWriter fw = null;

   public Trade(String command, IEssentials ess) {
      this(command, (Trade)null, (Double)null, (ItemStack)null, (Integer)null, ess);
   }

   public Trade(String command, Trade fallback, IEssentials ess) {
      this(command, fallback, (Double)null, (ItemStack)null, (Integer)null, ess);
   }

   public Trade(double money, IEssentials ess) {
      this((String)null, (Trade)null, money, (ItemStack)null, (Integer)null, ess);
   }

   public Trade(ItemStack items, IEssentials ess) {
      this((String)null, (Trade)null, (Double)null, items, (Integer)null, ess);
   }

   public Trade(int exp, IEssentials ess) {
      this((String)null, (Trade)null, (Double)null, (ItemStack)null, exp, ess);
   }

   private Trade(String command, Trade fallback, Double money, ItemStack item, Integer exp, IEssentials ess) {
      this.command = command;
      this.fallbackTrade = fallback;
      this.money = money;
      this.itemStack = item;
      this.exp = exp;
      this.ess = ess;
   }

   public void isAffordableFor(IUser user) throws ChargeException {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "checking if " + user.getName() + " can afford charge.");
      }

      if (this.getMoney() != null && this.getMoney() > (double)0.0F && !user.canAfford(this.getMoney())) {
         throw new ChargeException(I18n._("notEnoughMoney"));
      } else if (this.getItemStack() != null && !user.getInventory().containsAtLeast(this.itemStack, this.itemStack.getAmount())) {
         throw new ChargeException(I18n._("missingItems", this.getItemStack().getAmount(), this.getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")));
      } else {
         double money;
         if (this.command != null && !this.command.isEmpty() && (double)0.0F < (money = this.getCommandCost(user)) && !user.canAfford(money)) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         } else if (this.exp != null && this.exp > 0 && SetExpFix.getTotalExperience(user) < this.exp) {
            throw new ChargeException(I18n._("notEnoughExperience"));
         }
      }
   }

   public void pay(IUser user) {
      this.pay(user, true);
   }

   public boolean pay(IUser user, boolean dropItems) {
      boolean success = true;
      if (this.getMoney() != null && this.getMoney() > (double)0.0F) {
         user.giveMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         if (!dropItems) {
            success = InventoryWorkaround.addAllItems(user.getInventory(), this.getItemStack());
         } else {
            Map<Integer, ItemStack> leftOver = InventoryWorkaround.addItems(user.getInventory(), this.getItemStack());
            Location loc = user.getLocation();

            for(ItemStack itemStack : leftOver.values()) {
               int maxStackSize = itemStack.getType().getMaxStackSize();
               int stacks = itemStack.getAmount() / maxStackSize;
               int leftover = itemStack.getAmount() % maxStackSize;
               Item[] itemStacks = new Item[stacks + (leftover > 0 ? 1 : 0)];

               for(int i = 0; i < stacks; ++i) {
                  ItemStack stack = itemStack.clone();
                  stack.setAmount(maxStackSize);
                  itemStacks[i] = loc.getWorld().dropItem(loc, stack);
               }

               if (leftover > 0) {
                  ItemStack stack = itemStack.clone();
                  stack.setAmount(leftover);
                  itemStacks[stacks] = loc.getWorld().dropItem(loc, stack);
               }
            }
         }

         user.updateInventory();
      }

      if (this.getExperience() != null) {
         SetExpFix.setTotalExperience(user, SetExpFix.getTotalExperience(user) + this.getExperience());
      }

      return success;
   }

   public void charge(IUser user) throws ChargeException {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "charging user " + user.getName());
      }

      if (this.getMoney() != null) {
         if (!user.canAfford(this.getMoney()) && this.getMoney() > (double)0.0F) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         }

         user.takeMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         if (!user.getInventory().containsAtLeast(this.itemStack, this.itemStack.getAmount())) {
            throw new ChargeException(I18n._("missingItems", this.getItemStack().getAmount(), this.getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")));
         }

         user.getInventory().removeItem(new ItemStack[]{this.getItemStack()});
         user.updateInventory();
      }

      if (this.command != null) {
         double cost = this.getCommandCost(user);
         if (!user.canAfford(cost) && cost > (double)0.0F) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         }

         user.takeMoney(cost);
      }

      if (this.getExperience() != null) {
         int experience = SetExpFix.getTotalExperience(user);
         if (experience < this.getExperience() && this.getExperience() > 0) {
            throw new ChargeException(I18n._("notEnoughExperience"));
         }

         SetExpFix.setTotalExperience(user, experience - this.getExperience());
      }

   }

   public Double getMoney() {
      return this.money;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public Integer getExperience() {
      return this.exp;
   }

   public TradeType getType() {
      if (this.getExperience() != null) {
         return Trade.TradeType.EXP;
      } else {
         return this.getItemStack() != null ? Trade.TradeType.ITEM : Trade.TradeType.MONEY;
      }
   }

   public Double getCommandCost(IUser user) {
      double cost = (double)0.0F;
      if (this.command != null && !this.command.isEmpty()) {
         cost = this.ess.getSettings().getCommandCost(this.command.charAt(0) == '/' ? this.command.substring(1) : this.command);
         if (cost == (double)0.0F && this.fallbackTrade != null) {
            cost = this.fallbackTrade.getCommandCost(user);
         }

         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "calculated command (" + this.command + ") cost for " + user.getName() + " as " + cost);
         }
      }

      return cost == (double)0.0F || !user.isAuthorized("essentials.nocommandcost.all") && !user.isAuthorized("essentials.nocommandcost." + this.command) ? cost : (double)0.0F;
   }

   public static void log(String type, String subtype, String event, String sender, Trade charge, String receiver, Trade pay, Location loc, IEssentials ess) {
      if ((loc != null || ess.getSettings().isEcoLogUpdateEnabled()) && (loc == null || ess.getSettings().isEcoLogEnabled())) {
         if (fw == null) {
            try {
               fw = new FileWriter(new File(ess.getDataFolder(), "trade.log"), true);
            } catch (IOException ex) {
               Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
            }
         }

         StringBuilder sb = new StringBuilder();
         sb.append(type).append(",").append(subtype).append(",").append(event).append(",\"");
         sb.append(DateFormat.getDateTimeInstance(0, 0).format(new Date()));
         sb.append("\",\"");
         if (sender != null) {
            sb.append(sender);
         }

         sb.append("\",");
         if (charge == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (charge.getItemStack() != null) {
               sb.append(charge.getItemStack().getAmount()).append(",");
               sb.append(charge.getItemStack().getType().toString()).append(",");
               sb.append(charge.getItemStack().getDurability());
            }

            if (charge.getMoney() != null) {
               sb.append(charge.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (charge.getExperience() != null) {
               sb.append(charge.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         sb.append(",\"");
         if (receiver != null) {
            sb.append(receiver);
         }

         sb.append("\",");
         if (pay == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (pay.getItemStack() != null) {
               sb.append(pay.getItemStack().getAmount()).append(",");
               sb.append(pay.getItemStack().getType().toString()).append(",");
               sb.append(pay.getItemStack().getDurability());
            }

            if (pay.getMoney() != null) {
               sb.append(pay.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (pay.getExperience() != null) {
               sb.append(pay.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         if (loc == null) {
            sb.append(",\"\",\"\",\"\",\"\"");
         } else {
            sb.append(",\"");
            sb.append(loc.getWorld().getName()).append("\",");
            sb.append(loc.getBlockX()).append(",");
            sb.append(loc.getBlockY()).append(",");
            sb.append(loc.getBlockZ()).append(",");
         }

         sb.append("\n");

         try {
            fw.write(sb.toString());
            fw.flush();
         } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
         }

      }
   }

   public static void closeLog() {
      if (fw != null) {
         try {
            fw.close();
         } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
         }

         fw = null;
      }

   }

   public static enum TradeType {
      MONEY,
      EXP,
      ITEM;
   }
}
