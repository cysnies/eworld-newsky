package com.comphenix.protocol;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.concurrency.AbstractIntervalTree;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.comphenix.protocol.utility.ChatExtensions;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

class CommandPacket extends CommandBase {
   public static final ReportType REPORT_CANNOT_SEND_MESSAGE = new ReportType("Cannot send chat message.");
   public static final String NAME = "packet";
   public static final int PAGE_LINE_COUNT = 9;
   private Plugin plugin;
   private Logger logger;
   private ProtocolManager manager;
   private ChatExtensions chatter;
   private Map pagedMessage = new WeakHashMap();
   private AbstractIntervalTree clientListeners;
   private AbstractIntervalTree serverListeners;
   private CommandFilter filter;

   public CommandPacket(ErrorReporter reporter, Plugin plugin, Logger logger, CommandFilter filter, ProtocolManager manager) {
      super(reporter, "protocol.admin", "packet", 1);
      this.clientListeners = this.createTree(ConnectionSide.CLIENT_SIDE);
      this.serverListeners = this.createTree(ConnectionSide.SERVER_SIDE);
      this.plugin = plugin;
      this.logger = logger;
      this.manager = manager;
      this.filter = filter;
      this.chatter = new ChatExtensions(manager);
   }

   private AbstractIntervalTree createTree(final ConnectionSide side) {
      return new AbstractIntervalTree() {
         protected Integer decrementKey(Integer key) {
            return key != null ? key - 1 : null;
         }

         protected Integer incrementKey(Integer key) {
            return key != null ? key + 1 : null;
         }

         protected void onEntryAdded(AbstractIntervalTree.Entry added) {
            if (added != null) {
               Range<Integer> key = added.getKey();
               DetailedPacketListener listener = (DetailedPacketListener)added.getValue();
               DetailedPacketListener corrected = CommandPacket.this.createPacketListener(side, (Integer)key.lowerEndpoint(), (Integer)key.upperEndpoint(), listener.isDetailed());
               added.setValue(corrected);
               if (corrected != null) {
                  CommandPacket.this.manager.addPacketListener(corrected);
               } else {
                  this.remove(key.lowerEndpoint(), key.upperEndpoint());
               }
            }

         }

         protected void onEntryRemoved(AbstractIntervalTree.Entry removed) {
            if (removed != null) {
               DetailedPacketListener listener = (DetailedPacketListener)removed.getValue();
               if (listener != null) {
                  CommandPacket.this.manager.removePacketListener(listener);
               }
            }

         }
      };
   }

   public void sendMessageSilently(CommandSender receiver, String message) {
      try {
         this.chatter.sendMessageSilently(receiver, message);
      } catch (InvocationTargetException e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_SEND_MESSAGE).error(e).callerParam(receiver, message));
      }

   }

   public void broadcastMessageSilently(String message, String permission) {
      try {
         this.chatter.broadcastMessageSilently(message, permission);
      } catch (InvocationTargetException e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_SEND_MESSAGE).error(e).callerParam(message, permission));
      }

   }

   private void printPage(CommandSender sender, int pageIndex) {
      List<String> paged = (List)this.pagedMessage.get(sender);
      if (paged != null) {
         int lastPage = (paged.size() - 1) / 9 + 1;

         for(int i = 9 * (pageIndex - 1); i < 9 * pageIndex; ++i) {
            if (i < paged.size()) {
               this.sendMessageSilently(sender, " " + (String)paged.get(i));
            }
         }

         if (pageIndex < lastPage) {
            this.sendMessageSilently(sender, "Send /packet page " + (pageIndex + 1) + " for the next page.");
         }
      } else {
         this.sendMessageSilently(sender, ChatColor.RED + "No pages found.");
      }

   }

   protected boolean handleCommand(CommandSender sender, String[] args) {
      try {
         SubCommand subCommand = this.parseCommand(args, 0);
         if (subCommand == CommandPacket.SubCommand.PAGE) {
            int page = Integer.parseInt(args[1]);
            if (page > 0) {
               this.printPage(sender, page);
            } else {
               this.sendMessageSilently(sender, ChatColor.RED + "Page index must be greater than zero.");
            }

            return true;
         }

         ConnectionSide side = this.parseSide(args, 1, ConnectionSide.BOTH);
         Integer lastIndex = args.length - 1;
         Boolean detailed = this.parseBoolean(args, "detailed", lastIndex);
         if (detailed == null) {
            detailed = false;
         } else {
            lastIndex = lastIndex - 1;
         }

         List<Range<Integer>> ranges = RangeParser.getRanges(args, 2, lastIndex, Ranges.closed(0, 255));
         if (ranges.isEmpty()) {
            ranges.add(Ranges.closed(0, 255));
         }

         if (subCommand == CommandPacket.SubCommand.ADD) {
            if (args.length == 1) {
               sender.sendMessage(ChatColor.RED + "Please specify a connectionn side.");
               return false;
            }

            this.executeAddCommand(sender, side, detailed, ranges);
         } else if (subCommand == CommandPacket.SubCommand.REMOVE) {
            this.executeRemoveCommand(sender, side, detailed, ranges);
         } else if (subCommand == CommandPacket.SubCommand.NAMES) {
            this.executeNamesCommand(sender, side, ranges);
         }
      } catch (NumberFormatException e) {
         this.sendMessageSilently(sender, ChatColor.RED + "Cannot parse number: " + e.getMessage());
      } catch (IllegalArgumentException e) {
         this.sendMessageSilently(sender, ChatColor.RED + e.getMessage());
      }

      return true;
   }

   private void executeAddCommand(CommandSender sender, ConnectionSide side, Boolean detailed, List ranges) {
      for(Range range : ranges) {
         DetailedPacketListener listener = this.addPacketListeners(side, (Integer)range.lowerEndpoint(), (Integer)range.upperEndpoint(), detailed);
         this.sendMessageSilently(sender, ChatColor.BLUE + "Added listener " + this.getWhitelistInfo(listener));
      }

   }

   private void executeRemoveCommand(CommandSender sender, ConnectionSide side, Boolean detailed, List ranges) {
      int count = 0;

      for(Range range : ranges) {
         count += this.removePacketListeners(side, (Integer)range.lowerEndpoint(), (Integer)range.upperEndpoint(), detailed).size();
      }

      this.sendMessageSilently(sender, ChatColor.BLUE + "Fully removed " + count + " listeners.");
   }

   private void executeNamesCommand(CommandSender sender, ConnectionSide side, List ranges) {
      Set<Integer> named = this.getNamedPackets(side);
      List<String> messages = new ArrayList();

      for(Range range : ranges) {
         for(int id : range.asSet(DiscreteDomains.integers())) {
            if (named.contains(id)) {
               messages.add(ChatColor.WHITE + "" + id + ": " + ChatColor.BLUE + Packets.getDeclaredName(id));
            }
         }
      }

      if (sender instanceof Player && messages.size() > 0 && messages.size() > 9) {
         this.pagedMessage.put(sender, messages);
         this.printPage(sender, 1);
      } else {
         for(String message : messages) {
            this.sendMessageSilently(sender, message);
         }
      }

   }

   private String getWhitelistInfo(PacketListener listener) {
      boolean sendingEmpty = ListeningWhitelist.isEmpty(listener.getSendingWhitelist());
      boolean receivingEmpty = ListeningWhitelist.isEmpty(listener.getReceivingWhitelist());
      if (!sendingEmpty && !receivingEmpty) {
         return String.format("Sending: %s, Receiving: %s", listener.getSendingWhitelist(), listener.getReceivingWhitelist());
      } else if (!sendingEmpty) {
         return listener.getSendingWhitelist().toString();
      } else {
         return !receivingEmpty ? listener.getReceivingWhitelist().toString() : "[None]";
      }
   }

   private Set getValidPackets(ConnectionSide side) throws FieldAccessException {
      HashSet<Integer> supported = Sets.newHashSet();
      if (side.isForClient()) {
         supported.addAll(Packets.Client.getSupported());
      } else if (side.isForServer()) {
         supported.addAll(Packets.Server.getSupported());
      }

      return supported;
   }

   private Set getNamedPackets(ConnectionSide side) {
      Set<Integer> valids = null;
      Set<Integer> result = Sets.newHashSet();

      try {
         var6 = this.getValidPackets(side);
      } catch (FieldAccessException var5) {
         var6 = Ranges.closed(0, 255).asSet(DiscreteDomains.integers());
      }

      if (side.isForClient()) {
         result.addAll(Packets.Client.getRegistry().values());
      }

      if (side.isForServer()) {
         result.addAll(Packets.Server.getRegistry().values());
      }

      result.retainAll((Collection)var6);
      return result;
   }

   public DetailedPacketListener createPacketListener(final ConnectionSide side, int idStart, int idStop, final boolean detailed) {
      Set<Integer> range = Ranges.closed(idStart, idStop).asSet(DiscreteDomains.integers());

      Set<Integer> packets;
      try {
         packets = new HashSet(this.getValidPackets(side));
         packets.retainAll(range);
      } catch (FieldAccessException var8) {
         packets = range;
      }

      if (packets.isEmpty()) {
         return null;
      } else {
         final ListeningWhitelist whitelist = new ListeningWhitelist(ListenerPriority.MONITOR, packets, GamePhase.BOTH);
         return new DetailedPacketListener() {
            public void onPacketSending(PacketEvent event) {
               if (side.isForServer() && CommandPacket.this.filter.filterEvent(event)) {
                  this.printInformation(event);
               }

            }

            public void onPacketReceiving(PacketEvent event) {
               if (side.isForClient() && CommandPacket.this.filter.filterEvent(event)) {
                  this.printInformation(event);
               }

            }

            private void printInformation(PacketEvent event) {
               String verb = side.isForClient() ? "Received" : "Sent";
               String format = side.isForClient() ? "%s %s (%s) from %s" : "%s %s (%s) to %s";
               String shortDescription = String.format(format, event.isCancelled() ? "Cancelled" : verb, Packets.getDeclaredName(event.getPacketID()), event.getPacketID(), event.getPlayer().getName());
               if (detailed) {
                  try {
                     Object packet = event.getPacket().getHandle();

                     Class<?> clazz;
                     for(clazz = packet.getClass(); (!MinecraftReflection.isMinecraftClass(clazz) || Factory.class.isAssignableFrom(clazz)) && clazz != Object.class; clazz = clazz.getSuperclass()) {
                     }

                     CommandPacket.this.logger.info(shortDescription + ":\n" + PrettyPrinter.printObject(packet, clazz, MinecraftReflection.getPacketClass(), 3, new PrettyPrinter.ObjectPrinter() {
                        public boolean print(StringBuilder output, Object value) {
                           if (value != null) {
                              EquivalentConverter<Object> converter = (EquivalentConverter)BukkitConverters.getConvertersForGeneric().get(value.getClass());
                              if (converter != null) {
                                 output.append(converter.getSpecific(value));
                                 return true;
                              }
                           }

                           return false;
                        }
                     }));
                  } catch (IllegalAccessException e) {
                     CommandPacket.this.logger.log(Level.WARNING, "Unable to use reflection.", e);
                  }
               } else {
                  CommandPacket.this.logger.info(shortDescription + ".");
               }

            }

            public ListeningWhitelist getSendingWhitelist() {
               return side.isForServer() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
            }

            public ListeningWhitelist getReceivingWhitelist() {
               return side.isForClient() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
            }

            public Plugin getPlugin() {
               return CommandPacket.this.plugin;
            }

            public boolean isDetailed() {
               return detailed;
            }
         };
      }
   }

   public DetailedPacketListener addPacketListeners(ConnectionSide side, int idStart, int idStop, boolean detailed) {
      DetailedPacketListener listener = this.createPacketListener(side, idStart, idStop, detailed);
      if (listener != null) {
         if (side.isForClient()) {
            this.clientListeners.put(idStart, idStop, listener);
         }

         if (side.isForServer()) {
            this.serverListeners.put(idStart, idStop, listener);
         }

         return listener;
      } else {
         throw new IllegalArgumentException("No packets found in the range " + idStart + " - " + idStop + ".");
      }
   }

   public Set removePacketListeners(ConnectionSide side, int idStart, int idStop, boolean detailed) {
      HashSet<AbstractIntervalTree<Integer, DetailedPacketListener>.Entry> result = Sets.newHashSet();
      if (side.isForClient()) {
         result.addAll(this.clientListeners.remove(idStart, idStop, true));
      }

      if (side.isForServer()) {
         result.addAll(this.serverListeners.remove(idStart, idStop, true));
      }

      return result;
   }

   private SubCommand parseCommand(String[] args, int index) {
      String text = args[index].toLowerCase();
      if ("add".startsWith(text)) {
         return CommandPacket.SubCommand.ADD;
      } else if ("remove".startsWith(text)) {
         return CommandPacket.SubCommand.REMOVE;
      } else if ("names".startsWith(text)) {
         return CommandPacket.SubCommand.NAMES;
      } else if ("page".startsWith(text)) {
         return CommandPacket.SubCommand.PAGE;
      } else {
         throw new IllegalArgumentException(text + " is not a valid sub command. Must be add or remove.");
      }
   }

   private ConnectionSide parseSide(String[] args, int index, ConnectionSide defaultValue) {
      if (index < args.length) {
         String text = args[index].toLowerCase();
         if ("client".startsWith(text)) {
            return ConnectionSide.CLIENT_SIDE;
         } else if ("server".startsWith(text)) {
            return ConnectionSide.SERVER_SIDE;
         } else {
            throw new IllegalArgumentException(text + " is not a connection side.");
         }
      } else {
         return defaultValue;
      }
   }

   private static enum SubCommand {
      ADD,
      REMOVE,
      NAMES,
      PAGE;
   }

   private interface DetailedPacketListener extends PacketListener {
      boolean isDetailed();
   }
}
