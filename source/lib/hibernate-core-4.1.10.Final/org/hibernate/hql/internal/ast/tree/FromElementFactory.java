package org.hibernate.hql.internal.ast.tree;

import antlr.ASTFactory;
import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.AliasGenerator;
import org.hibernate.hql.internal.ast.util.PathHelper;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class FromElementFactory implements SqlTokenTypes {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FromElementFactory.class.getName());
   private FromClause fromClause;
   private FromElement origin;
   private String path;
   private String classAlias;
   private String[] columns;
   private boolean implied;
   private boolean inElementsFunction;
   private boolean collection;
   private QueryableCollection queryableCollection;
   private CollectionType collectionType;

   public FromElementFactory(FromClause fromClause, FromElement origin, String path) {
      this.fromClause = fromClause;
      this.origin = origin;
      this.path = path;
      this.collection = false;
   }

   public FromElementFactory(FromClause fromClause, FromElement origin, String path, String classAlias, String[] columns, boolean implied) {
      this(fromClause, origin, path);
      this.classAlias = classAlias;
      this.columns = columns;
      this.implied = implied;
      this.collection = true;
   }

   FromElement addFromElement() throws SemanticException {
      FromClause parentFromClause = this.fromClause.getParentFromClause();
      if (parentFromClause != null) {
         String pathAlias = PathHelper.getAlias(this.path);
         FromElement parentFromElement = parentFromClause.getFromElement(pathAlias);
         if (parentFromElement != null) {
            return this.createFromElementInSubselect(this.path, pathAlias, parentFromElement, this.classAlias);
         }
      }

      EntityPersister entityPersister = this.fromClause.getSessionFactoryHelper().requireClassPersister(this.path);
      FromElement elem = this.createAndAddFromElement(this.path, this.classAlias, entityPersister, (EntityType)((Queryable)entityPersister).getType(), (String)null);
      this.fromClause.getWalker().addQuerySpaces(entityPersister.getQuerySpaces());
      return elem;
   }

   private FromElement createFromElementInSubselect(String path, String pathAlias, FromElement parentFromElement, String classAlias) throws SemanticException {
      LOG.debugf("createFromElementInSubselect() : path = %s", path);
      FromElement fromElement = this.evaluateFromElementPath(path, classAlias);
      EntityPersister entityPersister = fromElement.getEntityPersister();
      String tableAlias = null;
      boolean correlatedSubselect = pathAlias.equals(parentFromElement.getClassAlias());
      if (correlatedSubselect) {
         tableAlias = fromElement.getTableAlias();
      } else {
         tableAlias = null;
      }

      if (fromElement.getFromClause() != this.fromClause) {
         LOG.debug("createFromElementInSubselect() : creating a new FROM element...");
         fromElement = this.createFromElement(entityPersister);
         this.initializeAndAddFromElement(fromElement, path, classAlias, entityPersister, (EntityType)((Queryable)entityPersister).getType(), tableAlias);
      }

      LOG.debugf("createFromElementInSubselect() : %s -> %s", path, fromElement);
      return fromElement;
   }

   private FromElement evaluateFromElementPath(String path, String classAlias) throws SemanticException {
      ASTFactory factory = this.fromClause.getASTFactory();
      FromReferenceNode pathNode = (FromReferenceNode)PathHelper.parsePath(path, factory);
      pathNode.recursiveResolve(0, false, classAlias, (AST)null);
      return pathNode.getImpliedJoin() != null ? pathNode.getImpliedJoin() : pathNode.getFromElement();
   }

   FromElement createCollectionElementsJoin(QueryableCollection queryableCollection, String collectionName) throws SemanticException {
      JoinSequence collectionJoinSequence = this.fromClause.getSessionFactoryHelper().createCollectionJoinSequence(queryableCollection, collectionName);
      this.queryableCollection = queryableCollection;
      return this.createCollectionJoin(collectionJoinSequence, (String)null);
   }

   FromElement createCollection(QueryableCollection queryableCollection, String role, JoinType joinType, boolean fetchFlag, boolean indexed) throws SemanticException {
      if (!this.collection) {
         throw new IllegalStateException("FromElementFactory not initialized for collections!");
      } else {
         this.inElementsFunction = indexed;
         this.queryableCollection = queryableCollection;
         this.collectionType = queryableCollection.getCollectionType();
         String roleAlias = this.fromClause.getAliasGenerator().createName(role);
         boolean explicitSubqueryFromElement = this.fromClause.isSubQuery() && !this.implied;
         if (explicitSubqueryFromElement) {
            String pathRoot = StringHelper.root(this.path);
            FromElement origin = this.fromClause.getFromElement(pathRoot);
            if (origin == null || origin.getFromClause() != this.fromClause) {
               this.implied = true;
            }
         }

         if (explicitSubqueryFromElement && DotNode.useThetaStyleImplicitJoins) {
            this.implied = true;
         }

         Type elementType = queryableCollection.getElementType();
         FromElement elem;
         if (elementType.isEntityType()) {
            elem = this.createEntityAssociation(role, roleAlias, joinType);
         } else if (elementType.isComponentType()) {
            JoinSequence joinSequence = this.createJoinSequence(roleAlias, joinType);
            elem = this.createCollectionJoin(joinSequence, roleAlias);
         } else {
            JoinSequence joinSequence = this.createJoinSequence(roleAlias, joinType);
            elem = this.createCollectionJoin(joinSequence, roleAlias);
         }

         elem.setRole(role);
         elem.setQueryableCollection(queryableCollection);
         if (this.implied) {
            elem.setIncludeSubclasses(false);
         }

         if (explicitSubqueryFromElement) {
            elem.setInProjectionList(true);
         }

         if (fetchFlag) {
            elem.setFetch(true);
         }

         return elem;
      }
   }

   FromElement createEntityJoin(String entityClass, String tableAlias, JoinSequence joinSequence, boolean fetchFlag, boolean inFrom, EntityType type) throws SemanticException {
      FromElement elem = this.createJoin(entityClass, tableAlias, joinSequence, type, false);
      elem.setFetch(fetchFlag);
      EntityPersister entityPersister = elem.getEntityPersister();
      int numberOfTables = entityPersister.getQuerySpaces().length;
      if (numberOfTables > 1 && this.implied && !elem.useFromFragment()) {
         LOG.debug("createEntityJoin() : Implied multi-table entity join");
         elem.setUseFromFragment(true);
      }

      if (this.implied && inFrom) {
         joinSequence.setUseThetaStyle(false);
         elem.setUseFromFragment(true);
         elem.setImpliedInFromClause(true);
      }

      if (elem.getWalker().isSubQuery() && (elem.getFromClause() != elem.getOrigin().getFromClause() || DotNode.useThetaStyleImplicitJoins)) {
         elem.setType(134);
         joinSequence.setUseThetaStyle(true);
         elem.setUseFromFragment(false);
      }

      return elem;
   }

   public FromElement createComponentJoin(ComponentType type) {
      return new ComponentJoin(this.fromClause, this.origin, this.classAlias, this.path, type);
   }

   FromElement createElementJoin(QueryableCollection queryableCollection) throws SemanticException {
      this.implied = true;
      this.inElementsFunction = true;
      Type elementType = queryableCollection.getElementType();
      if (!elementType.isEntityType()) {
         throw new IllegalArgumentException("Cannot create element join for a collection of non-entities!");
      } else {
         this.queryableCollection = queryableCollection;
         SessionFactoryHelper sfh = this.fromClause.getSessionFactoryHelper();
         FromElement destination = null;
         String tableAlias = null;
         EntityPersister entityPersister = queryableCollection.getElementPersister();
         tableAlias = this.fromClause.getAliasGenerator().createName(entityPersister.getEntityName());
         String associatedEntityName = entityPersister.getEntityName();
         EntityPersister targetEntityPersister = sfh.requireClassPersister(associatedEntityName);
         destination = this.createAndAddFromElement(associatedEntityName, this.classAlias, targetEntityPersister, (EntityType)queryableCollection.getElementType(), tableAlias);
         if (this.implied) {
            destination.setIncludeSubclasses(false);
         }

         this.fromClause.addCollectionJoinFromElementByPath(this.path, destination);
         this.fromClause.getWalker().addQuerySpaces(entityPersister.getQuerySpaces());
         CollectionType type = queryableCollection.getCollectionType();
         String role = type.getRole();
         String roleAlias = this.origin.getTableAlias();
         String[] targetColumns = sfh.getCollectionElementColumns(role, roleAlias);
         AssociationType elementAssociationType = sfh.getElementAssociationType(type);
         JoinType joinType = JoinType.INNER_JOIN;
         JoinSequence joinSequence = sfh.createJoinSequence(this.implied, elementAssociationType, tableAlias, joinType, targetColumns);
         FromElement elem = this.initializeJoin(this.path, destination, joinSequence, targetColumns, this.origin, false);
         elem.setUseFromFragment(true);
         elem.setCollectionTableAlias(roleAlias);
         return elem;
      }
   }

   private FromElement createCollectionJoin(JoinSequence collectionJoinSequence, String tableAlias) throws SemanticException {
      String text = this.queryableCollection.getTableName();
      AST ast = this.createFromElement(text);
      FromElement destination = (FromElement)ast;
      Type elementType = this.queryableCollection.getElementType();
      if (elementType.isCollectionType()) {
         throw new SemanticException("Collections of collections are not supported!");
      } else {
         destination.initializeCollection(this.fromClause, this.classAlias, tableAlias);
         destination.setType(136);
         destination.setIncludeSubclasses(false);
         destination.setCollectionJoin(true);
         destination.setJoinSequence(collectionJoinSequence);
         destination.setOrigin(this.origin, false);
         destination.setCollectionTableAlias(tableAlias);
         this.origin.setText("");
         this.origin.setCollectionJoin(true);
         this.fromClause.addCollectionJoinFromElementByPath(this.path, destination);
         this.fromClause.getWalker().addQuerySpaces(this.queryableCollection.getCollectionSpaces());
         return destination;
      }
   }

   private FromElement createEntityAssociation(String role, String roleAlias, JoinType joinType) throws SemanticException {
      Queryable entityPersister = (Queryable)this.queryableCollection.getElementPersister();
      String associatedEntityName = entityPersister.getEntityName();
      FromElement elem;
      if (this.queryableCollection.isOneToMany()) {
         LOG.debugf("createEntityAssociation() : One to many - path = %s role = %s associatedEntityName = %s", this.path, role, associatedEntityName);
         JoinSequence joinSequence = this.createJoinSequence(roleAlias, joinType);
         elem = this.createJoin(associatedEntityName, roleAlias, joinSequence, (EntityType)this.queryableCollection.getElementType(), false);
      } else {
         LOG.debugf("createManyToMany() : path = %s role = %s associatedEntityName = %s", this.path, role, associatedEntityName);
         elem = this.createManyToMany(role, associatedEntityName, roleAlias, entityPersister, (EntityType)this.queryableCollection.getElementType(), joinType);
         this.fromClause.getWalker().addQuerySpaces(this.queryableCollection.getCollectionSpaces());
      }

      elem.setCollectionTableAlias(roleAlias);
      return elem;
   }

   private FromElement createJoin(String entityClass, String tableAlias, JoinSequence joinSequence, EntityType type, boolean manyToMany) throws SemanticException {
      EntityPersister entityPersister = this.fromClause.getSessionFactoryHelper().requireClassPersister(entityClass);
      FromElement destination = this.createAndAddFromElement(entityClass, this.classAlias, entityPersister, type, tableAlias);
      return this.initializeJoin(this.path, destination, joinSequence, this.getColumns(), this.origin, manyToMany);
   }

   private FromElement createManyToMany(String role, String associatedEntityName, String roleAlias, Queryable entityPersister, EntityType type, JoinType joinType) throws SemanticException {
      SessionFactoryHelper sfh = this.fromClause.getSessionFactoryHelper();
      FromElement elem;
      if (this.inElementsFunction) {
         JoinSequence joinSequence = this.createJoinSequence(roleAlias, joinType);
         elem = this.createJoin(associatedEntityName, roleAlias, joinSequence, type, true);
      } else {
         String tableAlias = this.fromClause.getAliasGenerator().createName(entityPersister.getEntityName());
         String[] secondJoinColumns = sfh.getCollectionElementColumns(role, roleAlias);
         JoinSequence joinSequence = this.createJoinSequence(roleAlias, joinType);
         joinSequence.addJoin(sfh.getElementAssociationType(this.collectionType), tableAlias, joinType, secondJoinColumns);
         elem = this.createJoin(associatedEntityName, tableAlias, joinSequence, type, false);
         elem.setUseFromFragment(true);
      }

      return elem;
   }

   private JoinSequence createJoinSequence(String roleAlias, JoinType joinType) {
      SessionFactoryHelper sessionFactoryHelper = this.fromClause.getSessionFactoryHelper();
      String[] joinColumns = this.getColumns();
      if (this.collectionType == null) {
         throw new IllegalStateException("collectionType is null!");
      } else {
         return sessionFactoryHelper.createJoinSequence(this.implied, this.collectionType, roleAlias, joinType, joinColumns);
      }
   }

   private FromElement createAndAddFromElement(String className, String classAlias, EntityPersister entityPersister, EntityType type, String tableAlias) {
      if (!(entityPersister instanceof Joinable)) {
         throw new IllegalArgumentException("EntityPersister " + entityPersister + " does not implement Joinable!");
      } else {
         FromElement element = this.createFromElement(entityPersister);
         this.initializeAndAddFromElement(element, className, classAlias, entityPersister, type, tableAlias);
         return element;
      }
   }

   private void initializeAndAddFromElement(FromElement element, String className, String classAlias, EntityPersister entityPersister, EntityType type, String tableAlias) {
      if (tableAlias == null) {
         AliasGenerator aliasGenerator = this.fromClause.getAliasGenerator();
         tableAlias = aliasGenerator.createName(entityPersister.getEntityName());
      }

      element.initializeEntity(this.fromClause, className, entityPersister, type, classAlias, tableAlias);
   }

   private FromElement createFromElement(EntityPersister entityPersister) {
      Joinable joinable = (Joinable)entityPersister;
      String text = joinable.getTableName();
      AST ast = this.createFromElement(text);
      FromElement element = (FromElement)ast;
      return element;
   }

   private AST createFromElement(String text) {
      AST ast = ASTUtil.create(this.fromClause.getASTFactory(), this.implied ? 135 : 134, text);
      ast.setType(134);
      return ast;
   }

   private FromElement initializeJoin(String path, FromElement destination, JoinSequence joinSequence, String[] columns, FromElement origin, boolean manyToMany) {
      destination.setType(136);
      destination.setJoinSequence(joinSequence);
      destination.setColumns(columns);
      destination.setOrigin(origin, manyToMany);
      this.fromClause.addJoinByPathMap(path, destination);
      return destination;
   }

   private String[] getColumns() {
      if (this.columns == null) {
         throw new IllegalStateException("No foriegn key columns were supplied!");
      } else {
         return this.columns;
      }
   }
}
