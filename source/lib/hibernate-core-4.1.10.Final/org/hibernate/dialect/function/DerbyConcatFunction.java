package org.hibernate.dialect.function;

import java.util.Iterator;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class DerbyConcatFunction implements SQLFunction {
   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return true;
   }

   public Type getReturnType(Type argumentType, Mapping mapping) throws QueryException {
      return StandardBasicTypes.STRING;
   }

   public String render(Type argumentType, List args, SessionFactoryImplementor factory) throws QueryException {
      boolean areAllArgsParams = true;

      for(String arg : args) {
         if (!"?".equals(arg)) {
            areAllArgsParams = false;
            break;
         }
      }

      return areAllArgsParams ? join(args.iterator(), new StringTransformer() {
         public String transform(String string) {
            return "cast( ? as varchar(32672) )";
         }
      }, new StringJoinTemplate() {
         public String getBeginning() {
            return "varchar( ";
         }

         public String getSeparator() {
            return " || ";
         }

         public String getEnding() {
            return " )";
         }
      }) : join(args.iterator(), new StringTransformer() {
         public String transform(String string) {
            return string;
         }
      }, new StringJoinTemplate() {
         public String getBeginning() {
            return "(";
         }

         public String getSeparator() {
            return "||";
         }

         public String getEnding() {
            return ")";
         }
      });
   }

   private static String join(Iterator elements, StringTransformer elementTransformer, StringJoinTemplate template) {
      StringBuilder buffer = new StringBuilder(template.getBeginning());

      while(elements.hasNext()) {
         String element = (String)elements.next();
         buffer.append(elementTransformer.transform(element));
         if (elements.hasNext()) {
            buffer.append(template.getSeparator());
         }
      }

      return buffer.append(template.getEnding()).toString();
   }

   private interface StringJoinTemplate {
      String getBeginning();

      String getSeparator();

      String getEnding();
   }

   private interface StringTransformer {
      String transform(String var1);
   }
}
