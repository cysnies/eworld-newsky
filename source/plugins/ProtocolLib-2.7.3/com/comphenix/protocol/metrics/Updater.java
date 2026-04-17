package com.comphenix.protocol.metrics;

import com.google.common.base.Preconditions;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Updater {
   private static final String[] noUpdateTag = new String[]{"-DEV", "-PRE", "-SNAPSHOT"};
   private static final String DBOUrl = "http://dev.bukkit.org/server-mods/";
   private static final int BYTE_SIZE = 1024;
   private final Plugin plugin;
   private final String slug;
   private volatile long totalSize;
   private volatile int sizeLine;
   private volatile int multiplier;
   private volatile URL url;
   private volatile String updateFolder = YamlConfiguration.loadConfiguration(new File("bukkit.yml")).getString("settings.update-folder");
   private volatile UpdateResult result;
   private volatile boolean announce;
   private volatile UpdateType type;
   private volatile String downloadedVersion;
   private volatile String versionTitle;
   private volatile String versionLink;
   private volatile File file;
   private volatile Logger logger;
   private static final String TITLE = "title";
   private static final String LINK = "link";
   private static final String ITEM = "item";

   public Updater(Plugin plugin, Logger logger, String slug, File file, String permission) {
      this.result = Updater.UpdateResult.SUCCESS;
      Preconditions.checkNotNull(plugin, "plugin");
      Preconditions.checkNotNull(logger, "logger");
      Preconditions.checkNotNull(slug, "slug");
      Preconditions.checkNotNull(file, "file");
      Preconditions.checkNotNull(permission, "permission");
      this.plugin = plugin;
      this.file = file;
      this.slug = slug;
      this.logger = logger;
   }

   public synchronized UpdateResult update(UpdateType type, boolean announce) {
      this.type = type;
      this.announce = announce;

      try {
         this.url = null;
         this.url = new URL("http://dev.bukkit.org/server-mods/" + this.slug + "/files.rss");
      } catch (MalformedURLException var10) {
         this.logger.warning("The author of this plugin has misconfigured their Auto Update system");
         this.logger.warning("The project slug added ('" + this.slug + "') is invalid, and does not exist on dev.bukkit.org");
         this.result = Updater.UpdateResult.FAIL_BADSLUG;
      }

      if (this.url != null) {
         try {
            this.readFeed();
         } catch (ConnectException ex) {
            this.logger.warning("Update problem: " + ex.getMessage());
            return Updater.UpdateResult.FAIL_DBO;
         }

         if (this.versionCheck(this.versionTitle)) {
            String fileLink = this.getFile(this.versionLink);
            if (fileLink != null && type != Updater.UpdateType.NO_DOWNLOAD) {
               String[] split = fileLink.split("/");
               String name = split[split.length - 1];
               this.logger.info("Downloading " + fileLink);
               if (this.downloadedVersion != null && this.downloadedVersion.equalsIgnoreCase(this.versionLink)) {
                  this.result = Updater.UpdateResult.UPDATE_AVAILABLE;
               } else {
                  File path = new File("plugins/");
                  this.saveFile(path, name, fileLink);
                  this.downloadedVersion = this.versionLink;
                  this.result = Updater.UpdateResult.SUCCESS;

                  try {
                     if (!this.file.delete()) {
                        File zeroCurrentJar = new File(path, this.updateFolder + "/" + this.file.getName());
                        zeroCurrentJar.createNewFile();
                     }
                  } catch (IOException var8) {
                     this.logger.warning("Cannot delete old ProtocolLib version: " + this.file.getName());
                  }
               }
            } else {
               this.result = Updater.UpdateResult.UPDATE_AVAILABLE;
            }
         }
      }

      return this.result;
   }

   public UpdateResult getResult() {
      return this.result;
   }

   public long getFileSize() {
      return this.totalSize;
   }

   public String getLatestVersionString() {
      return this.versionTitle;
   }

   private void saveFile(File folder, String file, String u) {
      if (!folder.exists()) {
         folder.mkdir();
      }

      BufferedInputStream in = null;
      FileOutputStream fout = null;

      try {
         URL url = new URL(u);
         int fileLength = url.openConnection().getContentLength();
         in = new BufferedInputStream(url.openStream());
         fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);
         byte[] data = new byte[1024];
         if (this.announce) {
            this.logger.info("About to download a new update: " + this.versionTitle);
         }

         long downloaded = 0L;

         int count;
         while((count = in.read(data, 0, 1024)) != -1) {
            downloaded += (long)count;
            fout.write(data, 0, count);
            int percent = (int)(downloaded * 100L / (long)fileLength);
            if (this.announce && percent % 10 == 0) {
               this.logger.info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
            }
         }

         if (this.announce) {
            this.logger.info("Finished updating.");
         }
      } catch (Exception ex) {
         this.logger.warning("The auto-updater tried to download a new update, but was unsuccessful.");
         this.logger.log(Level.INFO, "Error message to submit as a ticket.", ex);
         this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
      } finally {
         try {
            if (in != null) {
               in.close();
            }

            if (fout != null) {
               fout.close();
            }
         } catch (Exception var20) {
         }

      }

   }

   public boolean pluginFile(String name) {
      for(File file : (new File("plugins")).listFiles()) {
         if (file.getName().equals(name)) {
            return true;
         }
      }

      return false;
   }

   private String getFile(String link) {
      String download = null;

      try {
         URL url = new URL(link);
         URLConnection urlConn = url.openConnection();
         InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
         BufferedReader buff = new BufferedReader(inStream);
         int counter = 0;

         String line;
         while((line = buff.readLine()) != null) {
            ++counter;
            if (line.contains("<li class=\"user-action user-action-download\">")) {
               download = line.split("<a href=\"")[1].split("\">Download</a>")[0];
            } else if (line.contains("<dt>Size</dt>")) {
               this.sizeLine = counter + 1;
            } else if (counter == this.sizeLine) {
               String size = line.replaceAll("<dd>", "").replaceAll("</dd>", "");
               this.multiplier = size.contains("MiB") ? 1048576 : 1024;
               size = size.replace(" KiB", "").replace(" MiB", "");
               this.totalSize = (long)(Double.parseDouble(size) * (double)this.multiplier);
            }
         }

         URLConnection var11 = null;
         InputStreamReader var12 = null;
         buff.close();
         Object var13 = null;
         return download;
      } catch (Exception ex) {
         ex.printStackTrace();
         this.logger.warning("The auto-updater tried to contact dev.bukkit.org, but was unsuccessful.");
         this.result = Updater.UpdateResult.FAIL_DBO;
         return null;
      }
   }

   private boolean versionCheck(String title) {
      if (this.type != Updater.UpdateType.NO_VERSION_CHECK) {
         String[] parts = title.split(" ");
         String version = this.plugin.getDescription().getVersion();
         if (parts.length != 2) {
            this.logger.warning("The author of this plugin has misconfigured their Auto Update system");
            this.logger.warning("Files uploaded to BukkitDev should contain the version number, seperated from the name by a 'v', such as PluginName v1.0");
            this.logger.warning("Please notify the author (" + (String)this.plugin.getDescription().getAuthors().get(0) + ") of this error.");
            this.result = Updater.UpdateResult.FAIL_NOVERSION;
            return false;
         }

         String remoteVersion = parts[1].split(" ")[0];
         int remVer = -1;
         int curVer = 0;

         try {
            remVer = this.calVer(remoteVersion);
            curVer = this.calVer(version);
         } catch (NumberFormatException var8) {
            remVer = -1;
         }

         if (this.hasTag(version)) {
            this.result = Updater.UpdateResult.NOT_SUPPORTED;
            return false;
         }

         if (version.equalsIgnoreCase(remoteVersion) || curVer >= remVer) {
            this.result = Updater.UpdateResult.NO_UPDATE;
            return false;
         }
      }

      return true;
   }

   private Integer calVer(String s) throws NumberFormatException {
      if (s.contains(".")) {
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < s.length(); ++i) {
            Character c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
               sb.append(c);
            }
         }

         return Integer.parseInt(sb.toString());
      } else {
         return Integer.parseInt(s);
      }
   }

   private boolean hasTag(String version) {
      for(String string : noUpdateTag) {
         if (version.contains(string)) {
            return true;
         }
      }

      return false;
   }

   private void readFeed() throws ConnectException {
      try {
         String title = "";
         String link = "";
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         InputStream in = this.read();
         XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

         while(eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
               if (event.asStartElement().getName().getLocalPart().equals("title")) {
                  event = eventReader.nextEvent();
                  title = event.asCharacters().getData();
               } else if (event.asStartElement().getName().getLocalPart().equals("link")) {
                  event = eventReader.nextEvent();
                  link = event.asCharacters().getData();
               }
            } else if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("item")) {
               this.versionTitle = title;
               this.versionLink = link;
               break;
            }
         }

      } catch (XMLStreamException e) {
         throw new RuntimeException(e);
      }
   }

   private InputStream read() throws ConnectException {
      try {
         return this.url.openStream();
      } catch (ConnectException e) {
         throw e;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static enum UpdateResult {
      SUCCESS(1, "The updater found an update, and has readied it to be loaded the next time the server restarts/reloads."),
      NO_UPDATE(2, "The updater did not find an update, and nothing was downloaded."),
      FAIL_DOWNLOAD(3, "The updater found an update, but was unable to download it."),
      FAIL_DBO(4, "For some reason, the updater was unable to contact dev.bukkit.org to download the file."),
      FAIL_NOVERSION(5, "When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'."),
      FAIL_BADSLUG(6, "The slug provided by the plugin running the updater was invalid and doesn't exist on DBO."),
      UPDATE_AVAILABLE(7, "The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded."),
      NOT_SUPPORTED(8, "Updating SNAPSHOT versions are not supported. Please perform a manual upgrade.");

      private static final Map valueList = new HashMap();
      private final int value;
      private final String description;

      private UpdateResult(int value, String description) {
         this.value = value;
         this.description = description;
      }

      public int getValue() {
         return this.value;
      }

      public static UpdateResult getResult(int value) {
         return (UpdateResult)valueList.get(value);
      }

      public String toString() {
         return this.description;
      }

      static {
         for(UpdateResult result : values()) {
            valueList.put(result.value, result);
         }

      }
   }

   public static enum UpdateType {
      DEFAULT(1),
      NO_VERSION_CHECK(2),
      NO_DOWNLOAD(3);

      private static final Map valueList = new HashMap();
      private final int value;

      private UpdateType(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }

      public static UpdateType getResult(int value) {
         return (UpdateType)valueList.get(value);
      }

      static {
         for(UpdateType result : values()) {
            valueList.put(result.value, result);
         }

      }
   }
}
