package org.hibernate.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.cache.CacheException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.loading.internal.CollectionLoadContext;
import org.hibernate.engine.loading.internal.EntityLoadContext;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.service.jdbc.dialect.internal.AbstractDialectResolver;
import org.hibernate.service.jndi.JndiException;
import org.hibernate.service.jndi.JndiNameException;
import org.hibernate.type.BasicType;
import org.hibernate.type.SerializationException;
import org.hibernate.type.Type;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class CoreMessageLogger_$logger implements Serializable, CoreMessageLogger, BasicLogger {
   private static final long serialVersionUID = 1L;
   private static final String projectCode = "HHH";
   private static final String FQCN = CoreMessageLogger_$logger.class.getName();
   protected final Logger log;
   private static final String unableToCloseOutputFile = "Error closing output file: %s";
   private static final String unableToCreateSchema = "Error creating schema ";
   private static final String logicalConnectionReleasingPhysicalConnection = "Logical connection releasing its physical connection";
   private static final String unableToExecuteResolver = "Error executing resolver [%s] : %s";
   private static final String logicalConnectionClosed = "*** Logical connection closed ***";
   private static final String unableToWriteCachedFile = "I/O reported error writing cached file : %s: %s";
   private static final String disablingContextualLOBCreationSinceCreateClobFailed = "Disabling contextual LOB creation as createClob() method threw error : %s";
   private static final String unregisteredStatement = "ResultSet's statement was not registered";
   private static final String unableToGetDatabaseMetadata = "Could not get database metadata";
   private static final String jaccContextId = "JACC contextID: %s";
   private static final String invalidArrayElementType = "Array element type error\n%s";
   private static final String splitQueries = "Manipulation query [%s] resulted in [%s] split queries";
   private static final String unableToReleaseCreatedMBeanServer = "Unable to release created MBeanServer : %s";
   private static final String unableToUpdateHiValue = "Could not update hi value in: %s";
   private static final String unableToAccessSessionFactory = "Error while accessing session factory with JNDI name %s";
   private static final String searchingForMappingDocuments = "Searching for mapping documents in jar: %s";
   private static final String transactions = "Transactions: %s";
   private static final String failSafeCollectionsCleanup = "Fail-safe cleanup (collections) : %s";
   private static final String statementsPrepared = "Statements prepared: %s";
   private static final String usingDriver = "using driver [%s] at URL [%s]";
   private static final String deprecatedOracle9Dialect = "The Oracle9Dialect dialect has been deprecated; use either Oracle9iDialect or Oracle10gDialect instead";
   private static final String synchronizationFailed = "Exception calling user Synchronization [%s] : %s";
   private static final String unableToLoadCommand = "Error performing load command : %s";
   private static final String disablingContextualLOBCreationSinceConnectionNull = "Disabling contextual LOB creation as connection was null";
   private static final String schemaUpdateComplete = "Schema update complete";
   private static final String usingStreams = "Using java.io streams to persist binary types";
   private static final String exceptionInBeforeTransactionCompletionInterceptor = "Exception in interceptor beforeTransactionCompletion()";
   private static final String alreadySessionBound = "Already session bound on call to bind(); make sure you clean up your sessions!";
   private static final String unableToAccessTypeInfoResultSet = "Error accessing type info result set : %s";
   private static final String disallowingInsertStatementComment = "Disallowing insert statement comment for select-identity due to Oracle driver bug";
   private static final String loggingStatistics = "Logging statistics....";
   private static final String sessionsClosed = "Sessions closed: %s";
   private static final String unableToLogWarnings = "Could not log warnings";
   private static final String noPersistentClassesFound = "no persistent classes found for query class: %s";
   private static final String writingGeneratedSchemaToFile = "Writing generated schema to file: %s";
   private static final String unsuccessful = "Unsuccessful: %s";
   private static final String unableToAccessEjb3Configuration = "Naming exception occurred accessing Ejb3Configuration";
   private static final String indexes = "Indexes: %s";
   private static final String collectionsLoaded = "Collections loaded: %s";
   private static final String duplicateMetadata = "Found more than one <persistence-unit-metadata>, subsequent ignored";
   private static final String unableToApplyConstraints = "Unable to apply constraints on DDL for %s";
   private static final String orderByAnnotationIndexedCollection = "@OrderBy not allowed for an indexed collection, annotation ignored.";
   private static final String instantiatingExplicitConnectionProvider = "Instantiating explicit connection provider: %s";
   private static final String collectionsFetched = "Collections fetched (minimize this): %s";
   private static final String schemaExportComplete = "Schema export complete";
   private static final String subResolverException = "sub-resolver threw unexpected exception, continuing to next : %s";
   private static final String unableToDeserializeCache = "Could not deserialize cache file: %s : %s";
   private static final String queriesExecuted = "Queries executed to database: %s";
   private static final String unableToUpdateQueryHiValue = "Could not updateQuery hi value in: %s";
   private static final String unableToObtainConnectionMetadata = "Could not obtain connection metadata : %s";
   private static final String jndiNameDoesNotHandleSessionFactoryReference = "JNDI name %s does not handle a session factory reference";
   private static final String startingQueryCache = "Starting query cache at region: %s";
   private static final String entityManagerClosedBySomeoneElse = "Entity Manager closed by someone else (%s must not be used)";
   private static final String factoryBoundToJndiName = "Bound factory to JNDI name: %s";
   private static final String timestampCacheMisses = "update timestamps cache misses: %s";
   private static final String unableToPerformJdbcCommit = "JDBC commit failed";
   private static final String unexpectedLiteralTokenType = "Unexpected literal token type [%s] passed for numeric processing";
   private static final String queryCacheMisses = "Query cache misses: %s";
   private static final String javassistEnhancementFailed = "Javassist Enhancement failed: %s";
   private static final String flushes = "Flushes: %s";
   private static final String naturalIdQueriesExecuted = "NaturalId queries executed to database: %s";
   private static final String unregisteredResultSetWithoutStatement = "ResultSet had no statement associated with it, but was not yet registered";
   private static final String parsingXmlWarningForFile = "Warning parsing XML: %s(%s) %s";
   private static final String optimisticLockFailures = "Optimistic lock failures: %s";
   private static final String duplicateGeneratorTable = "Duplicate generator table: %s";
   private static final String forcingTableUse = "Forcing table use for sequence-style generator due to pooled optimizer selection where db does not support pooled sequences";
   private static final String duplicateListener = "entity-listener duplication, first event definition will be used: %s";
   private static final String foreignKeys = "Foreign keys: %s";
   private static final String startingServiceAtJndiName = "Starting service at JNDI name: %s";
   private static final String unableToReleaseBatchStatement = "Unable to release batch statement...";
   private static final String serviceProperties = "Service properties: %s";
   private static final String naturalIdCacheMisses = "NaturalId cache misses: %s";
   private static final String unexpectedRowCounts = "JDBC driver did not return the expected number of row counts";
   private static final String entitiesUpdated = "Entities updated: %s";
   private static final String queryCachePuts = "Query cache puts: %s";
   private static final String sessionsOpened = "Sessions opened: %s";
   private static final String stoppingService = "Stopping service";
   private static final String unableToLocateCustomOptimizerClass = "Unable to interpret specified optimizer [%s], falling back to noop";
   private static final String unableToCloseSession = "Could not close session";
   private static final String entityManagerFactoryAlreadyRegistered = "Entity manager factory name (%s) is already registered.  If entity manager will be clustered or passivated, specify a unique value for property '%s'";
   private static final String collectionsUpdated = "Collections updated: %s";
   private static final String cacheProvider = "Cache provider: %s";
   private static final String illegalPropertySetterArgument = "IllegalArgumentException in class: %s, setter method of property: %s";
   private static final String namedQueryError = "Error in named query: %s";
   private static final String deprecatedForceDescriminatorAnnotation = "@ForceDiscriminator is deprecated use @DiscriminatorOptions instead.";
   private static final String timestampCachePuts = "update timestamps cache puts: %s";
   private static final String unableToReleaseCacheLock = "Could not release a cache lock : %s";
   private static final String invalidJndiName = "Invalid JNDI name: %s";
   private static final String jdbcDriverNotSpecified = "No JDBC Driver class was specified by property %s";
   private static final String providerClassDeprecated = "%s has been deprecated in favor of %s; that provider will be used instead.";
   private static final String unableToDetermineLockModeValue = "Unable to determine lock mode value : %s -> %s";
   private static final String unableToCloseStreamError = "Could not close stream on hibernate.properties: %s";
   private static final String unableToCleanUpCallableStatement = "Unable to clean up callable statement";
   private static final String collectionsRemoved = "Collections removed: %s";
   private static final String unableToRunSchemaUpdate = "Error running schema update";
   private static final String setManagerLookupClass = "You should set hibernate.transaction.manager_lookup_class if cache is enabled";
   private static final String unableToQueryDatabaseMetadata = "Unable to query java.sql.DatabaseMetaData";
   private static final String typeDefinedNoRegistrationKeys = "Type [%s] defined no registration keys; ignoring";
   private static final String noAppropriateConnectionProvider = "No appropriate connection provider encountered, assuming application will be supplying connections";
   private static final String unableToReadOrInitHiValue = "Could not read or init a hi value";
   private static final String compositeIdClassDoesNotOverrideEquals = "Composite-id class does not override equals(): %s";
   private static final String unableToCompleteSchemaValidation = "Could not complete schema validation";
   private static final String hsqldbSupportsOnlyReadCommittedIsolation = "HSQLDB supports only READ_UNCOMMITTED isolation";
   private static final String noDefaultConstructor = "No default (no-argument) constructor for class: %s (class must be instantiated by Interceptor)";
   private static final String addingOverrideFor = "Adding override for %s: %s";
   private static final String bytecodeProvider = "Bytecode provider name : %s";
   private static final String preparedStatementAlreadyInBatch = "PreparedStatement was already in the batch, [%s].";
   private static final String configuringFromFile = "Configuring from file: %s";
   private static final String schemaExportUnsuccessful = "Schema export unsuccessful";
   private static final String sqlExceptionEscapedProxy = "SQLException escaped proxy";
   private static final String transactionStrategy = "Transaction strategy: %s";
   private static final String runningSchemaValidator = "Running schema validator";
   private static final String readingMappingsFromFile = "Reading mappings from file: %s";
   private static final String jdbcRollbackFailed = "JDBC rollback failed";
   private static final String runningHbm2ddlSchemaExport = "Running hbm2ddl schema export";
   private static final String unableToLocateMBeanServer = "Unable to locate MBeanServer on JMX service shutdown";
   private static final String JavaSqlTypesMappedSameCodeMultipleTimes = "java.sql.Types mapped the same code [%s] multiple times; was [%s]; now [%s]";
   private static final String secondLevelCacheMisses = "Second level cache misses: %s";
   private static final String unableToObjectConnectionMetadata = "Could not obtain connection metadata: %s";
   private static final String unknownIngresVersion = "Unknown Ingres major version [%s]; using Ingres 9.2 dialect";
   private static final String unableToCloseConnection = "Error closing connection";
   private static final String loadingCollectionKeyNotFound = "In CollectionLoadContext#endLoadingCollections, localLoadingCollectionKeys contained [%s], but no LoadingCollectionEntry was found in loadContexts";
   private static final String batchContainedStatementsOnRelease = "On release of batch it still contained JDBC statements";
   private static final String jdbcIsolationLevel = "JDBC isolation level: %s";
   private static final String unableToPerformManagedFlush = "Error during managed flush [%s]";
   private static final String unableToCloseInputStreamForResource = "Could not close input stream for %s";
   private static final String usingReflectionOptimizer = "Using bytecode reflection optimizer";
   private static final String columns = "Columns: %s";
   private static final String duplicateImport = "Duplicate import: %s -> %s";
   private static final String recognizedObsoleteHibernateNamespace = "Recognized obsolete hibernate namespace %s. Use namespace %s instead. Refer to Hibernate 3.6 Migration Guide!";
   private static final String unableToMarkForRollbackOnPersistenceException = "Unable to mark for rollback on PersistenceException: ";
   private static final String unableToDestroyQueryCache = "Unable to destroy query cache: %s: %s";
   private static final String callingJoinTransactionOnNonJtaEntityManager = "Calling joinTransaction() on a non JTA EntityManager";
   private static final String unableToParseMetadata = "Could not parse the package-level metadata [%s]";
   private static final String unableToSynchronizeDatabaseStateWithSession = "Could not synchronize database state with session: %s";
   private static final String cannotResolveNonNullableTransientDependencies = "Attempting to save one or more entities that have a non-nullable association with an unsaved transient entity. The unsaved transient entity must be saved in an operation prior to saving these dependent entities.\n\tUnsaved transient entity: (%s)\n\tDependent entities: (%s)\n\tNon-nullable association(s): (%s)";
   private static final String unableToStopHibernateService = "Exception while stopping service";
   private static final String firstOrMaxResultsSpecifiedWithCollectionFetch = "firstResult/maxResults specified with collection fetch; applying in memory!";
   private static final String factoryUnboundFromJndiName = "Unbound factory from JNDI name: %s";
   private static final String cleaningUpConnectionPool = "Cleaning up connection pool [%s]";
   private static final String unableToBindValueToParameter = "Could not bind value '%s' to parameter: %s; %s";
   private static final String ignoringTableGeneratorConstraints = "Ignoring unique constraints specified on table generator [%s]";
   private static final String secondLevelCachePuts = "Second level cache puts: %s";
   private static final String naturalIdCacheHits = "NaturalId cache hits: %s";
   private static final String readOnlyCacheConfiguredForMutableCollection = "read-only cache configured for mutable collection [%s]";
   private static final String sqlWarning = "SQL Error: %s, SQLState: %s";
   private static final String usingFollowOnLocking = "Encountered request for locking however dialect reports that database prefers locking be done in a separate select (follow-on locking); results will be locked after initial query executes";
   private static final String unableToLocateConfigFile = "Unable to locate config file: %s";
   private static final String unableToObtainConnectionToQueryMetadata = "Could not obtain connection to query metadata : %s";
   private static final String entitiesLoaded = "Entities loaded: %s";
   private static final String unsupportedAfterStatement = "Overriding release mode as connection provider does not support 'after_statement'";
   private static final String embedXmlAttributesNoLongerSupported = "embed-xml attributes were intended to be used for DOM4J entity mode. Since that entity mode has been removed, embed-xml attributes are no longer supported and should be removed from mappings.";
   private static final String synchronizationAlreadyRegistered = "Synchronization [%s] was already registered";
   private static final String tableNotFound = "Table not found: %s";
   private static final String deprecatedUuidGenerator = "DEPRECATED : use [%s] instead with custom [%s] implementation";
   private static final String entitiesDeleted = "Entities deleted: %s";
   private static final String parsingXmlErrorForFile = "Error parsing XML: %s(%s) %s";
   private static final String unableToTransformClass = "Unable to transform class: %s";
   private static final String version = "Hibernate Core {%s}";
   private static final String configuringFromResource = "Configuring from resource: %s";
   private static final String containerProvidingNullPersistenceUnitRootUrl = "Container is providing a null PersistenceUnitRootUrl: discovery impossible";
   private static final String unableToReleaseContext = "Unable to release initial context: %s";
   private static final String resolvedSqlTypeDescriptorForDifferentSqlCode = "Resolved SqlTypeDescriptor is for a different SQL code. %s has sqlCode=%s; type override %s has sqlCode=%s";
   private static final String unknownBytecodeProvider = "unrecognized bytecode provider [%s], using javassist by default";
   private static final String entitiesInserted = "Entities inserted: %s";
   private static final String unableToRetrieveCache = "Unable to retreive cache from JNDI [%s]: %s";
   private static final String honoringOptimizerSetting = "Config specified explicit optimizer of [%s], but [%s=%s; honoring optimizer setting";
   private static final String runningHbm2ddlSchemaUpdate = "Running hbm2ddl schema update";
   private static final String unableToCleanUpPreparedStatement = "Unable to clean up prepared statement";
   private static final String unableToClosePooledConnection = "Problem closing pooled connection";
   private static final String naturalIdCachePuts = "NaturalId cache puts: %s";
   private static final String exceptionHeaderNotFound = "%s No %s found";
   private static final String immutableAnnotationOnNonRoot = "@Immutable used on a non root entity: ignored for %s";
   private static final String deprecatedTransactionManagerStrategy = "Using deprecated %s strategy [%s], use newer %s strategy instead [%s]";
   private static final String processingPersistenceUnitInfoName = "Processing PersistenceUnitInfo [\n\tname: %s\n\t...]";
   private static final String ignoringUnrecognizedQueryHint = "Ignoring unrecognized query hint [%s]";
   private static final String unableToConstructCurrentSessionContext = "Unable to construct current session context [%s]";
   private static final String unableToCloseInputStream = "Could not close input stream";
   private static final String usingDialect = "Using dialect: %s";
   private static final String autoFlushWillNotWork = "JTASessionContext being used with JDBCTransactionFactory; auto-flush will not operate correctly with getCurrentSession()";
   private static final String unableToStopService = "Error stopping service [%s] : %s";
   private static final String usingTimestampWorkaround = "Using workaround for JVM bug in java.sql.Timestamp";
   private static final String unableToRetrieveTypeInfoResultSet = "Unable to retrieve type info result set : %s";
   private static final String processEqualityExpression = "processEqualityExpression() : No expression to process!";
   private static final String unableToReleaseTypeInfoResultSet = "Unable to release type info result set";
   private static final String duplicateJoins = "Duplicate joins for class: %s";
   private static final String propertyNotFound = "Property %s not found in class but described in <mapping-file/> (possible typo error)";
   private static final String rdmsOs2200Dialect = "RDMSOS2200Dialect version: 1.0";
   private static final String usingUuidHexGenerator = "Using %s which does not generate IETF RFC 4122 compliant UUID values; consider using %s instead";
   private static final String unableToCloseJar = "Could not close jar: %s";
   private static final String duplicateGeneratorName = "Duplicate generator name %s";
   private static final String unableToCloseOutputStream = "IOException occurred closing output stream";
   private static final String pooledOptimizerReportedInitialValue = "Pooled optimizer source reported [%s] as the initial value; use of 1 or greater highly recommended";
   private static final String unableToDetermineTransactionStatus = "Could not determine transaction status";
   private static final String sortAnnotationIndexedCollection = "@Sort not allowed for an indexed collection, annotation ignored.";
   private static final String entitiesFetched = "Entities fetched (minimize this): %s";
   private static final String compositeIdClassDoesNotOverrideHashCode = "Composite-id class does not override hashCode(): %s";
   private static final String entityAnnotationOnNonRoot = "@org.hibernate.annotations.Entity used on a non root entity: ignored for %s";
   private static final String unsupportedInitialValue = "Hibernate does not support SequenceGenerator.initialValue() unless '%s' set";
   private static final String entityMappedAsNonAbstract = "Entity [%s] is abstract-class/interface explicitly mapped as non-abstract; be sure to supply entity-names";
   private static final String parsingXmlWarning = "Warning parsing XML (%s) : %s";
   private static final String unableToCloseStream = "IOException occurred closing stream";
   private static final String unableToBuildEnhancementMetamodel = "Unable to build enhancement metamodel for %s";
   private static final String hql = "HQL: %s, time: %sms, rows: %s";
   private static final String maxQueryTime = "Max query time: %sms";
   private static final String unableToReadHiValue = "Could not read a hi value - you need to populate the table: %s";
   private static final String persistenceProviderCallerDoesNotImplementEjb3SpecCorrectly = "Persistence provider caller does not implement the EJB3 spec correctly.PersistenceUnitInfo.getNewTempClassLoader() is null.";
   private static final String unableToBindFactoryToJndi = "Could not bind factory to JNDI";
   private static final String writeLocksNotSupported = "Write locks via update not supported for non-versioned entities [%s]";
   private static final String incompleteMappingMetadataCacheProcessing = "Mapping metadata cache was not completely processed";
   private static final String noSessionFactoryWithJndiName = "No session factory with JNDI name %s";
   private static final String queryCacheHits = "Query cache hits: %s";
   private static final String disablingContextualLOBCreationSinceOldJdbcVersion = "Disabling contextual LOB creation as JDBC driver reported JDBC version [%s] less than 4";
   private static final String unableToConstructSqlExceptionConverter = "Unable to construct instance of specified SQLExceptionConverter : %s";
   private static final String undeterminedH2Version = "Unable to determine H2 database version, certain features may not work";
   private static final String unableToCreateProxyFactory = "Could not create proxy factory for:%s";
   private static final String deprecatedOracleDialect = "The OracleDialect dialect has been deprecated; use Oracle8iDialect instead";
   private static final String definingFlushBeforeCompletionIgnoredInHem = "Defining %s=true ignored in HEM";
   private static final String settersOfLazyClassesCannotBeFinal = "Setters of lazy classes cannot be final: %s.%s";
   private static final String entityIdentifierValueBindingExists = "Setting entity-identifier value binding where one already existed : %s.";
   private static final String closing = "Closing";
   private static final String unableToMarkForRollbackOnTransientObjectException = "Unable to mark for rollback on TransientObjectException: ";
   private static final String jndiInitialContextProperties = "JNDI InitialContext properties:%s";
   private static final String unableToSetTransactionToRollbackOnly = "Could not set transaction to rollback only";
   private static final String unableToDetermineTransactionStatusAfterCommit = "Could not determine transaction status after commit";
   private static final String startingUpdateTimestampsCache = "Starting update timestamps cache at region: %s";
   private static final String unableToRollbackJta = "JTA rollback failed";
   private static final String unableToLogSqlWarnings = "Unable to log SQLWarnings : %s";
   private static final String failSafeEntitiesCleanup = "Fail-safe cleanup (entities) : %s";
   private static final String unableToJoinTransaction = "Cannot join transaction: do not override %s";
   private static final String unsupportedMultiTableBulkHqlJpaql = "The %s.%s.%s version of H2 implements temporary table creation such that it commits current transaction; multi-table, bulk hql/jpaql will not work properly";
   private static final String noColumnsSpecifiedForIndex = "There were not column names specified for index %s on table %s";
   private static final String unableToReadColumnValueFromResultSet = "Could not read column value from result set: %s; %s";
   private static final String illegalPropertyGetterArgument = "IllegalArgumentException in class: %s, getter method of property: %s";
   private static final String connectionsObtained = "Connections obtained: %s";
   private static final String transactionStartedOnNonRootSession = "Transaction started on non-root session";
   private static final String scopingTypesToSessionFactoryAfterAlreadyScoped = "Scoping types to session factory %s after already scoped %s";
   private static final String unableToConfigureSqlExceptionConverter = "Unable to configure SQLExceptionConverter : %s";
   private static final String unableToInstantiateConfiguredSchemaNameResolver = "Unable to instantiate configured schema name resolver [%s] %s";
   private static final String unableToExecuteBatch = "Exception executing batch [%s]";
   private static final String unsupportedOracleVersion = "Oracle 11g is not yet fully supported; using Oracle 10g dialect";
   private static final String autoCommitMode = "Autocommit mode: %s";
   private static final String secondLevelCacheHits = "Second level cache hits: %s";
   private static final String usingDefaultIdGeneratorSegmentValue = "Explicit segment value for id generator [%s.%s] suggested; using default [%s]";
   private static final String unableToRollbackConnection = "Unable to rollback connection on exception [%s]";
   private static final String factoryJndiRename = "A factory was renamed from [%s] to [%s] in JNDI";
   private static final String unableToRollbackIsolatedTransaction = "Unable to rollback isolated transaction on error [%s] : [%s]";
   private static final String usingAstQueryTranslatorFactory = "Using ASTQueryTranslatorFactory";
   private static final String aliasSpecificLockingWithFollowOnLocking = "Alias-specific lock modes requested, which is not currently supported with follow-on locking; all acquired locks will be [%s]";
   private static final String unableToDestroyCache = "Unable to destroy cache: %s";
   private static final String unableToReadClass = "Unable to read class: %s";
   private static final String needsLimit = "FirstResult/maxResults specified on polymorphic query; applying in memory!";
   private static final String fetchingDatabaseMetadata = "Fetching database metadata";
   private static final String readingCachedMappings = "Reading mappings from cache file: %s";
   private static final String statementsClosed = "Statements closed: %s";
   private static final String unableToWrapResultSet = "Error wrapping result set";
   private static final String unableToBuildSessionFactoryUsingMBeanClasspath = "Could not build SessionFactory using the MBean classpath - will try again using client classpath: %s";
   private static final String naturalIdMaxQueryTime = "Max NaturalId query time: %sms";
   private static final String forcingContainerResourceCleanup = "Forcing container resource cleanup on transaction completion";
   private static final String timestampCacheHits = "update timestamps cache hits: %s";
   private static final String willNotRegisterListeners = "Property hibernate.search.autoregister_listeners is set to false. No attempt will be made to register Hibernate Search event listeners.";
   private static final String unableToCompleteSchemaUpdate = "Could not complete schema update";
   private static final String unableToLoadProperties = "Problem loading properties from hibernate.properties";
   private static final String creatingSubcontextInfo = "Creating subcontext: %s";
   private static final String gettersOfLazyClassesCannotBeFinal = "Getters of lazy classes cannot be final: %s.%s";
   private static final String expired = "An item was expired by the cache while it was locked (increase your cache timeout): %s";
   private static final String unableToCommitJta = "JTA commit failed";
   private static final String unsuccessfulCreate = "Unsuccessful: %s";
   private static final String unknownSqlServerVersion = "Unknown Microsoft SQL Server major version [%s] using SQL Server 2000 dialect";
   private static final String unableToBindEjb3ConfigurationToJndi = "Could not bind Ejb3Configuration to JNDI";
   private static final String deprecatedDerbyDialect = "The DerbyDialect dialect has been deprecated; use one of the version-specific dialects instead";
   private static final String unableToCopySystemProperties = "Could not copy system properties, system properties will be ignored";
   private static final String invalidOnDeleteAnnotation = "Inapropriate use of @OnDelete on entity, annotation ignored: %s";
   private static final String validatorNotFound = "Hibernate Validator not found: ignoring";
   private static final String parsingXmlError = "Error parsing XML (%s) : %s";
   private static final String unableToCloseIterator = "Unable to close iterator";
   private static final String foundMappingDocument = "Found mapping document in jar: %s";
   private static final String containsJoinFetchedCollection = "Ignoring bag join fetch [%s] due to prior collection join fetch";
   private static final String unableToRemoveBagJoinFetch = "Unable to erase previously added bag join fetch";
   private static final String unableToObjectConnectionToQueryMetadata = "Could not obtain connection to query metadata: %s";
   private static final String packageNotFound = "Package not found or wo package-info.java: %s";
   private static final String invalidPrimaryKeyJoinColumnAnnotation = "Root entity should not hold an PrimaryKeyJoinColum(s), will be ignored";
   private static final String invalidTableAnnotation = "Illegal use of @Table in a subclass of a SINGLE_TABLE hierarchy: %s";
   private static final String unableToReleaseIsolatedConnection = "Unable to release isolated connection [%s]";
   private static final String handlingTransientEntity = "Handling transient entity in delete processing";
   private static final String unableToUnbindFactoryFromJndi = "Could not unbind factory from JNDI";
   private static final String jdbcAutoCommitFalseBreaksEjb3Spec = "%s = false breaks the EJB3 specification";
   private static final String renamedProperty = "Property [%s] has been renamed to [%s]; update your properties appropriately";
   private static final String unableToCloseInitialContext = "Error closing InitialContext [%s]";
   private static final String configuringFromUrl = "Configuring from URL: %s";
   private static final String unableToResolveAggregateFunction = "Could not resolve aggregate function [%s]; using standard definition";
   private static final String tooManyInExpressions = "Dialect [%s] limits the number of elements in an IN predicate to %s entries.  However, the given parameter list [%s] contained %s entries, which will likely cause failures to execute the query in the database";
   private static final String cachedFileNotFound = "I/O reported cached file could not be found : %s : %s";
   private static final String proxoolProviderClassNotFound = "proxool properties were encountered, but the %s provider class was not found on the classpath; these properties are going to be ignored.";
   private static final String unableToLocateConfiguredSchemaNameResolver = "Unable to locate configured schema name resolver class [%s] %s";
   private static final String invalidDiscriminatorAnnotation = "Discriminator column has to be defined in the root entity, it will be ignored in subclass: %s";
   private static final String legacyTransactionManagerStrategy = "Encountered legacy TransactionManagerLookup specified; convert to newer %s contract specified via %s setting";
   private static final String exceptionInAfterTransactionCompletionInterceptor = "Exception in interceptor afterTransactionCompletion()";
   private static final String typeRegistrationOverridesPrevious = "Type registration [%s] overrides previous : %s";
   private static final String unableToInstantiateUuidGenerationStrategy = "Unable to instantiate UUID generation strategy class : %s";
   private static final String unableToDestroyUpdateTimestampsCache = "Unable to destroy update timestamps cache: %s: %s";
   private static final String unableToLocateUuidGenerationStrategy = "Unable to locate requested UUID generation strategy class : %s";
   private static final String overridingTransactionStrategyDangerous = "Overriding %s is dangerous, this might break the EJB3 specification implementation";
   private static final String successfulTransactions = "Successful transactions: %s";
   private static final String namingExceptionAccessingFactory = "Naming exception occurred accessing factory: %s";
   private static final String unableToFindPersistenceXmlInClasspath = "Could not find any META-INF/persistence.xml file in the classpath";
   private static final String hydratingEntitiesCount = "On EntityLoadContext#clear, hydratingEntities contained [%s] entries";
   private static final String propertiesLoaded = "Loaded properties from resource hibernate.properties: %s";
   private static final String configuredSessionFactory = "Configured SessionFactory: %s";
   private static final String propertiesNotFound = "hibernate.properties not found";
   private static final String unknownOracleVersion = "Unknown Oracle major version [%s]";
   private static final String hibernateConnectionPoolSize = "Hibernate connection pool size: %s";
   private static final String configuringFromXmlDocument = "Configuring from XML document";
   private static final String disablingContextualLOBCreation = "Disabling contextual LOB creation as %s is true";
   private static final String localLoadingCollectionKeysCount = "On CollectionLoadContext#cleanup, localLoadingCollectionKeys contained [%s] entries";
   private static final String tableFound = "Table found: %s";
   private static final String unableToCloseInputFiles = "Error closing input files: %s";
   private static final String unableToInstantiateOptimizer = "Unable to instantiate specified optimizer [%s], falling back to noop";
   private static final String usingOldDtd = "Don't use old DTDs, read the Hibernate 3.x Migration Guide!";
   private static final String collectionsRecreated = "Collections recreated: %s";
   private static final String updatingSchema = "Updating schema";
   private static final String unsupportedIngresVersion = "Ingres 10 is not yet fully supported; using Ingres 9.3 dialect";
   private static final String unableToObtainInitialContext = "Could not obtain initial context";
   private static final String failed = "an assertion failure occured (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session): %s";
   private static final String unableToToggleAutoCommit = "Could not toggle autocommit";
   private static final String missingArguments = "Function template anticipated %s arguments, but %s arguments encountered";
   private static final String unableToResolveMappingFile = "Unable to resolve mapping file [%s]";
   private static final String requiredDifferentProvider = "Required a different provider: %s";
   private static final String exceptionInSubResolver = "Sub-resolver threw unexpected exception, continuing to next : %s";
   private static final String missingEntityAnnotation = "Class annotated @org.hibernate.annotations.Entity but not javax.persistence.Entity (most likely a user error): %s";
   private static final String startTime = "Start time: %s";
   private static final String connectionProperties = "Connection properties: %s";
   private static final String unableToSwitchToMethodUsingColumnIndex = "Exception switching from method: [%s] to a method using the column index. Reverting to using: [%<s]";
   private static final String configurationResource = "Configuration resource: %s";
   private static final String readingMappingsFromResource = "Reading mappings from resource: %s";
   private static final String usingDefaultTransactionStrategy = "Using default transaction strategy (direct JDBC transactions)";
   private static final String guidGenerated = "GUID identifier generated: %s";
   private static final String lazyPropertyFetchingAvailable = "Lazy property fetching available for: %s";
   private static final String unsupportedProperty = "Usage of obsolete property: %s no longer supported, use: %s";
   private static final String unableToDropTemporaryIdTable = "Unable to drop temporary id table after use [%s]";
   private static final String unableToCloseSessionDuringRollback = "Could not close session during rollback";
   private static final String factoryUnboundFromName = "A factory was unbound from name: %s";
   private static final String exceptionHeaderFound = "%s %s found";
   private static final String usingHibernateBuiltInConnectionPool = "Using Hibernate built-in connection pool (not for production use!)";
   private static final String unableToCloseSessionButSwallowingError = "Could not close session; swallowing exception[%s] as transaction completed";
   private static final String invalidSubStrategy = "Mixing inheritance strategy in a entity hierarchy is not allowed, ignoring sub strategy in: %s";
   private static final String unableToLoadDerbyDriver = "Unable to load/access derby driver class sysinfo to check versions : %s";
   private static final String narrowingProxy = "Narrowing proxy to %s - this operation breaks ==";
   private static final String closingUnreleasedBatch = "Closing un-released batch";
   private static final String invalidEditOfReadOnlyItem = "Application attempted to edit read only item: %s";
   private static final String parameterPositionOccurredAsBothJpaAndHibernatePositionalParameter = "Parameter position [%s] occurred as both JPA and Hibernate positional parameter";
   private static final String expectedType = "Expected type: %s, actual value: %s";
   private static final String unableToCleanupTemporaryIdTable = "Unable to cleanup temporary id table after use [%s]";
   private static final String jdbcUrlNotSpecified = "JDBC URL was not specified by property %s";
   private static final String c3p0ProviderClassNotFound = "c3p0 properties were encountered, but the %s provider class was not found on the classpath; these properties are going to be ignored.";
   private static final String couldNotBindJndiListener = "Could not bind JNDI listener";
   private static final String warningsCreatingTempTable = "Warnings creating temp table : %s";

   public CoreMessageLogger_$logger(Logger log) {
      this.log = log;
   }

   public final boolean isTraceEnabled() {
      return this.log.isTraceEnabled();
   }

   public final void trace(Object message) {
      this.log.trace(FQCN, message, (Throwable)null);
   }

   public final void trace(Object message, Throwable t) {
      this.log.trace(FQCN, message, t);
   }

   public final void trace(String loggerFqcn, Object message, Throwable t) {
      this.log.trace(loggerFqcn, message, t);
   }

   public final void trace(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.trace(loggerFqcn, message, params, t);
   }

   public final void tracev(String format, Object... params) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, params);
   }

   public final void tracev(String format, Object param1) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1);
   }

   public final void tracev(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1, param2);
   }

   public final void tracev(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1, param2, param3);
   }

   public final void tracev(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.TRACE, t, format, params);
   }

   public final void tracev(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1);
   }

   public final void tracev(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1, param2);
   }

   public final void tracev(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1, param2, param3);
   }

   public final void tracef(String format, Object... params) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, params);
   }

   public final void tracef(String format, Object param1) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1);
   }

   public final void tracef(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1, param2);
   }

   public final void tracef(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1, param2, param3);
   }

   public final void tracef(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.TRACE, t, format, params);
   }

   public final void tracef(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1);
   }

   public final void tracef(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1, param2);
   }

   public final void tracef(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1, param2, param3);
   }

   public final boolean isDebugEnabled() {
      return this.log.isDebugEnabled();
   }

   public final void debug(Object message) {
      this.log.debug(FQCN, message, (Throwable)null);
   }

   public final void debug(Object message, Throwable t) {
      this.log.debug(FQCN, message, t);
   }

   public final void debug(String loggerFqcn, Object message, Throwable t) {
      this.log.debug(loggerFqcn, message, t);
   }

   public final void debug(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.debug(loggerFqcn, message, params, t);
   }

   public final void debugv(String format, Object... params) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, params);
   }

   public final void debugv(String format, Object param1) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1);
   }

   public final void debugv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2);
   }

   public final void debugv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2, param3);
   }

   public final void debugv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.DEBUG, t, format, params);
   }

   public final void debugv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1);
   }

   public final void debugv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1, param2);
   }

   public final void debugv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1, param2, param3);
   }

   public final void debugf(String format, Object... params) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, params);
   }

   public final void debugf(String format, Object param1) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1);
   }

   public final void debugf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2);
   }

   public final void debugf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2, param3);
   }

   public final void debugf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.DEBUG, t, format, params);
   }

   public final void debugf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1);
   }

   public final void debugf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1, param2);
   }

   public final void debugf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1, param2, param3);
   }

   public final boolean isInfoEnabled() {
      return this.log.isInfoEnabled();
   }

   public final void info(Object message) {
      this.log.info(FQCN, message, (Throwable)null);
   }

   public final void info(Object message, Throwable t) {
      this.log.info(FQCN, message, t);
   }

   public final void info(String loggerFqcn, Object message, Throwable t) {
      this.log.info(loggerFqcn, message, t);
   }

   public final void info(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.info(loggerFqcn, message, params, t);
   }

   public final void infov(String format, Object... params) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, params);
   }

   public final void infov(String format, Object param1) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1);
   }

   public final void infov(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1, param2);
   }

   public final void infov(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1, param2, param3);
   }

   public final void infov(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.INFO, t, format, params);
   }

   public final void infov(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.INFO, t, format, param1);
   }

   public final void infov(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.INFO, t, format, param1, param2);
   }

   public final void infov(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.INFO, t, format, param1, param2, param3);
   }

   public final void infof(String format, Object... params) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, params);
   }

   public final void infof(String format, Object param1) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1);
   }

   public final void infof(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1, param2);
   }

   public final void infof(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1, param2, param3);
   }

   public final void infof(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.INFO, t, format, params);
   }

   public final void infof(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.INFO, t, format, param1);
   }

   public final void infof(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.INFO, t, format, param1, param2);
   }

   public final void infof(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.INFO, t, format, param1, param2, param3);
   }

   public final void warn(Object message) {
      this.log.warn(FQCN, message, (Throwable)null);
   }

   public final void warn(Object message, Throwable t) {
      this.log.warn(FQCN, message, t);
   }

   public final void warn(String loggerFqcn, Object message, Throwable t) {
      this.log.warn(loggerFqcn, message, t);
   }

   public final void warn(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.warn(loggerFqcn, message, params, t);
   }

   public final void warnv(String format, Object... params) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, params);
   }

   public final void warnv(String format, Object param1) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1);
   }

   public final void warnv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1, param2);
   }

   public final void warnv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1, param2, param3);
   }

   public final void warnv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.WARN, t, format, params);
   }

   public final void warnv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.WARN, t, format, param1);
   }

   public final void warnv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.WARN, t, format, param1, param2);
   }

   public final void warnv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.WARN, t, format, param1, param2, param3);
   }

   public final void warnf(String format, Object... params) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, params);
   }

   public final void warnf(String format, Object param1) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1);
   }

   public final void warnf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1, param2);
   }

   public final void warnf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1, param2, param3);
   }

   public final void warnf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.WARN, t, format, params);
   }

   public final void warnf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.WARN, t, format, param1);
   }

   public final void warnf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.WARN, t, format, param1, param2);
   }

   public final void warnf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.WARN, t, format, param1, param2, param3);
   }

   public final void error(Object message) {
      this.log.error(FQCN, message, (Throwable)null);
   }

   public final void error(Object message, Throwable t) {
      this.log.error(FQCN, message, t);
   }

   public final void error(String loggerFqcn, Object message, Throwable t) {
      this.log.error(loggerFqcn, message, t);
   }

   public final void error(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.error(loggerFqcn, message, params, t);
   }

   public final void errorv(String format, Object... params) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, params);
   }

   public final void errorv(String format, Object param1) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1);
   }

   public final void errorv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1, param2);
   }

   public final void errorv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1, param2, param3);
   }

   public final void errorv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.ERROR, t, format, params);
   }

   public final void errorv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1);
   }

   public final void errorv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1, param2);
   }

   public final void errorv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1, param2, param3);
   }

   public final void errorf(String format, Object... params) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, params);
   }

   public final void errorf(String format, Object param1) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1);
   }

   public final void errorf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1, param2);
   }

   public final void errorf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1, param2, param3);
   }

   public final void errorf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.ERROR, t, format, params);
   }

   public final void errorf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1);
   }

   public final void errorf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1, param2);
   }

   public final void errorf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1, param2, param3);
   }

   public final void fatal(Object message) {
      this.log.fatal(FQCN, message, (Throwable)null);
   }

   public final void fatal(Object message, Throwable t) {
      this.log.fatal(FQCN, message, t);
   }

   public final void fatal(String loggerFqcn, Object message, Throwable t) {
      this.log.fatal(loggerFqcn, message, t);
   }

   public final void fatal(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.fatal(loggerFqcn, message, params, t);
   }

   public final void fatalv(String format, Object... params) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, params);
   }

   public final void fatalv(String format, Object param1) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1);
   }

   public final void fatalv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1, param2);
   }

   public final void fatalv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1, param2, param3);
   }

   public final void fatalv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.FATAL, t, format, params);
   }

   public final void fatalv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1);
   }

   public final void fatalv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1, param2);
   }

   public final void fatalv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1, param2, param3);
   }

   public final void fatalf(String format, Object... params) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, params);
   }

   public final void fatalf(String format, Object param1) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1);
   }

   public final void fatalf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1, param2);
   }

   public final void fatalf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1, param2, param3);
   }

   public final void fatalf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.FATAL, t, format, params);
   }

   public final void fatalf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1);
   }

   public final void fatalf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1, param2);
   }

   public final void fatalf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1, param2, param3);
   }

   public final boolean isEnabled(Logger.Level level) {
      return this.log.isEnabled(level);
   }

   public final void log(Logger.Level level, Object message) {
      this.log.log(FQCN, level, message, (Object[])null, (Throwable)null);
   }

   public final void log(Logger.Level level, Object message, Throwable t) {
      this.log.log(FQCN, level, message, (Object[])null, t);
   }

   public final void log(Logger.Level level, String loggerFqcn, Object message, Throwable t) {
      this.log.log(level, loggerFqcn, message, t);
   }

   public final void log(String loggerFqcn, Logger.Level level, Object message, Object[] params, Throwable t) {
      this.log.log(loggerFqcn, level, message, params, t);
   }

   public final void logv(Logger.Level level, String format, Object... params) {
      this.log.logv(FQCN, level, (Throwable)null, format, params);
   }

   public final void logv(Logger.Level level, String format, Object param1) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1);
   }

   public final void logv(Logger.Level level, String format, Object param1, Object param2) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1, param2);
   }

   public final void logv(Logger.Level level, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1, param2, param3);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logv(FQCN, level, t, format, params);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logv(FQCN, level, t, format, param1);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, level, t, format, param1, param2);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, level, t, format, param1, param2, param3);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logv(loggerFqcn, level, t, format, params);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logv(loggerFqcn, level, t, format, param1);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logv(loggerFqcn, level, t, format, param1, param2);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(loggerFqcn, level, t, format, param1, param2, param3);
   }

   public final void logf(Logger.Level level, String format, Object... params) {
      this.log.logf(FQCN, level, (Throwable)null, format, params);
   }

   public final void logf(Logger.Level level, String format, Object param1) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1);
   }

   public final void logf(Logger.Level level, String format, Object param1, Object param2) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1, param2);
   }

   public final void logf(Logger.Level level, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1, param2, param3);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logf(FQCN, level, t, format, params);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logf(FQCN, level, t, format, param1);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, level, t, format, param1, param2);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, level, t, format, param1, param2, param3);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logf(loggerFqcn, level, t, format, params);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logf(loggerFqcn, level, t, format, param1);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logf(loggerFqcn, level, t, format, param1, param2);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(loggerFqcn, level, t, format, param1, param2, param3);
   }

   public final void unableToCloseOutputFile(String outputFile, IOException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000291: " + this.unableToCloseOutputFile$str(), outputFile);
   }

   protected String unableToCloseOutputFile$str() {
      return "Error closing output file: %s";
   }

   public final void unableToCreateSchema(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000306: " + this.unableToCreateSchema$str(), new Object[0]);
   }

   protected String unableToCreateSchema$str() {
      return "Error creating schema ";
   }

   public final void logicalConnectionReleasingPhysicalConnection() {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000163: " + this.logicalConnectionReleasingPhysicalConnection$str(), new Object[0]);
   }

   protected String logicalConnectionReleasingPhysicalConnection$str() {
      return "Logical connection releasing its physical connection";
   }

   public final void unableToExecuteResolver(AbstractDialectResolver abstractDialectResolver, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000316: " + this.unableToExecuteResolver$str(), abstractDialectResolver, message);
   }

   protected String unableToExecuteResolver$str() {
      return "Error executing resolver [%s] : %s";
   }

   public final void logicalConnectionClosed() {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000162: " + this.logicalConnectionClosed$str(), new Object[0]);
   }

   protected String logicalConnectionClosed$str() {
      return "*** Logical connection closed ***";
   }

   public final void unableToWriteCachedFile(String path, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000378: " + this.unableToWriteCachedFile$str(), path, message);
   }

   protected String unableToWriteCachedFile$str() {
      return "I/O reported error writing cached file : %s: %s";
   }

   public final void disablingContextualLOBCreationSinceCreateClobFailed(Throwable t) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000424: " + this.disablingContextualLOBCreationSinceCreateClobFailed$str(), t);
   }

   protected String disablingContextualLOBCreationSinceCreateClobFailed$str() {
      return "Disabling contextual LOB creation as createClob() method threw error : %s";
   }

   public final void unregisteredStatement() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000387: " + this.unregisteredStatement$str(), new Object[0]);
   }

   protected String unregisteredStatement$str() {
      return "ResultSet's statement was not registered";
   }

   public final void unableToGetDatabaseMetadata(SQLException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000319: " + this.unableToGetDatabaseMetadata$str(), new Object[0]);
   }

   protected String unableToGetDatabaseMetadata$str() {
      return "Could not get database metadata";
   }

   public final void jaccContextId(String contextId) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000140: " + this.jaccContextId$str(), contextId);
   }

   protected String jaccContextId$str() {
      return "JACC contextID: %s";
   }

   public final void invalidArrayElementType(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000132: " + this.invalidArrayElementType$str(), message);
   }

   protected String invalidArrayElementType$str() {
      return "Array element type error\n%s";
   }

   public final void splitQueries(String sourceQuery, int length) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000245: " + this.splitQueries$str(), sourceQuery, length);
   }

   protected String splitQueries$str() {
      return "Manipulation query [%s] resulted in [%s] split queries";
   }

   public final void unableToReleaseCreatedMBeanServer(String string) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000355: " + this.unableToReleaseCreatedMBeanServer$str(), string);
   }

   protected String unableToReleaseCreatedMBeanServer$str() {
      return "Unable to release created MBeanServer : %s";
   }

   public final Object unableToUpdateHiValue(String tableName) {
      Object result = String.format("HHH000375: " + this.unableToUpdateHiValue$str(), tableName);
      return result;
   }

   protected String unableToUpdateHiValue$str() {
      return "Could not update hi value in: %s";
   }

   public final void unableToAccessSessionFactory(String sfJNDIName, NamingException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000272: " + this.unableToAccessSessionFactory$str(), sfJNDIName);
   }

   protected String unableToAccessSessionFactory$str() {
      return "Error while accessing session factory with JNDI name %s";
   }

   public final void searchingForMappingDocuments(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000235: " + this.searchingForMappingDocuments$str(), name);
   }

   protected String searchingForMappingDocuments$str() {
      return "Searching for mapping documents in jar: %s";
   }

   public final void transactions(long transactionCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000266: " + this.transactions$str(), transactionCount);
   }

   protected String transactions$str() {
      return "Transactions: %s";
   }

   public final void failSafeCollectionsCleanup(CollectionLoadContext collectionLoadContext) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000100: " + this.failSafeCollectionsCleanup$str(), collectionLoadContext);
   }

   protected String failSafeCollectionsCleanup$str() {
      return "Fail-safe cleanup (collections) : %s";
   }

   public final void statementsPrepared(long prepareStatementCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000253: " + this.statementsPrepared$str(), prepareStatementCount);
   }

   protected String statementsPrepared$str() {
      return "Statements prepared: %s";
   }

   public final void usingDriver(String driverClassName, String url) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000401: " + this.usingDriver$str(), driverClassName, url);
   }

   protected String usingDriver$str() {
      return "using driver [%s] at URL [%s]";
   }

   public final void deprecatedOracle9Dialect() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000063: " + this.deprecatedOracle9Dialect$str(), new Object[0]);
   }

   protected String deprecatedOracle9Dialect$str() {
      return "The Oracle9Dialect dialect has been deprecated; use either Oracle9iDialect or Oracle10gDialect instead";
   }

   public final void synchronizationFailed(Synchronization synchronization, Throwable t) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000260: " + this.synchronizationFailed$str(), synchronization, t);
   }

   protected String synchronizationFailed$str() {
      return "Exception calling user Synchronization [%s] : %s";
   }

   public final void unableToLoadCommand(HibernateException e) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000327: " + this.unableToLoadCommand$str(), e);
   }

   protected String unableToLoadCommand$str() {
      return "Error performing load command : %s";
   }

   public final void disablingContextualLOBCreationSinceConnectionNull() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000422: " + this.disablingContextualLOBCreationSinceConnectionNull$str(), new Object[0]);
   }

   protected String disablingContextualLOBCreationSinceConnectionNull$str() {
      return "Disabling contextual LOB creation as connection was null";
   }

   public final void schemaUpdateComplete() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000232: " + this.schemaUpdateComplete$str(), new Object[0]);
   }

   protected String schemaUpdateComplete$str() {
      return "Schema update complete";
   }

   public final void usingStreams() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000407: " + this.usingStreams$str(), new Object[0]);
   }

   protected String usingStreams$str() {
      return "Using java.io streams to persist binary types";
   }

   public final void exceptionInBeforeTransactionCompletionInterceptor(Throwable e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000088: " + this.exceptionInBeforeTransactionCompletionInterceptor$str(), new Object[0]);
   }

   protected String exceptionInBeforeTransactionCompletionInterceptor$str() {
      return "Exception in interceptor beforeTransactionCompletion()";
   }

   public final void alreadySessionBound() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000002: " + this.alreadySessionBound$str(), new Object[0]);
   }

   protected String alreadySessionBound$str() {
      return "Already session bound on call to bind(); make sure you clean up your sessions!";
   }

   public final void unableToAccessTypeInfoResultSet(String string) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000273: " + this.unableToAccessTypeInfoResultSet$str(), string);
   }

   protected String unableToAccessTypeInfoResultSet$str() {
      return "Error accessing type info result set : %s";
   }

   public final void disallowingInsertStatementComment() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000067: " + this.disallowingInsertStatementComment$str(), new Object[0]);
   }

   protected String disallowingInsertStatementComment$str() {
      return "Disallowing insert statement comment for select-identity due to Oracle driver bug";
   }

   public final void loggingStatistics() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000161: " + this.loggingStatistics$str(), new Object[0]);
   }

   protected String loggingStatistics$str() {
      return "Logging statistics....";
   }

   public final void sessionsClosed(long sessionCloseCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000241: " + this.sessionsClosed$str(), sessionCloseCount);
   }

   protected String sessionsClosed$str() {
      return "Sessions closed: %s";
   }

   public final void unableToLogWarnings(SQLException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000336: " + this.unableToLogWarnings$str(), new Object[0]);
   }

   protected String unableToLogWarnings$str() {
      return "Could not log warnings";
   }

   public final void noPersistentClassesFound(String query) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000183: " + this.noPersistentClassesFound$str(), query);
   }

   protected String noPersistentClassesFound$str() {
      return "no persistent classes found for query class: %s";
   }

   public final void writingGeneratedSchemaToFile(String outputFile) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000417: " + this.writingGeneratedSchemaToFile$str(), outputFile);
   }

   protected String writingGeneratedSchemaToFile$str() {
      return "Writing generated schema to file: %s";
   }

   public final void unsuccessful(String sql) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000388: " + this.unsuccessful$str(), sql);
   }

   protected String unsuccessful$str() {
      return "Unsuccessful: %s";
   }

   public final void unableToAccessEjb3Configuration(NamingException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000271: " + this.unableToAccessEjb3Configuration$str(), new Object[0]);
   }

   protected String unableToAccessEjb3Configuration$str() {
      return "Naming exception occurred accessing Ejb3Configuration";
   }

   public final void indexes(Set keySet) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000126: " + this.indexes$str(), keySet);
   }

   protected String indexes$str() {
      return "Indexes: %s";
   }

   public final void collectionsLoaded(long collectionLoadCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000033: " + this.collectionsLoaded$str(), collectionLoadCount);
   }

   protected String collectionsLoaded$str() {
      return "Collections loaded: %s";
   }

   public final void duplicateMetadata() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000074: " + this.duplicateMetadata$str(), new Object[0]);
   }

   protected String duplicateMetadata$str() {
      return "Found more than one <persistence-unit-metadata>, subsequent ignored";
   }

   public final void unableToApplyConstraints(String className, Exception e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000274: " + this.unableToApplyConstraints$str(), className);
   }

   protected String unableToApplyConstraints$str() {
      return "Unable to apply constraints on DDL for %s";
   }

   public final void orderByAnnotationIndexedCollection() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000189: " + this.orderByAnnotationIndexedCollection$str(), new Object[0]);
   }

   protected String orderByAnnotationIndexedCollection$str() {
      return "@OrderBy not allowed for an indexed collection, annotation ignored.";
   }

   public final void instantiatingExplicitConnectionProvider(String providerClassName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000130: " + this.instantiatingExplicitConnectionProvider$str(), providerClassName);
   }

   protected String instantiatingExplicitConnectionProvider$str() {
      return "Instantiating explicit connection provider: %s";
   }

   public final void collectionsFetched(long collectionFetchCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000032: " + this.collectionsFetched$str(), collectionFetchCount);
   }

   protected String collectionsFetched$str() {
      return "Collections fetched (minimize this): %s";
   }

   public final void schemaExportComplete() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000230: " + this.schemaExportComplete$str(), new Object[0]);
   }

   protected String schemaExportComplete$str() {
      return "Schema export complete";
   }

   public final void subResolverException(String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000257: " + this.subResolverException$str(), message);
   }

   protected String subResolverException$str() {
      return "sub-resolver threw unexpected exception, continuing to next : %s";
   }

   public final void unableToDeserializeCache(String path, SerializationException error) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000307: " + this.unableToDeserializeCache$str(), path, error);
   }

   protected String unableToDeserializeCache$str() {
      return "Could not deserialize cache file: %s : %s";
   }

   public final void queriesExecuted(long queryExecutionCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000210: " + this.queriesExecuted$str(), queryExecutionCount);
   }

   protected String queriesExecuted$str() {
      return "Queries executed to database: %s";
   }

   public final void unableToUpdateQueryHiValue(String tableName, SQLException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000376: " + this.unableToUpdateQueryHiValue$str(), tableName);
   }

   protected String unableToUpdateQueryHiValue$str() {
      return "Could not updateQuery hi value in: %s";
   }

   public final void unableToObtainConnectionMetadata(String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000341: " + this.unableToObtainConnectionMetadata$str(), message);
   }

   protected String unableToObtainConnectionMetadata$str() {
      return "Could not obtain connection metadata : %s";
   }

   public final void jndiNameDoesNotHandleSessionFactoryReference(String sfJNDIName, ClassCastException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000155: " + this.jndiNameDoesNotHandleSessionFactoryReference$str(), sfJNDIName);
   }

   protected String jndiNameDoesNotHandleSessionFactoryReference$str() {
      return "JNDI name %s does not handle a session factory reference";
   }

   public final void startingQueryCache(String region) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000248: " + this.startingQueryCache$str(), region);
   }

   protected String startingQueryCache$str() {
      return "Starting query cache at region: %s";
   }

   public final void entityManagerClosedBySomeoneElse(String autoCloseSession) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000082: " + this.entityManagerClosedBySomeoneElse$str(), autoCloseSession);
   }

   protected String entityManagerClosedBySomeoneElse$str() {
      return "Entity Manager closed by someone else (%s must not be used)";
   }

   public final void factoryBoundToJndiName(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000094: " + this.factoryBoundToJndiName$str(), name);
   }

   protected String factoryBoundToJndiName$str() {
      return "Bound factory to JNDI name: %s";
   }

   public final void timestampCacheMisses(long updateTimestampsCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000435: " + this.timestampCacheMisses$str(), updateTimestampsCachePutCount);
   }

   protected String timestampCacheMisses$str() {
      return "update timestamps cache misses: %s";
   }

   public final String unableToPerformJdbcCommit() {
      String result = String.format("HHH000345: " + this.unableToPerformJdbcCommit$str());
      return result;
   }

   protected String unableToPerformJdbcCommit$str() {
      return "JDBC commit failed";
   }

   public final void unexpectedLiteralTokenType(int type) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000380: " + this.unexpectedLiteralTokenType$str(), type);
   }

   protected String unexpectedLiteralTokenType$str() {
      return "Unexpected literal token type [%s] passed for numeric processing";
   }

   public final void queryCacheMisses(long queryCacheMissCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000214: " + this.queryCacheMisses$str(), queryCacheMissCount);
   }

   protected String queryCacheMisses$str() {
      return "Query cache misses: %s";
   }

   public final String javassistEnhancementFailed(String entityName) {
      String result = String.format("HHH000142: " + this.javassistEnhancementFailed$str(), entityName);
      return result;
   }

   protected String javassistEnhancementFailed$str() {
      return "Javassist Enhancement failed: %s";
   }

   public final void flushes(long flushCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000105: " + this.flushes$str(), flushCount);
   }

   protected String flushes$str() {
      return "Flushes: %s";
   }

   public final void naturalIdQueriesExecuted(long naturalIdQueriesExecutionCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000442: " + this.naturalIdQueriesExecuted$str(), naturalIdQueriesExecutionCount);
   }

   protected String naturalIdQueriesExecuted$str() {
      return "NaturalId queries executed to database: %s";
   }

   public final void unregisteredResultSetWithoutStatement() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000386: " + this.unregisteredResultSetWithoutStatement$str(), new Object[0]);
   }

   protected String unregisteredResultSetWithoutStatement$str() {
      return "ResultSet had no statement associated with it, but was not yet registered";
   }

   public final void parsingXmlWarningForFile(String file, int lineNumber, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000199: " + this.parsingXmlWarningForFile$str(), file, lineNumber, message);
   }

   protected String parsingXmlWarningForFile$str() {
      return "Warning parsing XML: %s(%s) %s";
   }

   public final void optimisticLockFailures(long optimisticFailureCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000187: " + this.optimisticLockFailures$str(), optimisticFailureCount);
   }

   protected String optimisticLockFailures$str() {
      return "Optimistic lock failures: %s";
   }

   public final void duplicateGeneratorTable(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000070: " + this.duplicateGeneratorTable$str(), name);
   }

   protected String duplicateGeneratorTable$str() {
      return "Duplicate generator table: %s";
   }

   public final void forcingTableUse() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000107: " + this.forcingTableUse$str(), new Object[0]);
   }

   protected String forcingTableUse$str() {
      return "Forcing table use for sequence-style generator due to pooled optimizer selection where db does not support pooled sequences";
   }

   public final void duplicateListener(String className) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000073: " + this.duplicateListener$str(), className);
   }

   protected String duplicateListener$str() {
      return "entity-listener duplication, first event definition will be used: %s";
   }

   public final void foreignKeys(Set keySet) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000108: " + this.foreignKeys$str(), keySet);
   }

   protected String foreignKeys$str() {
      return "Foreign keys: %s";
   }

   public final void startingServiceAtJndiName(String boundName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000249: " + this.startingServiceAtJndiName$str(), boundName);
   }

   protected String startingServiceAtJndiName$str() {
      return "Starting service at JNDI name: %s";
   }

   public final void unableToReleaseBatchStatement() {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000352: " + this.unableToReleaseBatchStatement$str(), new Object[0]);
   }

   protected String unableToReleaseBatchStatement$str() {
      return "Unable to release batch statement...";
   }

   public final void serviceProperties(Properties properties) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000240: " + this.serviceProperties$str(), properties);
   }

   protected String serviceProperties$str() {
      return "Service properties: %s";
   }

   public final void naturalIdCacheMisses(long naturalIdCacheMissCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000440: " + this.naturalIdCacheMisses$str(), naturalIdCacheMissCount);
   }

   protected String naturalIdCacheMisses$str() {
      return "NaturalId cache misses: %s";
   }

   public final void unexpectedRowCounts() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000381: " + this.unexpectedRowCounts$str(), new Object[0]);
   }

   protected String unexpectedRowCounts$str() {
      return "JDBC driver did not return the expected number of row counts";
   }

   public final void entitiesUpdated(long entityUpdateCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000080: " + this.entitiesUpdated$str(), entityUpdateCount);
   }

   protected String entitiesUpdated$str() {
      return "Entities updated: %s";
   }

   public final void queryCachePuts(long queryCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000215: " + this.queryCachePuts$str(), queryCachePutCount);
   }

   protected String queryCachePuts$str() {
      return "Query cache puts: %s";
   }

   public final void sessionsOpened(long sessionOpenCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000242: " + this.sessionsOpened$str(), sessionOpenCount);
   }

   protected String sessionsOpened$str() {
      return "Sessions opened: %s";
   }

   public final void stoppingService() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000255: " + this.stoppingService$str(), new Object[0]);
   }

   protected String stoppingService$str() {
      return "Stopping service";
   }

   public final void unableToLocateCustomOptimizerClass(String type) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000321: " + this.unableToLocateCustomOptimizerClass$str(), type);
   }

   protected String unableToLocateCustomOptimizerClass$str() {
      return "Unable to interpret specified optimizer [%s], falling back to noop";
   }

   public final void unableToCloseSession(HibernateException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000294: " + this.unableToCloseSession$str(), new Object[0]);
   }

   protected String unableToCloseSession$str() {
      return "Could not close session";
   }

   public final void entityManagerFactoryAlreadyRegistered(String emfName, String propertyName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000436: " + this.entityManagerFactoryAlreadyRegistered$str(), emfName, propertyName);
   }

   protected String entityManagerFactoryAlreadyRegistered$str() {
      return "Entity manager factory name (%s) is already registered.  If entity manager will be clustered or passivated, specify a unique value for property '%s'";
   }

   public final void collectionsUpdated(long collectionUpdateCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000036: " + this.collectionsUpdated$str(), collectionUpdateCount);
   }

   protected String collectionsUpdated$str() {
      return "Collections updated: %s";
   }

   public final void cacheProvider(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000024: " + this.cacheProvider$str(), name);
   }

   protected String cacheProvider$str() {
      return "Cache provider: %s";
   }

   public final void illegalPropertySetterArgument(String name, String propertyName) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000123: " + this.illegalPropertySetterArgument$str(), name, propertyName);
   }

   protected String illegalPropertySetterArgument$str() {
      return "IllegalArgumentException in class: %s, setter method of property: %s";
   }

   public final void namedQueryError(String queryName, HibernateException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000177: " + this.namedQueryError$str(), queryName);
   }

   protected String namedQueryError$str() {
      return "Error in named query: %s";
   }

   public final void deprecatedForceDescriminatorAnnotation() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000062: " + this.deprecatedForceDescriminatorAnnotation$str(), new Object[0]);
   }

   protected String deprecatedForceDescriminatorAnnotation$str() {
      return "@ForceDiscriminator is deprecated use @DiscriminatorOptions instead.";
   }

   public final void timestampCachePuts(long updateTimestampsCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000433: " + this.timestampCachePuts$str(), updateTimestampsCachePutCount);
   }

   protected String timestampCachePuts$str() {
      return "update timestamps cache puts: %s";
   }

   public final void unableToReleaseCacheLock(CacheException ce) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000353: " + this.unableToReleaseCacheLock$str(), ce);
   }

   protected String unableToReleaseCacheLock$str() {
      return "Could not release a cache lock : %s";
   }

   public final void invalidJndiName(String name, JndiNameException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000135: " + this.invalidJndiName$str(), name);
   }

   protected String invalidJndiName$str() {
      return "Invalid JNDI name: %s";
   }

   public final void jdbcDriverNotSpecified(String driver) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000148: " + this.jdbcDriverNotSpecified$str(), driver);
   }

   protected String jdbcDriverNotSpecified$str() {
      return "No JDBC Driver class was specified by property %s";
   }

   public final void providerClassDeprecated(String providerClassName, String actualProviderClassName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000208: " + this.providerClassDeprecated$str(), providerClassName, actualProviderClassName);
   }

   protected String providerClassDeprecated$str() {
      return "%s has been deprecated in favor of %s; that provider will be used instead.";
   }

   public final void unableToDetermineLockModeValue(String hintName, Object value) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000311: " + this.unableToDetermineLockModeValue$str(), hintName, value);
   }

   protected String unableToDetermineLockModeValue$str() {
      return "Unable to determine lock mode value : %s -> %s";
   }

   public final void unableToCloseStreamError(IOException error) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000297: " + this.unableToCloseStreamError$str(), error);
   }

   protected String unableToCloseStreamError$str() {
      return "Could not close stream on hibernate.properties: %s";
   }

   public final void unableToCleanUpCallableStatement(SQLException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000281: " + this.unableToCleanUpCallableStatement$str(), new Object[0]);
   }

   protected String unableToCleanUpCallableStatement$str() {
      return "Unable to clean up callable statement";
   }

   public final void collectionsRemoved(long collectionRemoveCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000035: " + this.collectionsRemoved$str(), collectionRemoveCount);
   }

   protected String collectionsRemoved$str() {
      return "Collections removed: %s";
   }

   public final void unableToRunSchemaUpdate(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000366: " + this.unableToRunSchemaUpdate$str(), new Object[0]);
   }

   protected String unableToRunSchemaUpdate$str() {
      return "Error running schema update";
   }

   public final void setManagerLookupClass() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000426: " + this.setManagerLookupClass$str(), new Object[0]);
   }

   protected String setManagerLookupClass$str() {
      return "You should set hibernate.transaction.manager_lookup_class if cache is enabled";
   }

   public final String unableToQueryDatabaseMetadata() {
      String result = String.format("HHH000347: " + this.unableToQueryDatabaseMetadata$str());
      return result;
   }

   protected String unableToQueryDatabaseMetadata$str() {
      return "Unable to query java.sql.DatabaseMetaData";
   }

   public final void typeDefinedNoRegistrationKeys(BasicType type) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000269: " + this.typeDefinedNoRegistrationKeys$str(), type);
   }

   protected String typeDefinedNoRegistrationKeys$str() {
      return "Type [%s] defined no registration keys; ignoring";
   }

   public final void noAppropriateConnectionProvider() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000181: " + this.noAppropriateConnectionProvider$str(), new Object[0]);
   }

   protected String noAppropriateConnectionProvider$str() {
      return "No appropriate connection provider encountered, assuming application will be supplying connections";
   }

   public final void unableToReadOrInitHiValue(SQLException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000351: " + this.unableToReadOrInitHiValue$str(), new Object[0]);
   }

   protected String unableToReadOrInitHiValue$str() {
      return "Could not read or init a hi value";
   }

   public final void compositeIdClassDoesNotOverrideEquals(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000038: " + this.compositeIdClassDoesNotOverrideEquals$str(), name);
   }

   protected String compositeIdClassDoesNotOverrideEquals$str() {
      return "Composite-id class does not override equals(): %s";
   }

   public final void unableToCompleteSchemaValidation(SQLException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000300: " + this.unableToCompleteSchemaValidation$str(), new Object[0]);
   }

   protected String unableToCompleteSchemaValidation$str() {
      return "Could not complete schema validation";
   }

   public final void hsqldbSupportsOnlyReadCommittedIsolation() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000118: " + this.hsqldbSupportsOnlyReadCommittedIsolation$str(), new Object[0]);
   }

   protected String hsqldbSupportsOnlyReadCommittedIsolation$str() {
      return "HSQLDB supports only READ_UNCOMMITTED isolation";
   }

   public final void noDefaultConstructor(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000182: " + this.noDefaultConstructor$str(), name);
   }

   protected String noDefaultConstructor$str() {
      return "No default (no-argument) constructor for class: %s (class must be instantiated by Interceptor)";
   }

   public final void addingOverrideFor(String name, String name2) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000418: " + this.addingOverrideFor$str(), name, name2);
   }

   protected String addingOverrideFor$str() {
      return "Adding override for %s: %s";
   }

   public final void bytecodeProvider(String provider) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000021: " + this.bytecodeProvider$str(), provider);
   }

   protected String bytecodeProvider$str() {
      return "Bytecode provider name : %s";
   }

   public final void preparedStatementAlreadyInBatch(String sql) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000202: " + this.preparedStatementAlreadyInBatch$str(), sql);
   }

   protected String preparedStatementAlreadyInBatch$str() {
      return "PreparedStatement was already in the batch, [%s].";
   }

   public final void configuringFromFile(String file) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000042: " + this.configuringFromFile$str(), file);
   }

   protected String configuringFromFile$str() {
      return "Configuring from file: %s";
   }

   public final void schemaExportUnsuccessful(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000231: " + this.schemaExportUnsuccessful$str(), new Object[0]);
   }

   protected String schemaExportUnsuccessful$str() {
      return "Schema export unsuccessful";
   }

   public final void sqlExceptionEscapedProxy(SQLException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000246: " + this.sqlExceptionEscapedProxy$str(), new Object[0]);
   }

   protected String sqlExceptionEscapedProxy$str() {
      return "SQLException escaped proxy";
   }

   public final void transactionStrategy(String strategyClassName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000268: " + this.transactionStrategy$str(), strategyClassName);
   }

   protected String transactionStrategy$str() {
      return "Transaction strategy: %s";
   }

   public final void runningSchemaValidator() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000229: " + this.runningSchemaValidator$str(), new Object[0]);
   }

   protected String runningSchemaValidator$str() {
      return "Running schema validator";
   }

   public final void readingMappingsFromFile(String path) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000220: " + this.readingMappingsFromFile$str(), path);
   }

   protected String readingMappingsFromFile$str() {
      return "Reading mappings from file: %s";
   }

   public final String jdbcRollbackFailed() {
      String result = String.format("HHH000151: " + this.jdbcRollbackFailed$str());
      return result;
   }

   protected String jdbcRollbackFailed$str() {
      return "JDBC rollback failed";
   }

   public final void runningHbm2ddlSchemaExport() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000227: " + this.runningHbm2ddlSchemaExport$str(), new Object[0]);
   }

   protected String runningHbm2ddlSchemaExport$str() {
      return "Running hbm2ddl schema export";
   }

   public final void unableToLocateMBeanServer() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000332: " + this.unableToLocateMBeanServer$str(), new Object[0]);
   }

   protected String unableToLocateMBeanServer$str() {
      return "Unable to locate MBeanServer on JMX service shutdown";
   }

   public final void JavaSqlTypesMappedSameCodeMultipleTimes(int code, String old, String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000141: " + this.JavaSqlTypesMappedSameCodeMultipleTimes$str(), code, old, name);
   }

   protected String JavaSqlTypesMappedSameCodeMultipleTimes$str() {
      return "java.sql.Types mapped the same code [%s] multiple times; was [%s]; now [%s]";
   }

   public final void secondLevelCacheMisses(long secondLevelCacheMissCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000238: " + this.secondLevelCacheMisses$str(), secondLevelCacheMissCount);
   }

   protected String secondLevelCacheMisses$str() {
      return "Second level cache misses: %s";
   }

   public final void unableToObjectConnectionMetadata(SQLException error) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000339: " + this.unableToObjectConnectionMetadata$str(), error);
   }

   protected String unableToObjectConnectionMetadata$str() {
      return "Could not obtain connection metadata: %s";
   }

   public final void unknownIngresVersion(int databaseMajorVersion) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000383: " + this.unknownIngresVersion$str(), databaseMajorVersion);
   }

   protected String unknownIngresVersion$str() {
      return "Unknown Ingres major version [%s]; using Ingres 9.2 dialect";
   }

   public final void unableToCloseConnection(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000284: " + this.unableToCloseConnection$str(), new Object[0]);
   }

   protected String unableToCloseConnection$str() {
      return "Error closing connection";
   }

   public final void loadingCollectionKeyNotFound(CollectionKey collectionKey) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000159: " + this.loadingCollectionKeyNotFound$str(), collectionKey);
   }

   protected String loadingCollectionKeyNotFound$str() {
      return "In CollectionLoadContext#endLoadingCollections, localLoadingCollectionKeys contained [%s], but no LoadingCollectionEntry was found in loadContexts";
   }

   public final void batchContainedStatementsOnRelease() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000010: " + this.batchContainedStatementsOnRelease$str(), new Object[0]);
   }

   protected String batchContainedStatementsOnRelease$str() {
      return "On release of batch it still contained JDBC statements";
   }

   public final void jdbcIsolationLevel(String isolationLevelToString) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000149: " + this.jdbcIsolationLevel$str(), isolationLevelToString);
   }

   protected String jdbcIsolationLevel$str() {
      return "JDBC isolation level: %s";
   }

   public final void unableToPerformManagedFlush(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000346: " + this.unableToPerformManagedFlush$str(), message);
   }

   protected String unableToPerformManagedFlush$str() {
      return "Error during managed flush [%s]";
   }

   public final void unableToCloseInputStreamForResource(String resourceName, IOException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000288: " + this.unableToCloseInputStreamForResource$str(), resourceName);
   }

   protected String unableToCloseInputStreamForResource$str() {
      return "Could not close input stream for %s";
   }

   public final void usingReflectionOptimizer() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000406: " + this.usingReflectionOptimizer$str(), new Object[0]);
   }

   protected String usingReflectionOptimizer$str() {
      return "Using bytecode reflection optimizer";
   }

   public final void columns(Set keySet) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000037: " + this.columns$str(), keySet);
   }

   protected String columns$str() {
      return "Columns: %s";
   }

   public final void duplicateImport(String entityName, String rename) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000071: " + this.duplicateImport$str(), entityName, rename);
   }

   protected String duplicateImport$str() {
      return "Duplicate import: %s -> %s";
   }

   public final void recognizedObsoleteHibernateNamespace(String oldHibernateNamespace, String hibernateNamespace) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000223: " + this.recognizedObsoleteHibernateNamespace$str(), oldHibernateNamespace, hibernateNamespace);
   }

   protected String recognizedObsoleteHibernateNamespace$str() {
      return "Recognized obsolete hibernate namespace %s. Use namespace %s instead. Refer to Hibernate 3.6 Migration Guide!";
   }

   public final void unableToMarkForRollbackOnPersistenceException(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000337: " + this.unableToMarkForRollbackOnPersistenceException$str(), new Object[0]);
   }

   protected String unableToMarkForRollbackOnPersistenceException$str() {
      return "Unable to mark for rollback on PersistenceException: ";
   }

   public final void unableToDestroyQueryCache(String region, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000309: " + this.unableToDestroyQueryCache$str(), region, message);
   }

   protected String unableToDestroyQueryCache$str() {
      return "Unable to destroy query cache: %s: %s";
   }

   public final void callingJoinTransactionOnNonJtaEntityManager() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000027: " + this.callingJoinTransactionOnNonJtaEntityManager$str(), new Object[0]);
   }

   protected String callingJoinTransactionOnNonJtaEntityManager$str() {
      return "Calling joinTransaction() on a non JTA EntityManager";
   }

   public final void unableToParseMetadata(String packageName) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000344: " + this.unableToParseMetadata$str(), packageName);
   }

   protected String unableToParseMetadata$str() {
      return "Could not parse the package-level metadata [%s]";
   }

   public final void unableToSynchronizeDatabaseStateWithSession(HibernateException he) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000371: " + this.unableToSynchronizeDatabaseStateWithSession$str(), he);
   }

   protected String unableToSynchronizeDatabaseStateWithSession$str() {
      return "Could not synchronize database state with session: %s";
   }

   public final void cannotResolveNonNullableTransientDependencies(String transientEntityString, Set dependentEntityStrings, Set nonNullableAssociationPaths) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000437: " + this.cannotResolveNonNullableTransientDependencies$str(), transientEntityString, dependentEntityStrings, nonNullableAssociationPaths);
   }

   protected String cannotResolveNonNullableTransientDependencies$str() {
      return "Attempting to save one or more entities that have a non-nullable association with an unsaved transient entity. The unsaved transient entity must be saved in an operation prior to saving these dependent entities.\n\tUnsaved transient entity: (%s)\n\tDependent entities: (%s)\n\tNon-nullable association(s): (%s)";
   }

   public final void unableToStopHibernateService(Exception e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000368: " + this.unableToStopHibernateService$str(), new Object[0]);
   }

   protected String unableToStopHibernateService$str() {
      return "Exception while stopping service";
   }

   public final void firstOrMaxResultsSpecifiedWithCollectionFetch() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000104: " + this.firstOrMaxResultsSpecifiedWithCollectionFetch$str(), new Object[0]);
   }

   protected String firstOrMaxResultsSpecifiedWithCollectionFetch$str() {
      return "firstResult/maxResults specified with collection fetch; applying in memory!";
   }

   public final void factoryUnboundFromJndiName(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000097: " + this.factoryUnboundFromJndiName$str(), name);
   }

   protected String factoryUnboundFromJndiName$str() {
      return "Unbound factory from JNDI name: %s";
   }

   public final void cleaningUpConnectionPool(String url) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000030: " + this.cleaningUpConnectionPool$str(), url);
   }

   protected String cleaningUpConnectionPool$str() {
      return "Cleaning up connection pool [%s]";
   }

   public final void unableToBindValueToParameter(String nullSafeToString, int index, String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000278: " + this.unableToBindValueToParameter$str(), nullSafeToString, index, message);
   }

   protected String unableToBindValueToParameter$str() {
      return "Could not bind value '%s' to parameter: %s; %s";
   }

   public final void ignoringTableGeneratorConstraints(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000120: " + this.ignoringTableGeneratorConstraints$str(), name);
   }

   protected String ignoringTableGeneratorConstraints$str() {
      return "Ignoring unique constraints specified on table generator [%s]";
   }

   public final void secondLevelCachePuts(long secondLevelCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000239: " + this.secondLevelCachePuts$str(), secondLevelCachePutCount);
   }

   protected String secondLevelCachePuts$str() {
      return "Second level cache puts: %s";
   }

   public final void naturalIdCacheHits(long naturalIdCacheHitCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000439: " + this.naturalIdCacheHits$str(), naturalIdCacheHitCount);
   }

   protected String naturalIdCacheHits$str() {
      return "NaturalId cache hits: %s";
   }

   public final void readOnlyCacheConfiguredForMutableCollection(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000222: " + this.readOnlyCacheConfiguredForMutableCollection$str(), name);
   }

   protected String readOnlyCacheConfiguredForMutableCollection$str() {
      return "read-only cache configured for mutable collection [%s]";
   }

   public final void sqlWarning(int errorCode, String sqlState) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000247: " + this.sqlWarning$str(), errorCode, sqlState);
   }

   protected String sqlWarning$str() {
      return "SQL Error: %s, SQLState: %s";
   }

   public final void usingFollowOnLocking() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000444: " + this.usingFollowOnLocking$str(), new Object[0]);
   }

   protected String usingFollowOnLocking$str() {
      return "Encountered request for locking however dialect reports that database prefers locking be done in a separate select (follow-on locking); results will be locked after initial query executes";
   }

   public final String unableToLocateConfigFile(String path) {
      String result = String.format("HHH000330: " + this.unableToLocateConfigFile$str(), path);
      return result;
   }

   protected String unableToLocateConfigFile$str() {
      return "Unable to locate config file: %s";
   }

   public final void unableToObtainConnectionToQueryMetadata(String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000342: " + this.unableToObtainConnectionToQueryMetadata$str(), message);
   }

   protected String unableToObtainConnectionToQueryMetadata$str() {
      return "Could not obtain connection to query metadata : %s";
   }

   public final void entitiesLoaded(long entityLoadCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000079: " + this.entitiesLoaded$str(), entityLoadCount);
   }

   protected String entitiesLoaded$str() {
      return "Entities loaded: %s";
   }

   public final void unsupportedAfterStatement() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000390: " + this.unsupportedAfterStatement$str(), new Object[0]);
   }

   protected String unsupportedAfterStatement$str() {
      return "Overriding release mode as connection provider does not support 'after_statement'";
   }

   public final void embedXmlAttributesNoLongerSupported() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000446: " + this.embedXmlAttributesNoLongerSupported$str(), new Object[0]);
   }

   protected String embedXmlAttributesNoLongerSupported$str() {
      return "embed-xml attributes were intended to be used for DOM4J entity mode. Since that entity mode has been removed, embed-xml attributes are no longer supported and should be removed from mappings.";
   }

   public final void synchronizationAlreadyRegistered(Synchronization synchronization) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000259: " + this.synchronizationAlreadyRegistered$str(), synchronization);
   }

   protected String synchronizationAlreadyRegistered$str() {
      return "Synchronization [%s] was already registered";
   }

   public final void tableNotFound(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000262: " + this.tableNotFound$str(), name);
   }

   protected String tableNotFound$str() {
      return "Table not found: %s";
   }

   public final void deprecatedUuidGenerator(String name, String name2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000065: " + this.deprecatedUuidGenerator$str(), name, name2);
   }

   protected String deprecatedUuidGenerator$str() {
      return "DEPRECATED : use [%s] instead with custom [%s] implementation";
   }

   public final void entitiesDeleted(long entityDeleteCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000076: " + this.entitiesDeleted$str(), entityDeleteCount);
   }

   protected String entitiesDeleted$str() {
      return "Entities deleted: %s";
   }

   public final void parsingXmlErrorForFile(String file, int lineNumber, String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000197: " + this.parsingXmlErrorForFile$str(), file, lineNumber, message);
   }

   protected String parsingXmlErrorForFile$str() {
      return "Error parsing XML: %s(%s) %s";
   }

   public final void unableToTransformClass(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000373: " + this.unableToTransformClass$str(), message);
   }

   protected String unableToTransformClass$str() {
      return "Unable to transform class: %s";
   }

   public final void version(String versionString) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000412: " + this.version$str(), versionString);
   }

   protected String version$str() {
      return "Hibernate Core {%s}";
   }

   public final void configuringFromResource(String resource) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000043: " + this.configuringFromResource$str(), resource);
   }

   protected String configuringFromResource$str() {
      return "Configuring from resource: %s";
   }

   public final void containerProvidingNullPersistenceUnitRootUrl() {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000050: " + this.containerProvidingNullPersistenceUnitRootUrl$str(), new Object[0]);
   }

   protected String containerProvidingNullPersistenceUnitRootUrl$str() {
      return "Container is providing a null PersistenceUnitRootUrl: discovery impossible";
   }

   public final void unableToReleaseContext(String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000354: " + this.unableToReleaseContext$str(), message);
   }

   protected String unableToReleaseContext$str() {
      return "Unable to release initial context: %s";
   }

   public final void resolvedSqlTypeDescriptorForDifferentSqlCode(String name, String valueOf, String name2, String valueOf2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000419: " + this.resolvedSqlTypeDescriptorForDifferentSqlCode$str(), new Object[]{name, valueOf, name2, valueOf2});
   }

   protected String resolvedSqlTypeDescriptorForDifferentSqlCode$str() {
      return "Resolved SqlTypeDescriptor is for a different SQL code. %s has sqlCode=%s; type override %s has sqlCode=%s";
   }

   public final void unknownBytecodeProvider(String providerName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000382: " + this.unknownBytecodeProvider$str(), providerName);
   }

   protected String unknownBytecodeProvider$str() {
      return "unrecognized bytecode provider [%s], using javassist by default";
   }

   public final void entitiesInserted(long entityInsertCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000078: " + this.entitiesInserted$str(), entityInsertCount);
   }

   protected String entitiesInserted$str() {
      return "Entities inserted: %s";
   }

   public final void unableToRetrieveCache(String namespace, String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000361: " + this.unableToRetrieveCache$str(), namespace, message);
   }

   protected String unableToRetrieveCache$str() {
      return "Unable to retreive cache from JNDI [%s]: %s";
   }

   public final void honoringOptimizerSetting(String none, String incrementParam, int incrementSize) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000116: " + this.honoringOptimizerSetting$str(), none, incrementParam, incrementSize);
   }

   protected String honoringOptimizerSetting$str() {
      return "Config specified explicit optimizer of [%s], but [%s=%s; honoring optimizer setting";
   }

   public final void runningHbm2ddlSchemaUpdate() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000228: " + this.runningHbm2ddlSchemaUpdate$str(), new Object[0]);
   }

   protected String runningHbm2ddlSchemaUpdate$str() {
      return "Running hbm2ddl schema update";
   }

   public final void unableToCleanUpPreparedStatement(SQLException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000282: " + this.unableToCleanUpPreparedStatement$str(), new Object[0]);
   }

   protected String unableToCleanUpPreparedStatement$str() {
      return "Unable to clean up prepared statement";
   }

   public final void unableToClosePooledConnection(SQLException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000293: " + this.unableToClosePooledConnection$str(), new Object[0]);
   }

   protected String unableToClosePooledConnection$str() {
      return "Problem closing pooled connection";
   }

   public final void naturalIdCachePuts(long naturalIdCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000438: " + this.naturalIdCachePuts$str(), naturalIdCachePutCount);
   }

   protected String naturalIdCachePuts$str() {
      return "NaturalId cache puts: %s";
   }

   public final void exceptionHeaderNotFound(String exceptionHeader, String metaInfOrmXml) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000086: " + this.exceptionHeaderNotFound$str(), exceptionHeader, metaInfOrmXml);
   }

   protected String exceptionHeaderNotFound$str() {
      return "%s No %s found";
   }

   public final void immutableAnnotationOnNonRoot(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000124: " + this.immutableAnnotationOnNonRoot$str(), className);
   }

   protected String immutableAnnotationOnNonRoot$str() {
      return "@Immutable used on a non root entity: ignored for %s";
   }

   public final void deprecatedTransactionManagerStrategy(String name, String transactionManagerStrategy, String name2, String jtaPlatform) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000427: " + this.deprecatedTransactionManagerStrategy$str(), new Object[]{name, transactionManagerStrategy, name2, jtaPlatform});
   }

   protected String deprecatedTransactionManagerStrategy$str() {
      return "Using deprecated %s strategy [%s], use newer %s strategy instead [%s]";
   }

   public final void processingPersistenceUnitInfoName(String persistenceUnitName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000204: " + this.processingPersistenceUnitInfoName$str(), persistenceUnitName);
   }

   protected String processingPersistenceUnitInfoName$str() {
      return "Processing PersistenceUnitInfo [\n\tname: %s\n\t...]";
   }

   public final void ignoringUnrecognizedQueryHint(String hintName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000121: " + this.ignoringUnrecognizedQueryHint$str(), hintName);
   }

   protected String ignoringUnrecognizedQueryHint$str() {
      return "Ignoring unrecognized query hint [%s]";
   }

   public final void unableToConstructCurrentSessionContext(String impl, Throwable e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000302: " + this.unableToConstructCurrentSessionContext$str(), impl);
   }

   protected String unableToConstructCurrentSessionContext$str() {
      return "Unable to construct current session context [%s]";
   }

   public final void unableToCloseInputStream(IOException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000287: " + this.unableToCloseInputStream$str(), new Object[0]);
   }

   protected String unableToCloseInputStream$str() {
      return "Could not close input stream";
   }

   public final void usingDialect(Dialect dialect) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000400: " + this.usingDialect$str(), dialect);
   }

   protected String usingDialect$str() {
      return "Using dialect: %s";
   }

   public final void autoFlushWillNotWork() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000008: " + this.autoFlushWillNotWork$str(), new Object[0]);
   }

   protected String autoFlushWillNotWork$str() {
      return "JTASessionContext being used with JDBCTransactionFactory; auto-flush will not operate correctly with getCurrentSession()";
   }

   public final void unableToStopService(Class class1, String string) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000369: " + this.unableToStopService$str(), class1, string);
   }

   protected String unableToStopService$str() {
      return "Error stopping service [%s] : %s";
   }

   public final void usingTimestampWorkaround() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000408: " + this.usingTimestampWorkaround$str(), new Object[0]);
   }

   protected String usingTimestampWorkaround$str() {
      return "Using workaround for JVM bug in java.sql.Timestamp";
   }

   public final void unableToRetrieveTypeInfoResultSet(String string) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000362: " + this.unableToRetrieveTypeInfoResultSet$str(), string);
   }

   protected String unableToRetrieveTypeInfoResultSet$str() {
      return "Unable to retrieve type info result set : %s";
   }

   public final void processEqualityExpression() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000203: " + this.processEqualityExpression$str(), new Object[0]);
   }

   protected String processEqualityExpression$str() {
      return "processEqualityExpression() : No expression to process!";
   }

   public final void unableToReleaseTypeInfoResultSet() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000357: " + this.unableToReleaseTypeInfoResultSet$str(), new Object[0]);
   }

   protected String unableToReleaseTypeInfoResultSet$str() {
      return "Unable to release type info result set";
   }

   public final void duplicateJoins(String entityName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000072: " + this.duplicateJoins$str(), entityName);
   }

   protected String duplicateJoins$str() {
      return "Duplicate joins for class: %s";
   }

   public final void propertyNotFound(String property) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000207: " + this.propertyNotFound$str(), property);
   }

   protected String propertyNotFound$str() {
      return "Property %s not found in class but described in <mapping-file/> (possible typo error)";
   }

   public final void rdmsOs2200Dialect() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000218: " + this.rdmsOs2200Dialect$str(), new Object[0]);
   }

   protected String rdmsOs2200Dialect$str() {
      return "RDMSOS2200Dialect version: 1.0";
   }

   public final void usingUuidHexGenerator(String name, String name2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000409: " + this.usingUuidHexGenerator$str(), name, name2);
   }

   protected String usingUuidHexGenerator$str() {
      return "Using %s which does not generate IETF RFC 4122 compliant UUID values; consider using %s instead";
   }

   public final void unableToCloseJar(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000290: " + this.unableToCloseJar$str(), message);
   }

   protected String unableToCloseJar$str() {
      return "Could not close jar: %s";
   }

   public final void duplicateGeneratorName(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000069: " + this.duplicateGeneratorName$str(), name);
   }

   protected String duplicateGeneratorName$str() {
      return "Duplicate generator name %s";
   }

   public final void unableToCloseOutputStream(IOException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000292: " + this.unableToCloseOutputStream$str(), new Object[0]);
   }

   protected String unableToCloseOutputStream$str() {
      return "IOException occurred closing output stream";
   }

   public final void pooledOptimizerReportedInitialValue(IntegralDataTypeHolder value) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000201: " + this.pooledOptimizerReportedInitialValue$str(), value);
   }

   protected String pooledOptimizerReportedInitialValue$str() {
      return "Pooled optimizer source reported [%s] as the initial value; use of 1 or greater highly recommended";
   }

   public final String unableToDetermineTransactionStatus() {
      String result = String.format("HHH000312: " + this.unableToDetermineTransactionStatus$str());
      return result;
   }

   protected String unableToDetermineTransactionStatus$str() {
      return "Could not determine transaction status";
   }

   public final void sortAnnotationIndexedCollection() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000244: " + this.sortAnnotationIndexedCollection$str(), new Object[0]);
   }

   protected String sortAnnotationIndexedCollection$str() {
      return "@Sort not allowed for an indexed collection, annotation ignored.";
   }

   public final void entitiesFetched(long entityFetchCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000077: " + this.entitiesFetched$str(), entityFetchCount);
   }

   protected String entitiesFetched$str() {
      return "Entities fetched (minimize this): %s";
   }

   public final void compositeIdClassDoesNotOverrideHashCode(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000039: " + this.compositeIdClassDoesNotOverrideHashCode$str(), name);
   }

   protected String compositeIdClassDoesNotOverrideHashCode$str() {
      return "Composite-id class does not override hashCode(): %s";
   }

   public final void entityAnnotationOnNonRoot(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000081: " + this.entityAnnotationOnNonRoot$str(), className);
   }

   protected String entityAnnotationOnNonRoot$str() {
      return "@org.hibernate.annotations.Entity used on a non root entity: ignored for %s";
   }

   public final void unsupportedInitialValue(String propertyName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000392: " + this.unsupportedInitialValue$str(), propertyName);
   }

   protected String unsupportedInitialValue$str() {
      return "Hibernate does not support SequenceGenerator.initialValue() unless '%s' set";
   }

   public final void entityMappedAsNonAbstract(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000084: " + this.entityMappedAsNonAbstract$str(), name);
   }

   protected String entityMappedAsNonAbstract$str() {
      return "Entity [%s] is abstract-class/interface explicitly mapped as non-abstract; be sure to supply entity-names";
   }

   public final void parsingXmlWarning(int lineNumber, String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000198: " + this.parsingXmlWarning$str(), lineNumber, message);
   }

   protected String parsingXmlWarning$str() {
      return "Warning parsing XML (%s) : %s";
   }

   public final void unableToCloseStream(IOException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000296: " + this.unableToCloseStream$str(), new Object[0]);
   }

   protected String unableToCloseStream$str() {
      return "IOException occurred closing stream";
   }

   public final void unableToBuildEnhancementMetamodel(String className) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000279: " + this.unableToBuildEnhancementMetamodel$str(), className);
   }

   protected String unableToBuildEnhancementMetamodel$str() {
      return "Unable to build enhancement metamodel for %s";
   }

   public final void hql(String hql, Long valueOf, Long valueOf2) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000117: " + this.hql$str(), hql, valueOf, valueOf2);
   }

   protected String hql$str() {
      return "HQL: %s, time: %sms, rows: %s";
   }

   public final void maxQueryTime(long queryExecutionMaxTime) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000173: " + this.maxQueryTime$str(), queryExecutionMaxTime);
   }

   protected String maxQueryTime$str() {
      return "Max query time: %sms";
   }

   public final String unableToReadHiValue(String tableName) {
      String result = String.format("HHH000350: " + this.unableToReadHiValue$str(), tableName);
      return result;
   }

   protected String unableToReadHiValue$str() {
      return "Could not read a hi value - you need to populate the table: %s";
   }

   public final void persistenceProviderCallerDoesNotImplementEjb3SpecCorrectly() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000200: " + this.persistenceProviderCallerDoesNotImplementEjb3SpecCorrectly$str(), new Object[0]);
   }

   protected String persistenceProviderCallerDoesNotImplementEjb3SpecCorrectly$str() {
      return "Persistence provider caller does not implement the EJB3 spec correctly.PersistenceUnitInfo.getNewTempClassLoader() is null.";
   }

   public final void unableToBindFactoryToJndi(JndiException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000277: " + this.unableToBindFactoryToJndi$str(), new Object[0]);
   }

   protected String unableToBindFactoryToJndi$str() {
      return "Could not bind factory to JNDI";
   }

   public final void writeLocksNotSupported(String entityName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000416: " + this.writeLocksNotSupported$str(), entityName);
   }

   protected String writeLocksNotSupported$str() {
      return "Write locks via update not supported for non-versioned entities [%s]";
   }

   public final void incompleteMappingMetadataCacheProcessing() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000125: " + this.incompleteMappingMetadataCacheProcessing$str(), new Object[0]);
   }

   protected String incompleteMappingMetadataCacheProcessing$str() {
      return "Mapping metadata cache was not completely processed";
   }

   public final void noSessionFactoryWithJndiName(String sfJNDIName, NameNotFoundException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000184: " + this.noSessionFactoryWithJndiName$str(), sfJNDIName);
   }

   protected String noSessionFactoryWithJndiName$str() {
      return "No session factory with JNDI name %s";
   }

   public final void queryCacheHits(long queryCacheHitCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000213: " + this.queryCacheHits$str(), queryCacheHitCount);
   }

   protected String queryCacheHits$str() {
      return "Query cache hits: %s";
   }

   public final void disablingContextualLOBCreationSinceOldJdbcVersion(int jdbcMajorVersion) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000423: " + this.disablingContextualLOBCreationSinceOldJdbcVersion$str(), jdbcMajorVersion);
   }

   protected String disablingContextualLOBCreationSinceOldJdbcVersion$str() {
      return "Disabling contextual LOB creation as JDBC driver reported JDBC version [%s] less than 4";
   }

   public final void unableToConstructSqlExceptionConverter(Throwable t) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000303: " + this.unableToConstructSqlExceptionConverter$str(), t);
   }

   protected String unableToConstructSqlExceptionConverter$str() {
      return "Unable to construct instance of specified SQLExceptionConverter : %s";
   }

   public final void undeterminedH2Version() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000431: " + this.undeterminedH2Version$str(), new Object[0]);
   }

   protected String undeterminedH2Version$str() {
      return "Unable to determine H2 database version, certain features may not work";
   }

   public final void unableToCreateProxyFactory(String entityName, HibernateException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000305: " + this.unableToCreateProxyFactory$str(), entityName);
   }

   protected String unableToCreateProxyFactory$str() {
      return "Could not create proxy factory for:%s";
   }

   public final void deprecatedOracleDialect() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000064: " + this.deprecatedOracleDialect$str(), new Object[0]);
   }

   protected String deprecatedOracleDialect$str() {
      return "The OracleDialect dialect has been deprecated; use Oracle8iDialect instead";
   }

   public final void definingFlushBeforeCompletionIgnoredInHem(String flushBeforeCompletion) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000059: " + this.definingFlushBeforeCompletionIgnoredInHem$str(), flushBeforeCompletion);
   }

   protected String definingFlushBeforeCompletionIgnoredInHem$str() {
      return "Defining %s=true ignored in HEM";
   }

   public final void settersOfLazyClassesCannotBeFinal(String entityName, String name) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000243: " + this.settersOfLazyClassesCannotBeFinal$str(), entityName, name);
   }

   protected String settersOfLazyClassesCannotBeFinal$str() {
      return "Setters of lazy classes cannot be final: %s.%s";
   }

   public final void entityIdentifierValueBindingExists(String name) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000429: " + this.entityIdentifierValueBindingExists$str(), name);
   }

   protected String entityIdentifierValueBindingExists$str() {
      return "Setting entity-identifier value binding where one already existed : %s.";
   }

   public final void closing() {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000031: " + this.closing$str(), new Object[0]);
   }

   protected String closing$str() {
      return "Closing";
   }

   public final void unableToMarkForRollbackOnTransientObjectException(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000338: " + this.unableToMarkForRollbackOnTransientObjectException$str(), new Object[0]);
   }

   protected String unableToMarkForRollbackOnTransientObjectException$str() {
      return "Unable to mark for rollback on TransientObjectException: ";
   }

   public final void jndiInitialContextProperties(Hashtable hash) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000154: " + this.jndiInitialContextProperties$str(), hash);
   }

   protected String jndiInitialContextProperties$str() {
      return "JNDI InitialContext properties:%s";
   }

   public final void unableToSetTransactionToRollbackOnly(SystemException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000367: " + this.unableToSetTransactionToRollbackOnly$str(), new Object[0]);
   }

   protected String unableToSetTransactionToRollbackOnly$str() {
      return "Could not set transaction to rollback only";
   }

   public final String unableToDetermineTransactionStatusAfterCommit() {
      String result = String.format("HHH000313: " + this.unableToDetermineTransactionStatusAfterCommit$str());
      return result;
   }

   protected String unableToDetermineTransactionStatusAfterCommit$str() {
      return "Could not determine transaction status after commit";
   }

   public final void startingUpdateTimestampsCache(String region) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000250: " + this.startingUpdateTimestampsCache$str(), region);
   }

   protected String startingUpdateTimestampsCache$str() {
      return "Starting update timestamps cache at region: %s";
   }

   public final String unableToRollbackJta() {
      String result = String.format("HHH000365: " + this.unableToRollbackJta$str());
      return result;
   }

   protected String unableToRollbackJta$str() {
      return "JTA rollback failed";
   }

   public final void unableToLogSqlWarnings(SQLException sqle) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000335: " + this.unableToLogSqlWarnings$str(), sqle);
   }

   protected String unableToLogSqlWarnings$str() {
      return "Unable to log SQLWarnings : %s";
   }

   public final void failSafeEntitiesCleanup(EntityLoadContext entityLoadContext) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000101: " + this.failSafeEntitiesCleanup$str(), entityLoadContext);
   }

   protected String failSafeEntitiesCleanup$str() {
      return "Fail-safe cleanup (entities) : %s";
   }

   public final void unableToJoinTransaction(String transactionStrategy) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000326: " + this.unableToJoinTransaction$str(), transactionStrategy);
   }

   protected String unableToJoinTransaction$str() {
      return "Cannot join transaction: do not override %s";
   }

   public final void unsupportedMultiTableBulkHqlJpaql(int majorVersion, int minorVersion, int buildId) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000393: " + this.unsupportedMultiTableBulkHqlJpaql$str(), majorVersion, minorVersion, buildId);
   }

   protected String unsupportedMultiTableBulkHqlJpaql$str() {
      return "The %s.%s.%s version of H2 implements temporary table creation such that it commits current transaction; multi-table, bulk hql/jpaql will not work properly";
   }

   public final void noColumnsSpecifiedForIndex(String indexName, String tableName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000432: " + this.noColumnsSpecifiedForIndex$str(), indexName, tableName);
   }

   protected String noColumnsSpecifiedForIndex$str() {
      return "There were not column names specified for index %s on table %s";
   }

   public final void unableToReadColumnValueFromResultSet(String name, String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000349: " + this.unableToReadColumnValueFromResultSet$str(), name, message);
   }

   protected String unableToReadColumnValueFromResultSet$str() {
      return "Could not read column value from result set: %s; %s";
   }

   public final void illegalPropertyGetterArgument(String name, String propertyName) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000122: " + this.illegalPropertyGetterArgument$str(), name, propertyName);
   }

   protected String illegalPropertyGetterArgument$str() {
      return "IllegalArgumentException in class: %s, getter method of property: %s";
   }

   public final void connectionsObtained(long connectCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000048: " + this.connectionsObtained$str(), connectCount);
   }

   protected String connectionsObtained$str() {
      return "Connections obtained: %s";
   }

   public final void transactionStartedOnNonRootSession() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000267: " + this.transactionStartedOnNonRootSession$str(), new Object[0]);
   }

   protected String transactionStartedOnNonRootSession$str() {
      return "Transaction started on non-root session";
   }

   public final void scopingTypesToSessionFactoryAfterAlreadyScoped(SessionFactoryImplementor factory, SessionFactoryImplementor factory2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000233: " + this.scopingTypesToSessionFactoryAfterAlreadyScoped$str(), factory, factory2);
   }

   protected String scopingTypesToSessionFactoryAfterAlreadyScoped$str() {
      return "Scoping types to session factory %s after already scoped %s";
   }

   public final void unableToConfigureSqlExceptionConverter(HibernateException e) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000301: " + this.unableToConfigureSqlExceptionConverter$str(), e);
   }

   protected String unableToConfigureSqlExceptionConverter$str() {
      return "Unable to configure SQLExceptionConverter : %s";
   }

   public final void unableToInstantiateConfiguredSchemaNameResolver(String resolverClassName, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000320: " + this.unableToInstantiateConfiguredSchemaNameResolver$str(), resolverClassName, message);
   }

   protected String unableToInstantiateConfiguredSchemaNameResolver$str() {
      return "Unable to instantiate configured schema name resolver [%s] %s";
   }

   public final void unableToExecuteBatch(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000315: " + this.unableToExecuteBatch$str(), message);
   }

   protected String unableToExecuteBatch$str() {
      return "Exception executing batch [%s]";
   }

   public final void unsupportedOracleVersion() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000394: " + this.unsupportedOracleVersion$str(), new Object[0]);
   }

   protected String unsupportedOracleVersion$str() {
      return "Oracle 11g is not yet fully supported; using Oracle 10g dialect";
   }

   public final void autoCommitMode(boolean autocommit) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000006: " + this.autoCommitMode$str(), autocommit);
   }

   protected String autoCommitMode$str() {
      return "Autocommit mode: %s";
   }

   public final void secondLevelCacheHits(long secondLevelCacheHitCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000237: " + this.secondLevelCacheHits$str(), secondLevelCacheHitCount);
   }

   protected String secondLevelCacheHits$str() {
      return "Second level cache hits: %s";
   }

   public final void usingDefaultIdGeneratorSegmentValue(String tableName, String segmentColumnName, String defaultToUse) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000398: " + this.usingDefaultIdGeneratorSegmentValue$str(), tableName, segmentColumnName, defaultToUse);
   }

   protected String usingDefaultIdGeneratorSegmentValue$str() {
      return "Explicit segment value for id generator [%s.%s] suggested; using default [%s]";
   }

   public final void unableToRollbackConnection(Exception ignore) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000363: " + this.unableToRollbackConnection$str(), ignore);
   }

   protected String unableToRollbackConnection$str() {
      return "Unable to rollback connection on exception [%s]";
   }

   public final void factoryJndiRename(String oldName, String newName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000096: " + this.factoryJndiRename$str(), oldName, newName);
   }

   protected String factoryJndiRename$str() {
      return "A factory was renamed from [%s] to [%s] in JNDI";
   }

   public final void unableToRollbackIsolatedTransaction(Exception e, Exception ignore) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000364: " + this.unableToRollbackIsolatedTransaction$str(), e, ignore);
   }

   protected String unableToRollbackIsolatedTransaction$str() {
      return "Unable to rollback isolated transaction on error [%s] : [%s]";
   }

   public final void usingAstQueryTranslatorFactory() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000397: " + this.usingAstQueryTranslatorFactory$str(), new Object[0]);
   }

   protected String usingAstQueryTranslatorFactory$str() {
      return "Using ASTQueryTranslatorFactory";
   }

   public final void aliasSpecificLockingWithFollowOnLocking(LockMode lockMode) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000445: " + this.aliasSpecificLockingWithFollowOnLocking$str(), lockMode);
   }

   protected String aliasSpecificLockingWithFollowOnLocking$str() {
      return "Alias-specific lock modes requested, which is not currently supported with follow-on locking; all acquired locks will be [%s]";
   }

   public final void unableToDestroyCache(String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000308: " + this.unableToDestroyCache$str(), message);
   }

   protected String unableToDestroyCache$str() {
      return "Unable to destroy cache: %s";
   }

   public final void unableToReadClass(String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000348: " + this.unableToReadClass$str(), message);
   }

   protected String unableToReadClass$str() {
      return "Unable to read class: %s";
   }

   public final void needsLimit() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000180: " + this.needsLimit$str(), new Object[0]);
   }

   protected String needsLimit$str() {
      return "FirstResult/maxResults specified on polymorphic query; applying in memory!";
   }

   public final void fetchingDatabaseMetadata() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000102: " + this.fetchingDatabaseMetadata$str(), new Object[0]);
   }

   protected String fetchingDatabaseMetadata$str() {
      return "Fetching database metadata";
   }

   public final void readingCachedMappings(File cachedFile) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000219: " + this.readingCachedMappings$str(), cachedFile);
   }

   protected String readingCachedMappings$str() {
      return "Reading mappings from cache file: %s";
   }

   public final void statementsClosed(long closeStatementCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000252: " + this.statementsClosed$str(), closeStatementCount);
   }

   protected String statementsClosed$str() {
      return "Statements closed: %s";
   }

   public final void unableToWrapResultSet(SQLException e) {
      this.log.logf(FQCN, Level.INFO, e, "HHH000377: " + this.unableToWrapResultSet$str(), new Object[0]);
   }

   protected String unableToWrapResultSet$str() {
      return "Error wrapping result set";
   }

   public final void unableToBuildSessionFactoryUsingMBeanClasspath(String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000280: " + this.unableToBuildSessionFactoryUsingMBeanClasspath$str(), message);
   }

   protected String unableToBuildSessionFactoryUsingMBeanClasspath$str() {
      return "Could not build SessionFactory using the MBean classpath - will try again using client classpath: %s";
   }

   public final void naturalIdMaxQueryTime(long naturalIdQueryExecutionMaxTime) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000441: " + this.naturalIdMaxQueryTime$str(), naturalIdQueryExecutionMaxTime);
   }

   protected String naturalIdMaxQueryTime$str() {
      return "Max NaturalId query time: %sms";
   }

   public final void forcingContainerResourceCleanup() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000106: " + this.forcingContainerResourceCleanup$str(), new Object[0]);
   }

   protected String forcingContainerResourceCleanup$str() {
      return "Forcing container resource cleanup on transaction completion";
   }

   public final void timestampCacheHits(long updateTimestampsCachePutCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000434: " + this.timestampCacheHits$str(), updateTimestampsCachePutCount);
   }

   protected String timestampCacheHits$str() {
      return "update timestamps cache hits: %s";
   }

   public final void willNotRegisterListeners() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000414: " + this.willNotRegisterListeners$str(), new Object[0]);
   }

   protected String willNotRegisterListeners$str() {
      return "Property hibernate.search.autoregister_listeners is set to false. No attempt will be made to register Hibernate Search event listeners.";
   }

   public final void unableToCompleteSchemaUpdate(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000299: " + this.unableToCompleteSchemaUpdate$str(), new Object[0]);
   }

   protected String unableToCompleteSchemaUpdate$str() {
      return "Could not complete schema update";
   }

   public final void unableToLoadProperties() {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000329: " + this.unableToLoadProperties$str(), new Object[0]);
   }

   protected String unableToLoadProperties$str() {
      return "Problem loading properties from hibernate.properties";
   }

   public final void creatingSubcontextInfo(String intermediateContextName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000053: " + this.creatingSubcontextInfo$str(), intermediateContextName);
   }

   protected String creatingSubcontextInfo$str() {
      return "Creating subcontext: %s";
   }

   public final void gettersOfLazyClassesCannotBeFinal(String entityName, String name) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000112: " + this.gettersOfLazyClassesCannotBeFinal$str(), entityName, name);
   }

   protected String gettersOfLazyClassesCannotBeFinal$str() {
      return "Getters of lazy classes cannot be final: %s.%s";
   }

   public final void expired(Object key) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000092: " + this.expired$str(), key);
   }

   protected String expired$str() {
      return "An item was expired by the cache while it was locked (increase your cache timeout): %s";
   }

   public final String unableToCommitJta() {
      String result = String.format("HHH000298: " + this.unableToCommitJta$str());
      return result;
   }

   protected String unableToCommitJta$str() {
      return "JTA commit failed";
   }

   public final void unsuccessfulCreate(String string) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000389: " + this.unsuccessfulCreate$str(), string);
   }

   protected String unsuccessfulCreate$str() {
      return "Unsuccessful: %s";
   }

   public final void unknownSqlServerVersion(int databaseMajorVersion) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000385: " + this.unknownSqlServerVersion$str(), databaseMajorVersion);
   }

   protected String unknownSqlServerVersion$str() {
      return "Unknown Microsoft SQL Server major version [%s] using SQL Server 2000 dialect";
   }

   public final void unableToBindEjb3ConfigurationToJndi(JndiException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000276: " + this.unableToBindEjb3ConfigurationToJndi$str(), new Object[0]);
   }

   protected String unableToBindEjb3ConfigurationToJndi$str() {
      return "Could not bind Ejb3Configuration to JNDI";
   }

   public final void deprecatedDerbyDialect() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000430: " + this.deprecatedDerbyDialect$str(), new Object[0]);
   }

   protected String deprecatedDerbyDialect$str() {
      return "The DerbyDialect dialect has been deprecated; use one of the version-specific dialects instead";
   }

   public final void unableToCopySystemProperties() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000304: " + this.unableToCopySystemProperties$str(), new Object[0]);
   }

   protected String unableToCopySystemProperties$str() {
      return "Could not copy system properties, system properties will be ignored";
   }

   public final void invalidOnDeleteAnnotation(String entityName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000136: " + this.invalidOnDeleteAnnotation$str(), entityName);
   }

   protected String invalidOnDeleteAnnotation$str() {
      return "Inapropriate use of @OnDelete on entity, annotation ignored: %s";
   }

   public final void validatorNotFound() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000410: " + this.validatorNotFound$str(), new Object[0]);
   }

   protected String validatorNotFound$str() {
      return "Hibernate Validator not found: ignoring";
   }

   public final void parsingXmlError(int lineNumber, String message) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000196: " + this.parsingXmlError$str(), lineNumber, message);
   }

   protected String parsingXmlError$str() {
      return "Error parsing XML (%s) : %s";
   }

   public final void unableToCloseIterator(SQLException e) {
      this.log.logf(FQCN, Level.INFO, e, "HHH000289: " + this.unableToCloseIterator$str(), new Object[0]);
   }

   protected String unableToCloseIterator$str() {
      return "Unable to close iterator";
   }

   public final void foundMappingDocument(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000109: " + this.foundMappingDocument$str(), name);
   }

   protected String foundMappingDocument$str() {
      return "Found mapping document in jar: %s";
   }

   public final void containsJoinFetchedCollection(String role) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000051: " + this.containsJoinFetchedCollection$str(), role);
   }

   protected String containsJoinFetchedCollection$str() {
      return "Ignoring bag join fetch [%s] due to prior collection join fetch";
   }

   public final void unableToRemoveBagJoinFetch() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000358: " + this.unableToRemoveBagJoinFetch$str(), new Object[0]);
   }

   protected String unableToRemoveBagJoinFetch$str() {
      return "Unable to erase previously added bag join fetch";
   }

   public final void unableToObjectConnectionToQueryMetadata(SQLException error) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000340: " + this.unableToObjectConnectionToQueryMetadata$str(), error);
   }

   protected String unableToObjectConnectionToQueryMetadata$str() {
      return "Could not obtain connection to query metadata: %s";
   }

   public final void packageNotFound(String packageName) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000194: " + this.packageNotFound$str(), packageName);
   }

   protected String packageNotFound$str() {
      return "Package not found or wo package-info.java: %s";
   }

   public final void invalidPrimaryKeyJoinColumnAnnotation() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000137: " + this.invalidPrimaryKeyJoinColumnAnnotation$str(), new Object[0]);
   }

   protected String invalidPrimaryKeyJoinColumnAnnotation$str() {
      return "Root entity should not hold an PrimaryKeyJoinColum(s), will be ignored";
   }

   public final void invalidTableAnnotation(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000139: " + this.invalidTableAnnotation$str(), className);
   }

   protected String invalidTableAnnotation$str() {
      return "Illegal use of @Table in a subclass of a SINGLE_TABLE hierarchy: %s";
   }

   public final void unableToReleaseIsolatedConnection(Throwable ignore) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000356: " + this.unableToReleaseIsolatedConnection$str(), ignore);
   }

   protected String unableToReleaseIsolatedConnection$str() {
      return "Unable to release isolated connection [%s]";
   }

   public final void handlingTransientEntity() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000114: " + this.handlingTransientEntity$str(), new Object[0]);
   }

   protected String handlingTransientEntity$str() {
      return "Handling transient entity in delete processing";
   }

   public final void unableToUnbindFactoryFromJndi(JndiException e) {
      this.log.logf(FQCN, Level.WARN, e, "HHH000374: " + this.unableToUnbindFactoryFromJndi$str(), new Object[0]);
   }

   protected String unableToUnbindFactoryFromJndi$str() {
      return "Could not unbind factory from JNDI";
   }

   public final void jdbcAutoCommitFalseBreaksEjb3Spec(String autocommit) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000144: " + this.jdbcAutoCommitFalseBreaksEjb3Spec$str(), autocommit);
   }

   protected String jdbcAutoCommitFalseBreaksEjb3Spec$str() {
      return "%s = false breaks the EJB3 specification";
   }

   public final void renamedProperty(Object propertyName, Object newPropertyName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000225: " + this.renamedProperty$str(), propertyName, newPropertyName);
   }

   protected String renamedProperty$str() {
      return "Property [%s] has been renamed to [%s]; update your properties appropriately";
   }

   public final void unableToCloseInitialContext(String string) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000285: " + this.unableToCloseInitialContext$str(), string);
   }

   protected String unableToCloseInitialContext$str() {
      return "Error closing InitialContext [%s]";
   }

   public final void configuringFromUrl(URL url) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000044: " + this.configuringFromUrl$str(), url);
   }

   protected String configuringFromUrl$str() {
      return "Configuring from URL: %s";
   }

   public final void unableToResolveAggregateFunction(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000359: " + this.unableToResolveAggregateFunction$str(), name);
   }

   protected String unableToResolveAggregateFunction$str() {
      return "Could not resolve aggregate function [%s]; using standard definition";
   }

   public final void tooManyInExpressions(String dialectName, int limit, String paramName, int size) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000443: " + this.tooManyInExpressions$str(), new Object[]{dialectName, limit, paramName, size});
   }

   protected String tooManyInExpressions$str() {
      return "Dialect [%s] limits the number of elements in an IN predicate to %s entries.  However, the given parameter list [%s] contained %s entries, which will likely cause failures to execute the query in the database";
   }

   public final void cachedFileNotFound(String path, FileNotFoundException error) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000023: " + this.cachedFileNotFound$str(), path, error);
   }

   protected String cachedFileNotFound$str() {
      return "I/O reported cached file could not be found : %s : %s";
   }

   public final void proxoolProviderClassNotFound(String proxoolProviderClassName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000209: " + this.proxoolProviderClassNotFound$str(), proxoolProviderClassName);
   }

   protected String proxoolProviderClassNotFound$str() {
      return "proxool properties were encountered, but the %s provider class was not found on the classpath; these properties are going to be ignored.";
   }

   public final void unableToLocateConfiguredSchemaNameResolver(String resolverClassName, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000331: " + this.unableToLocateConfiguredSchemaNameResolver$str(), resolverClassName, message);
   }

   protected String unableToLocateConfiguredSchemaNameResolver$str() {
      return "Unable to locate configured schema name resolver class [%s] %s";
   }

   public final void invalidDiscriminatorAnnotation(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000133: " + this.invalidDiscriminatorAnnotation$str(), className);
   }

   protected String invalidDiscriminatorAnnotation$str() {
      return "Discriminator column has to be defined in the root entity, it will be ignored in subclass: %s";
   }

   public final void legacyTransactionManagerStrategy(String name, String jtaPlatform) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000428: " + this.legacyTransactionManagerStrategy$str(), name, jtaPlatform);
   }

   protected String legacyTransactionManagerStrategy$str() {
      return "Encountered legacy TransactionManagerLookup specified; convert to newer %s contract specified via %s setting";
   }

   public final void exceptionInAfterTransactionCompletionInterceptor(Throwable e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000087: " + this.exceptionInAfterTransactionCompletionInterceptor$str(), new Object[0]);
   }

   protected String exceptionInAfterTransactionCompletionInterceptor$str() {
      return "Exception in interceptor afterTransactionCompletion()";
   }

   public final void typeRegistrationOverridesPrevious(String key, Type old) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000270: " + this.typeRegistrationOverridesPrevious$str(), key, old);
   }

   protected String typeRegistrationOverridesPrevious$str() {
      return "Type registration [%s] overrides previous : %s";
   }

   public final void unableToInstantiateUuidGenerationStrategy(Exception ignore) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000325: " + this.unableToInstantiateUuidGenerationStrategy$str(), ignore);
   }

   protected String unableToInstantiateUuidGenerationStrategy$str() {
      return "Unable to instantiate UUID generation strategy class : %s";
   }

   public final void unableToDestroyUpdateTimestampsCache(String region, String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000310: " + this.unableToDestroyUpdateTimestampsCache$str(), region, message);
   }

   protected String unableToDestroyUpdateTimestampsCache$str() {
      return "Unable to destroy update timestamps cache: %s: %s";
   }

   public final void unableToLocateUuidGenerationStrategy(String strategyClassName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000334: " + this.unableToLocateUuidGenerationStrategy$str(), strategyClassName);
   }

   protected String unableToLocateUuidGenerationStrategy$str() {
      return "Unable to locate requested UUID generation strategy class : %s";
   }

   public final void overridingTransactionStrategyDangerous(String transactionStrategy) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000193: " + this.overridingTransactionStrategyDangerous$str(), transactionStrategy);
   }

   protected String overridingTransactionStrategyDangerous$str() {
      return "Overriding %s is dangerous, this might break the EJB3 specification implementation";
   }

   public final void successfulTransactions(long committedTransactionCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000258: " + this.successfulTransactions$str(), committedTransactionCount);
   }

   protected String successfulTransactions$str() {
      return "Successful transactions: %s";
   }

   public final void namingExceptionAccessingFactory(NamingException exception) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000178: " + this.namingExceptionAccessingFactory$str(), exception);
   }

   protected String namingExceptionAccessingFactory$str() {
      return "Naming exception occurred accessing factory: %s";
   }

   public final void unableToFindPersistenceXmlInClasspath() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000318: " + this.unableToFindPersistenceXmlInClasspath$str(), new Object[0]);
   }

   protected String unableToFindPersistenceXmlInClasspath$str() {
      return "Could not find any META-INF/persistence.xml file in the classpath";
   }

   public final void hydratingEntitiesCount(int size) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000119: " + this.hydratingEntitiesCount$str(), size);
   }

   protected String hydratingEntitiesCount$str() {
      return "On EntityLoadContext#clear, hydratingEntities contained [%s] entries";
   }

   public final void propertiesLoaded(Properties maskOut) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000205: " + this.propertiesLoaded$str(), maskOut);
   }

   protected String propertiesLoaded$str() {
      return "Loaded properties from resource hibernate.properties: %s";
   }

   public final void configuredSessionFactory(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000041: " + this.configuredSessionFactory$str(), name);
   }

   protected String configuredSessionFactory$str() {
      return "Configured SessionFactory: %s";
   }

   public final void propertiesNotFound() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000206: " + this.propertiesNotFound$str(), new Object[0]);
   }

   protected String propertiesNotFound$str() {
      return "hibernate.properties not found";
   }

   public final void unknownOracleVersion(int databaseMajorVersion) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000384: " + this.unknownOracleVersion$str(), databaseMajorVersion);
   }

   protected String unknownOracleVersion$str() {
      return "Unknown Oracle major version [%s]";
   }

   public final void hibernateConnectionPoolSize(int poolSize) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000115: " + this.hibernateConnectionPoolSize$str(), poolSize);
   }

   protected String hibernateConnectionPoolSize$str() {
      return "Hibernate connection pool size: %s";
   }

   public final void configuringFromXmlDocument() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000045: " + this.configuringFromXmlDocument$str(), new Object[0]);
   }

   protected String configuringFromXmlDocument$str() {
      return "Configuring from XML document";
   }

   public final void disablingContextualLOBCreation(String nonContextualLobCreation) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000421: " + this.disablingContextualLOBCreation$str(), nonContextualLobCreation);
   }

   protected String disablingContextualLOBCreation$str() {
      return "Disabling contextual LOB creation as %s is true";
   }

   public final void localLoadingCollectionKeysCount(int size) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000160: " + this.localLoadingCollectionKeysCount$str(), size);
   }

   protected String localLoadingCollectionKeysCount$str() {
      return "On CollectionLoadContext#cleanup, localLoadingCollectionKeys contained [%s] entries";
   }

   public final void tableFound(String string) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000261: " + this.tableFound$str(), string);
   }

   protected String tableFound$str() {
      return "Table found: %s";
   }

   public final void unableToCloseInputFiles(String name, IOException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000286: " + this.unableToCloseInputFiles$str(), name);
   }

   protected String unableToCloseInputFiles$str() {
      return "Error closing input files: %s";
   }

   public final void unableToInstantiateOptimizer(String type) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000322: " + this.unableToInstantiateOptimizer$str(), type);
   }

   protected String unableToInstantiateOptimizer$str() {
      return "Unable to instantiate specified optimizer [%s], falling back to noop";
   }

   public final void usingOldDtd() {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000404: " + this.usingOldDtd$str(), new Object[0]);
   }

   protected String usingOldDtd$str() {
      return "Don't use old DTDs, read the Hibernate 3.x Migration Guide!";
   }

   public final void collectionsRecreated(long collectionRecreateCount) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000034: " + this.collectionsRecreated$str(), collectionRecreateCount);
   }

   protected String collectionsRecreated$str() {
      return "Collections recreated: %s";
   }

   public final void updatingSchema() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000396: " + this.updatingSchema$str(), new Object[0]);
   }

   protected String updatingSchema$str() {
      return "Updating schema";
   }

   public final void unsupportedIngresVersion() {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000391: " + this.unsupportedIngresVersion$str(), new Object[0]);
   }

   protected String unsupportedIngresVersion$str() {
      return "Ingres 10 is not yet fully supported; using Ingres 9.3 dialect";
   }

   public final void unableToObtainInitialContext(NamingException e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000343: " + this.unableToObtainInitialContext$str(), new Object[0]);
   }

   protected String unableToObtainInitialContext$str() {
      return "Could not obtain initial context";
   }

   public final void failed(Throwable throwable) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000099: " + this.failed$str(), throwable);
   }

   protected String failed$str() {
      return "an assertion failure occured (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session): %s";
   }

   public final void unableToToggleAutoCommit(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000372: " + this.unableToToggleAutoCommit$str(), new Object[0]);
   }

   protected String unableToToggleAutoCommit$str() {
      return "Could not toggle autocommit";
   }

   public final void missingArguments(int anticipatedNumberOfArguments, int numberOfArguments) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000174: " + this.missingArguments$str(), anticipatedNumberOfArguments, numberOfArguments);
   }

   protected String missingArguments$str() {
      return "Function template anticipated %s arguments, but %s arguments encountered";
   }

   public final void unableToResolveMappingFile(String xmlFile) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000360: " + this.unableToResolveMappingFile$str(), xmlFile);
   }

   protected String unableToResolveMappingFile$str() {
      return "Unable to resolve mapping file [%s]";
   }

   public final void requiredDifferentProvider(String provider) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000226: " + this.requiredDifferentProvider$str(), provider);
   }

   protected String requiredDifferentProvider$str() {
      return "Required a different provider: %s";
   }

   public final void exceptionInSubResolver(String message) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000089: " + this.exceptionInSubResolver$str(), message);
   }

   protected String exceptionInSubResolver$str() {
      return "Sub-resolver threw unexpected exception, continuing to next : %s";
   }

   public final void missingEntityAnnotation(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000175: " + this.missingEntityAnnotation$str(), className);
   }

   protected String missingEntityAnnotation$str() {
      return "Class annotated @org.hibernate.annotations.Entity but not javax.persistence.Entity (most likely a user error): %s";
   }

   public final void startTime(long startTime) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000251: " + this.startTime$str(), startTime);
   }

   protected String startTime$str() {
      return "Start time: %s";
   }

   public final void connectionProperties(Properties connectionProps) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000046: " + this.connectionProperties$str(), connectionProps);
   }

   protected String connectionProperties$str() {
      return "Connection properties: %s";
   }

   public final void unableToSwitchToMethodUsingColumnIndex(Method method) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000370: " + this.unableToSwitchToMethodUsingColumnIndex$str(), method);
   }

   protected String unableToSwitchToMethodUsingColumnIndex$str() {
      return "Exception switching from method: [%s] to a method using the column index. Reverting to using: [%<s]";
   }

   public final void configurationResource(String resource) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000040: " + this.configurationResource$str(), resource);
   }

   protected String configurationResource$str() {
      return "Configuration resource: %s";
   }

   public final void readingMappingsFromResource(String resourceName) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000221: " + this.readingMappingsFromResource$str(), resourceName);
   }

   protected String readingMappingsFromResource$str() {
      return "Reading mappings from resource: %s";
   }

   public final void usingDefaultTransactionStrategy() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000399: " + this.usingDefaultTransactionStrategy$str(), new Object[0]);
   }

   protected String usingDefaultTransactionStrategy$str() {
      return "Using default transaction strategy (direct JDBC transactions)";
   }

   public final void guidGenerated(String result) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000113: " + this.guidGenerated$str(), result);
   }

   protected String guidGenerated$str() {
      return "GUID identifier generated: %s";
   }

   public final void lazyPropertyFetchingAvailable(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000157: " + this.lazyPropertyFetchingAvailable$str(), name);
   }

   protected String lazyPropertyFetchingAvailable$str() {
      return "Lazy property fetching available for: %s";
   }

   public final void unsupportedProperty(Object propertyName, Object newPropertyName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000395: " + this.unsupportedProperty$str(), propertyName, newPropertyName);
   }

   protected String unsupportedProperty$str() {
      return "Usage of obsolete property: %s no longer supported, use: %s";
   }

   public final void unableToDropTemporaryIdTable(String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000314: " + this.unableToDropTemporaryIdTable$str(), message);
   }

   protected String unableToDropTemporaryIdTable$str() {
      return "Unable to drop temporary id table after use [%s]";
   }

   public final void unableToCloseSessionDuringRollback(Exception e) {
      this.log.logf(FQCN, Level.ERROR, e, "HHH000295: " + this.unableToCloseSessionDuringRollback$str(), new Object[0]);
   }

   protected String unableToCloseSessionDuringRollback$str() {
      return "Could not close session during rollback";
   }

   public final void factoryUnboundFromName(String name) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000098: " + this.factoryUnboundFromName$str(), name);
   }

   protected String factoryUnboundFromName$str() {
      return "A factory was unbound from name: %s";
   }

   public final void exceptionHeaderFound(String exceptionHeader, String metaInfOrmXml) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000085: " + this.exceptionHeaderFound$str(), exceptionHeader, metaInfOrmXml);
   }

   protected String exceptionHeaderFound$str() {
      return "%s %s found";
   }

   public final void usingHibernateBuiltInConnectionPool() {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000402: " + this.usingHibernateBuiltInConnectionPool$str(), new Object[0]);
   }

   protected String usingHibernateBuiltInConnectionPool$str() {
      return "Using Hibernate built-in connection pool (not for production use!)";
   }

   public final void unableToCloseSessionButSwallowingError(HibernateException e) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HHH000425: " + this.unableToCloseSessionButSwallowingError$str(), e);
   }

   protected String unableToCloseSessionButSwallowingError$str() {
      return "Could not close session; swallowing exception[%s] as transaction completed";
   }

   public final void invalidSubStrategy(String className) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000138: " + this.invalidSubStrategy$str(), className);
   }

   protected String invalidSubStrategy$str() {
      return "Mixing inheritance strategy in a entity hierarchy is not allowed, ignoring sub strategy in: %s";
   }

   public final void unableToLoadDerbyDriver(String message) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000328: " + this.unableToLoadDerbyDriver$str(), message);
   }

   protected String unableToLoadDerbyDriver$str() {
      return "Unable to load/access derby driver class sysinfo to check versions : %s";
   }

   public final void narrowingProxy(Class concreteProxyClass) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000179: " + this.narrowingProxy$str(), concreteProxyClass);
   }

   protected String narrowingProxy$str() {
      return "Narrowing proxy to %s - this operation breaks ==";
   }

   public final void closingUnreleasedBatch() {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000420: " + this.closingUnreleasedBatch$str(), new Object[0]);
   }

   protected String closingUnreleasedBatch$str() {
      return "Closing un-released batch";
   }

   public final void invalidEditOfReadOnlyItem(Object key) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000134: " + this.invalidEditOfReadOnlyItem$str(), key);
   }

   protected String invalidEditOfReadOnlyItem$str() {
      return "Application attempted to edit read only item: %s";
   }

   public final void parameterPositionOccurredAsBothJpaAndHibernatePositionalParameter(Integer position) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000195: " + this.parameterPositionOccurredAsBothJpaAndHibernatePositionalParameter$str(), position);
   }

   protected String parameterPositionOccurredAsBothJpaAndHibernatePositionalParameter$str() {
      return "Parameter position [%s] occurred as both JPA and Hibernate positional parameter";
   }

   public final void expectedType(String name, String string) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, "HHH000091: " + this.expectedType$str(), name, string);
   }

   protected String expectedType$str() {
      return "Expected type: %s, actual value: %s";
   }

   public final void unableToCleanupTemporaryIdTable(Throwable t) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000283: " + this.unableToCleanupTemporaryIdTable$str(), t);
   }

   protected String unableToCleanupTemporaryIdTable$str() {
      return "Unable to cleanup temporary id table after use [%s]";
   }

   public final String jdbcUrlNotSpecified(String url) {
      String result = String.format("HHH000152: " + this.jdbcUrlNotSpecified$str(), url);
      return result;
   }

   protected String jdbcUrlNotSpecified$str() {
      return "JDBC URL was not specified by property %s";
   }

   public final void c3p0ProviderClassNotFound(String c3p0ProviderClassName) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000022: " + this.c3p0ProviderClassNotFound$str(), c3p0ProviderClassName);
   }

   protected String c3p0ProviderClassNotFound$str() {
      return "c3p0 properties were encountered, but the %s provider class was not found on the classpath; these properties are going to be ignored.";
   }

   public final void couldNotBindJndiListener() {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, "HHH000127: " + this.couldNotBindJndiListener$str(), new Object[0]);
   }

   protected String couldNotBindJndiListener$str() {
      return "Could not bind JNDI listener";
   }

   public final void warningsCreatingTempTable(SQLWarning warning) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, "HHH000413: " + this.warningsCreatingTempTable$str(), warning);
   }

   protected String warningsCreatingTempTable$str() {
      return "Warnings creating temp table : %s";
   }
}
