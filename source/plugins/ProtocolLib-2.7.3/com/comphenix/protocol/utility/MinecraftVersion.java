package com.comphenix.protocol.utility;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Server;

public class MinecraftVersion implements Comparable {
   private static final String VERSION_PATTERN = ".*\\(.*MC.\\s*([a-zA-z0-9\\-\\.]+)\\s*\\)";
   private final int major;
   private final int minor;
   private final int build;
   private final String development;
   private final SnapshotVersion snapshot;

   public MinecraftVersion(Server server) {
      this(extractVersion(server.getVersion()));
   }

   public MinecraftVersion(String versionOnly) {
      this(versionOnly, true);
   }

   private MinecraftVersion(String versionOnly, boolean parseSnapshot) {
      String[] section = versionOnly.split("-");
      SnapshotVersion snapshot = null;
      int[] numbers = new int[3];

      try {
         numbers = this.parseVersion(section[0]);
      } catch (NumberFormatException cause) {
         if (!parseSnapshot) {
            throw cause;
         }

         try {
            snapshot = new SnapshotVersion(section[0]);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            MinecraftVersion latest = new MinecraftVersion("1.6.4", false);
            boolean newer = snapshot.getSnapshotDate().compareTo(format.parse("2013-07-08")) > 0;
            numbers[0] = latest.getMajor();
            numbers[1] = latest.getMinor() + (newer ? 1 : -1);
            numbers[2] = 0;
         } catch (Exception e) {
            throw new IllegalStateException("Cannot parse " + section[0], e);
         }
      }

      this.major = numbers[0];
      this.minor = numbers[1];
      this.build = numbers[2];
      this.development = section.length > 1 ? section[1] : (snapshot != null ? "snapshot" : null);
      this.snapshot = snapshot;
   }

   public MinecraftVersion(int major, int minor, int build) {
      this(major, minor, build, (String)null);
   }

   public MinecraftVersion(int major, int minor, int build, String development) {
      this.major = major;
      this.minor = minor;
      this.build = build;
      this.development = development;
      this.snapshot = null;
   }

   private int[] parseVersion(String version) {
      String[] elements = version.split("\\.");
      int[] numbers = new int[3];
      if (elements.length < 1) {
         throw new IllegalStateException("Corrupt MC version: " + version);
      } else {
         for(int i = 0; i < Math.min(numbers.length, elements.length); ++i) {
            numbers[i] = Integer.parseInt(elements[i].trim());
         }

         return numbers;
      }
   }

   public int getMajor() {
      return this.major;
   }

   public int getMinor() {
      return this.minor;
   }

   public int getBuild() {
      return this.build;
   }

   public String getDevelopmentStage() {
      return this.development;
   }

   public SnapshotVersion getSnapshot() {
      return this.snapshot;
   }

   public boolean isSnapshot() {
      return this.snapshot != null;
   }

   public String getVersion() {
      return this.getDevelopmentStage() == null ? String.format("%s.%s.%s", this.getMajor(), this.getMinor(), this.getBuild()) : String.format("%s.%s.%s-%s%s", this.getMajor(), this.getMinor(), this.getBuild(), this.getDevelopmentStage(), this.isSnapshot() ? this.snapshot : "");
   }

   public int compareTo(MinecraftVersion o) {
      return o == null ? 1 : ComparisonChain.start().compare(this.getMajor(), o.getMajor()).compare(this.getMinor(), o.getMinor()).compare(this.getBuild(), o.getBuild()).compare(this.getDevelopmentStage(), o.getDevelopmentStage(), Ordering.natural().nullsLast()).compare(this.getSnapshot(), o.getSnapshot(), Ordering.natural().nullsFirst()).result();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof MinecraftVersion)) {
         return false;
      } else {
         MinecraftVersion other = (MinecraftVersion)obj;
         return this.getMajor() == other.getMajor() && this.getMinor() == other.getMinor() && this.getBuild() == other.getBuild() && Objects.equal(this.getDevelopmentStage(), other.getDevelopmentStage());
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.getMajor(), this.getMinor(), this.getBuild()});
   }

   public String toString() {
      return String.format("(MC: %s)", this.getVersion());
   }

   public static String extractVersion(String text) {
      Pattern versionPattern = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-\\.]+)\\s*\\)");
      Matcher version = versionPattern.matcher(text);
      if (version.matches() && version.group(1) != null) {
         return version.group(1);
      } else {
         throw new IllegalStateException("Cannot parse version String '" + text + "'");
      }
   }

   public static MinecraftVersion fromServerVersion(String serverVersion) {
      return new MinecraftVersion(extractVersion(serverVersion));
   }
}
