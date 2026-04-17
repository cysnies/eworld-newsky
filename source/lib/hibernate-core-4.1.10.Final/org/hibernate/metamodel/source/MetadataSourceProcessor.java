package org.hibernate.metamodel.source;

import java.util.List;
import org.hibernate.metamodel.MetadataSources;

public interface MetadataSourceProcessor {
   void prepare(MetadataSources var1);

   void processIndependentMetadata(MetadataSources var1);

   void processTypeDependentMetadata(MetadataSources var1);

   void processMappingMetadata(MetadataSources var1, List var2);

   void processMappingDependentMetadata(MetadataSources var1);
}
