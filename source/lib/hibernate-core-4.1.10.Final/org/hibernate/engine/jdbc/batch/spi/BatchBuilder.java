package org.hibernate.engine.jdbc.batch.spi;

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.service.Service;
import org.hibernate.service.spi.Manageable;

public interface BatchBuilder extends Service, Manageable {
   Batch buildBatch(BatchKey var1, JdbcCoordinator var2);
}
