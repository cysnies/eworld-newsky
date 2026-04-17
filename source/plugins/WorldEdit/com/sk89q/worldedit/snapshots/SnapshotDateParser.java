package com.sk89q.worldedit.snapshots;

import java.io.File;
import java.util.Calendar;

public interface SnapshotDateParser {
   Calendar detectDate(File var1);
}
