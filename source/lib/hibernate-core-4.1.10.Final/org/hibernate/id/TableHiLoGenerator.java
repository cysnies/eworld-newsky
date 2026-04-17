package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;

/** @deprecated */
@Deprecated
public class TableHiLoGenerator extends TableGenerator {
   public static final String MAX_LO = "max_lo";
   private OptimizerFactory.LegacyHiLoAlgorithmOptimizer hiloOptimizer;
   private int maxLo;

   public void configure(Type type, Properties params, Dialect d) {
      super.configure(type, params, d);
      this.maxLo = ConfigurationHelper.getInt("max_lo", params, 32767);
      if (this.maxLo >= 1) {
         this.hiloOptimizer = new OptimizerFactory.LegacyHiLoAlgorithmOptimizer(type.getReturnedClass(), this.maxLo);
      }

   }

   public synchronized Serializable generate(final SessionImplementor session, Object obj) {
      if (this.maxLo >= 1) {
         return this.hiloOptimizer.generate(new AccessCallback() {
            public IntegralDataTypeHolder getNextValue() {
               return TableHiLoGenerator.this.generateHolder(session);
            }
         });
      } else {
         IntegralDataTypeHolder value;
         for(value = null; value == null || value.lt(0L); value = this.generateHolder(session)) {
         }

         return value.makeValue();
      }
   }
}
