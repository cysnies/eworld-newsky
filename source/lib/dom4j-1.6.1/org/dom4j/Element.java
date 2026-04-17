package org.dom4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Element extends Branch {
   QName getQName();

   void setQName(QName var1);

   Namespace getNamespace();

   QName getQName(String var1);

   Namespace getNamespaceForPrefix(String var1);

   Namespace getNamespaceForURI(String var1);

   List getNamespacesForURI(String var1);

   String getNamespacePrefix();

   String getNamespaceURI();

   String getQualifiedName();

   List additionalNamespaces();

   List declaredNamespaces();

   Element addAttribute(String var1, String var2);

   Element addAttribute(QName var1, String var2);

   Element addComment(String var1);

   Element addCDATA(String var1);

   Element addEntity(String var1, String var2);

   Element addNamespace(String var1, String var2);

   Element addProcessingInstruction(String var1, String var2);

   Element addProcessingInstruction(String var1, Map var2);

   Element addText(String var1);

   void add(Attribute var1);

   void add(CDATA var1);

   void add(Entity var1);

   void add(Text var1);

   void add(Namespace var1);

   boolean remove(Attribute var1);

   boolean remove(CDATA var1);

   boolean remove(Entity var1);

   boolean remove(Namespace var1);

   boolean remove(Text var1);

   String getText();

   String getTextTrim();

   String getStringValue();

   Object getData();

   void setData(Object var1);

   List attributes();

   void setAttributes(List var1);

   int attributeCount();

   Iterator attributeIterator();

   Attribute attribute(int var1);

   Attribute attribute(String var1);

   Attribute attribute(QName var1);

   String attributeValue(String var1);

   String attributeValue(String var1, String var2);

   String attributeValue(QName var1);

   String attributeValue(QName var1, String var2);

   /** @deprecated */
   void setAttributeValue(String var1, String var2);

   /** @deprecated */
   void setAttributeValue(QName var1, String var2);

   Element element(String var1);

   Element element(QName var1);

   List elements();

   List elements(String var1);

   List elements(QName var1);

   Iterator elementIterator();

   Iterator elementIterator(String var1);

   Iterator elementIterator(QName var1);

   boolean isRootElement();

   boolean hasMixedContent();

   boolean isTextOnly();

   void appendAttributes(Element var1);

   Element createCopy();

   Element createCopy(String var1);

   Element createCopy(QName var1);

   String elementText(String var1);

   String elementText(QName var1);

   String elementTextTrim(String var1);

   String elementTextTrim(QName var1);

   Node getXPathResult(int var1);
}
