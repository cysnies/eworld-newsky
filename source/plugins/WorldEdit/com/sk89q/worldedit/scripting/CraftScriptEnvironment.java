package com.sk89q.worldedit.scripting;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;

public abstract class CraftScriptEnvironment {
   protected WorldEdit controller;
   protected LocalPlayer player;
   protected LocalConfiguration config;
   protected LocalSession session;
   protected ServerInterface server;

   public CraftScriptEnvironment(WorldEdit controller, ServerInterface server, LocalConfiguration config, LocalSession session, LocalPlayer player) {
      this.controller = controller;
      this.player = player;
      this.config = config;
      this.server = server;
      this.session = session;
   }
}
