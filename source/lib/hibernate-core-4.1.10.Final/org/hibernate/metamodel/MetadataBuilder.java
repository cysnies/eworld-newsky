package org.hibernate.metamodel;

import javax.persistence.SharedCacheMode;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.NamingStrategy;

public interface MetadataBuilder {
   MetadataBuilder with(NamingStrategy var1);

   MetadataBuilder with(MetadataSourceProcessingOrder var1);

   MetadataBuilder with(SharedCacheMode var1);

   MetadataBuilder with(AccessType var1);

   MetadataBuilder withNewIdentifierGeneratorsEnabled(boolean var1);

   Metadata buildMetadata();
}
