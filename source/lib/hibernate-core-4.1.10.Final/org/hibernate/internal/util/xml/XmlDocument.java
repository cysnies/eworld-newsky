package org.hibernate.internal.util.xml;

import java.io.Serializable;
import org.dom4j.Document;

public interface XmlDocument extends Serializable {
   Document getDocumentTree();

   Origin getOrigin();
}
