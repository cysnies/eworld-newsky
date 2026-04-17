package org.hibernate.id.uuid;

import java.util.UUID;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerationStrategy;

public class StandardRandomStrategy implements UUIDGenerationStrategy {
   public static final StandardRandomStrategy INSTANCE = new StandardRandomStrategy();

   public int getGeneratedVersion() {
      return 4;
   }

   public UUID generateUUID(SessionImplementor session) {
      return UUID.randomUUID();
   }
}
