package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;

public class SequenceHiLoGenerator extends SequenceGenerator {
   public static final String MAX_LO = "max_lo";
   private int maxLo;
   private OptimizerFactory.LegacyHiLoAlgorithmOptimizer hiloOptimizer;

   public void configure(Type type, Properties params, Dialect d) throws MappingException {
      super.configure(type, params, d);
      this.maxLo = ConfigurationHelper.getInt("max_lo", params, 9);
      if (this.maxLo >= 1) {
         this.hiloOptimizer = new OptimizerFactory.LegacyHiLoAlgorithmOptimizer(this.getIdentifierType().getReturnedClass(), this.maxLo);
      }

   }

   public synchronized Serializable generate(final SessionImplementor session, Object obj) {
      if (this.maxLo >= 1) {
         return this.hiloOptimizer.generate(new AccessCallback() {
            public IntegralDataTypeHolder getNextValue() {
               return SequenceHiLoGenerator.this.generateHolder(session);
            }
         });
      } else {
         IntegralDataTypeHolder value;
         for(value = null; value == null || value.lt(0L); value = super.generateHolder(session)) {
         }

         return value.makeValue();
      }
   }

   OptimizerFactory.LegacyHiLoAlgorithmOptimizer getHiloOptimizer() {
      return this.hiloOptimizer;
   }
}
