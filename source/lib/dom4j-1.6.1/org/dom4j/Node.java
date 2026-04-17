package org.dom4j;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public interface Node extends Cloneable {
   short ANY_NODE = 0;
   short ELEMENT_NODE = 1;
   short ATTRIBUTE_NODE = 2;
   short TEXT_NODE = 3;
   short CDATA_SECTION_NODE = 4;
   short ENTITY_REFERENCE_NODE = 5;
   short PROCESSING_INSTRUCTION_NODE = 7;
   short COMMENT_NODE = 8;
   short DOCUMENT_NODE = 9;
   short DOCUMENT_TYPE_NODE = 10;
   short NAMESPACE_NODE = 13;
   short UNKNOWN_NODE = 14;
   short MAX_NODE_TYPE = 14;

   boolean supportsParent();

   Element getParent();

   void setParent(Element var1);

   Document getDocument();

   void setDocument(Document var1);

   boolean isReadOnly();

   boolean hasContent();

   String getName();

   void setName(String var1);

   String getText();

   void setText(String var1);

   String getStringValue();

   String getPath();

   String getPath(Element var1);

   String getUniquePath();

   String getUniquePath(Element var1);

   String asXML();

   void write(Writer var1) throws IOException;

   short getNodeType();

   String getNodeTypeName();

   Node detach();

   List selectNodes(String var1);

   Object selectObject(String var1);

   List selectNodes(String var1, String var2);

   List selectNodes(String var1, String var2, boolean var3);

   Node selectSingleNode(String var1);

   String valueOf(String var1);

   Number numberValueOf(String var1);

   boolean matches(String var1);

   XPath createXPath(String var1) throws InvalidXPathException;

   Node asXPathResult(Element var1);

   void accept(Visitor var1);

   Object clone();
}
