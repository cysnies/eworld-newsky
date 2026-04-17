package com.sk89q.worldedit.snapshots;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ModificationTimerParser implements SnapshotDateParser {
   public Calendar detectDate(File file) {
      Calendar cal = new GregorianCalendar();
      cal.setTimeInMillis(file.lastModified());
      return cal;
   }
}
