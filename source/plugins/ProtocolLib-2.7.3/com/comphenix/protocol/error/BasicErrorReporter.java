package com.comphenix.protocol.error;

import com.comphenix.protocol.reflect.PrettyPrinter;
import java.io.PrintStream;
import org.bukkit.plugin.Plugin;

public class BasicErrorReporter implements ErrorReporter {
   private final PrintStream output;

   public BasicErrorReporter() {
      this(System.err);
   }

   public BasicErrorReporter(PrintStream output) {
      this.output = output;
   }

   public void reportMinimal(Plugin sender, String methodName, Throwable error) {
      this.output.println("Unhandled exception occured in " + methodName + " for " + sender.getName());
      error.printStackTrace(this.output);
   }

   public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
      this.reportMinimal(sender, methodName, error);
      this.printParameters(parameters);
   }

   public void reportWarning(Object sender, Report report) {
      this.output.println("[" + sender.getClass().getSimpleName() + "] " + report.getReportMessage());
      if (report.getException() != null) {
         report.getException().printStackTrace(this.output);
      }

      this.printParameters(report.getCallerParameters());
   }

   public void reportWarning(Object sender, Report.ReportBuilder reportBuilder) {
      this.reportWarning(sender, reportBuilder.build());
   }

   public void reportDetailed(Object sender, Report report) {
      this.reportWarning(sender, report);
   }

   public void reportDetailed(Object sender, Report.ReportBuilder reportBuilder) {
      this.reportWarning(sender, reportBuilder);
   }

   private void printParameters(Object[] parameters) {
      if (parameters != null && parameters.length > 0) {
         this.output.println("Parameters: ");

         try {
            for(Object parameter : parameters) {
               if (parameter == null) {
                  this.output.println("[NULL]");
               } else {
                  this.output.println(PrettyPrinter.printObject(parameter));
               }
            }
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         }
      }

   }
}
