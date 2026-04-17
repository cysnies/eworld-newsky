package com.goncalomb.bukkit.nbteditor.commands;

import com.goncalomb.bukkit.EntityTypeMap;
import com.goncalomb.bukkit.UtilsMc;
import com.goncalomb.bukkit.betterplugin.BetterCommand;
import com.goncalomb.bukkit.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.betterplugin.BetterCommandType;
import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.betterplugin.SubCommand;
import com.goncalomb.bukkit.nbteditor.bos.BookOfSouls;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.FallingBlockNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.Attribute;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeContainer;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeType;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CommandBOS extends BetterCommand {
   public CommandBOS() {
      super("bookofsouls", "nbteditor.bookofsouls");
      this.setAlises(new String[]{"bos"});
      this.setDescription(Lang._("nbt.cmds.bos.description"));
   }

   static BookOfSouls getBos(Player player) throws BetterCommandException {
      return getBos(player, false);
   }

   static BookOfSouls getBos(Player player, boolean nullIfMissing) throws BetterCommandException {
      ItemStack item = player.getItemInHand();
      if (BookOfSouls.isValidBook(item)) {
         BookOfSouls bos = BookOfSouls.getFromBook(item);
         if (bos != null) {
            return bos;
         } else {
            throw new BetterCommandException(Lang._("nbt.bos.corrupted"));
         }
      } else if (!nullIfMissing) {
         throw new BetterCommandException(Lang._("nbt.cmds.bos.holding"));
      } else {
         return null;
      }
   }

   @SubCommand.Command(
      args = "get",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 1,
      usage = "<entity>"
   )
   public boolean getCommand(CommandSender sender, String[] args) {
      if (args.length == 1) {
         EntityType entityType = EntityTypeMap.getByName(args[0]);
         if (entityType != null && EntityNBT.isValidType(entityType)) {
            PlayerInventory inv = ((Player)sender).getInventory();
            if (inv.firstEmpty() == -1) {
               sender.sendMessage(Lang._("common.inventory-full"));
               return true;
            }

            BookOfSouls bos = new BookOfSouls(EntityNBT.fromEntityType(entityType));
            inv.addItem(new ItemStack[]{bos.getBook()});
            sender.sendMessage(Lang._("nbt.cmds.bos.give"));
            if (entityType == EntityType.ENDERMAN) {
               sender.sendMessage(ChatColor.YELLOW + "(Enderman's carring block id is limited to 127 due to a minecraft bug)");
            }

            return true;
         }

         sender.sendMessage(Lang._("nbt.invalid-entity"));
      }

      sender.sendMessage(Lang._("nbt.entities-prefix") + EntityTypeMap.getEntityNames(EntityNBT.getValidEntityTypes()));
      return false;
   }

   @SubCommand.Command(
      args = "getempty",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean getemptyCommand(CommandSender sender, String[] args) {
      PlayerInventory inv = ((Player)sender).getInventory();
      if (inv.firstEmpty() == -1) {
         sender.sendMessage(Lang._("common.inventory-full"));
         return true;
      } else {
         inv.addItem(new ItemStack[]{BookOfSouls.getEmpty()});
         sender.sendMessage(Lang._("nbt.cmds.bos.give"));
         return true;
      }
   }

   @SubCommand.Command(
      args = "var",
      type = BetterCommandType.PLAYER_ONLY,
      minargs = 1,
      maxargs = Integer.MAX_VALUE,
      usage = "<variable> [value]"
   )
   public boolean varCommand(CommandSender sender, String[] args) throws BetterCommandException {
      BookOfSouls bos = getBos((Player)sender);
      NBTVariable variable = bos.getEntityNBT().getVariable(args[0]);
      if (variable != null) {
         if (args.length >= 2) {
            if (bos.getEntityNBT() instanceof FallingBlockNBT && ((FallingBlockNBT)bos.getEntityNBT()).hasTileEntityData() && variable.getName().equals("block")) {
               sender.sendMessage(Lang._("nbt.cmds.bos.falling-block-nop"));
               return true;
            }

            String value = UtilsMc.parseColors(StringUtils.join(args, " ", 1, args.length));
            if (variable.setValue(value)) {
               bos.saveBook();
               sender.sendMessage(Lang._("nbt.variable.updated"));
               return true;
            }

            sender.sendMessage(Lang._format("nbt.variable.invalid-format", args[0]));
         }

         sender.sendMessage(ChatColor.YELLOW + variable.getFormat());
      } else {
         sender.sendMessage(Lang._format("nbt.cmds.bos.no-variable", args[0]));
      }

      return true;
   }

   @SubCommand.Command(
      args = "clearvar",
      type = BetterCommandType.PLAYER_ONLY,
      minargs = 1,
      usage = "<variable>"
   )
   public boolean clearvarCommand(CommandSender sender, String[] args) throws BetterCommandException {
      BookOfSouls bos = getBos((Player)sender);
      NBTVariable variable = bos.getEntityNBT().getVariable(args[0]);
      if (variable != null) {
         variable.clear();
         bos.saveBook();
         sender.sendMessage(Lang._("nbt.variable.cleared"));
      } else {
         sender.sendMessage(Lang._format("nbt.variable.invalid-format", args[0]));
      }

      return true;
   }

   @SubCommand.Command(
      args = "riding",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean ridingCommand(CommandSender sender, String[] args) throws BetterCommandException {
      Player player = (Player)sender;
      BookOfSouls bos = getBos(player);
      bos.openRidingInventory(player);
      return true;
   }

   @SubCommand.Command(
      args = "items",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean itemsCommand(CommandSender sender, String[] args) throws BetterCommandException {
      Player player = (Player)sender;
      BookOfSouls bos = getBos(player);
      if (!bos.openInventory(player)) {
         player.sendMessage(Lang._("nbt.cmds.bos.no-inventory"));
      }

      return true;
   }

   @SubCommand.Command(
      args = "offers",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean offersCommand(CommandSender sender, String[] args) throws BetterCommandException {
      Player player = (Player)sender;
      if (!getBos(player).openOffersInventory(player)) {
         player.sendMessage(Lang._("nbt.cmds.bos.no-villager"));
      } else {
         player.sendMessage(Lang._("nbt.cmds.bos.villager-info"));
      }

      return true;
   }

   @SubCommand.Command(
      args = "dropchance",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 5,
      usage = "[<head> <chest> <legs> <feet> <hand>]"
   )
   public boolean dropchanceCommand(CommandSender sender, String[] args) throws BetterCommandException {
      Player player = (Player)sender;
      if (args.length == 0) {
         BookOfSouls bos = getBos(player);
         if (!bos.clearMobDropChance()) {
            player.sendMessage(Lang._("nbt.cmds.bos.no-mob"));
         } else {
            bos.saveBook();
            player.sendMessage(Lang._("nbt.cmds.bos.drop-chance.cleared"));
         }

         return true;
      } else if (args.length == 5) {
         float head = 0.0F;
         float chest = 0.0F;
         float legs = 0.0F;
         float feet = 0.0F;
         float hand = 0.0F;
         boolean invalid = false;

         try {
            head = Float.parseFloat(args[0]);
            chest = Float.parseFloat(args[1]);
            legs = Float.parseFloat(args[2]);
            feet = Float.parseFloat(args[3]);
            hand = Float.parseFloat(args[4]);
         } catch (NumberFormatException var11) {
            invalid = true;
         }

         if (!invalid && !(head < 0.0F) && !(head > 1.0F) && !(chest < 0.0F) && !(chest > 1.0F) && !(legs < 0.0F) && !(legs > 1.0F) && !(feet < 0.0F) && !(feet > 1.0F) && !(hand < 0.0F) && !(hand > 1.0F)) {
            BookOfSouls bos = getBos(player);
            if (!bos.setMobDropChance(head, chest, legs, feet, hand)) {
               player.sendMessage(Lang._("nbt.cmds.bos.no-mob"));
            } else {
               bos.saveBook();
               player.sendMessage(Lang._("nbt.cmds.bos.drop-chance.set"));
            }

            return true;
         } else {
            player.sendMessage(Lang._("nbt.cmds.bos.drop-chance.invalid"));
            return true;
         }
      } else {
         return false;
      }
   }

   @SubCommand.Command(
      args = "attr add",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 2,
      usage = "<attribute> <base>"
   )
   public boolean attr_addCommand(CommandSender sender, String[] args) throws BetterCommandException {
      if (args.length == 2) {
         BookOfSouls bos = getBos((Player)sender);
         EntityNBT entityNbt = bos.getEntityNBT();
         if (!(entityNbt instanceof MobNBT)) {
            sender.sendMessage(Lang._("nbt.cmds.bos.no-mob"));
            return true;
         }

         AttributeType attributeType = AttributeType.getByName(args[0]);
         if (attributeType != null) {
            double base;
            try {
               base = Double.parseDouble(args[1]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(Lang._("nbt.cmds.bos.attr-invalid-base"));
               return true;
            }

            AttributeContainer attributes = ((MobNBT)entityNbt).getAttributes();
            attributes.setAttribute(new Attribute(attributeType, base));
            ((MobNBT)entityNbt).setAttributes(attributes);
            bos.saveBook();
            sender.sendMessage(Lang._("nbt.cmds.bos.attr-add"));
            return true;
         }

         sender.sendMessage(Lang._("nbt.cmds.bos.attr-invalid"));
      }

      sender.sendMessage(Lang._("nbt.attributes-prefix") + StringUtils.join(AttributeType.values(), ", "));
      return false;
   }

   @SubCommand.Command(
      args = "attr del",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 1,
      usage = "<attribute>"
   )
   public boolean attr_delCommand(CommandSender sender, String[] args) throws BetterCommandException {
      if (args.length == 1) {
         BookOfSouls bos = getBos((Player)sender);
         EntityNBT entityNbt = bos.getEntityNBT();
         if (!(entityNbt instanceof MobNBT)) {
            sender.sendMessage(Lang._("nbt.cmds.bos.no-mob"));
            return true;
         }

         AttributeType attributeType = AttributeType.getByName(args[0]);
         if (attributeType != null) {
            AttributeContainer attributes = ((MobNBT)entityNbt).getAttributes();
            if (attributes.removeAttribute(attributeType) != null) {
               ((MobNBT)entityNbt).setAttributes(attributes);
               bos.saveBook();
               sender.sendMessage(Lang._("nbt.cmds.bos.attr-del"));
               return true;
            }

            sender.sendMessage(Lang._format("nbt.cmds.bos.attr-nop", attributeType.toString()));
            return true;
         }

         sender.sendMessage(Lang._("nbt.cmds.bos.attr-invalid"));
      }

      sender.sendMessage(Lang._("nbt.attributes-prefix") + StringUtils.join(AttributeType.values(), ", "));
      return false;
   }

   @SubCommand.Command(
      args = "attr delall",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean attr_delallCommand(CommandSender sender, String[] args) throws BetterCommandException {
      BookOfSouls bos = getBos((Player)sender);
      EntityNBT entityNbt = bos.getEntityNBT();
      if (entityNbt instanceof MobNBT) {
         ((MobNBT)entityNbt).setAttributes((AttributeContainer)null);
         bos.saveBook();
         sender.sendMessage(Lang._("nbt.cmds.bos.attr-cleared"));
         return true;
      } else {
         sender.sendMessage(Lang._("nbt.cmds.bos.no-mob"));
         return true;
      }
   }
}
