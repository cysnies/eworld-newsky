package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

public class EssentialsSign {
   private static final Set EMPTY_SET = new HashSet();
   protected final transient String signName;

   public EssentialsSign(String signName) {
      this.signName = signName;
   }

   public final boolean onSignCreate(SignChangeEvent event, IEssentials ess) {
      ISign sign = new EventSign(event);
      User user = ess.getUser(event.getPlayer());
      if (!user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".create") && !user.isAuthorized("essentials.signs.create." + this.signName.toLowerCase(Locale.ENGLISH))) {
         return true;
      } else {
         sign.setLine(0, I18n._("signFormatFail", this.signName));

         try {
            boolean ret = this.onSignCreate(sign, user, this.getUsername(user), ess);
            if (ret) {
               sign.setLine(0, this.getSuccessName());
            }

            return ret;
         } catch (ChargeException ex) {
            ess.showError(user, ex, this.signName);
         } catch (SignException ex) {
            ess.showError(user, ex, this.signName);
         }

         return true;
      }
   }

   public String getSuccessName() {
      return I18n._("signFormatSuccess", this.signName);
   }

   public String getTemplateName() {
      return I18n._("signFormatTemplate", this.signName);
   }

   private String getUsername(User user) {
      return user.getName().substring(0, user.getName().length() > 13 ? 13 : user.getName().length());
   }

   public final boolean onSignInteract(Block block, Player player, IEssentials ess) {
      ISign sign = new BlockSign(block);
      User user = ess.getUser(player);
      if (user.checkSignThrottle()) {
         return false;
      } else {
         try {
            return !user.isDead() && (user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".use") || user.isAuthorized("essentials.signs.use." + this.signName.toLowerCase(Locale.ENGLISH))) && this.onSignInteract(sign, user, this.getUsername(user), ess);
         } catch (ChargeException ex) {
            ess.showError(user, ex, this.signName);
            return false;
         } catch (SignException ex) {
            ess.showError(user, ex, this.signName);
            return false;
         }
      }
   }

   public final boolean onSignBreak(Block block, Player player, IEssentials ess) {
      ISign sign = new BlockSign(block);
      User user = ess.getUser(player);

      try {
         return (user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".break") || user.isAuthorized("essentials.signs.break." + this.signName.toLowerCase(Locale.ENGLISH))) && this.onSignBreak(sign, user, this.getUsername(user), ess);
      } catch (SignException ex) {
         ess.showError(user, ex, this.signName);
         return false;
      }
   }

   protected boolean onSignCreate(ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onSignInteract(ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onSignBreak(ISign sign, User player, String username, IEssentials ess) throws SignException {
      return true;
   }

   public final boolean onBlockPlace(Block block, Player player, IEssentials ess) {
      User user = ess.getUser(player);

      try {
         return this.onBlockPlace(block, user, this.getUsername(user), ess);
      } catch (ChargeException ex) {
         ess.showError(user, ex, this.signName);
      } catch (SignException ex) {
         ess.showError(user, ex, this.signName);
      }

      return false;
   }

   public final boolean onBlockInteract(Block block, Player player, IEssentials ess) {
      User user = ess.getUser(player);

      try {
         return this.onBlockInteract(block, user, this.getUsername(user), ess);
      } catch (ChargeException ex) {
         ess.showError(user, ex, this.signName);
      } catch (SignException ex) {
         ess.showError(user, ex, this.signName);
      }

      return false;
   }

   public final boolean onBlockBreak(Block block, Player player, IEssentials ess) {
      User user = ess.getUser(player);

      try {
         return this.onBlockBreak(block, user, this.getUsername(user), ess);
      } catch (SignException ex) {
         ess.showError(user, ex, this.signName);
         return false;
      }
   }

   public boolean onBlockBreak(Block block, IEssentials ess) {
      return true;
   }

   public boolean onBlockExplode(Block block, IEssentials ess) {
      return true;
   }

   public boolean onBlockBurn(Block block, IEssentials ess) {
      return true;
   }

   public boolean onBlockIgnite(Block block, IEssentials ess) {
      return true;
   }

   public boolean onBlockPush(Block block, IEssentials ess) {
      return true;
   }

   public static boolean checkIfBlockBreaksSigns(Block block) {
      Block sign = block.getRelative(BlockFace.UP);
      if (sign.getType() == Material.SIGN_POST && isValidSign(new BlockSign(sign))) {
         return true;
      } else {
         BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

         for(BlockFace blockFace : directions) {
            Block signblock = block.getRelative(blockFace);
            if (signblock.getType() == Material.WALL_SIGN) {
               Sign signMat = (Sign)signblock.getState().getData();
               if (signMat != null && signMat.getFacing() == blockFace && isValidSign(new BlockSign(signblock))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static boolean isValidSign(ISign sign) {
      return sign.getLine(0).matches("§1\\[.*\\]");
   }

   protected boolean onBlockPlace(Block block, User player, String username, IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onBlockInteract(Block block, User player, String username, IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onBlockBreak(Block block, User player, String username, IEssentials ess) throws SignException {
      return true;
   }

   public Set getBlocks() {
      return EMPTY_SET;
   }

   public boolean areHeavyEventRequired() {
      return false;
   }

   protected final void validateTrade(ISign sign, int index, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (!line.isEmpty()) {
         Trade trade = this.getTrade(sign, index, 0, ess);
         Double money = trade.getMoney();
         if (money != null) {
            sign.setLine(index, Util.shortCurrency(money, ess));
         }

      }
   }

   protected final void validateTrade(ISign sign, int amountIndex, int itemIndex, User player, IEssentials ess) throws SignException {
      if (!sign.getLine(itemIndex).equalsIgnoreCase("exp") && !sign.getLine(itemIndex).equalsIgnoreCase("xp")) {
         Trade trade = this.getTrade(sign, amountIndex, itemIndex, player, ess);
         ItemStack item = trade.getItemStack();
         sign.setLine(amountIndex, Integer.toString(item.getAmount()));
         sign.setLine(itemIndex, sign.getLine(itemIndex).trim());
      } else {
         int amount = this.getIntegerPositive(sign.getLine(amountIndex));
         sign.setLine(amountIndex, Integer.toString(amount));
         sign.setLine(itemIndex, "exp");
      }
   }

   protected final Trade getTrade(ISign sign, int amountIndex, int itemIndex, User player, IEssentials ess) throws SignException {
      if (!sign.getLine(itemIndex).equalsIgnoreCase("exp") && !sign.getLine(itemIndex).equalsIgnoreCase("xp")) {
         ItemStack item = this.getItemStack(sign.getLine(itemIndex), 1, ess);
         int amount = Math.min(this.getIntegerPositive(sign.getLine(amountIndex)), item.getType().getMaxStackSize() * player.getInventory().getSize());
         if (item.getTypeId() != 0 && amount >= 1) {
            item.setAmount(amount);
            return new Trade(item, ess);
         } else {
            throw new SignException(I18n._("moreThanZero"));
         }
      } else {
         int amount = this.getIntegerPositive(sign.getLine(amountIndex));
         return new Trade(amount, ess);
      }
   }

   protected final void validateInteger(ISign sign, int index) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("Empty line " + index);
      } else {
         int quantity = this.getIntegerPositive(line);
         sign.setLine(index, Integer.toString(quantity));
      }
   }

   protected final int getIntegerPositive(String line) throws SignException {
      int quantity = this.getInteger(line);
      if (quantity < 1) {
         throw new SignException(I18n._("moreThanZero"));
      } else {
         return quantity;
      }
   }

   protected final int getInteger(String line) throws SignException {
      try {
         int quantity = Integer.parseInt(line);
         return quantity;
      } catch (NumberFormatException ex) {
         throw new SignException("Invalid sign", ex);
      }
   }

   protected final ItemStack getItemStack(String itemName, int quantity, IEssentials ess) throws SignException {
      try {
         ItemStack item = ess.getItemDb().get(itemName);
         item.setAmount(quantity);
         return item;
      } catch (Exception ex) {
         throw new SignException(ex.getMessage(), ex);
      }
   }

   protected final Double getMoney(String line) throws SignException {
      boolean isMoney = line.matches("^[^0-9-\\.][\\.0-9]+$");
      return isMoney ? this.getDoublePositive(line.substring(1)) : null;
   }

   protected final Double getDoublePositive(String line) throws SignException {
      double quantity = this.getDouble(line);
      if ((double)Math.round(quantity * (double)100.0F) < (double)1.0F) {
         throw new SignException(I18n._("moreThanZero"));
      } else {
         return quantity;
      }
   }

   protected final Double getDouble(String line) throws SignException {
      try {
         return Double.parseDouble(line);
      } catch (NumberFormatException ex) {
         throw new SignException(ex.getMessage(), ex);
      }
   }

   protected final Trade getTrade(ISign sign, int index, IEssentials ess) throws SignException {
      return this.getTrade(sign, index, 1, ess);
   }

   protected final Trade getTrade(ISign sign, int index, int decrement, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         return new Trade(this.signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
      } else {
         Double money = this.getMoney(line);
         if (money == null) {
            String[] split = line.split("[ :]+", 2);
            if (split.length != 2) {
               throw new SignException(I18n._("invalidCharge"));
            } else {
               int quantity = this.getIntegerPositive(split[0]);
               String item = split[1].toLowerCase(Locale.ENGLISH);
               if (item.equalsIgnoreCase("times")) {
                  sign.setLine(index, quantity - decrement + " times");
                  sign.updateSign();
                  return new Trade(this.signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
               } else if (!item.equalsIgnoreCase("exp") && !item.equalsIgnoreCase("xp")) {
                  ItemStack stack = this.getItemStack(item, quantity, ess);
                  sign.setLine(index, quantity + " " + item);
                  return new Trade(stack, ess);
               } else {
                  sign.setLine(index, quantity + " exp");
                  return new Trade(quantity, ess);
               }
            }
         } else {
            return new Trade(money, ess);
         }
      }
   }

   static class EventSign implements ISign {
      private final transient SignChangeEvent event;
      private final transient Block block;
      private final transient org.bukkit.block.Sign sign;

      EventSign(SignChangeEvent event) {
         this.event = event;
         this.block = event.getBlock();
         this.sign = (org.bukkit.block.Sign)this.block.getState();
      }

      public final String getLine(int index) {
         return this.event.getLine(index);
      }

      public final void setLine(int index, String text) {
         this.event.setLine(index, text);
         this.sign.setLine(index, text);
         this.updateSign();
      }

      public Block getBlock() {
         return this.block;
      }

      public void updateSign() {
         this.sign.update();
      }
   }

   static class BlockSign implements ISign {
      private final transient org.bukkit.block.Sign sign;
      private final transient Block block;

      BlockSign(Block block) {
         this.block = block;
         this.sign = (org.bukkit.block.Sign)block.getState();
      }

      public final String getLine(int index) {
         return this.sign.getLine(index);
      }

      public final void setLine(int index, String text) {
         this.sign.setLine(index, text);
      }

      public final Block getBlock() {
         return this.block;
      }

      public final void updateSign() {
         this.sign.update();
      }
   }

   public interface ISign {
      String getLine(int var1);

      void setLine(int var1, String var2);

      Block getBlock();

      void updateSign();
   }
}
