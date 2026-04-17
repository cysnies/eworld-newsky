package org.hibernate.dialect.function;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public abstract class AbstractAnsiTrimEmulationFunction implements SQLFunction {
   public final boolean hasArguments() {
      return true;
   }

   public final boolean hasParenthesesIfNoArguments() {
      return false;
   }

   public final Type getReturnType(Type argumentType, Mapping mapping) throws QueryException {
      return StandardBasicTypes.STRING;
   }

   public final String render(Type argumentType, List args, SessionFactoryImplementor factory) throws QueryException {
      if (args.size() == 1) {
         return this.resolveBothSpaceTrimFunction().render(argumentType, args, factory);
      } else if ("from".equalsIgnoreCase((String)args.get(0))) {
         return this.resolveBothSpaceTrimFromFunction().render(argumentType, args, factory);
      } else {
         boolean leading = true;
         boolean trailing = true;
         int potentialTrimCharacterArgIndex = 1;
         String firstArg = (String)args.get(0);
         if ("leading".equalsIgnoreCase(firstArg)) {
            trailing = false;
         } else if ("trailing".equalsIgnoreCase(firstArg)) {
            leading = false;
         } else if (!"both".equalsIgnoreCase(firstArg)) {
            potentialTrimCharacterArgIndex = 0;
         }

         String potentialTrimCharacter = (String)args.get(potentialTrimCharacterArgIndex);
         String trimCharacter;
         String trimSource;
         if ("from".equalsIgnoreCase(potentialTrimCharacter)) {
            trimCharacter = "' '";
            trimSource = (String)args.get(potentialTrimCharacterArgIndex + 1);
         } else if (potentialTrimCharacterArgIndex + 1 >= args.size()) {
            trimCharacter = "' '";
            trimSource = potentialTrimCharacter;
         } else {
            trimCharacter = potentialTrimCharacter;
            if ("from".equalsIgnoreCase((String)args.get(potentialTrimCharacterArgIndex + 1))) {
               trimSource = (String)args.get(potentialTrimCharacterArgIndex + 2);
            } else {
               trimSource = (String)args.get(potentialTrimCharacterArgIndex + 1);
            }
         }

         List<String> argsToUse = new ArrayList();
         argsToUse.add(trimSource);
         argsToUse.add(trimCharacter);
         if (trimCharacter.equals("' '")) {
            if (leading && trailing) {
               return this.resolveBothSpaceTrimFunction().render(argumentType, argsToUse, factory);
            } else {
               return leading ? this.resolveLeadingSpaceTrimFunction().render(argumentType, argsToUse, factory) : this.resolveTrailingSpaceTrimFunction().render(argumentType, argsToUse, factory);
            }
         } else if (leading && trailing) {
            return this.resolveBothTrimFunction().render(argumentType, argsToUse, factory);
         } else {
            return leading ? this.resolveLeadingTrimFunction().render(argumentType, argsToUse, factory) : this.resolveTrailingTrimFunction().render(argumentType, argsToUse, factory);
         }
      }
   }

   protected abstract SQLFunction resolveBothSpaceTrimFunction();

   protected abstract SQLFunction resolveBothSpaceTrimFromFunction();

   protected abstract SQLFunction resolveLeadingSpaceTrimFunction();

   protected abstract SQLFunction resolveTrailingSpaceTrimFunction();

   protected abstract SQLFunction resolveBothTrimFunction();

   protected abstract SQLFunction resolveLeadingTrimFunction();

   protected abstract SQLFunction resolveTrailingTrimFunction();
}
