package org.hibernate.tool.instrument.javassist;

import org.hibernate.bytecode.buildtime.internal.JavassistInstrumenter;
import org.hibernate.bytecode.buildtime.spi.Instrumenter;
import org.hibernate.bytecode.buildtime.spi.Logger;
import org.hibernate.tool.instrument.BasicInstrumentationTask;

public class InstrumentTask extends BasicInstrumentationTask {
   protected Instrumenter buildInstrumenter(Logger logger, Instrumenter.Options options) {
      return new JavassistInstrumenter(logger, options);
   }
}
