package org.hibernate.internal.util.xml;

import java.io.Serializable;
import org.dom4j.Document;

public class XmlDocumentImpl implements XmlDocument, Serializable {
   private final Document documentTree;
   private final Origin origin;

   public XmlDocumentImpl(Document documentTree, String originType, String originName) {
      this(documentTree, new OriginImpl(originType, originName));
   }

   public XmlDocumentImpl(Document documentTree, Origin origin) {
      this.documentTree = documentTree;
      this.origin = origin;
   }

   public Document getDocumentTree() {
      return this.documentTree;
   }

   public Origin getOrigin() {
      return this.origin;
   }
}
