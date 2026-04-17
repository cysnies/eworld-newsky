package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public abstract class TrimFunctionTemplate implements SQLFunction {
   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return false;
   }

   public Type getReturnType(Type firstArgument, Mapping mapping) throws QueryException {
      return StandardBasicTypes.STRING;
   }

   public String render(Type firstArgument, List args, SessionFactoryImplementor factory) throws QueryException {
      Options options = new Options();
      String trimSource;
      if (args.size() == 1) {
         trimSource = (String)args.get(0);
      } else if ("from".equalsIgnoreCase((String)args.get(0))) {
         trimSource = (String)args.get(1);
      } else {
         int potentialTrimCharacterArgIndex = 1;
         String firstArg = (String)args.get(0);
         if ("leading".equalsIgnoreCase(firstArg)) {
            options.setTrimSpecification(TrimFunctionTemplate.Specification.LEADING);
         } else if ("trailing".equalsIgnoreCase(firstArg)) {
            options.setTrimSpecification(TrimFunctionTemplate.Specification.TRAILING);
         } else if (!"both".equalsIgnoreCase(firstArg)) {
            potentialTrimCharacterArgIndex = 0;
         }

         String potentialTrimCharacter = (String)args.get(potentialTrimCharacterArgIndex);
         if ("from".equalsIgnoreCase(potentialTrimCharacter)) {
            trimSource = (String)args.get(potentialTrimCharacterArgIndex + 1);
         } else if (potentialTrimCharacterArgIndex + 1 >= args.size()) {
            trimSource = potentialTrimCharacter;
         } else {
            options.setTrimCharacter(potentialTrimCharacter);
            if ("from".equalsIgnoreCase((String)args.get(potentialTrimCharacterArgIndex + 1))) {
               trimSource = (String)args.get(potentialTrimCharacterArgIndex + 2);
            } else {
               trimSource = (String)args.get(potentialTrimCharacterArgIndex + 1);
            }
         }
      }

      return this.render(options, trimSource, factory);
   }

   protected abstract String render(Options var1, String var2, SessionFactoryImplementor var3);

   public static class Options {
      public static final String DEFAULT_TRIM_CHARACTER = "' '";
      private String trimCharacter = "' '";
      private Specification trimSpecification;

      public Options() {
         this.trimSpecification = TrimFunctionTemplate.Specification.BOTH;
      }

      public String getTrimCharacter() {
         return this.trimCharacter;
      }

      public void setTrimCharacter(String trimCharacter) {
         this.trimCharacter = trimCharacter;
      }

      public Specification getTrimSpecification() {
         return this.trimSpecification;
      }

      public void setTrimSpecification(Specification trimSpecification) {
         this.trimSpecification = trimSpecification;
      }
   }

   public static class Specification {
      public static final Specification LEADING = new Specification("leading");
      public static final Specification TRAILING = new Specification("trailing");
      public static final Specification BOTH = new Specification("both");
      private final String name;

      private Specification(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }
   }
}
