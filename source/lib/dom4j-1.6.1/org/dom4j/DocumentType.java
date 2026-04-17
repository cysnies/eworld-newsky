package org.dom4j;

import java.util.List;

public interface DocumentType extends Node {
   String getElementName();

   void setElementName(String var1);

   String getPublicID();

   void setPublicID(String var1);

   String getSystemID();

   void setSystemID(String var1);

   List getInternalDeclarations();

   void setInternalDeclarations(List var1);

   List getExternalDeclarations();

   void setExternalDeclarations(List var1);
}
