package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.data.MissingWorldException;
import com.sk89q.worldedit.snapshots.InvalidSnapshotException;
import com.sk89q.worldedit.snapshots.Snapshot;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

public class SnapshotCommands {
   private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
   private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
   private final WorldEdit we;

   public SnapshotCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"list"},
      usage = "[num]",
      desc = "List snapshots",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.snapshots.list"})
   public void list(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      if (config.snapshotRepo == null) {
         player.printError("Snapshot/backup restore is not configured.");
      } else {
         try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, player.getWorld().getName());
            if (snapshots.size() > 0) {
               int num = args.argsLength() > 0 ? Math.min(40, Math.max(5, args.getInteger(0))) : 5;
               player.print("Snapshots for world: '" + player.getWorld().getName() + "'");

               for(byte i = 0; i < Math.min(num, snapshots.size()); ++i) {
                  player.print(i + 1 + ". " + ((Snapshot)snapshots.get(i)).getName());
               }

               player.print("Use /snap use [snapshot] or /snap use latest.");
            } else {
               player.printError("No snapshots are available. See console for details.");
               File dir = config.snapshotRepo.getDirectory();

               try {
                  logger.info("WorldEdit found no snapshots: looked in: " + dir.getCanonicalPath());
               } catch (IOException var9) {
                  logger.info("WorldEdit found no snapshots: looked in (NON-RESOLVABLE PATH - does it exist?): " + dir.getPath());
               }
            }
         } catch (MissingWorldException var10) {
            player.printError("No snapshots were found for this world.");
         }

      }
   }

   @Command(
      aliases = {"use"},
      usage = "<snapshot>",
      desc = "Choose a snapshot to use",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   public void use(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      if (config.snapshotRepo == null) {
         player.printError("Snapshot/backup restore is not configured.");
      } else {
         String name = args.getString(0);
         if (name.equalsIgnoreCase("latest")) {
            try {
               Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot(player.getWorld().getName());
               if (snapshot != null) {
                  session.setSnapshot((Snapshot)null);
                  player.print("Now using newest snapshot.");
               } else {
                  player.printError("No snapshots were found.");
               }
            } catch (MissingWorldException var9) {
               player.printError("No snapshots were found for this world.");
            }
         } else {
            try {
               session.setSnapshot(config.snapshotRepo.getSnapshot(name));
               player.print("Snapshot set to: " + name);
            } catch (InvalidSnapshotException var8) {
               player.printError("That snapshot does not exist or is not available.");
            }
         }

      }
   }

   @Command(
      aliases = {"sel"},
      usage = "<index>",
      desc = "Choose the snapshot based on the list id",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   public void sel(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      if (config.snapshotRepo == null) {
         player.printError("Snapshot/backup restore is not configured.");
      } else {
         int index = -1;

         try {
            index = Integer.parseInt(args.getString(0));
         } catch (NumberFormatException var10) {
            player.printError("Invalid index, " + args.getString(0) + " is not a valid integer.");
            return;
         }

         if (index < 1) {
            player.printError("Invalid index, must be equal or higher then 1.");
         } else {
            try {
               List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, player.getWorld().getName());
               if (snapshots.size() < index) {
                  player.printError("Invalid index, must be between 1 and " + snapshots.size() + ".");
                  return;
               }

               Snapshot snapshot = (Snapshot)snapshots.get(index - 1);
               if (snapshot == null) {
                  player.printError("That snapshot does not exist or is not available.");
                  return;
               }

               session.setSnapshot(snapshot);
               player.print("Snapshot set to: " + snapshot.getName());
            } catch (MissingWorldException var9) {
               player.printError("No snapshots were found for this world.");
            }

         }
      }
   }

   @Command(
      aliases = {"before"},
      usage = "<date>",
      desc = "Choose the nearest snapshot before a date",
      min = 1,
      max = -1
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   public void before(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      if (config.snapshotRepo == null) {
         player.printError("Snapshot/backup restore is not configured.");
      } else {
         Calendar date = session.detectDate(args.getJoinedStrings(0));
         if (date == null) {
            player.printError("Could not detect the date inputted.");
         } else {
            try {
               Snapshot snapshot = config.snapshotRepo.getSnapshotBefore(date, player.getWorld().getName());
               if (snapshot == null) {
                  dateFormat.setTimeZone(session.getTimeZone());
                  player.printError("Couldn't find a snapshot before " + dateFormat.format(date.getTime()) + ".");
               } else {
                  session.setSnapshot(snapshot);
                  player.print("Snapshot set to: " + snapshot.getName());
               }
            } catch (MissingWorldException var8) {
               player.printError("No snapshots were found for this world.");
            }
         }

      }
   }

   @Command(
      aliases = {"after"},
      usage = "<date>",
      desc = "Choose the nearest snapshot after a date",
      min = 1,
      max = -1
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   public void after(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      if (config.snapshotRepo == null) {
         player.printError("Snapshot/backup restore is not configured.");
      } else {
         Calendar date = session.detectDate(args.getJoinedStrings(0));
         if (date == null) {
            player.printError("Could not detect the date inputted.");
         } else {
            try {
               Snapshot snapshot = config.snapshotRepo.getSnapshotAfter(date, player.getWorld().getName());
               if (snapshot == null) {
                  dateFormat.setTimeZone(session.getTimeZone());
                  player.printError("Couldn't find a snapshot after " + dateFormat.format(date.getTime()) + ".");
               } else {
                  session.setSnapshot(snapshot);
                  player.print("Snapshot set to: " + snapshot.getName());
               }
            } catch (MissingWorldException var8) {
               player.printError("No snapshots were found for this world.");
            }
         }

      }
   }
}
