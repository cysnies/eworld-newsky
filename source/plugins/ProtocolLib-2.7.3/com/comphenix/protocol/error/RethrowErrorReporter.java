package com.comphenix.protocol.error;

import com.google.common.base.Joiner;
import org.bukkit.plugin.Plugin;

public class RethrowErrorReporter implements ErrorReporter {
   public void reportMinimal(Plugin sender, String methodName, Throwable error) {
      throw new RuntimeException("Minimal error by " + sender + " in " + methodName, error);
   }

   public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
      throw new RuntimeException("Minimal error by " + sender + " in " + methodName + " with " + Joiner.on(",").join(parameters), error);
   }

   public void reportWarning(Object sender, Report.ReportBuilder reportBuilder) {
      this.reportWarning(sender, reportBuilder.build());
   }

   public void reportWarning(Object sender, Report report) {
      throw new RuntimeException("Warning by " + sender + ": " + report);
   }

   public void reportDetailed(Object sender, Report.ReportBuilder reportBuilder) {
      this.reportDetailed(sender, reportBuilder.build());
   }

   public void reportDetailed(Object sender, Report report) {
      throw new RuntimeException("Detailed error " + sender + ": " + report, report.getException());
   }
}
