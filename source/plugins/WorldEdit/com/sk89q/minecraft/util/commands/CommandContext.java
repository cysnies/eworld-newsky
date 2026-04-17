package com.sk89q.minecraft.util.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandContext {
   protected final String command;
   protected final List parsedArgs;
   protected final List originalArgIndices;
   protected final String[] originalArgs;
   protected final Set booleanFlags;
   protected final Map valueFlags;

   public CommandContext(String args) throws CommandException {
      this((String[])args.split(" "), (Set)null);
   }

   public CommandContext(String[] args) throws CommandException {
      this((String[])args, (Set)null);
   }

   public CommandContext(String args, Set valueFlags) throws CommandException {
      this(args.split(" "), valueFlags);
   }

   public CommandContext(String[] args, Set valueFlags) throws CommandException {
      this.booleanFlags = new HashSet();
      this.valueFlags = new HashMap();
      if (valueFlags == null) {
         valueFlags = Collections.emptySet();
      }

      this.originalArgs = args;
      this.command = args[0];
      List<Integer> argIndexList = new ArrayList(args.length);
      List<String> argList = new ArrayList(args.length);

      for(int i = 1; i < args.length; ++i) {
         String arg = args[i];
         if (arg.length() != 0) {
            argIndexList.add(i);
            switch (arg.charAt(0)) {
               case '"':
               case '\'':
                  StringBuilder build = new StringBuilder();
                  char quotedChar = arg.charAt(0);

                  int endIndex;
                  for(endIndex = i; endIndex < args.length; ++endIndex) {
                     String arg2 = args[endIndex];
                     if (arg2.charAt(arg2.length() - 1) == quotedChar && arg2.length() > 1) {
                        if (endIndex != i) {
                           build.append(' ');
                        }

                        build.append(arg2.substring(endIndex == i ? 1 : 0, arg2.length() - 1));
                        break;
                     }

                     if (endIndex == i) {
                        build.append(arg2.substring(1));
                     } else {
                        build.append(' ').append(arg2);
                     }
                  }

                  if (endIndex < args.length) {
                     arg = build.toString();
                     i = endIndex;
                  }

                  if (arg.length() == 0) {
                     break;
                  }
               default:
                  argList.add(arg);
            }
         }
      }

      this.originalArgIndices = new ArrayList(argIndexList.size());
      this.parsedArgs = new ArrayList(argList.size());
      int nextArg = 0;

      while(nextArg < argList.size()) {
         String arg = (String)argList.get(nextArg++);
         if (arg.charAt(0) == '-' && arg.length() != 1 && arg.matches("^-[a-zA-Z]+$")) {
            if (arg.equals("--")) {
               while(nextArg < argList.size()) {
                  this.originalArgIndices.add(argIndexList.get(nextArg));
                  this.parsedArgs.add(argList.get(nextArg++));
               }
               break;
            }

            for(int i = 1; i < arg.length(); ++i) {
               char flagName = arg.charAt(i);
               if (valueFlags.contains(flagName)) {
                  if (this.valueFlags.containsKey(flagName)) {
                     throw new CommandException("Value flag '" + flagName + "' already given");
                  }

                  if (nextArg >= argList.size()) {
                     throw new CommandException("No value specified for the '-" + flagName + "' flag.");
                  }

                  this.valueFlags.put(flagName, argList.get(nextArg++));
               } else {
                  this.booleanFlags.add(flagName);
               }
            }
         } else {
            this.originalArgIndices.add(argIndexList.get(nextArg - 1));
            this.parsedArgs.add(arg);
         }
      }

   }

   public String getCommand() {
      return this.command;
   }

   public boolean matches(String command) {
      return this.command.equalsIgnoreCase(command);
   }

   public String getString(int index) {
      return (String)this.parsedArgs.get(index);
   }

   public String getString(int index, String def) {
      return index < this.parsedArgs.size() ? (String)this.parsedArgs.get(index) : def;
   }

   public String getJoinedStrings(int initialIndex) {
      initialIndex = (Integer)this.originalArgIndices.get(initialIndex);
      StringBuilder buffer = new StringBuilder(this.originalArgs[initialIndex]);

      for(int i = initialIndex + 1; i < this.originalArgs.length; ++i) {
         buffer.append(" ").append(this.originalArgs[i]);
      }

      return buffer.toString();
   }

   public int getInteger(int index) throws NumberFormatException {
      return Integer.parseInt((String)this.parsedArgs.get(index));
   }

   public int getInteger(int index, int def) throws NumberFormatException {
      return index < this.parsedArgs.size() ? Integer.parseInt((String)this.parsedArgs.get(index)) : def;
   }

   public double getDouble(int index) throws NumberFormatException {
      return Double.parseDouble((String)this.parsedArgs.get(index));
   }

   public double getDouble(int index, double def) throws NumberFormatException {
      return index < this.parsedArgs.size() ? Double.parseDouble((String)this.parsedArgs.get(index)) : def;
   }

   public String[] getSlice(int index) {
      String[] slice = new String[this.originalArgs.length - index];
      System.arraycopy(this.originalArgs, index, slice, 0, this.originalArgs.length - index);
      return slice;
   }

   public String[] getPaddedSlice(int index, int padding) {
      String[] slice = new String[this.originalArgs.length - index + padding];
      System.arraycopy(this.originalArgs, index, slice, padding, this.originalArgs.length - index);
      return slice;
   }

   public String[] getParsedSlice(int index) {
      String[] slice = new String[this.parsedArgs.size() - index];
      System.arraycopy(this.parsedArgs.toArray(new String[this.parsedArgs.size()]), index, slice, 0, this.parsedArgs.size() - index);
      return slice;
   }

   public String[] getParsedPaddedSlice(int index, int padding) {
      String[] slice = new String[this.parsedArgs.size() - index + padding];
      System.arraycopy(this.parsedArgs.toArray(new String[this.parsedArgs.size()]), index, slice, padding, this.parsedArgs.size() - index);
      return slice;
   }

   public boolean hasFlag(char ch) {
      return this.booleanFlags.contains(ch) || this.valueFlags.containsKey(ch);
   }

   public Set getFlags() {
      return this.booleanFlags;
   }

   public Map getValueFlags() {
      return this.valueFlags;
   }

   public String getFlag(char ch) {
      return (String)this.valueFlags.get(ch);
   }

   public String getFlag(char ch, String def) {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : value;
   }

   public int getFlagInteger(char ch) throws NumberFormatException {
      return Integer.parseInt((String)this.valueFlags.get(ch));
   }

   public int getFlagInteger(char ch, int def) throws NumberFormatException {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : Integer.parseInt(value);
   }

   public double getFlagDouble(char ch) throws NumberFormatException {
      return Double.parseDouble((String)this.valueFlags.get(ch));
   }

   public double getFlagDouble(char ch, double def) throws NumberFormatException {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : Double.parseDouble(value);
   }

   public int argsLength() {
      return this.parsedArgs.size();
   }
}
