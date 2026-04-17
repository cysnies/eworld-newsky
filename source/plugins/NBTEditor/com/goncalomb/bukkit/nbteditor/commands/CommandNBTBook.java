package com.goncalomb.bukkit.nbteditor.commands;

import com.goncalomb.bukkit.UtilsMc;
import com.goncalomb.bukkit.betterplugin.BetterCommand;
import com.goncalomb.bukkit.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.betterplugin.BetterCommandType;
import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.betterplugin.SubCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

public class CommandNBTBook extends BetterCommand {
   public CommandNBTBook() {
      super("nbtbook", "nbteditor.book");
      this.setAlises(new String[]{"nbtb"});
      this.setDescription(Lang._("nbt.cmds.nbtb.description"));
   }

   @SubCommand.Command(
      args = "colors",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean colorsCommand(CommandSender sender, String[] args) throws BetterCommandException {
      HandItemWrapper.Book item = new HandItemWrapper.Book((Player)sender, HandItemWrapper.Book.BookType.BOTH);
      if (((BookMeta)item.meta).hasTitle()) {
         ((BookMeta)item.meta).setTitle(UtilsMc.parseColors(((BookMeta)item.meta).getTitle()));
      }

      if (((BookMeta)item.meta).hasAuthor()) {
         ((BookMeta)item.meta).setAuthor(UtilsMc.parseColors(((BookMeta)item.meta).getAuthor()));
      }

      int i = 1;

      for(int l = ((BookMeta)item.meta).getPageCount(); i <= l; ++i) {
         ((BookMeta)item.meta).setPage(i, UtilsMc.parseColors(((BookMeta)item.meta).getPage(i)));
      }

      item.save();
      sender.sendMessage(Lang._("nbt.cmds.nbtb.colors"));
      return true;
   }

   @SubCommand.Command(
      args = "title",
      type = BetterCommandType.PLAYER_ONLY,
      minargs = 1,
      maxargs = Integer.MAX_VALUE,
      usage = "<title>"
   )
   public boolean titleCommand(CommandSender sender, String[] args) throws BetterCommandException {
      HandItemWrapper.Book item = new HandItemWrapper.Book((Player)sender, HandItemWrapper.Book.BookType.WRITTEN);
      ((BookMeta)item.meta).setTitle(UtilsMc.parseColors(UtilsMc.parseColors(StringUtils.join(args, " "))));
      item.save();
      sender.sendMessage(Lang._("nbt.cmds.nbtb.title"));
      return true;
   }

   @SubCommand.Command(
      args = "author",
      type = BetterCommandType.PLAYER_ONLY,
      minargs = 1,
      maxargs = Integer.MAX_VALUE,
      usage = "<author>"
   )
   public boolean authorCommand(CommandSender sender, String[] args) throws BetterCommandException {
      HandItemWrapper.Book item = new HandItemWrapper.Book((Player)sender, HandItemWrapper.Book.BookType.WRITTEN);
      ((BookMeta)item.meta).setAuthor(UtilsMc.parseColors(UtilsMc.parseColors(StringUtils.join(args, " "))));
      item.save();
      sender.sendMessage(Lang._("nbt.cmds.nbtb.author"));
      return true;
   }

   @SubCommand.Command(
      args = "unsign",
      type = BetterCommandType.PLAYER_ONLY
   )
   public boolean unsignCommand(CommandSender sender, String[] args) throws BetterCommandException {
      HandItemWrapper.Book item = new HandItemWrapper.Book((Player)sender, HandItemWrapper.Book.BookType.WRITTEN);
      ((BookMeta)item.meta).setTitle((String)null);
      ((BookMeta)item.meta).setAuthor((String)null);
      item.item.setType(Material.BOOK_AND_QUILL);
      item.save();
      sender.sendMessage(Lang._("nbt.cmds.nbtb.unsign"));
      return true;
   }
}
