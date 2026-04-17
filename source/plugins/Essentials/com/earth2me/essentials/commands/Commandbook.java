package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Commandbook extends EssentialsCommand {
   public Commandbook() {
      super("book");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack item = user.getItemInHand();
      String player = user.getName();
      if (item.getType() == Material.WRITTEN_BOOK) {
         BookMeta bmeta = (BookMeta)item.getItemMeta();
         if (args.length > 1 && args[0].equalsIgnoreCase("author")) {
            if (!user.isAuthorized("essentals.book.author") || !this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new Exception(I18n._("denyChangeAuthor"));
            }

            bmeta.setAuthor(args[1]);
            item.setItemMeta(bmeta);
            user.sendMessage(I18n._("bookAuthorSet", getFinalArg(args, 1)));
         } else if (args.length > 1 && args[0].equalsIgnoreCase("title")) {
            if (!user.isAuthorized("essentials.book.title") || !this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new Exception(I18n._("denyChangeTitle"));
            }

            bmeta.setTitle(args[1]);
            item.setItemMeta(bmeta);
            user.sendMessage(I18n._("bookTitleSet", getFinalArg(args, 1)));
         } else {
            if (!this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new Exception(I18n._("denyBookEdit"));
            }

            ItemStack newItem = new ItemStack(Material.BOOK_AND_QUILL, item.getAmount());
            newItem.setItemMeta(bmeta);
            user.setItemInHand(newItem);
            user.sendMessage(I18n._("editBookContents"));
         }
      } else {
         if (item.getType() != Material.BOOK_AND_QUILL) {
            throw new Exception(I18n._("holdBook"));
         }

         BookMeta bmeta = (BookMeta)item.getItemMeta();
         if (!user.isAuthorized("essentals.book.author")) {
            bmeta.setAuthor(player);
         }

         ItemStack newItem = new ItemStack(Material.WRITTEN_BOOK, item.getAmount());
         newItem.setItemMeta(bmeta);
         user.setItemInHand(newItem);
         user.sendMessage(I18n._("bookLocked"));
      }

   }

   private boolean isAuthor(BookMeta bmeta, String player) {
      String author = bmeta.getAuthor();
      return author != null && author.equalsIgnoreCase(player);
   }
}
