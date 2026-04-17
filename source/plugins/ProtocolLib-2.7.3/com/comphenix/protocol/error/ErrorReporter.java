package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

public interface ErrorReporter {
   void reportMinimal(Plugin var1, String var2, Throwable var3);

   void reportMinimal(Plugin var1, String var2, Throwable var3, Object... var4);

   void reportWarning(Object var1, Report var2);

   void reportWarning(Object var1, Report.ReportBuilder var2);

   void reportDetailed(Object var1, Report var2);

   void reportDetailed(Object var1, Report.ReportBuilder var2);
}
