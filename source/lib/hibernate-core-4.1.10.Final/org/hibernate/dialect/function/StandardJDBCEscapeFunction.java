package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class StandardJDBCEscapeFunction extends StandardSQLFunction {
   public StandardJDBCEscapeFunction(String name) {
      super(name);
   }

   public StandardJDBCEscapeFunction(String name, Type typeValue) {
      super(name, typeValue);
   }

   public String render(Type argumentType, List args, SessionFactoryImplementor factory) {
      return "{fn " + super.render(argumentType, args, factory) + "}";
   }

   public String toString() {
      return "{fn " + this.getName() + "...}";
   }
}
