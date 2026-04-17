package org.dom4j;

public interface Attribute extends Node {
   QName getQName();

   Namespace getNamespace();

   void setNamespace(Namespace var1);

   String getNamespacePrefix();

   String getNamespaceURI();

   String getQualifiedName();

   String getValue();

   void setValue(String var1);

   Object getData();

   void setData(Object var1);
}
