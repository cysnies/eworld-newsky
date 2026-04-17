package org.hibernate.cfg;

public interface AvailableSettings {
   String SESSION_FACTORY_NAME = "hibernate.session_factory_name";
   String SESSION_FACTORY_NAME_IS_JNDI = "hibernate.session_factory_name_is_jndi";
   String CONNECTION_PROVIDER = "hibernate.connection.provider_class";
   String DRIVER = "hibernate.connection.driver_class";
   String URL = "hibernate.connection.url";
   String USER = "hibernate.connection.username";
   String PASS = "hibernate.connection.password";
   String ISOLATION = "hibernate.connection.isolation";
   String AUTOCOMMIT = "hibernate.connection.autocommit";
   String POOL_SIZE = "hibernate.connection.pool_size";
   String DATASOURCE = "hibernate.connection.datasource";
   String CONNECTION_PREFIX = "hibernate.connection";
   String JNDI_CLASS = "hibernate.jndi.class";
   String JNDI_URL = "hibernate.jndi.url";
   String JNDI_PREFIX = "hibernate.jndi";
   String DIALECT = "hibernate.dialect";
   String DIALECT_RESOLVERS = "hibernate.dialect_resolvers";
   String DEFAULT_SCHEMA = "hibernate.default_schema";
   String DEFAULT_CATALOG = "hibernate.default_catalog";
   String SHOW_SQL = "hibernate.show_sql";
   String FORMAT_SQL = "hibernate.format_sql";
   String USE_SQL_COMMENTS = "hibernate.use_sql_comments";
   String MAX_FETCH_DEPTH = "hibernate.max_fetch_depth";
   String DEFAULT_BATCH_FETCH_SIZE = "hibernate.default_batch_fetch_size";
   String USE_STREAMS_FOR_BINARY = "hibernate.jdbc.use_streams_for_binary";
   String USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
   String USE_GET_GENERATED_KEYS = "hibernate.jdbc.use_get_generated_keys";
   String STATEMENT_FETCH_SIZE = "hibernate.jdbc.fetch_size";
   String STATEMENT_BATCH_SIZE = "hibernate.jdbc.batch_size";
   String BATCH_STRATEGY = "hibernate.jdbc.factory_class";
   String BATCH_VERSIONED_DATA = "hibernate.jdbc.batch_versioned_data";
   String OUTPUT_STYLESHEET = "hibernate.xml.output_stylesheet";
   String C3P0_MAX_SIZE = "hibernate.c3p0.max_size";
   String C3P0_MIN_SIZE = "hibernate.c3p0.min_size";
   String C3P0_TIMEOUT = "hibernate.c3p0.timeout";
   String C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";
   String C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
   String C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";
   /** @deprecated */
   String PROXOOL_PREFIX = "hibernate.proxool";
   String PROXOOL_XML = "hibernate.proxool.xml";
   String PROXOOL_PROPERTIES = "hibernate.proxool.properties";
   String PROXOOL_EXISTING_POOL = "hibernate.proxool.existing_pool";
   String PROXOOL_POOL_ALIAS = "hibernate.proxool.pool_alias";
   String AUTO_CLOSE_SESSION = "hibernate.transaction.auto_close_session";
   String FLUSH_BEFORE_COMPLETION = "hibernate.transaction.flush_before_completion";
   String RELEASE_CONNECTIONS = "hibernate.connection.release_mode";
   String CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
   String TRANSACTION_STRATEGY = "hibernate.transaction.factory_class";
   String JTA_PLATFORM = "hibernate.transaction.jta.platform";
   /** @deprecated */
   @Deprecated
   String TRANSACTION_MANAGER_STRATEGY = "hibernate.transaction.manager_lookup_class";
   /** @deprecated */
   @Deprecated
   String USER_TRANSACTION = "jta.UserTransaction";
   String CACHE_REGION_FACTORY = "hibernate.cache.region.factory_class";
   String CACHE_PROVIDER_CONFIG = "hibernate.cache.provider_configuration_file_resource_path";
   String CACHE_NAMESPACE = "hibernate.cache.jndi";
   String USE_QUERY_CACHE = "hibernate.cache.use_query_cache";
   String QUERY_CACHE_FACTORY = "hibernate.cache.query_cache_factory";
   String USE_SECOND_LEVEL_CACHE = "hibernate.cache.use_second_level_cache";
   String USE_MINIMAL_PUTS = "hibernate.cache.use_minimal_puts";
   String CACHE_REGION_PREFIX = "hibernate.cache.region_prefix";
   String USE_STRUCTURED_CACHE = "hibernate.cache.use_structured_entries";
   String GENERATE_STATISTICS = "hibernate.generate_statistics";
   String USE_IDENTIFIER_ROLLBACK = "hibernate.use_identifier_rollback";
   String USE_REFLECTION_OPTIMIZER = "hibernate.bytecode.use_reflection_optimizer";
   String QUERY_TRANSLATOR = "hibernate.query.factory_class";
   String QUERY_SUBSTITUTIONS = "hibernate.query.substitutions";
   String QUERY_STARTUP_CHECKING = "hibernate.query.startup_check";
   String HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
   String HBM2DDL_IMPORT_FILES = "hibernate.hbm2ddl.import_files";
   String HBM2DDL_IMPORT_FILES_SQL_EXTRACTOR = "hibernate.hbm2ddl.import_files_sql_extractor";
   String SQL_EXCEPTION_CONVERTER = "hibernate.jdbc.sql_exception_converter";
   String WRAP_RESULT_SETS = "hibernate.jdbc.wrap_result_sets";
   String ORDER_UPDATES = "hibernate.order_updates";
   String ORDER_INSERTS = "hibernate.order_inserts";
   String DEFAULT_ENTITY_MODE = "hibernate.default_entity_mode";
   String JACC_CONTEXTID = "hibernate.jacc_context_id";
   String GLOBALLY_QUOTED_IDENTIFIERS = "hibernate.globally_quoted_identifiers";
   String CHECK_NULLABILITY = "hibernate.check_nullability";
   String BYTECODE_PROVIDER = "hibernate.bytecode.provider";
   String JPAQL_STRICT_COMPLIANCE = "hibernate.query.jpaql_strict_compliance";
   String PREFER_POOLED_VALUES_LO = "hibernate.id.optimizer.pooled.prefer_lo";
   /** @deprecated */
   @Deprecated
   String QUERY_PLAN_CACHE_MAX_STRONG_REFERENCES = "hibernate.query.plan_cache_max_strong_references";
   /** @deprecated */
   @Deprecated
   String QUERY_PLAN_CACHE_MAX_SOFT_REFERENCES = "hibernate.query.plan_cache_max_soft_references";
   String QUERY_PLAN_CACHE_MAX_SIZE = "hibernate.query.plan_cache_max_size";
   String QUERY_PLAN_CACHE_PARAMETER_METADATA_MAX_SIZE = "hibernate.query.plan_parameter_metadata_max_size";
   String NON_CONTEXTUAL_LOB_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
   String CLASSLOADERS = "hibernate.classLoaders";
   String APP_CLASSLOADER = "hibernate.classLoader.application";
   String RESOURCES_CLASSLOADER = "hibernate.classLoader.resources";
   String HIBERNATE_CLASSLOADER = "hibernate.classLoader.hibernate";
   String ENVIRONMENT_CLASSLOADER = "hibernate.classLoader.environment";
   String C3P0_CONFIG_PREFIX = "hibernate.c3p0";
   String PROXOOL_CONFIG_PREFIX = "hibernate.proxool";
   String JMX_ENABLED = "hibernate.jmx.enabled";
   String JMX_PLATFORM_SERVER = "hibernate.jmx.usePlatformServer";
   String JMX_AGENT_ID = "hibernate.jmx.agentId";
   String JMX_DOMAIN_NAME = "hibernate.jmx.defaultDomain";
   String JMX_SF_NAME = "hibernate.jmx.sessionFactoryName";
   String JMX_DEFAULT_OBJ_NAME_DOMAIN = "org.hibernate.core";
   String JTA_CACHE_TM = "hibernate.jta.cacheTransactionManager";
   String JTA_CACHE_UT = "hibernate.jta.cacheUserTransaction";
   String DEFAULT_CACHE_CONCURRENCY_STRATEGY = "hibernate.cache.default_cache_concurrency_strategy";
   String USE_NEW_ID_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
   String CUSTOM_ENTITY_DIRTINESS_STRATEGY = "hibernate.entity_dirtiness_strategy";
   String MULTI_TENANT = "hibernate.multiTenancy";
   String MULTI_TENANT_CONNECTION_PROVIDER = "hibernate.multi_tenant_connection_provider";
   String MULTI_TENANT_IDENTIFIER_RESOLVER = "hibernate.tenant_identifier_resolver";
   String FORCE_DISCRIMINATOR_IN_SELECTS_BY_DEFAULT = "hibernate.discriminator.force_in_select";
   String ENABLE_LAZY_LOAD_NO_TRANS = "hibernate.enable_lazy_load_no_trans";
   String HQL_BULK_ID_STRATEGY = "hibernate.hql.bulk_id_strategy";
   String USE_NATIONALIZED_CHARACTER_DATA = "hibernate.use_nationalized_character_data";
}
