package com.earth2me.essentials.textreader;

import com.earth2me.essentials.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookPager {
   private final transient IText text;

   public BookPager(IText text) {
      this.text = text;
   }

   public List getPages(String pageStr) throws Exception {
      List<String> lines = this.text.getLines();
      List<String> chapters = this.text.getChapters();
      List<String> pageLines = new ArrayList();
      Map<String, Integer> bookmarks = this.text.getBookmarks();
      if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH))) {
         throw new Exception(I18n._("infoUnknownChapter"));
      } else {
         int chapterstart = (Integer)bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;

         int chapterend;
         for(chapterend = chapterstart; chapterend < lines.size(); ++chapterend) {
            String line = (String)lines.get(chapterend);
            if (line.length() > 0 && line.charAt(0) == '#') {
               break;
            }
         }

         for(int lineNo = chapterstart; lineNo < chapterend; ++lineNo) {
            String pageLine = "§0" + (String)lines.get(lineNo);
            double max = (double)18.0F;
            int lineLength = pageLine.length();
            double length = (double)0.0F;
            int pointer = 0;
            int start = 0;

            for(double weight = (double)1.0F; pointer < lineLength; ++pointer) {
               Character letter = pageLine.charAt(pointer);
               if (length >= (double)18.0F || letter == 167 && length + (double)1.0F >= (double)18.0F) {
                  String tempLine = pageLine.substring(start, pointer);
                  pageLines.add(tempLine);
                  start = pointer;
                  length = (double)0.0F;
               }

               if (letter == 167 && pointer + 1 < lineLength) {
                  Character nextLetter = pageLine.charAt(pointer + 1);
                  if (nextLetter != 'l' && nextLetter != 'L') {
                     weight = (double)1.0F;
                  } else {
                     weight = (double)1.25F;
                  }

                  ++pointer;
               } else if (letter == ' ') {
                  length += 0.7 * weight;
               } else {
                  length += weight;
               }
            }

            if (length > (double)0.0F) {
               String tempLine = pageLine.substring(start, lineLength);
               pageLines.add(tempLine);
            }
         }

         List<String> pages = new ArrayList();

         for(int count = 0; count < pageLines.size(); count += 12) {
            StringBuilder newPage = new StringBuilder();

            for(int i = count; i < count + 12 && i < pageLines.size(); ++i) {
               newPage.append("\n").append((String)pageLines.get(i));
            }

            pages.add(newPage.toString());
         }

         return pages;
      }
   }
}
