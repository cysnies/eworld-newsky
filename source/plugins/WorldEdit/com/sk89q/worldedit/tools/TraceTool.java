package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;

public interface TraceTool extends Tool {
   boolean actPrimary(ServerInterface var1, LocalConfiguration var2, LocalPlayer var3, LocalSession var4);
}
