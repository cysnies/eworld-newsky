package org.hibernate.metamodel.source;

import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.metamodel.Metadata;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.FetchProfile;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.metamodel.binding.TypeDef;
import org.hibernate.metamodel.relational.Database;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.TypeResolver;

public interface MetadataImplementor extends Metadata, BindingContext, Mapping {
   ServiceRegistry getServiceRegistry();

   Database getDatabase();

   TypeResolver getTypeResolver();

   void addImport(String var1, String var2);

   void addEntity(EntityBinding var1);

   void addCollection(PluralAttributeBinding var1);

   void addFetchProfile(FetchProfile var1);

   void addTypeDefinition(TypeDef var1);

   void addFilterDefinition(FilterDefinition var1);

   void addIdGenerator(IdGenerator var1);

   void registerIdentifierGenerator(String var1, String var2);

   void addNamedNativeQuery(NamedSQLQueryDefinition var1);

   void addNamedQuery(NamedQueryDefinition var1);

   void addResultSetMapping(ResultSetMappingDefinition var1);

   void setGloballyQuotedIdentifiers(boolean var1);

   MetaAttributeContext getGlobalMetaAttributeContext();
}
