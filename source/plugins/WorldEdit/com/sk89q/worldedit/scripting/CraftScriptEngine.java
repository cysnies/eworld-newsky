package com.sk89q.worldedit.scripting;

import java.util.Map;
import javax.script.ScriptException;

public interface CraftScriptEngine {
   void setTimeLimit(int var1);

   int getTimeLimit();

   Object evaluate(String var1, String var2, Map var3) throws ScriptException, Throwable;
}
