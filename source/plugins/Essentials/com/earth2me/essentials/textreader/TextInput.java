package com.earth2me.essentials.textreader;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TextInput implements IText {
   private final transient List lines;
   private final transient List chapters;
   private final transient Map bookmarks;
   private final transient long lastChange;
   private static final HashMap cache = new HashMap();

   public TextInput(CommandSender sender, String filename, boolean createFile, IEssentials ess) throws IOException {
      File file = null;
      if (sender instanceof Player) {
         User user = ess.getUser(sender);
         file = new File(ess.getDataFolder(), filename + "_" + Util.sanitizeFileName(user.getName()) + ".txt");
         if (!file.exists()) {
            file = new File(ess.getDataFolder(), filename + "_" + Util.sanitizeFileName(user.getGroup()) + ".txt");
         }
      }

      if (file == null || !file.exists()) {
         file = new File(ess.getDataFolder(), filename + ".txt");
      }

      if (file.exists()) {
         this.lastChange = file.lastModified();
         boolean readFromfile;
         synchronized(cache) {
            SoftReference<TextInput> inputRef = (SoftReference)cache.get(file.getName());
            TextInput input;
            if (inputRef != null && (input = (TextInput)inputRef.get()) != null && input.lastChange >= this.lastChange) {
               this.lines = Collections.unmodifiableList(input.getLines());
               this.chapters = Collections.unmodifiableList(input.getChapters());
               this.bookmarks = Collections.unmodifiableMap(input.getBookmarks());
               readFromfile = false;
            } else {
               this.lines = new ArrayList();
               this.chapters = new ArrayList();
               this.bookmarks = new HashMap();
               cache.put(file.getName(), new SoftReference(this));
               readFromfile = true;
            }
         }

         if (readFromfile) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            try {
               for(int lineNumber = 0; bufferedReader.ready(); ++lineNumber) {
                  String line = bufferedReader.readLine();
                  if (line == null) {
                     break;
                  }

                  if (line.length() > 0 && line.charAt(0) == '#') {
                     this.bookmarks.put(line.substring(1).toLowerCase(Locale.ENGLISH).replaceAll("&[0-9a-fk]", ""), lineNumber);
                     this.chapters.add(line.substring(1).replace('&', '§').replace("§§", "&"));
                  }

                  this.lines.add(line.replace('&', '§').replace("§§", "&"));
               }
            } finally {
               bufferedReader.close();
            }
         }
      } else {
         this.lastChange = 0L;
         this.lines = Collections.emptyList();
         this.chapters = Collections.emptyList();
         this.bookmarks = Collections.emptyMap();
         if (createFile) {
            InputStream input = ess.getResource(filename + ".txt");
            OutputStream output = new FileOutputStream(file);

            try {
               byte[] buffer = new byte[1024];

               for(int length = input.read(buffer); length > 0; length = input.read(buffer)) {
                  output.write(buffer, 0, length);
               }
            } finally {
               output.close();
               input.close();
            }

            throw new FileNotFoundException("File " + filename + ".txt does not exist. Creating one for you.");
         }
      }

   }

   public List getLines() {
      return this.lines;
   }

   public List getChapters() {
      return this.chapters;
   }

   public Map getBookmarks() {
      return this.bookmarks;
   }
}
