package com.sk89q.worldedit.scripting.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class RhinoScriptEngineFactory implements ScriptEngineFactory {
   private static List names = new ArrayList(5);
   private static List mimeTypes;
   private static List extensions;

   public String getEngineName() {
      return "Rhino JavaScript Engine (SK)";
   }

   public String getEngineVersion() {
      return "unknown";
   }

   public List getExtensions() {
      return extensions;
   }

   public String getLanguageName() {
      return "EMCAScript";
   }

   public String getLanguageVersion() {
      return "1.8";
   }

   public String getMethodCallSyntax(String obj, String m, String... args) {
      StringBuilder s = new StringBuilder();
      s.append(obj);
      s.append(".");
      s.append(m);
      s.append("(");

      for(int i = 0; i < args.length; ++i) {
         s.append(args[i]);
         if (i < args.length - 1) {
            s.append(",");
         }
      }

      s.append(")");
      return s.toString();
   }

   public List getMimeTypes() {
      return mimeTypes;
   }

   public List getNames() {
      return names;
   }

   public String getOutputStatement(String str) {
      return "print(" + str.replace("\\", "\\\\").replace("\"", "\\\\\"").replace(";", "\\\\;") + ")";
   }

   public Object getParameter(String key) {
      if (key.equals("javax.script.engine")) {
         return this.getEngineName();
      } else if (key.equals("javax.script.engine_version")) {
         return this.getEngineVersion();
      } else if (key.equals("javax.script.name")) {
         return this.getEngineName();
      } else if (key.equals("javax.script.language")) {
         return this.getLanguageName();
      } else if (key.equals("javax.script.language_version")) {
         return this.getLanguageVersion();
      } else if (key.equals("THREADING")) {
         return "MULTITHREADED";
      } else {
         throw new IllegalArgumentException("Invalid key");
      }
   }

   public String getProgram(String... statements) {
      StringBuilder s = new StringBuilder();

      for(String stmt : statements) {
         s.append(stmt);
         s.append(";");
      }

      return s.toString();
   }

   public ScriptEngine getScriptEngine() {
      return new RhinoScriptEngine();
   }

   static {
      names.add("ECMAScript");
      names.add("ecmascript");
      names.add("JavaScript");
      names.add("javascript");
      names.add("js");
      names = Collections.unmodifiableList(names);
      mimeTypes = new ArrayList(4);
      mimeTypes.add("application/ecmascript");
      mimeTypes.add("text/ecmascript");
      mimeTypes.add("application/javascript");
      mimeTypes.add("text/javascript");
      mimeTypes = Collections.unmodifiableList(mimeTypes);
      extensions = new ArrayList(2);
      extensions.add("emcascript");
      extensions.add("js");
      extensions = Collections.unmodifiableList(extensions);
   }
}
