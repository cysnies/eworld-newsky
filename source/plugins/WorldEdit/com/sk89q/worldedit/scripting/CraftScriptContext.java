package com.sk89q.worldedit.scripting;

import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.commands.InsufficientArgumentsException;
import com.sk89q.worldedit.patterns.Pattern;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CraftScriptContext extends CraftScriptEnvironment {
   private List editSessions = new ArrayList();
   private String[] args;

   public CraftScriptContext(WorldEdit controller, ServerInterface server, LocalConfiguration config, LocalSession session, LocalPlayer player, String[] args) {
      super(controller, server, config, session, player);
      this.args = args;
   }

   public EditSession remember() {
      EditSession editSession = this.controller.getEditSessionFactory().getEditSession(this.player.getWorld(), this.session.getBlockChangeLimit(), this.session.getBlockBag(this.player), this.player);
      editSession.enableQueue();
      this.editSessions.add(editSession);
      return editSession;
   }

   public LocalPlayer getPlayer() {
      return this.player;
   }

   public LocalSession getSession() {
      return this.session;
   }

   public LocalConfiguration getConfiguration() {
      return this.config;
   }

   public List getEditSessions() {
      return Collections.unmodifiableList(this.editSessions);
   }

   public void print(String msg) {
      this.player.print(msg);
   }

   public void error(String msg) {
      this.player.printError(msg);
   }

   public void printRaw(String msg) {
      this.player.printRaw(msg);
   }

   public void checkArgs(int min, int max, String usage) throws InsufficientArgumentsException {
      if (this.args.length <= min || max != -1 && this.args.length - 1 > max) {
         throw new InsufficientArgumentsException("Usage: " + usage);
      }
   }

   public BaseBlock getBlock(String arg, boolean allAllowed) throws UnknownItemException, DisallowedItemException {
      return this.controller.getBlock(this.player, arg, allAllowed);
   }

   public BaseBlock getBlock(String id) throws UnknownItemException, DisallowedItemException {
      return this.controller.getBlock(this.player, id, false);
   }

   public Pattern getBlockPattern(String list) throws UnknownItemException, DisallowedItemException {
      return this.controller.getBlockPattern(this.player, list);
   }

   public Set getBlockIDs(String list, boolean allBlocksAllowed) throws UnknownItemException, DisallowedItemException {
      return this.controller.getBlockIDs(this.player, list, allBlocksAllowed);
   }

   /** @deprecated */
   @Deprecated
   public File getSafeFile(String folder, String filename) throws FilenameException {
      File dir = this.controller.getWorkingDirectoryFile(folder);
      return this.controller.getSafeOpenFile(this.player, dir, filename, (String)null, (String[])null);
   }

   public File getSafeOpenFile(String folder, String filename, String defaultExt, String... exts) throws FilenameException {
      File dir = this.controller.getWorkingDirectoryFile(folder);
      return this.controller.getSafeOpenFile(this.player, dir, filename, defaultExt, exts);
   }

   public File getSafeSaveFile(String folder, String filename, String defaultExt, String... exts) throws FilenameException {
      File dir = this.controller.getWorkingDirectoryFile(folder);
      return this.controller.getSafeSaveFile(this.player, dir, filename, defaultExt, exts);
   }
}
