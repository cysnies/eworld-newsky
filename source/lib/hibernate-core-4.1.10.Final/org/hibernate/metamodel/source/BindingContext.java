package org.hibernate.metamodel.source;

import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.domain.Type;
import org.hibernate.service.ServiceRegistry;

public interface BindingContext {
   ServiceRegistry getServiceRegistry();

   NamingStrategy getNamingStrategy();

   MappingDefaults getMappingDefaults();

   MetadataImplementor getMetadataImplementor();

   Class locateClassByName(String var1);

   Type makeJavaType(String var1);

   boolean isGloballyQuotedIdentifiers();

   ValueHolder makeClassReference(String var1);

   String qualifyClassName(String var1);
}
