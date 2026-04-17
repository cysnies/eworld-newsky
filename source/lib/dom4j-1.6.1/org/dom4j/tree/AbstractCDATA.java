package org.dom4j.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.dom4j.CDATA;
import org.dom4j.Visitor;

public abstract class AbstractCDATA extends AbstractCharacterData implements CDATA {
   public short getNodeType() {
      return 4;
   }

   public String toString() {
      return super.toString() + " [CDATA: \"" + this.getText() + "\"]";
   }

   public String asXML() {
      StringWriter writer = new StringWriter();

      try {
         this.write(writer);
      } catch (IOException var3) {
      }

      return writer.toString();
   }

   public void write(Writer writer) throws IOException {
      writer.write("<![CDATA[");
      if (this.getText() != null) {
         writer.write(this.getText());
      }

      writer.write("]]>");
   }

   public void accept(Visitor visitor) {
      visitor.visit((CDATA)this);
   }
}
