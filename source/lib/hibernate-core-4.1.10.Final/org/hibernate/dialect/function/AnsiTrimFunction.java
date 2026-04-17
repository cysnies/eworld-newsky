package org.hibernate.dialect.function;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public class AnsiTrimFunction extends TrimFunctionTemplate {
   protected String render(TrimFunctionTemplate.Options options, String trimSource, SessionFactoryImplementor factory) {
      return "trim(" + options.getTrimSpecification().getName() + ' ' + options.getTrimCharacter() + " from " + trimSource + ')';
   }
}
