package com.earth2me.essentials.textreader;

import com.earth2me.essentials.I18n;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;

public class TextPager {
   private final transient IText text;
   private final transient boolean onePage;

   public TextPager(IText text) {
      this(text, false);
   }

   public TextPager(IText text, boolean onePage) {
      this.text = text;
      this.onePage = onePage;
   }

   public void showPage(String pageStr, String chapterPageStr, String commandName, CommandSender sender) {
      List<String> lines = this.text.getLines();
      List<String> chapters = this.text.getChapters();
      Map<String, Integer> bookmarks = this.text.getBookmarks();
      if (pageStr != null && !pageStr.isEmpty() && !pageStr.matches("[0-9]+")) {
         int chapterpage = 0;
         if (chapterPageStr != null) {
            try {
               chapterpage = Integer.parseInt(chapterPageStr) - 1;
            } catch (Exception var15) {
               chapterpage = 0;
            }

            if (chapterpage < 0) {
               chapterpage = 0;
            }
         }

         if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH))) {
            sender.sendMessage(I18n._("infoUnknownChapter"));
         } else {
            int chapterstart = (Integer)bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;

            int chapterend;
            for(chapterend = chapterstart; chapterend < lines.size(); ++chapterend) {
               String line = (String)lines.get(chapterend);
               if (line.length() > 0 && line.charAt(0) == '#') {
                  break;
               }
            }

            int start = chapterstart + (this.onePage ? 0 : chapterpage * 9);
            int page = chapterpage + 1;
            int pages = (chapterend - chapterstart) / 9 + ((chapterend - chapterstart) % 9 > 0 ? 1 : 0);
            if (!this.onePage && commandName != null) {
               sender.sendMessage(I18n._("infoChapterPages", pageStr, page, pages));
            }

            for(int i = start; i < chapterend && i < start + (this.onePage ? 20 : 9); ++i) {
               sender.sendMessage("§r" + (String)lines.get(i));
            }

            if (!this.onePage && page < pages && commandName != null) {
               sender.sendMessage(I18n._("readNextPage", commandName, pageStr + " " + (page + 1)));
            }

         }
      } else if (!lines.isEmpty() && ((String)lines.get(0)).startsWith("#")) {
         if (!this.onePage) {
            sender.sendMessage(I18n._("infoChapter"));
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for(String string : chapters) {
               if (!first) {
                  sb.append(", ");
               }

               first = false;
               sb.append(string);
            }

            sender.sendMessage(sb.toString());
         }
      } else {
         int page = 1;

         try {
            page = Integer.parseInt(pageStr);
         } catch (Exception var16) {
            page = 1;
         }

         if (page < 1) {
            page = 1;
         }

         int start = this.onePage ? 0 : (page - 1) * 9;

         int end;
         for(end = 0; end < lines.size(); ++end) {
            String line = (String)lines.get(end);
            if (line.startsWith("#")) {
               break;
            }
         }

         int pages = end / 9 + (end % 9 > 0 ? 1 : 0);
         if (!this.onePage && commandName != null) {
            StringBuilder content = new StringBuilder();
            String[] title = commandName.split(" ", 2);
            if (title.length > 1) {
               content.append(I18n.capitalCase(title[0])).append(": ");
               content.append(title[1]);
            } else if (chapterPageStr != null) {
               content.append(I18n.capitalCase(commandName)).append(": ");
               content.append(chapterPageStr);
            } else {
               content.append(I18n.capitalCase(commandName));
            }

            sender.sendMessage(I18n._("infoPages", page, pages, content));
         }

         for(int i = start; i < end && i < start + (this.onePage ? 20 : 9); ++i) {
            sender.sendMessage("§r" + (String)lines.get(i));
         }

         if (!this.onePage && page < pages && commandName != null) {
            sender.sendMessage(I18n._("readNextPage", commandName, page + 1));
         }

      }
   }
}
