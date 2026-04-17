package org.hibernate.cfg;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import org.hibernate.AnnotationException;
import org.hibernate.DuplicateMappingException;
import org.hibernate.MappingException;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.hibernate.type.TypeResolver;

public interface Mappings {
   TypeResolver getTypeResolver();

   NamingStrategy getNamingStrategy();

   void setNamingStrategy(NamingStrategy var1);

   String getSchemaName();

   void setSchemaName(String var1);

   String getCatalogName();

   void setCatalogName(String var1);

   String getDefaultPackage();

   void setDefaultPackage(String var1);

   boolean isAutoImport();

   void setAutoImport(boolean var1);

   boolean isDefaultLazy();

   void setDefaultLazy(boolean var1);

   String getDefaultCascade();

   void setDefaultCascade(String var1);

   String getDefaultAccess();

   void setDefaultAccess(String var1);

   Iterator iterateClasses();

   PersistentClass getClass(String var1);

   PersistentClass locatePersistentClassByEntityName(String var1);

   void addClass(PersistentClass var1) throws DuplicateMappingException;

   void addImport(String var1, String var2) throws DuplicateMappingException;

   Collection getCollection(String var1);

   Iterator iterateCollections();

   void addCollection(Collection var1) throws DuplicateMappingException;

   Table getTable(String var1, String var2, String var3);

   Iterator iterateTables();

   Table addTable(String var1, String var2, String var3, String var4, boolean var5);

   Table addDenormalizedTable(String var1, String var2, String var3, boolean var4, String var5, Table var6) throws DuplicateMappingException;

   NamedQueryDefinition getQuery(String var1);

   void addQuery(String var1, NamedQueryDefinition var2) throws DuplicateMappingException;

   NamedSQLQueryDefinition getSQLQuery(String var1);

   void addSQLQuery(String var1, NamedSQLQueryDefinition var2) throws DuplicateMappingException;

   ResultSetMappingDefinition getResultSetMapping(String var1);

   void addResultSetMapping(ResultSetMappingDefinition var1) throws DuplicateMappingException;

   TypeDef getTypeDef(String var1);

   void addTypeDef(String var1, String var2, Properties var3);

   Map getFilterDefinitions();

   FilterDefinition getFilterDefinition(String var1);

   void addFilterDefinition(FilterDefinition var1);

   FetchProfile findOrCreateFetchProfile(String var1, MetadataSource var2);

   /** @deprecated */
   @Deprecated
   Iterator iterateAuxliaryDatabaseObjects();

   Iterator iterateAuxiliaryDatabaseObjects();

   /** @deprecated */
   @Deprecated
   ListIterator iterateAuxliaryDatabaseObjectsInReverse();

   ListIterator iterateAuxiliaryDatabaseObjectsInReverse();

   void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject var1);

   String getLogicalTableName(Table var1) throws MappingException;

   void addTableBinding(String var1, String var2, String var3, String var4, Table var5) throws DuplicateMappingException;

   void addColumnBinding(String var1, Column var2, Table var3) throws DuplicateMappingException;

   String getPhysicalColumnName(String var1, Table var2) throws MappingException;

   String getLogicalColumnName(String var1, Table var2) throws MappingException;

   void addSecondPass(SecondPass var1);

   void addSecondPass(SecondPass var1, boolean var2);

   void addPropertyReference(String var1, String var2);

   void addUniquePropertyReference(String var1, String var2);

   void addToExtendsQueue(ExtendsQueueEntry var1);

   MutableIdentifierGeneratorFactory getIdentifierGeneratorFactory();

   void addMappedSuperclass(Class var1, MappedSuperclass var2);

   MappedSuperclass getMappedSuperclass(Class var1);

   ObjectNameNormalizer getObjectNameNormalizer();

   Properties getConfigurationProperties();

   void addDefaultGenerator(IdGenerator var1);

   IdGenerator getGenerator(String var1);

   IdGenerator getGenerator(String var1, Map var2);

   void addGenerator(IdGenerator var1);

   void addGeneratorTable(String var1, Properties var2);

   Properties getGeneratorTableProperties(String var1, Map var2);

   Map getJoins(String var1);

   void addJoins(PersistentClass var1, Map var2);

   AnnotatedClassType getClassType(XClass var1);

   AnnotatedClassType addClassType(XClass var1);

   /** @deprecated */
   @Deprecated
   Map getTableUniqueConstraints();

   Map getUniqueConstraintHoldersByTable();

   /** @deprecated */
   @Deprecated
   void addUniqueConstraints(Table var1, List var2);

   void addUniqueConstraintHolders(Table var1, List var2);

   void addMappedBy(String var1, String var2, String var3);

   String getFromMappedBy(String var1, String var2);

   void addPropertyReferencedAssociation(String var1, String var2, String var3);

   String getPropertyReferencedAssociation(String var1, String var2);

   ReflectionManager getReflectionManager();

   void addDefaultQuery(String var1, NamedQueryDefinition var2);

   void addDefaultSQLQuery(String var1, NamedSQLQueryDefinition var2);

   void addDefaultResultSetMapping(ResultSetMappingDefinition var1);

   Map getClasses();

   void addAnyMetaDef(AnyMetaDef var1) throws AnnotationException;

   AnyMetaDef getAnyMetaDef(String var1);

   boolean isInSecondPass();

   PropertyData getPropertyAnnotatedWithMapsId(XClass var1, String var2);

   void addPropertyAnnotatedWithMapsId(XClass var1, PropertyData var2);

   void addPropertyAnnotatedWithMapsIdSpecj(XClass var1, PropertyData var2, String var3);

   boolean isSpecjProprietarySyntaxEnabled();

   boolean useNewGeneratorMappings();

   boolean useNationalizedCharacterData();

   PropertyData getPropertyAnnotatedWithIdAndToOne(XClass var1, String var2);

   void addToOneAndIdProperty(XClass var1, PropertyData var2);

   boolean forceDiscriminatorInSelectsByDefault();

   public static final class PropertyReference implements Serializable {
      public final String referencedClass;
      public final String propertyName;
      public final boolean unique;

      public PropertyReference(String referencedClass, String propertyName, boolean unique) {
         this.referencedClass = referencedClass;
         this.propertyName = propertyName;
         this.unique = unique;
      }
   }
}
