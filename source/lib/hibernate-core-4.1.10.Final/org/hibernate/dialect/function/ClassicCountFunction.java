package org.hibernate.dialect.function;

import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class ClassicCountFunction extends StandardSQLFunction {
   public ClassicCountFunction() {
      super("count");
   }

   public Type getReturnType(Type columnType, Mapping mapping) {
      return StandardBasicTypes.INTEGER;
   }
}
