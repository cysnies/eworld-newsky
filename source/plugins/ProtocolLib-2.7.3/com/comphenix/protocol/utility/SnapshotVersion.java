package com.comphenix.protocol.utility;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnapshotVersion implements Comparable {
   private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(\\d{2}w\\d{2})([a-z])");
   private final String rawString;
   private final Date snapshotDate;
   private final int snapshotWeekVersion;

   public SnapshotVersion(String version) {
      Matcher matcher = SNAPSHOT_PATTERN.matcher(version.trim());
      if (matcher.matches()) {
         try {
            this.snapshotDate = getDateFormat().parse(matcher.group(1));
            this.snapshotWeekVersion = matcher.group(2).charAt(0) - 97;
            this.rawString = version;
         } catch (ParseException e) {
            throw new IllegalArgumentException("Date implied by snapshot version is invalid.", e);
         }
      } else {
         throw new IllegalArgumentException("Cannot parse " + version + " as a snapshot version.");
      }
   }

   private static SimpleDateFormat getDateFormat() {
      SimpleDateFormat format = new SimpleDateFormat("yy'w'ww", Locale.US);
      format.setLenient(false);
      return format;
   }

   public int getSnapshotWeekVersion() {
      return this.snapshotWeekVersion;
   }

   public Date getSnapshotDate() {
      return this.snapshotDate;
   }

   public String getSnapshotString() {
      return this.rawString;
   }

   public int compareTo(SnapshotVersion o) {
      return o == null ? 1 : ComparisonChain.start().compare(this.snapshotDate, o.getSnapshotDate()).compare(this.snapshotWeekVersion, o.getSnapshotWeekVersion()).result();
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof SnapshotVersion)) {
         return false;
      } else {
         SnapshotVersion other = (SnapshotVersion)obj;
         return Objects.equal(this.snapshotDate, other.getSnapshotDate()) && this.snapshotWeekVersion == other.getSnapshotWeekVersion();
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.snapshotDate, this.snapshotWeekVersion});
   }

   public String toString() {
      return this.rawString;
   }
}
