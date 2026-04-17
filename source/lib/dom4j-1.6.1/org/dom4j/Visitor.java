package org.dom4j;

public interface Visitor {
   void visit(Document var1);

   void visit(DocumentType var1);

   void visit(Element var1);

   void visit(Attribute var1);

   void visit(CDATA var1);

   void visit(Comment var1);

   void visit(Entity var1);

   void visit(Namespace var1);

   void visit(ProcessingInstruction var1);

   void visit(Text var1);
}
