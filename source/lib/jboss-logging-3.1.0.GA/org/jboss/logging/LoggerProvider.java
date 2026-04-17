package org.jboss.logging;

import java.util.Map;

public interface LoggerProvider {
   Logger getLogger(String var1);

   Object putMdc(String var1, Object var2);

   Object getMdc(String var1);

   void removeMdc(String var1);

   Map getMdcMap();

   void clearNdc();

   String getNdc();

   int getNdcDepth();

   String popNdc();

   String peekNdc();

   void pushNdc(String var1);

   void setNdcMaxDepth(int var1);
}
