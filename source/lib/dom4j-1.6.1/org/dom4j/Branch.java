package org.dom4j;

import java.util.Iterator;
import java.util.List;

public interface Branch extends Node {
   Node node(int var1) throws IndexOutOfBoundsException;

   int indexOf(Node var1);

   int nodeCount();

   Element elementByID(String var1);

   List content();

   Iterator nodeIterator();

   void setContent(List var1);

   void appendContent(Branch var1);

   void clearContent();

   List processingInstructions();

   List processingInstructions(String var1);

   ProcessingInstruction processingInstruction(String var1);

   void setProcessingInstructions(List var1);

   Element addElement(String var1);

   Element addElement(QName var1);

   Element addElement(String var1, String var2);

   boolean removeProcessingInstruction(String var1);

   void add(Node var1);

   void add(Comment var1);

   void add(Element var1);

   void add(ProcessingInstruction var1);

   boolean remove(Node var1);

   boolean remove(Comment var1);

   boolean remove(Element var1);

   boolean remove(ProcessingInstruction var1);

   void normalize();
}
