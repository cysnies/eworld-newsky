package org.dom4j;

import java.util.Map;

public interface ProcessingInstruction extends Node {
   String getTarget();

   void setTarget(String var1);

   String getText();

   String getValue(String var1);

   Map getValues();

   void setValue(String var1, String var2);

   void setValues(Map var1);

   boolean removeValue(String var1);
}
