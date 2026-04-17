package org.hibernate.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public class CompositeNestedGeneratedValueGenerator implements IdentifierGenerator, Serializable, IdentifierGeneratorAggregator {
   private final GenerationContextLocator generationContextLocator;
   private List generationPlans = new ArrayList();

   public CompositeNestedGeneratedValueGenerator(GenerationContextLocator generationContextLocator) {
      this.generationContextLocator = generationContextLocator;
   }

   public void addGeneratedValuePlan(GenerationPlan plan) {
      this.generationPlans.add(plan);
   }

   public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
      Serializable context = this.generationContextLocator.locateGenerationContext(session, object);

      for(GenerationPlan plan : this.generationPlans) {
         plan.execute(session, object, context);
      }

      return context;
   }

   public void registerPersistentGenerators(Map generatorMap) {
      for(GenerationPlan plan : this.generationPlans) {
         plan.registerPersistentGenerators(generatorMap);
      }

   }

   public interface GenerationContextLocator {
      Serializable locateGenerationContext(SessionImplementor var1, Object var2);
   }

   public interface GenerationPlan {
      void execute(SessionImplementor var1, Object var2, Object var3);

      void registerPersistentGenerators(Map var1);
   }
}
