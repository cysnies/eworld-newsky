package org.dom4j;

import java.util.Map;
import org.xml.sax.EntityResolver;

public interface Document extends Branch {
   Element getRootElement();

   void setRootElement(Element var1);

   Document addComment(String var1);

   Document addProcessingInstruction(String var1, String var2);

   Document addProcessingInstruction(String var1, Map var2);

   Document addDocType(String var1, String var2, String var3);

   DocumentType getDocType();

   void setDocType(DocumentType var1);

   EntityResolver getEntityResolver();

   void setEntityResolver(EntityResolver var1);

   String getXMLEncoding();

   void setXMLEncoding(String var1);
}
