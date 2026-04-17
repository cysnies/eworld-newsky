package fr.neatmonster.nocheatplus.logging;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class StaticLogFile {
   public static Logger fileLogger = null;
   private static FileHandler fileHandler = null;

   public static void cleanup() {
      fileHandler.flush();
      fileHandler.close();
      Logger logger = Logger.getLogger("NoCheatPlus");
      logger.removeHandler(fileHandler);
      fileHandler = null;
   }

   public static void setupLogger(File logFile) {
      Logger logger = Logger.getAnonymousLogger();
      logger.setLevel(Level.INFO);
      logger.setUseParentHandlers(false);

      for(Handler h : logger.getHandlers()) {
         logger.removeHandler(h);
      }

      if (fileHandler != null) {
         fileHandler.close();
         logger.removeHandler(fileHandler);
         fileHandler = null;
      }

      try {
         try {
            logFile.getParentFile().mkdirs();
         } catch (Exception e) {
            LogUtil.logSevere((Throwable)e);
         }

         fileHandler = new FileHandler(logFile.getCanonicalPath(), true);
         fileHandler.setLevel(Level.ALL);
         fileHandler.setFormatter(StaticLogFile.LogFileFormatter.newInstance());
         logger.addHandler(fileHandler);
      } catch (Exception e) {
         LogUtil.logSevere((Throwable)e);
      }

      fileLogger = logger;
   }

   protected static class LogFileFormatter extends Formatter {
      private final SimpleDateFormat date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

      public static LogFileFormatter newInstance() {
         return new LogFileFormatter();
      }

      private LogFileFormatter() {
      }

      public String format(LogRecord record) {
         StringBuilder builder = new StringBuilder();
         Throwable ex = record.getThrown();
         builder.append(this.date.format(record.getMillis()));
         builder.append(" [");
         builder.append(record.getLevel().getLocalizedName().toUpperCase());
         builder.append("] ");
         builder.append(record.getMessage());
         builder.append('\n');
         if (ex != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
         }

         return builder.toString();
      }
   }
}
