package org.hibernate.dialect.function;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class TemplateRenderer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TemplateRenderer.class.getName());
   private final String template;
   private final String[] chunks;
   private final int[] paramIndexes;

   public TemplateRenderer(String template) {
      this.template = template;
      List<String> chunkList = new ArrayList();
      List<Integer> paramList = new ArrayList();
      StringBuilder chunk = new StringBuilder(10);
      StringBuilder index = new StringBuilder(2);

      for(int i = 0; i < template.length(); ++i) {
         char c = template.charAt(i);
         if (c != '?') {
            chunk.append(c);
         } else {
            chunkList.add(chunk.toString());
            chunk.delete(0, chunk.length());

            while(true) {
               ++i;
               if (i >= template.length()) {
                  break;
               }

               c = template.charAt(i);
               if (!Character.isDigit(c)) {
                  chunk.append(c);
                  break;
               }

               index.append(c);
            }

            paramList.add(Integer.valueOf(index.toString()));
            index.delete(0, index.length());
         }
      }

      if (chunk.length() > 0) {
         chunkList.add(chunk.toString());
      }

      this.chunks = (String[])chunkList.toArray(new String[chunkList.size()]);
      this.paramIndexes = new int[paramList.size()];

      for(int i = 0; i < this.paramIndexes.length; ++i) {
         this.paramIndexes[i] = (Integer)paramList.get(i);
      }

   }

   public String getTemplate() {
      return this.template;
   }

   public int getAnticipatedNumberOfArguments() {
      return this.paramIndexes.length;
   }

   public String render(List args, SessionFactoryImplementor factory) {
      int numberOfArguments = args.size();
      if (this.getAnticipatedNumberOfArguments() > 0 && numberOfArguments != this.getAnticipatedNumberOfArguments()) {
         LOG.missingArguments(this.getAnticipatedNumberOfArguments(), numberOfArguments);
      }

      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < this.chunks.length; ++i) {
         if (i < this.paramIndexes.length) {
            int index = this.paramIndexes[i] - 1;
            Object arg = index < numberOfArguments ? args.get(index) : null;
            if (arg != null) {
               buf.append(this.chunks[i]).append(arg);
            }
         } else {
            buf.append(this.chunks[i]);
         }
      }

      return buf.toString();
   }
}
