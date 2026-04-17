package org.hibernate.engine.jdbc.batch.internal;

import java.util.Map;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.BatchBuilder;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.spi.Configurable;
import org.jboss.logging.Logger;

public class BatchBuilderImpl implements BatchBuilder, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BatchBuilderImpl.class.getName());
   private int size;

   public BatchBuilderImpl() {
   }

   public void configure(Map configurationValues) {
      this.size = ConfigurationHelper.getInt("hibernate.jdbc.batch_size", configurationValues, this.size);
   }

   public BatchBuilderImpl(int size) {
      this.size = size;
   }

   public void setJdbcBatchSize(int size) {
      this.size = size;
   }

   public Batch buildBatch(BatchKey key, JdbcCoordinator jdbcCoordinator) {
      LOG.tracef("Building batch [size=%s]", this.size);
      return (Batch)(this.size > 1 ? new BatchingBatch(key, jdbcCoordinator, this.size) : new NonBatchingBatch(key, jdbcCoordinator));
   }

   public String getManagementDomain() {
      return null;
   }

   public String getManagementServiceType() {
      return null;
   }

   public Object getManagementBean() {
      return this;
   }
}
