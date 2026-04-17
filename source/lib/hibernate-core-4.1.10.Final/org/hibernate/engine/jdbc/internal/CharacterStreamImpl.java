package org.hibernate.engine.jdbc.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.type.descriptor.java.DataHelper;

public class CharacterStreamImpl implements CharacterStream {
   private final long length;
   private Reader reader;
   private String string;

   public CharacterStreamImpl(String chars) {
      this.string = chars;
      this.length = (long)chars.length();
   }

   public CharacterStreamImpl(Reader reader, long length) {
      this.reader = reader;
      this.length = length;
   }

   public Reader asReader() {
      if (this.reader == null) {
         this.reader = new StringReader(this.string);
      }

      return this.reader;
   }

   public String asString() {
      if (this.string == null) {
         this.string = DataHelper.extractString(this.reader);
      }

      return this.string;
   }

   public long getLength() {
      return this.length;
   }

   public void release() {
      if (this.reader != null) {
         try {
            this.reader.close();
         } catch (IOException var2) {
         }

      }
   }
}
