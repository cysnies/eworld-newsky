package org.hibernate.hql.internal.classic;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.hql.internal.NameGenerator;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.IteratorImpl;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.BasicLoader;
import org.hibernate.loader.Loader;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.sql.QuerySelect;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class QueryTranslatorImpl extends BasicLoader implements FilterTranslator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryTranslatorImpl.class.getName());
   private static final String[] NO_RETURN_ALIASES = new String[0];
   private final String queryIdentifier;
   private final String queryString;
   private final Map typeMap;
   private final Map collections;
   private List returnedTypes;
   private final List fromTypes;
   private final List scalarTypes;
   private final Map namedParameters;
   private final Map aliasNames;
   private final Map oneToOneOwnerNames;
   private final Map uniqueKeyOwnerReferences;
   private final Map decoratedPropertyMappings;
   private final List scalarSelectTokens;
   private final List whereTokens;
   private final List havingTokens;
   private final Map joins;
   private final List orderByTokens;
   private final List groupByTokens;
   private final Set querySpaces;
   private final Set entitiesToFetch;
   private final Map pathAliases;
   private final Map pathJoins;
   private Queryable[] persisters;
   private int[] owners;
   private EntityType[] ownerAssociationTypes;
   private String[] names;
   private boolean[] includeInSelect;
   private int selectLength;
   private Type[] returnTypes;
   private Type[] actualReturnTypes;
   private String[][] scalarColumnNames;
   private Map tokenReplacements;
   private int nameCount;
   private int parameterCount;
   private boolean distinct;
   private boolean compiled;
   private String sqlString;
   private Class holderClass;
   private Constructor holderConstructor;
   private boolean hasScalars;
   private boolean shallowQuery;
   private QueryTranslatorImpl superQuery;
   private QueryableCollection collectionPersister;
   private int collectionOwnerColumn;
   private String collectionOwnerName;
   private String fetchName;
   private String[] suffixes;
   private Map enabledFilters;

   public QueryTranslatorImpl(String queryIdentifier, String queryString, Map enabledFilters, SessionFactoryImplementor factory) {
      super(factory);
      this.typeMap = new LinkedHashMap();
      this.collections = new LinkedHashMap();
      this.returnedTypes = new ArrayList();
      this.fromTypes = new ArrayList();
      this.scalarTypes = new ArrayList();
      this.namedParameters = new HashMap();
      this.aliasNames = new HashMap();
      this.oneToOneOwnerNames = new HashMap();
      this.uniqueKeyOwnerReferences = new HashMap();
      this.decoratedPropertyMappings = new HashMap();
      this.scalarSelectTokens = new ArrayList();
      this.whereTokens = new ArrayList();
      this.havingTokens = new ArrayList();
      this.joins = new LinkedHashMap();
      this.orderByTokens = new ArrayList();
      this.groupByTokens = new ArrayList();
      this.querySpaces = new HashSet();
      this.entitiesToFetch = new HashSet();
      this.pathAliases = new HashMap();
      this.pathJoins = new HashMap();
      this.nameCount = 0;
      this.parameterCount = 0;
      this.distinct = false;
      this.collectionOwnerColumn = -1;
      this.queryIdentifier = queryIdentifier;
      this.queryString = queryString;
      this.enabledFilters = enabledFilters;
   }

   public QueryTranslatorImpl(String queryString, Map enabledFilters, SessionFactoryImplementor factory) {
      this(queryString, queryString, enabledFilters, factory);
   }

   void compile(QueryTranslatorImpl superquery) throws QueryException, MappingException {
      this.tokenReplacements = superquery.tokenReplacements;
      this.superQuery = superquery;
      this.shallowQuery = true;
      this.enabledFilters = superquery.getEnabledFilters();
      this.compile();
   }

   public synchronized void compile(Map replacements, boolean scalar) throws QueryException, MappingException {
      if (!this.compiled) {
         this.tokenReplacements = replacements;
         this.shallowQuery = scalar;
         this.compile();
      }

   }

   public synchronized void compile(String collectionRole, Map replacements, boolean scalar) throws QueryException, MappingException {
      if (!this.isCompiled()) {
         this.addFromAssociation("this", collectionRole);
         this.compile(replacements, scalar);
      }

   }

   private void compile() throws QueryException, MappingException {
      LOG.trace("Compiling query");

      try {
         ParserHelper.parse(new PreprocessingParser(this.tokenReplacements), this.queryString, " \n\r\f\t,()=<>&|+-=/*'^![]#~\\", this);
         this.renderSQL();
      } catch (QueryException qe) {
         qe.setQueryString(this.queryString);
         throw qe;
      } catch (MappingException me) {
         throw me;
      } catch (Exception e) {
         LOG.debug("Unexpected query compilation problem", e);
         e.printStackTrace();
         QueryException qe = new QueryException("Incorrect query syntax", e);
         qe.setQueryString(this.queryString);
         throw qe;
      }

      this.postInstantiate();
      this.compiled = true;
   }

   public String getSQLString() {
      return this.sqlString;
   }

   public List collectSqlStrings() {
      return ArrayHelper.toList((Object)(new String[]{this.sqlString}));
   }

   public String getQueryString() {
      return this.queryString;
   }

   protected Loadable[] getEntityPersisters() {
      return this.persisters;
   }

   public Type[] getReturnTypes() {
      return this.actualReturnTypes;
   }

   public String[] getReturnAliases() {
      return NO_RETURN_ALIASES;
   }

   public String[][] getColumnNames() {
      return this.scalarColumnNames;
   }

   private static void logQuery(String hql, String sql) {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("HQL: %s", hql);
         LOG.debugf("SQL: %s", sql);
      }

   }

   void setAliasName(String alias, String name) {
      this.aliasNames.put(alias, name);
   }

   public String getAliasName(String alias) {
      String name = (String)this.aliasNames.get(alias);
      if (name == null) {
         if (this.superQuery != null) {
            name = this.superQuery.getAliasName(alias);
         } else {
            name = alias;
         }
      }

      return name;
   }

   String unalias(String path) {
      String alias = StringHelper.root(path);
      String name = this.getAliasName(alias);
      return name != null ? name + path.substring(alias.length()) : path;
   }

   void addEntityToFetch(String name, String oneToOneOwnerName, AssociationType ownerAssociationType) {
      this.addEntityToFetch(name);
      if (oneToOneOwnerName != null) {
         this.oneToOneOwnerNames.put(name, oneToOneOwnerName);
      }

      if (ownerAssociationType != null) {
         this.uniqueKeyOwnerReferences.put(name, ownerAssociationType);
      }

   }

   private void addEntityToFetch(String name) {
      this.entitiesToFetch.add(name);
   }

   private int nextCount() {
      int var10000;
      if (this.superQuery == null) {
         int var10002 = this.nameCount;
         var10000 = var10002;
         this.nameCount = var10002 + 1;
      } else {
         QueryTranslatorImpl var1 = this.superQuery;
         int var2 = var1.nameCount;
         QueryTranslatorImpl var10001 = var1;
         var10000 = var2;
         var10001.nameCount = var2 + 1;
      }

      return var10000;
   }

   String createNameFor(String type) {
      return StringHelper.generateAlias(type, this.nextCount());
   }

   String createNameForCollection(String role) {
      return StringHelper.generateAlias(role, this.nextCount());
   }

   private String getType(String name) {
      String type = (String)this.typeMap.get(name);
      if (type == null && this.superQuery != null) {
         type = this.superQuery.getType(name);
      }

      return type;
   }

   private String getRole(String name) {
      String role = (String)this.collections.get(name);
      if (role == null && this.superQuery != null) {
         role = this.superQuery.getRole(name);
      }

      return role;
   }

   boolean isName(String name) {
      return this.aliasNames.containsKey(name) || this.typeMap.containsKey(name) || this.collections.containsKey(name) || this.superQuery != null && this.superQuery.isName(name);
   }

   PropertyMapping getPropertyMapping(String name) throws QueryException {
      PropertyMapping decorator = this.getDecoratedPropertyMapping(name);
      if (decorator != null) {
         return decorator;
      } else {
         String type = this.getType(name);
         if (type == null) {
            String role = this.getRole(name);
            if (role == null) {
               throw new QueryException("alias not found: " + name);
            } else {
               return this.getCollectionPersister(role);
            }
         } else {
            Queryable persister = this.getEntityPersister(type);
            if (persister == null) {
               throw new QueryException("persistent class not found: " + type);
            } else {
               return persister;
            }
         }
      }
   }

   private PropertyMapping getDecoratedPropertyMapping(String name) {
      return (PropertyMapping)this.decoratedPropertyMappings.get(name);
   }

   void decoratePropertyMapping(String name, PropertyMapping mapping) {
      this.decoratedPropertyMappings.put(name, mapping);
   }

   private Queryable getEntityPersisterForName(String name) throws QueryException {
      String type = this.getType(name);
      Queryable persister = this.getEntityPersister(type);
      if (persister == null) {
         throw new QueryException("persistent class not found: " + type);
      } else {
         return persister;
      }
   }

   Queryable getEntityPersisterUsingImports(String className) {
      String importedClassName = this.getFactory().getImportedClassName(className);
      if (importedClassName == null) {
         return null;
      } else {
         try {
            return (Queryable)this.getFactory().getEntityPersister(importedClassName);
         } catch (MappingException var4) {
            return null;
         }
      }
   }

   Queryable getEntityPersister(String entityName) throws QueryException {
      try {
         return (Queryable)this.getFactory().getEntityPersister(entityName);
      } catch (Exception var3) {
         throw new QueryException("persistent class not found: " + entityName);
      }
   }

   QueryableCollection getCollectionPersister(String role) throws QueryException {
      try {
         return (QueryableCollection)this.getFactory().getCollectionPersister(role);
      } catch (ClassCastException var3) {
         throw new QueryException("collection role is not queryable: " + role);
      } catch (Exception var4) {
         throw new QueryException("collection role not found: " + role);
      }
   }

   void addType(String name, String type) {
      this.typeMap.put(name, type);
   }

   void addCollection(String name, String role) {
      this.collections.put(name, role);
   }

   void addFrom(String name, String type, JoinSequence joinSequence) throws QueryException {
      this.addType(name, type);
      this.addFrom(name, joinSequence);
   }

   void addFromCollection(String name, String collectionRole, JoinSequence joinSequence) throws QueryException {
      this.addCollection(name, collectionRole);
      this.addJoin(name, joinSequence);
   }

   void addFrom(String name, JoinSequence joinSequence) throws QueryException {
      this.fromTypes.add(name);
      this.addJoin(name, joinSequence);
   }

   void addFromClass(String name, Queryable classPersister) throws QueryException {
      JoinSequence joinSequence = (new JoinSequence(this.getFactory())).setRoot(classPersister, name);
      this.addFrom(name, classPersister.getEntityName(), joinSequence);
   }

   void addSelectClass(String name) {
      this.returnedTypes.add(name);
   }

   void addSelectScalar(Type type) {
      this.scalarTypes.add(type);
   }

   void appendWhereToken(String token) {
      this.whereTokens.add(token);
   }

   void appendHavingToken(String token) {
      this.havingTokens.add(token);
   }

   void appendOrderByToken(String token) {
      this.orderByTokens.add(token);
   }

   void appendGroupByToken(String token) {
      this.groupByTokens.add(token);
   }

   void appendScalarSelectToken(String token) {
      this.scalarSelectTokens.add(token);
   }

   void appendScalarSelectTokens(String[] tokens) {
      this.scalarSelectTokens.add(tokens);
   }

   void addFromJoinOnly(String name, JoinSequence joinSequence) throws QueryException {
      this.addJoin(name, joinSequence.getFromPart());
   }

   void addJoin(String name, JoinSequence joinSequence) throws QueryException {
      if (!this.joins.containsKey(name)) {
         this.joins.put(name, joinSequence);
      }

   }

   void addNamedParameter(String name) {
      if (this.superQuery != null) {
         this.superQuery.addNamedParameter(name);
      }

      Integer loc = this.parameterCount++;
      Object o = this.namedParameters.get(name);
      if (o == null) {
         this.namedParameters.put(name, loc);
      } else if (o instanceof Integer) {
         ArrayList list = new ArrayList(4);
         list.add(o);
         list.add(loc);
         this.namedParameters.put(name, list);
      } else {
         ((ArrayList)o).add(loc);
      }

   }

   public int[] getNamedParameterLocs(String name) throws QueryException {
      Object o = this.namedParameters.get(name);
      if (o == null) {
         QueryException qe = new QueryException("Named parameter does not appear in Query: " + name);
         qe.setQueryString(this.queryString);
         throw qe;
      } else {
         return o instanceof Integer ? new int[]{(Integer)o} : ArrayHelper.toIntArray((ArrayList)o);
      }
   }

   private void renderSQL() throws QueryException, MappingException {
      int rtsize;
      if (this.returnedTypes.size() == 0 && this.scalarTypes.size() == 0) {
         this.returnedTypes = this.fromTypes;
         rtsize = this.returnedTypes.size();
      } else {
         rtsize = this.returnedTypes.size();
         Iterator iter = this.entitiesToFetch.iterator();

         while(iter.hasNext()) {
            this.returnedTypes.add(iter.next());
         }
      }

      int size = this.returnedTypes.size();
      this.persisters = new Queryable[size];
      this.names = new String[size];
      this.owners = new int[size];
      this.ownerAssociationTypes = new EntityType[size];
      this.suffixes = new String[size];
      this.includeInSelect = new boolean[size];

      for(int i = 0; i < size; ++i) {
         String name = (String)this.returnedTypes.get(i);
         this.persisters[i] = this.getEntityPersisterForName(name);
         this.suffixes[i] = size == 1 ? "" : Integer.toString(i) + '_';
         this.names[i] = name;
         this.includeInSelect[i] = !this.entitiesToFetch.contains(name);
         if (this.includeInSelect[i]) {
            ++this.selectLength;
         }

         if (name.equals(this.collectionOwnerName)) {
            this.collectionOwnerColumn = i;
         }

         String oneToOneOwner = (String)this.oneToOneOwnerNames.get(name);
         this.owners[i] = oneToOneOwner == null ? -1 : this.returnedTypes.indexOf(oneToOneOwner);
         this.ownerAssociationTypes[i] = (EntityType)this.uniqueKeyOwnerReferences.get(name);
      }

      if (ArrayHelper.isAllNegative(this.owners)) {
         this.owners = null;
      }

      String scalarSelect = this.renderScalarSelect();
      int scalarSize = this.scalarTypes.size();
      this.hasScalars = this.scalarTypes.size() != rtsize;
      this.returnTypes = new Type[scalarSize];

      for(int i = 0; i < scalarSize; ++i) {
         this.returnTypes[i] = (Type)this.scalarTypes.get(i);
      }

      QuerySelect sql = new QuerySelect(this.getFactory().getDialect());
      sql.setDistinct(this.distinct);
      if (!this.shallowQuery) {
         this.renderIdentifierSelect(sql);
         this.renderPropertiesSelect(sql);
      }

      if (this.collectionPersister != null) {
         sql.addSelectFragmentString(this.collectionPersister.selectFragment(this.fetchName, "__"));
      }

      if (this.hasScalars || this.shallowQuery) {
         sql.addSelectFragmentString(scalarSelect);
      }

      this.mergeJoins(sql.getJoinFragment());
      sql.setWhereTokens(this.whereTokens.iterator());
      sql.setGroupByTokens(this.groupByTokens.iterator());
      sql.setHavingTokens(this.havingTokens.iterator());
      sql.setOrderByTokens(this.orderByTokens.iterator());
      if (this.collectionPersister != null && this.collectionPersister.hasOrdering()) {
         sql.addOrderBy(this.collectionPersister.getSQLOrderByString(this.fetchName));
      }

      this.scalarColumnNames = NameGenerator.generateColumnNames(this.returnTypes, this.getFactory());
      Iterator iter = this.collections.values().iterator();

      while(iter.hasNext()) {
         CollectionPersister p = this.getCollectionPersister((String)iter.next());
         this.addQuerySpaces(p.getCollectionSpaces());
      }

      iter = this.typeMap.keySet().iterator();

      while(iter.hasNext()) {
         Queryable p = this.getEntityPersisterForName((String)iter.next());
         this.addQuerySpaces(p.getQuerySpaces());
      }

      this.sqlString = sql.toQueryString();
      if (this.holderClass != null) {
         this.holderConstructor = ReflectHelper.getConstructor(this.holderClass, this.returnTypes);
      }

      if (this.hasScalars) {
         this.actualReturnTypes = this.returnTypes;
      } else {
         this.actualReturnTypes = new Type[this.selectLength];
         int j = 0;

         for(int i = 0; i < this.persisters.length; ++i) {
            if (this.includeInSelect[i]) {
               this.actualReturnTypes[j++] = this.getFactory().getTypeResolver().getTypeFactory().manyToOne(this.persisters[i].getEntityName(), this.shallowQuery);
            }
         }
      }

   }

   private void renderIdentifierSelect(QuerySelect sql) {
      int size = this.returnedTypes.size();

      for(int k = 0; k < size; ++k) {
         String name = (String)this.returnedTypes.get(k);
         String suffix = size == 1 ? "" : Integer.toString(k) + '_';
         sql.addSelectFragmentString(this.persisters[k].identifierSelectFragment(name, suffix));
      }

   }

   private void renderPropertiesSelect(QuerySelect sql) {
      int size = this.returnedTypes.size();

      for(int k = 0; k < size; ++k) {
         String suffix = size == 1 ? "" : Integer.toString(k) + '_';
         String name = (String)this.returnedTypes.get(k);
         sql.addSelectFragmentString(this.persisters[k].propertySelectFragment(name, suffix, false));
      }

   }

   private String renderScalarSelect() {
      boolean isSubselect = this.superQuery != null;
      StringBuilder buf = new StringBuilder(20);
      if (this.scalarTypes.size() == 0) {
         int size = this.returnedTypes.size();

         for(int k = 0; k < size; ++k) {
            this.scalarTypes.add(this.getFactory().getTypeResolver().getTypeFactory().manyToOne(this.persisters[k].getEntityName(), this.shallowQuery));
            String[] idColumnNames = this.persisters[k].getIdentifierColumnNames();

            for(int i = 0; i < idColumnNames.length; ++i) {
               buf.append(this.returnedTypes.get(k)).append('.').append(idColumnNames[i]);
               if (!isSubselect) {
                  buf.append(" as ").append(NameGenerator.scalarName(k, i));
               }

               if (i != idColumnNames.length - 1 || k != size - 1) {
                  buf.append(", ");
               }
            }
         }
      } else {
         Iterator iter = this.scalarSelectTokens.iterator();
         int c = 0;
         boolean nolast = false;
         int parenCount = 0;

         while(iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof String) {
               String token = (String)next;
               if ("(".equals(token)) {
                  ++parenCount;
               } else if (")".equals(token)) {
                  --parenCount;
               }

               String lc = token.toLowerCase();
               if (lc.equals(", ")) {
                  if (nolast) {
                     nolast = false;
                  } else if (!isSubselect && parenCount == 0) {
                     int x = c++;
                     buf.append(" as ").append(NameGenerator.scalarName(x, 0));
                  }
               }

               buf.append(token);
               if (lc.equals("distinct") || lc.equals("all")) {
                  buf.append(' ');
               }
            } else {
               nolast = true;
               String[] tokens = (String[])next;

               for(int i = 0; i < tokens.length; ++i) {
                  buf.append(tokens[i]);
                  if (!isSubselect) {
                     buf.append(" as ").append(NameGenerator.scalarName(c, i));
                  }

                  if (i != tokens.length - 1) {
                     buf.append(", ");
                  }
               }

               ++c;
            }
         }

         if (!isSubselect && !nolast) {
            int x = c++;
            buf.append(" as ").append(NameGenerator.scalarName(x, 0));
         }
      }

      return buf.toString();
   }

   private void mergeJoins(JoinFragment ojf) throws MappingException, QueryException {
      for(Map.Entry me : this.joins.entrySet()) {
         String name = (String)me.getKey();
         JoinSequence join = (JoinSequence)me.getValue();
         join.setSelector(new JoinSequence.Selector() {
            public boolean includeSubclasses(String alias) {
               boolean include = QueryTranslatorImpl.this.returnedTypes.contains(alias) && !QueryTranslatorImpl.this.isShallowQuery();
               return include;
            }
         });
         if (this.typeMap.containsKey(name)) {
            ojf.addFragment(join.toJoinFragment(this.enabledFilters, true));
         } else if (this.collections.containsKey(name)) {
            ojf.addFragment(join.toJoinFragment(this.enabledFilters, true));
         }
      }

   }

   public final Set getQuerySpaces() {
      return this.querySpaces;
   }

   boolean isShallowQuery() {
      return this.shallowQuery;
   }

   void addQuerySpaces(Serializable[] spaces) {
      for(int i = 0; i < spaces.length; ++i) {
         this.querySpaces.add(spaces[i]);
      }

      if (this.superQuery != null) {
         this.superQuery.addQuerySpaces(spaces);
      }

   }

   void setDistinct(boolean distinct) {
      this.distinct = distinct;
   }

   boolean isSubquery() {
      return this.superQuery != null;
   }

   public CollectionPersister[] getCollectionPersisters() {
      return this.collectionPersister == null ? null : new CollectionPersister[]{this.collectionPersister};
   }

   protected String[] getCollectionSuffixes() {
      return this.collectionPersister == null ? null : new String[]{"__"};
   }

   void setCollectionToFetch(String role, String name, String ownerName, String entityName) throws QueryException {
      this.fetchName = name;
      this.collectionPersister = this.getCollectionPersister(role);
      this.collectionOwnerName = ownerName;
      if (this.collectionPersister.getElementType().isEntityType()) {
         this.addEntityToFetch(entityName);
      }

   }

   protected String[] getSuffixes() {
      return this.suffixes;
   }

   protected String[] getAliases() {
      return this.names;
   }

   private void addFromAssociation(String elementName, String collectionRole) throws QueryException {
      QueryableCollection persister = this.getCollectionPersister(collectionRole);
      Type collectionElementType = persister.getElementType();
      if (!collectionElementType.isEntityType()) {
         throw new QueryException("collection of values in filter: " + elementName);
      } else {
         String[] keyColumnNames = persister.getKeyColumnNames();
         JoinSequence join = new JoinSequence(this.getFactory());
         String collectionName = persister.isOneToMany() ? elementName : this.createNameForCollection(collectionRole);
         join.setRoot(persister, collectionName);
         if (!persister.isOneToMany()) {
            this.addCollection(collectionName, collectionRole);

            try {
               join.addJoin((AssociationType)persister.getElementType(), elementName, JoinType.INNER_JOIN, persister.getElementColumnNames(collectionName));
            } catch (MappingException me) {
               throw new QueryException(me);
            }
         }

         join.addCondition(collectionName, keyColumnNames, " = ?");
         EntityType elemType = (EntityType)collectionElementType;
         this.addFrom(elementName, elemType.getAssociatedEntityName(), join);
      }
   }

   String getPathAlias(String path) {
      return (String)this.pathAliases.get(path);
   }

   JoinSequence getPathJoin(String path) {
      return (JoinSequence)this.pathJoins.get(path);
   }

   void addPathAliasAndJoin(String path, String alias, JoinSequence joinSequence) {
      this.pathAliases.put(path, alias);
      this.pathJoins.put(path, joinSequence);
   }

   public List list(SessionImplementor session, QueryParameters queryParameters) throws HibernateException {
      return this.list(session, queryParameters, this.getQuerySpaces(), this.actualReturnTypes);
   }

   public Iterator iterate(QueryParameters queryParameters, EventSource session) throws HibernateException {
      boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();
      long startTime = 0L;
      if (stats) {
         startTime = System.currentTimeMillis();
      }

      try {
         List<Loader.AfterLoadAction> afterLoadActions = new ArrayList();
         ResultSet rs = this.executeQueryStatement(queryParameters, false, afterLoadActions, session);
         PreparedStatement st = (PreparedStatement)rs.getStatement();
         HolderInstantiator hi = HolderInstantiator.createClassicHolderInstantiator(this.holderConstructor, queryParameters.getResultTransformer());
         Iterator result = new IteratorImpl(rs, st, session, queryParameters.isReadOnly(session), this.returnTypes, this.getColumnNames(), hi);
         if (stats) {
            session.getFactory().getStatisticsImplementor().queryExecuted("HQL: " + this.queryString, 0, System.currentTimeMillis() - startTime);
         }

         return result;
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not execute query using iterate", this.getSQLString());
      }
   }

   public int executeUpdate(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      throw new UnsupportedOperationException("Not supported!  Use the AST translator...");
   }

   protected boolean[] includeInResultRow() {
      boolean[] isResultReturned = this.includeInSelect;
      if (this.hasScalars) {
         isResultReturned = new boolean[this.returnedTypes.size()];
         Arrays.fill(isResultReturned, true);
      }

      return isResultReturned;
   }

   protected ResultTransformer resolveResultTransformer(ResultTransformer resultTransformer) {
      return HolderInstantiator.resolveClassicResultTransformer(this.holderConstructor, resultTransformer);
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      Object[] resultRow = this.getResultRow(row, rs, session);
      return this.holderClass == null && resultRow.length == 1 ? resultRow[0] : resultRow;
   }

   protected Object[] getResultRow(Object[] row, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      Object[] resultRow;
      if (this.hasScalars) {
         String[][] scalarColumns = this.getColumnNames();
         int queryCols = this.returnTypes.length;
         resultRow = new Object[queryCols];

         for(int i = 0; i < queryCols; ++i) {
            resultRow[i] = this.returnTypes[i].nullSafeGet(rs, (String[])scalarColumns[i], session, (Object)null);
         }
      } else {
         resultRow = this.toResultRow(row);
      }

      return resultRow;
   }

   protected List getResultList(List results, ResultTransformer resultTransformer) throws QueryException {
      if (this.holderClass != null) {
         for(int i = 0; i < results.size(); ++i) {
            Object[] row = results.get(i);

            try {
               results.set(i, this.holderConstructor.newInstance(row));
            } catch (Exception e) {
               throw new QueryException("could not instantiate: " + this.holderClass, e);
            }
         }
      }

      return results;
   }

   private Object[] toResultRow(Object[] row) {
      if (this.selectLength == row.length) {
         return row;
      } else {
         Object[] result = new Object[this.selectLength];
         int j = 0;

         for(int i = 0; i < row.length; ++i) {
            if (this.includeInSelect[i]) {
               result[j++] = row[i];
            }
         }

         return result;
      }
   }

   void setHolderClass(Class clazz) {
      this.holderClass = clazz;
   }

   protected LockMode[] getLockModes(LockOptions lockOptions) {
      HashMap nameLockOptions = new HashMap();
      if (lockOptions == null) {
         lockOptions = LockOptions.NONE;
      }

      if (lockOptions.getAliasLockCount() > 0) {
         Iterator iter = lockOptions.getAliasLockIterator();

         while(iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            nameLockOptions.put(this.getAliasName((String)me.getKey()), me.getValue());
         }
      }

      LockMode[] lockModesArray = new LockMode[this.names.length];

      for(int i = 0; i < this.names.length; ++i) {
         LockMode lm = (LockMode)nameLockOptions.get(this.names[i]);
         if (lm == null) {
            lm = lockOptions.getLockMode();
         }

         lockModesArray[i] = lm;
      }

      return lockModesArray;
   }

   protected String applyLocks(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws QueryException {
      LockOptions lockOptions = parameters.getLockOptions();
      if (lockOptions != null && (lockOptions.getLockMode() != LockMode.NONE || lockOptions.getAliasLockCount() != 0)) {
         LockOptions locks = new LockOptions();
         locks.setLockMode(lockOptions.getLockMode());
         locks.setTimeOut(lockOptions.getTimeOut());
         locks.setScope(lockOptions.getScope());
         Iterator iter = lockOptions.getAliasLockIterator();

         while(iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            locks.setAliasSpecificLockMode(this.getAliasName((String)me.getKey()), (LockMode)me.getValue());
         }

         Map keyColumnNames = null;
         if (dialect.forUpdateOfColumns()) {
            keyColumnNames = new HashMap();

            for(int i = 0; i < this.names.length; ++i) {
               keyColumnNames.put(this.names[i], this.persisters[i].getIdentifierColumnNames());
            }
         }

         String result = dialect.applyLocksToSql(sql, locks, keyColumnNames);
         logQuery(this.queryString, result);
         return result;
      } else {
         return sql;
      }
   }

   protected boolean upgradeLocks() {
      return true;
   }

   protected int[] getCollectionOwners() {
      return new int[]{this.collectionOwnerColumn};
   }

   protected boolean isCompiled() {
      return this.compiled;
   }

   public String toString() {
      return this.queryString;
   }

   protected int[] getOwners() {
      return this.owners;
   }

   protected EntityType[] getOwnerAssociationTypes() {
      return this.ownerAssociationTypes;
   }

   public Class getHolderClass() {
      return this.holderClass;
   }

   public Map getEnabledFilters() {
      return this.enabledFilters;
   }

   public ScrollableResults scroll(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      HolderInstantiator hi = HolderInstantiator.createClassicHolderInstantiator(this.holderConstructor, queryParameters.getResultTransformer());
      return this.scroll(queryParameters, this.returnTypes, hi, session);
   }

   public String getQueryIdentifier() {
      return this.queryIdentifier;
   }

   protected boolean isSubselectLoadingEnabled() {
      return this.hasSubselectLoadableCollections();
   }

   public void validateScrollability() throws HibernateException {
      if (this.getCollectionPersisters() != null) {
         throw new HibernateException("Cannot scroll queries which initialize collections");
      }
   }

   public boolean containsCollectionFetches() {
      return false;
   }

   public boolean isManipulationStatement() {
      return false;
   }

   public Class getDynamicInstantiationResultType() {
      return this.holderClass;
   }

   public ParameterTranslations getParameterTranslations() {
      return new ParameterTranslations() {
         public boolean supportsOrdinalParameterMetadata() {
            return false;
         }

         public int getOrdinalParameterCount() {
            return 0;
         }

         public int getOrdinalParameterSqlLocation(int ordinalPosition) {
            return 0;
         }

         public Type getOrdinalParameterExpectedType(int ordinalPosition) {
            return null;
         }

         public Set getNamedParameterNames() {
            return QueryTranslatorImpl.this.namedParameters.keySet();
         }

         public int[] getNamedParameterSqlLocations(String name) {
            return QueryTranslatorImpl.this.getNamedParameterLocs(name);
         }

         public Type getNamedParameterExpectedType(String name) {
            return null;
         }
      };
   }
}
