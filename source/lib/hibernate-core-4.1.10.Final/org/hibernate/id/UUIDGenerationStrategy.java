package org.hibernate.id;

import java.io.Serializable;
import java.util.UUID;
import org.hibernate.engine.spi.SessionImplementor;

public interface UUIDGenerationStrategy extends Serializable {
   int getGeneratedVersion();

   UUID generateUUID(SessionImplementor var1);
}
