package com.comphenix.protocol.error;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.google.common.primitives.Primitives;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DetailedErrorReporter implements ErrorReporter {
   public static final ReportType REPORT_EXCEPTION_COUNT = new ReportType("Internal exception count: %s!");
   public static final String SECOND_LEVEL_PREFIX = "  ";
   public static final String DEFAULT_PREFIX = "  ";
   public static final String DEFAULT_SUPPORT_URL = "http://dev.bukkit.org/server-mods/protocollib/";
   public static final String ERROR_PERMISSION = "protocol.info";
   public static final int DEFAULT_MAX_ERROR_COUNT = 20;
   private ConcurrentMap warningCount;
   protected String prefix;
   protected String supportURL;
   protected AtomicInteger internalErrorCount;
   protected int maxErrorCount;
   protected Logger logger;
   protected WeakReference pluginReference;
   protected String pluginName;
   protected boolean apacheCommonsMissing;
   protected boolean detailedReporting;
   protected Map globalParameters;

   public DetailedErrorReporter(Plugin plugin) {
      this(plugin, "  ", "http://dev.bukkit.org/server-mods/protocollib/");
   }

   public DetailedErrorReporter(Plugin plugin, String prefix, String supportURL) {
      this(plugin, prefix, supportURL, 20, getBukkitLogger());
   }

   public DetailedErrorReporter(Plugin plugin, String prefix, String supportURL, int maxErrorCount, Logger logger) {
      this.warningCount = new ConcurrentHashMap();
      this.internalErrorCount = new AtomicInteger();
      this.globalParameters = new HashMap();
      if (plugin == null) {
         throw new IllegalArgumentException("Plugin cannot be NULL.");
      } else {
         this.pluginReference = new WeakReference(plugin);
         this.pluginName = plugin.getName();
         this.prefix = prefix;
         this.supportURL = supportURL;
         this.maxErrorCount = maxErrorCount;
         this.logger = logger;
      }
   }

   private static Logger getBukkitLogger() {
      try {
         return Bukkit.getLogger();
      } catch (Throwable var1) {
         return Logger.getLogger("Minecraft");
      }
   }

   public boolean isDetailedReporting() {
      return this.detailedReporting;
   }

   public void setDetailedReporting(boolean detailedReporting) {
      this.detailedReporting = detailedReporting;
   }

   public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
      if (this.reportMinimalNoSpam(sender, methodName, error) && parameters != null && parameters.length > 0) {
         this.logger.log(Level.SEVERE, this.printParameters(parameters));
      }

   }

   public void reportMinimal(Plugin sender, String methodName, Throwable error) {
      this.reportMinimalNoSpam(sender, methodName, error);
   }

   public boolean reportMinimalNoSpam(Plugin sender, String methodName, Throwable error) {
      String pluginName = PacketAdapter.getPluginName(sender);
      AtomicInteger counter = (AtomicInteger)this.warningCount.get(pluginName);
      if (counter == null) {
         AtomicInteger created = new AtomicInteger();
         counter = (AtomicInteger)this.warningCount.putIfAbsent(pluginName, created);
         if (counter == null) {
            counter = created;
         }
      }

      int errorCount = counter.incrementAndGet();
      if (errorCount < this.getMaxErrorCount()) {
         this.logger.log(Level.SEVERE, "[" + pluginName + "] Unhandled exception occured in " + methodName + " for " + pluginName, error);
         return true;
      } else {
         if (this.isPowerOfTwo(errorCount)) {
            this.logger.log(Level.SEVERE, "[" + pluginName + "] Unhandled exception number " + errorCount + " occured in " + methodName + " for " + pluginName, error);
         }

         return false;
      }
   }

   private boolean isPowerOfTwo(int number) {
      return (number & number - 1) == 0;
   }

   public void reportWarning(Object sender, Report.ReportBuilder reportBuilder) {
      if (reportBuilder == null) {
         throw new IllegalArgumentException("reportBuilder cannot be NULL.");
      } else {
         this.reportWarning(sender, reportBuilder.build());
      }
   }

   public void reportWarning(Object sender, Report report) {
      String message = "[" + this.pluginName + "] [" + this.getSenderName(sender) + "] " + report.getReportMessage();
      if (report.getException() != null) {
         this.logger.log(Level.WARNING, message, report.getException());
      } else {
         this.logger.log(Level.WARNING, message);
         if (this.detailedReporting) {
            this.printCallStack(Level.WARNING, this.logger);
         }
      }

      if (report.hasCallerParameters()) {
         this.logger.log(Level.WARNING, this.printParameters(report.getCallerParameters()));
      }

   }

   private String getSenderName(Object sender) {
      return sender != null ? ReportType.getSenderClass(sender).getSimpleName() : "NULL";
   }

   public void reportDetailed(Object sender, Report.ReportBuilder reportBuilder) {
      this.reportDetailed(sender, reportBuilder.build());
   }

   public void reportDetailed(Object sender, Report report) {
      Plugin plugin = (Plugin)this.pluginReference.get();
      int errorCount = this.internalErrorCount.incrementAndGet();
      if (errorCount > this.getMaxErrorCount()) {
         if (!this.isPowerOfTwo(errorCount)) {
            return;
         }

         this.reportWarning(this, (Report)Report.newBuilder(REPORT_EXCEPTION_COUNT).messageParam(errorCount).build());
      }

      StringWriter text = new StringWriter();
      PrintWriter writer = new PrintWriter(text);
      writer.println("[" + this.pluginName + "] INTERNAL ERROR: " + report.getReportMessage());
      writer.println("If this problem hasn't already been reported, please open a ticket");
      writer.println("at " + this.supportURL + " with the following data:");
      writer.println("          ===== STACK TRACE =====");
      if (report.getException() != null) {
         report.getException().printStackTrace(writer);
      } else if (this.detailedReporting) {
         this.printCallStack(writer);
      }

      writer.println("          ===== DUMP =====");
      if (report.hasCallerParameters()) {
         this.printParameters(writer, report.getCallerParameters());
      }

      for(String param : this.globalParameters()) {
         writer.println("  " + param + ":");
         writer.println(this.addPrefix(this.getStringDescription(this.getGlobalParameter(param)), "    "));
      }

      writer.println("Sender:");
      writer.println(this.addPrefix(this.getStringDescription(sender), "  "));
      if (plugin != null) {
         writer.println("Version:");
         writer.println(this.addPrefix(plugin.toString(), "  "));
      }

      if (Bukkit.getServer() != null) {
         writer.println("Server:");
         writer.println(this.addPrefix(Bukkit.getServer().getVersion(), "  "));
         if ("protocol.info" != null) {
            Bukkit.getServer().broadcast(String.format("Error %s (%s) occured in %s.", report.getReportMessage(), report.getException(), sender), "protocol.info");
         }
      }

      this.logger.severe(this.addPrefix(text.toString(), this.prefix));
   }

   private void printCallStack(Level level, Logger logger) {
      StringWriter text = new StringWriter();
      this.printCallStack(new PrintWriter(text));
      logger.log(level, text.toString());
   }

   private void printCallStack(PrintWriter writer) {
      Exception current = new Exception("Not an error! This is the call stack.");
      current.printStackTrace(writer);
   }

   private String printParameters(Object... parameters) {
      StringWriter writer = new StringWriter();
      this.printParameters(new PrintWriter(writer), parameters);
      return writer.toString();
   }

   private void printParameters(PrintWriter writer, Object[] parameters) {
      writer.println("Parameters: ");

      for(Object param : parameters) {
         writer.println(this.addPrefix(this.getStringDescription(param), "  "));
      }

   }

   protected String addPrefix(String text, String prefix) {
      return text.replaceAll("(?m)^", prefix);
   }

   protected String getStringDescription(Object value) {
      if (value == null) {
         return "[NULL]";
      } else if (!this.isSimpleType(value) && !(value instanceof Class)) {
         try {
            if (!this.apacheCommonsMissing) {
               return ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE, false, (Class)null);
            }
         } catch (Throwable var4) {
            this.apacheCommonsMissing = true;
         }

         try {
            return PrettyPrinter.printObject(value, value.getClass(), Object.class);
         } catch (IllegalAccessException e) {
            return "[Error: " + e.getMessage() + "]";
         }
      } else {
         return value.toString();
      }
   }

   protected boolean isSimpleType(Object test) {
      return test instanceof String || Primitives.isWrapperType(test.getClass());
   }

   public int getErrorCount() {
      return this.internalErrorCount.get();
   }

   public void setErrorCount(int errorCount) {
      this.internalErrorCount.set(errorCount);
   }

   public int getMaxErrorCount() {
      return this.maxErrorCount;
   }

   public void setMaxErrorCount(int maxErrorCount) {
      this.maxErrorCount = maxErrorCount;
   }

   public void addGlobalParameter(String key, Object value) {
      if (key == null) {
         throw new IllegalArgumentException("key cannot be NULL.");
      } else if (value == null) {
         throw new IllegalArgumentException("value cannot be NULL.");
      } else {
         this.globalParameters.put(key, value);
      }
   }

   public Object getGlobalParameter(String key) {
      if (key == null) {
         throw new IllegalArgumentException("key cannot be NULL.");
      } else {
         return this.globalParameters.get(key);
      }
   }

   public void clearGlobalParameters() {
      this.globalParameters.clear();
   }

   public Set globalParameters() {
      return this.globalParameters.keySet();
   }

   public String getSupportURL() {
      return this.supportURL;
   }

   public void setSupportURL(String supportURL) {
      this.supportURL = supportURL;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public Logger getLogger() {
      return this.logger;
   }

   public void setLogger(Logger logger) {
      this.logger = logger;
   }
}
