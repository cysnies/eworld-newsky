package com.earth2me.essentials;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ManagedFile {
   private static final int BUFFERSIZE = 8192;
   private final transient File file;

   public ManagedFile(String filename, IEssentials ess) {
      this.file = new File(ess.getDataFolder(), filename);
      if (this.file.exists()) {
         try {
            if (checkForVersion(this.file, ess.getDescription().getVersion()) && !this.file.delete()) {
               throw new IOException("Could not delete file " + this.file.toString());
            }
         } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
         }
      }

      if (!this.file.exists()) {
         try {
            copyResourceAscii("/" + filename, this.file);
         } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, I18n._("itemsCsvNotLoaded"), ex);
         }
      }

   }

   public static void copyResourceAscii(String resourceName, File file) throws IOException {
      InputStreamReader reader = new InputStreamReader(ManagedFile.class.getResourceAsStream(resourceName));

      try {
         MessageDigest digest = getDigest();
         DigestOutputStream digestStream = new DigestOutputStream(new FileOutputStream(file), digest);

         try {
            OutputStreamWriter writer = new OutputStreamWriter(digestStream);

            try {
               char[] buffer = new char[8192];

               while(true) {
                  int length = reader.read(buffer);
                  if (length < 0) {
                     writer.write("\n");
                     writer.flush();
                     BigInteger hashInt = new BigInteger(1, digest.digest());
                     digestStream.on(false);
                     digestStream.write(35);
                     digestStream.write(hashInt.toString(16).getBytes());
                     return;
                  }

                  writer.write(buffer, 0, length);
               }
            } finally {
               writer.close();
            }
         } finally {
            digestStream.close();
         }
      } finally {
         reader.close();
      }
   }

   public static boolean checkForVersion(File file, String version) throws IOException {
      if (file.length() < 33L) {
         return false;
      } else {
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

         boolean var6;
         try {
            byte[] buffer = new byte[(int)file.length()];
            int position = 0;

            do {
               int length = bis.read(buffer, position, Math.min((int)file.length() - position, 8192));
               if (length < 0) {
                  break;
               }

               position += length;
            } while((long)position < file.length());

            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            if (bais.skip(file.length() - 33L) == file.length() - 33L) {
               BufferedReader reader = new BufferedReader(new InputStreamReader(bais));

               try {
                  String hash = reader.readLine();
                  if (hash == null || !hash.matches("#[a-f0-9]{32}")) {
                     return false;
                  }

                  hash = hash.substring(1);
                  bais.reset();
                  String versionline = reader.readLine();
                  if (versionline == null || !versionline.matches("#version: .+")) {
                     return false;
                  }

                  String versioncheck = versionline.substring(10);
                  if (versioncheck.equalsIgnoreCase(version)) {
                     return false;
                  }

                  bais.reset();
                  MessageDigest digest = getDigest();
                  DigestInputStream digestStream = new DigestInputStream(bais, digest);

                  try {
                     byte[] bytes = new byte[(int)file.length() - 33];
                     digestStream.read(bytes);
                     BigInteger correct = new BigInteger(hash, 16);
                     BigInteger test = new BigInteger(1, digest.digest());
                     if (!correct.equals(test)) {
                        Bukkit.getLogger().warning("File " + file.toString() + " has been modified by user and file version differs, please update the file manually.");
                        return false;
                     }

                     boolean var15 = true;
                     return var15;
                  } finally {
                     digestStream.close();
                  }
               } finally {
                  reader.close();
               }
            }

            var6 = false;
         } finally {
            bis.close();
         }

         return var6;
      }
   }

   public static MessageDigest getDigest() throws IOException {
      try {
         return MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException ex) {
         throw new IOException(ex);
      }
   }

   public List getLines() {
      try {
         BufferedReader reader = new BufferedReader(new FileReader(this.file));

         try {
            List<String> lines = new ArrayList();

            while(true) {
               String line = reader.readLine();
               if (line == null) {
                  Object var9 = lines;
                  return (List)var9;
               }

               lines.add(line);
            }
         } finally {
            reader.close();
         }
      } catch (IOException ex) {
         Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
         return Collections.emptyList();
      }
   }
}
