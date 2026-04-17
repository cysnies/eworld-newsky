package org.dom4j.dom;

import java.util.List;
import org.dom4j.Branch;
import org.dom4j.CharacterData;
import org.dom4j.Element;
import org.dom4j.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DOMNodeHelper {
   public static final NodeList EMPTY_NODE_LIST = new EmptyNodeList();

   protected DOMNodeHelper() {
   }

   public static boolean supports(Node node, String feature, String version) {
      return false;
   }

   public static String getNamespaceURI(Node node) {
      return null;
   }

   public static String getPrefix(Node node) {
      return null;
   }

   public static String getLocalName(Node node) {
      return null;
   }

   public static void setPrefix(Node node, String prefix) throws DOMException {
      notSupported();
   }

   public static String getNodeValue(Node node) throws DOMException {
      return node.getText();
   }

   public static void setNodeValue(Node node, String nodeValue) throws DOMException {
      node.setText(nodeValue);
   }

   public static org.w3c.dom.Node getParentNode(Node node) {
      return asDOMNode(node.getParent());
   }

   public static NodeList getChildNodes(Node node) {
      return EMPTY_NODE_LIST;
   }

   public static org.w3c.dom.Node getFirstChild(Node node) {
      return null;
   }

   public static org.w3c.dom.Node getLastChild(Node node) {
      return null;
   }

   public static org.w3c.dom.Node getPreviousSibling(Node node) {
      Element parent = node.getParent();
      if (parent != null) {
         int index = parent.indexOf(node);
         if (index > 0) {
            Node previous = parent.node(index - 1);
            return asDOMNode(previous);
         }
      }

      return null;
   }

   public static org.w3c.dom.Node getNextSibling(Node node) {
      Element parent = node.getParent();
      if (parent != null) {
         int index = parent.indexOf(node);
         if (index >= 0) {
            ++index;
            if (index < parent.nodeCount()) {
               Node next = parent.node(index);
               return asDOMNode(next);
            }
         }
      }

      return null;
   }

   public static NamedNodeMap getAttributes(Node node) {
      return null;
   }

   public static Document getOwnerDocument(Node node) {
      return asDOMDocument(node.getDocument());
   }

   public static org.w3c.dom.Node insertBefore(Node node, org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException {
      if (node instanceof Branch) {
         Branch branch = (Branch)node;
         List list = branch.content();
         int index = list.indexOf(refChild);
         if (index < 0) {
            branch.add((Node)newChild);
         } else {
            list.add(index, newChild);
         }

         return newChild;
      } else {
         throw new DOMException((short)3, "Children not allowed for this node: " + node);
      }
   }

   public static org.w3c.dom.Node replaceChild(Node node, org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild) throws DOMException {
      if (node instanceof Branch) {
         Branch branch = (Branch)node;
         List list = branch.content();
         int index = list.indexOf(oldChild);
         if (index < 0) {
            throw new DOMException((short)8, "Tried to replace a non existing child for node: " + node);
         } else {
            list.set(index, newChild);
            return oldChild;
         }
      } else {
         throw new DOMException((short)3, "Children not allowed for this node: " + node);
      }
   }

   public static org.w3c.dom.Node removeChild(Node node, org.w3c.dom.Node oldChild) throws DOMException {
      if (node instanceof Branch) {
         Branch branch = (Branch)node;
         branch.remove((Node)oldChild);
         return oldChild;
      } else {
         throw new DOMException((short)3, "Children not allowed for this node: " + node);
      }
   }

   public static org.w3c.dom.Node appendChild(Node node, org.w3c.dom.Node newChild) throws DOMException {
      if (node instanceof Branch) {
         Branch branch = (Branch)node;
         org.w3c.dom.Node previousParent = newChild.getParentNode();
         if (previousParent != null) {
            previousParent.removeChild(newChild);
         }

         branch.add((Node)newChild);
         return newChild;
      } else {
         throw new DOMException((short)3, "Children not allowed for this node: " + node);
      }
   }

   public static boolean hasChildNodes(Node node) {
      return false;
   }

   public static org.w3c.dom.Node cloneNode(Node node, boolean deep) {
      return asDOMNode((Node)node.clone());
   }

   public static void normalize(Node node) {
      notSupported();
   }

   public static boolean isSupported(Node n, String feature, String version) {
      return false;
   }

   public static boolean hasAttributes(Node node) {
      if (node != null && node instanceof Element) {
         return ((Element)node).attributeCount() > 0;
      } else {
         return false;
      }
   }

   public static String getData(CharacterData charData) throws DOMException {
      return charData.getText();
   }

   public static void setData(CharacterData charData, String data) throws DOMException {
      charData.setText(data);
   }

   public static int getLength(CharacterData charData) {
      String text = charData.getText();
      return text != null ? text.length() : 0;
   }

   public static String substringData(CharacterData charData, int offset, int count) throws DOMException {
      if (count < 0) {
         throw new DOMException((short)1, "Illegal value for count: " + count);
      } else {
         String text = charData.getText();
         int length = text != null ? text.length() : 0;
         if (offset >= 0 && offset < length) {
            return offset + count > length ? text.substring(offset) : text.substring(offset, offset + count);
         } else {
            throw new DOMException((short)1, "No text at offset: " + offset);
         }
      }
   }

   public static void appendData(CharacterData charData, String arg) throws DOMException {
      if (charData.isReadOnly()) {
         throw new DOMException((short)7, "CharacterData node is read only: " + charData);
      } else {
         String text = charData.getText();
         if (text == null) {
            charData.setText(text);
         } else {
            charData.setText(text + arg);
         }

      }
   }

   public static void insertData(CharacterData data, int offset, String arg) throws DOMException {
      if (data.isReadOnly()) {
         throw new DOMException((short)7, "CharacterData node is read only: " + data);
      } else {
         String text = data.getText();
         if (text == null) {
            data.setText(arg);
         } else {
            int length = text.length();
            if (offset < 0 || offset > length) {
               throw new DOMException((short)1, "No text at offset: " + offset);
            }

            StringBuffer buffer = new StringBuffer(text);
            buffer.insert(offset, arg);
            data.setText(buffer.toString());
         }

      }
   }

   public static void deleteData(CharacterData charData, int offset, int count) throws DOMException {
      if (charData.isReadOnly()) {
         throw new DOMException((short)7, "CharacterData node is read only: " + charData);
      } else if (count < 0) {
         throw new DOMException((short)1, "Illegal value for count: " + count);
      } else {
         String text = charData.getText();
         if (text != null) {
            int length = text.length();
            if (offset < 0 || offset >= length) {
               throw new DOMException((short)1, "No text at offset: " + offset);
            }

            StringBuffer buffer = new StringBuffer(text);
            buffer.delete(offset, offset + count);
            charData.setText(buffer.toString());
         }

      }
   }

   public static void replaceData(CharacterData charData, int offset, int count, String arg) throws DOMException {
      if (charData.isReadOnly()) {
         throw new DOMException((short)7, "CharacterData node is read only: " + charData);
      } else if (count < 0) {
         throw new DOMException((short)1, "Illegal value for count: " + count);
      } else {
         String text = charData.getText();
         if (text != null) {
            int length = text.length();
            if (offset < 0 || offset >= length) {
               throw new DOMException((short)1, "No text at offset: " + offset);
            }

            StringBuffer buffer = new StringBuffer(text);
            buffer.replace(offset, offset + count, arg);
            charData.setText(buffer.toString());
         }

      }
   }

   public static void appendElementsByTagName(List list, Branch parent, String name) {
      boolean isStar = "*".equals(name);
      int i = 0;

      for(int size = parent.nodeCount(); i < size; ++i) {
         Node node = parent.node(i);
         if (node instanceof Element) {
            Element element = (Element)node;
            if (isStar || name.equals(element.getName())) {
               list.add(element);
            }

            appendElementsByTagName(list, element, name);
         }
      }

   }

   public static void appendElementsByTagNameNS(List list, Branch parent, String namespace, String localName) {
      boolean isStarNS = "*".equals(namespace);
      boolean isStar = "*".equals(localName);
      int i = 0;

      for(int size = parent.nodeCount(); i < size; ++i) {
         Node node = parent.node(i);
         if (node instanceof Element) {
            Element element = (Element)node;
            if ((isStarNS || (namespace == null || namespace.length() == 0) && (element.getNamespaceURI() == null || element.getNamespaceURI().length() == 0) || namespace != null && namespace.equals(element.getNamespaceURI())) && (isStar || localName.equals(element.getName()))) {
               list.add(element);
            }

            appendElementsByTagNameNS(list, element, namespace, localName);
         }
      }

   }

   public static NodeList createNodeList(final List list) {
      return new NodeList() {
         public org.w3c.dom.Node item(int index) {
            return index >= this.getLength() ? null : DOMNodeHelper.asDOMNode((Node)list.get(index));
         }

         public int getLength() {
            return list.size();
         }
      };
   }

   public static org.w3c.dom.Node asDOMNode(Node node) {
      if (node == null) {
         return null;
      } else if (node instanceof org.w3c.dom.Node) {
         return (org.w3c.dom.Node)node;
      } else {
         System.out.println("Cannot convert: " + node + " into a W3C DOM Node");
         notSupported();
         return null;
      }
   }

   public static Document asDOMDocument(org.dom4j.Document document) {
      if (document == null) {
         return null;
      } else if (document instanceof Document) {
         return (Document)document;
      } else {
         notSupported();
         return null;
      }
   }

   public static DocumentType asDOMDocumentType(org.dom4j.DocumentType dt) {
      if (dt == null) {
         return null;
      } else if (dt instanceof DocumentType) {
         return (DocumentType)dt;
      } else {
         notSupported();
         return null;
      }
   }

   public static Text asDOMText(CharacterData text) {
      if (text == null) {
         return null;
      } else if (text instanceof Text) {
         return (Text)text;
      } else {
         notSupported();
         return null;
      }
   }

   public static org.w3c.dom.Element asDOMElement(Node element) {
      if (element == null) {
         return null;
      } else if (element instanceof org.w3c.dom.Element) {
         return (org.w3c.dom.Element)element;
      } else {
         notSupported();
         return null;
      }
   }

   public static Attr asDOMAttr(Node attribute) {
      if (attribute == null) {
         return null;
      } else if (attribute instanceof Attr) {
         return (Attr)attribute;
      } else {
         notSupported();
         return null;
      }
   }

   public static void notSupported() {
      throw new DOMException((short)9, "Not supported yet");
   }

   public static class EmptyNodeList implements NodeList {
      public org.w3c.dom.Node item(int index) {
         return null;
      }

      public int getLength() {
         return 0;
      }
   }
}
