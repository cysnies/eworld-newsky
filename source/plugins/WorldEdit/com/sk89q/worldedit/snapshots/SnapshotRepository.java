package com.sk89q.worldedit.snapshots;

import com.sk89q.worldedit.data.MissingWorldException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SnapshotRepository {
   protected File dir;
   protected List dateParsers;

   public SnapshotRepository(File dir) {
      this.dateParsers = new ArrayList();
      this.dir = dir;
      dir.mkdirs();
      this.dateParsers.add(new YYMMDDHHIISSParser());
      this.dateParsers.add(new ModificationTimerParser());
   }

   public SnapshotRepository(String dir) {
      this(new File(dir));
   }

   public List getSnapshots(boolean newestFirst, String worldname) throws MissingWorldException {
      FilenameFilter filter = new FilenameFilter() {
         public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            return SnapshotRepository.this.isValidSnapshot(f);
         }
      };
      File[] snapshotFiles = this.dir.listFiles();
      if (snapshotFiles == null) {
         throw new MissingWorldException(worldname);
      } else {
         List<Snapshot> list = new ArrayList(snapshotFiles.length);

         for(File file : snapshotFiles) {
            if (this.isValidSnapshot(file)) {
               Snapshot snapshot = new Snapshot(this, file.getName());
               if (snapshot.containsWorld(worldname)) {
                  this.detectDate(snapshot);
                  list.add(snapshot);
               }
            } else if (file.isDirectory() && file.getName().equalsIgnoreCase(worldname)) {
               for(String name : file.list(filter)) {
                  Snapshot snapshot = new Snapshot(this, file.getName() + "/" + name);
                  this.detectDate(snapshot);
                  list.add(snapshot);
               }
            }
         }

         if (newestFirst) {
            Collections.sort(list, Collections.reverseOrder());
         } else {
            Collections.sort(list);
         }

         return list;
      }
   }

   public Snapshot getSnapshotAfter(Calendar date, String world) throws MissingWorldException {
      List<Snapshot> snapshots = this.getSnapshots(true, world);
      Snapshot last = null;

      for(Snapshot snapshot : snapshots) {
         if (snapshot.getDate() != null && snapshot.getDate().before(date)) {
            return last;
         }

         last = snapshot;
      }

      return last;
   }

   public Snapshot getSnapshotBefore(Calendar date, String world) throws MissingWorldException {
      List<Snapshot> snapshots = this.getSnapshots(false, world);
      Snapshot last = null;

      for(Snapshot snapshot : snapshots) {
         if (snapshot.getDate().after(date)) {
            return last;
         }

         last = snapshot;
      }

      return last;
   }

   protected void detectDate(Snapshot snapshot) {
      for(SnapshotDateParser parser : this.dateParsers) {
         Calendar date = parser.detectDate(snapshot.getFile());
         if (date != null) {
            snapshot.setDate(date);
            return;
         }
      }

      snapshot.setDate((Calendar)null);
   }

   public Snapshot getDefaultSnapshot(String world) throws MissingWorldException {
      List<Snapshot> snapshots = this.getSnapshots(true, world);
      return snapshots.size() == 0 ? null : (Snapshot)snapshots.get(0);
   }

   public boolean isValidSnapshotName(String snapshot) {
      return this.isValidSnapshot(new File(this.dir, snapshot));
   }

   protected boolean isValidSnapshot(File f) {
      if (!f.getName().matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
         return false;
      } else {
         return f.isDirectory() && (new File(f, "level.dat")).exists() || f.isFile() && (f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".tar.bz2") || f.getName().toLowerCase().endsWith(".tar.gz") || f.getName().toLowerCase().endsWith(".tar"));
      }
   }

   public Snapshot getSnapshot(String name) throws InvalidSnapshotException {
      if (!this.isValidSnapshotName(name)) {
         throw new InvalidSnapshotException();
      } else {
         return new Snapshot(this, name);
      }
   }

   public File getDirectory() {
      return this.dir;
   }
}
