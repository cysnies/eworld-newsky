package com.sk89q.worldedit.scripting;

import com.sk89q.worldedit.WorldEditException;
import java.util.Map;
import javax.script.ScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class RhinoCraftScriptEngine implements CraftScriptEngine {
   private int timeLimit;

   public void setTimeLimit(int milliseconds) {
      this.timeLimit = milliseconds;
   }

   public int getTimeLimit() {
      return this.timeLimit;
   }

   public Object evaluate(String script, String filename, Map args) throws ScriptException, Throwable {
      RhinoContextFactory factory = new RhinoContextFactory(this.timeLimit);
      Context cx = factory.enterContext();
      ScriptableObject scriptable = new ImporterTopLevel(cx);
      Scriptable scope = cx.initStandardObjects(scriptable);

      for(Map.Entry entry : args.entrySet()) {
         ScriptableObject.putProperty(scope, (String)entry.getKey(), Context.javaToJS(entry.getValue(), scope));
      }

      Object e;
      try {
         e = cx.evaluateString(scope, script, filename, 1, (Object)null);
      } catch (Error e) {
         throw new ScriptException(e.getMessage());
      } catch (RhinoException var17) {
         if (var17 instanceof WrappedException) {
            Throwable cause = ((WrappedException)var17).getCause();
            if (cause instanceof WorldEditException) {
               throw cause;
            }
         }

         int line;
         line = (line = var17.lineNumber()) == 0 ? -1 : line;
         String msg;
         if (var17 instanceof JavaScriptException) {
            msg = String.valueOf(((JavaScriptException)var17).getValue());
         } else {
            msg = var17.getMessage();
         }

         ScriptException scriptException = new ScriptException(msg, var17.sourceName(), line);
         scriptException.initCause(var17);
         throw scriptException;
      } finally {
         Context.exit();
      }

      return e;
   }
}
