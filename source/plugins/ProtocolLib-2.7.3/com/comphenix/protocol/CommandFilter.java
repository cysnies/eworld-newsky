package com.comphenix.protocol;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.Plugin;

public class CommandFilter extends CommandBase {
   public static final ReportType REPORT_FALLBACK_ENGINE = new ReportType("Falling back to the Rhino engine.");
   public static final ReportType REPORT_CANNOT_LOAD_FALLBACK_ENGINE = new ReportType("Could not load Rhino either. Please upgrade your JVM or OS.");
   public static final ReportType REPORT_PACKAGES_UNSUPPORTED_IN_ENGINE = new ReportType("Unable to initialize packages for JavaScript engine.");
   public static final ReportType REPORT_FILTER_REMOVED_FOR_ERROR = new ReportType("Removing filter %s for causing %s.");
   public static final ReportType REPORT_CANNOT_HANDLE_CONVERSATION = new ReportType("Cannot handle conversation.");
   public static final String NAME = "filter";
   private FilterFailedHandler defaultFailedHandler;
   private List filters = new ArrayList();
   private final Plugin plugin;
   private ProtocolConfig config;
   private ScriptEngine engine;
   private boolean uninitialized;

   public CommandFilter(ErrorReporter reporter, Plugin plugin, ProtocolConfig config) {
      super(reporter, "protocol.admin", "filter", 2);
      this.plugin = plugin;
      this.config = config;
      this.uninitialized = true;
   }

   private void initalizeScript() {
      try {
         this.initializeEngine();
         if (!this.isInitialized()) {
            throw new ScriptException("A JavaScript engine could not be found.");
         }

         this.plugin.getLogger().info("Loaded command filter engine.");
      } catch (ScriptException e1) {
         this.printPackageWarning(e1);
         if (!this.config.getScriptEngineName().equals("rhino")) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_FALLBACK_ENGINE));
            this.config.setScriptEngineName("rhino");
            this.config.saveAll();

            try {
               this.initializeEngine();
               if (!this.isInitialized()) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_LOAD_FALLBACK_ENGINE));
               }
            } catch (ScriptException e2) {
               this.printPackageWarning(e2);
            }
         }
      }

   }

   private void printPackageWarning(ScriptException e) {
      this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PACKAGES_UNSUPPORTED_IN_ENGINE).error(e));
   }

   private void initializeEngine() throws ScriptException {
      ScriptEngineManager manager = new ScriptEngineManager();
      this.engine = manager.getEngineByName(this.config.getScriptEngineName());
      if (this.engine != null) {
         this.engine.eval("importPackage(org.bukkit);");
         this.engine.eval("importPackage(com.comphenix.protocol.reflect);");
      }

   }

   public boolean isInitialized() {
      return this.engine != null;
   }

   private FilterFailedHandler getDefaultErrorHandler() {
      if (this.defaultFailedHandler == null) {
         this.defaultFailedHandler = new FilterFailedHandler() {
            public boolean handle(PacketEvent event, Filter filter, Exception ex) {
               CommandFilter.this.reporter.reportMinimal(CommandFilter.this.plugin, "filterEvent(PacketEvent)", ex, event);
               CommandFilter.this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(CommandFilter.REPORT_FILTER_REMOVED_FOR_ERROR).messageParam(filter.getName(), ex.getClass().getSimpleName()));
               return false;
            }
         };
      }

      return this.defaultFailedHandler;
   }

   public boolean filterEvent(PacketEvent event) {
      return this.filterEvent(event, this.getDefaultErrorHandler());
   }

   public boolean filterEvent(PacketEvent event, FilterFailedHandler handler) {
      Iterator<Filter> it = this.filters.iterator();

      while(it.hasNext()) {
         Filter filter = (Filter)it.next();

         try {
            if (!filter.evaluate(this.engine, event)) {
               return false;
            }
         } catch (Exception ex) {
            if (!handler.handle(event, filter, ex)) {
               it.remove();
            }
         }
      }

      return true;
   }

   private void checkScriptStatus() {
      if (this.uninitialized) {
         this.uninitialized = false;
         this.initalizeScript();
      }

   }

   protected boolean handleCommand(CommandSender sender, String[] args) {
      this.checkScriptStatus();
      if (!this.config.isDebug()) {
         sender.sendMessage(ChatColor.RED + "Debug mode must be enabled in the configuration first!");
         return true;
      } else if (!this.isInitialized()) {
         sender.sendMessage(ChatColor.RED + "JavaScript engine was not present. Filter system is disabled.");
         return true;
      } else {
         SubCommand command = this.parseCommand(args, 0);
         final String name = args[1];
         switch (command) {
            case ADD:
               if (this.findFilter(name) != null) {
                  sender.sendMessage(ChatColor.RED + "Filter " + name + " already exists. Remove it first.");
                  return true;
               }

               final Set<Integer> packets = this.parseRanges(args, 2);
               sender.sendMessage("Enter filter program ('}' to complete or CANCEL):");
               if (sender instanceof Conversable) {
                  final MultipleLinesPrompt prompt = new MultipleLinesPrompt(new CompilationSuccessCanceller(), "function(event, packet) {");
                  (new ConversationFactory(this.plugin)).withFirstPrompt(prompt).withEscapeSequence("CANCEL").withLocalEcho(false).addConversationAbandonedListener(new ConversationAbandonedListener() {
                     public void conversationAbandoned(ConversationAbandonedEvent event) {
                        try {
                           Conversable whom = event.getContext().getForWhom();
                           if (event.gracefulExit()) {
                              String predicate = prompt.removeAccumulatedInput(event.getContext());
                              Filter filter = new Filter(name, predicate, packets);
                              whom.sendRawMessage(prompt.getPromptText(event.getContext()));

                              try {
                                 filter.compile(CommandFilter.this.engine);
                                 CommandFilter.this.filters.add(filter);
                                 whom.sendRawMessage(ChatColor.GOLD + "Added filter " + name);
                              } catch (ScriptException e) {
                                 e.printStackTrace();
                                 whom.sendRawMessage(ChatColor.GOLD + "Compilation error: " + e.getMessage());
                              }
                           } else {
                              whom.sendRawMessage(ChatColor.RED + "Cancelled filter.");
                           }
                        } catch (Exception e) {
                           CommandFilter.this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(CommandFilter.REPORT_CANNOT_HANDLE_CONVERSATION).error(e).callerParam(event));
                        }

                     }
                  }).buildConversation((Conversable)sender).begin();
               } else {
                  sender.sendMessage(ChatColor.RED + "Only console and players are supported!");
               }
               break;
            case REMOVE:
               Filter filter = this.findFilter(name);
               if (filter != null) {
                  filter.close(this.engine);
                  this.filters.remove(filter);
                  sender.sendMessage(ChatColor.GOLD + "Removed filter " + name);
               } else {
                  sender.sendMessage(ChatColor.RED + "Unable to find a filter by the name " + name);
               }
         }

         return true;
      }
   }

   private Set parseRanges(String[] args, int start) {
      List<Range<Integer>> ranges = RangeParser.getRanges(args, 2, args.length - 1, Ranges.closed(0, 255));
      Set<Integer> flatten = new HashSet();
      if (ranges.isEmpty()) {
         ranges.add(Ranges.closed(0, 255));
      }

      for(Range range : ranges) {
         flatten.addAll(range.asSet(DiscreteDomains.integers()));
      }

      return flatten;
   }

   private Filter findFilter(String name) {
      for(Filter filter : this.filters) {
         if (filter.getName().equalsIgnoreCase(name)) {
            return filter;
         }
      }

      return null;
   }

   private SubCommand parseCommand(String[] args, int index) {
      String text = args[index].toUpperCase();

      try {
         return CommandFilter.SubCommand.valueOf(text);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(text + " is not a valid sub command. Must be add or remove.", e);
      }
   }

   private static enum SubCommand {
      ADD,
      REMOVE;
   }

   public static class Filter {
      private final String name;
      private final String predicate;
      private final IntegerSet ranges;

      public Filter(String name, String predicate, Set packets) {
         this.name = name;
         this.predicate = predicate;
         this.ranges = new IntegerSet(256);
         this.ranges.addAll(packets);
      }

      public String getName() {
         return this.name;
      }

      public String getPredicate() {
         return this.predicate;
      }

      public Set getRanges() {
         return this.ranges.toSet();
      }

      private boolean isApplicable(PacketEvent event) {
         return this.ranges.contains(event.getPacketID());
      }

      public boolean evaluate(ScriptEngine context, PacketEvent event) throws ScriptException {
         if (!this.isApplicable(event)) {
            return true;
         } else {
            this.compile(context);

            try {
               Object result = ((Invocable)context).invokeFunction(this.name, new Object[]{event, event.getPacket().getHandle()});
               if (result instanceof Boolean) {
                  return (Boolean)result;
               } else {
                  throw new ScriptException("Filter result wasn't a boolean: " + result);
               }
            } catch (NoSuchMethodException e) {
               throw new IllegalStateException("Unable to compile " + this.name + " into current script engine.", e);
            }
         }
      }

      public void compile(ScriptEngine context) throws ScriptException {
         if (context.get(this.name) == null) {
            context.eval("var " + this.name + " = function(event, packet) {\n" + this.predicate);
         }

      }

      public void close(ScriptEngine context) {
         context.put(this.name, (Object)null);
      }
   }

   private class CompilationSuccessCanceller implements MultipleLinesPrompt.MultipleConversationCanceller {
      private CompilationSuccessCanceller() {
      }

      public boolean cancelBasedOnInput(ConversationContext context, String in) {
         throw new UnsupportedOperationException("Cannot cancel on the last line alone.");
      }

      public void setConversation(Conversation conversation) {
      }

      public boolean cancelBasedOnInput(ConversationContext context, String currentLine, StringBuilder lines, int lineCount) {
         try {
            CommandFilter.this.engine.eval("function(event, packet) {\n" + lines.toString());
            return true;
         } catch (ScriptException e) {
            int realLineCount = lineCount + 1;
            return e.getLineNumber() < realLineCount;
         }
      }

      public CompilationSuccessCanceller clone() {
         return CommandFilter.this.new CompilationSuccessCanceller();
      }
   }

   public interface FilterFailedHandler {
      boolean handle(PacketEvent var1, Filter var2, Exception var3);
   }
}
