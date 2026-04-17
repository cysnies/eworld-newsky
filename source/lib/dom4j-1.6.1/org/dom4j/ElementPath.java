package org.dom4j;

public interface ElementPath {
   int size();

   Element getElement(int var1);

   String getPath();

   Element getCurrent();

   void addHandler(String var1, ElementHandler var2);

   void removeHandler(String var1);
}
