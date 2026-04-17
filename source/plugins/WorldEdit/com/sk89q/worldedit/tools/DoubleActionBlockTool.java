package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;

public interface DoubleActionBlockTool extends BlockTool {
   boolean actSecondary(ServerInterface var1, LocalConfiguration var2, LocalPlayer var3, LocalSession var4, WorldVector var5);
}
