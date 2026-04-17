package org.hibernate.hql.internal.classic;

import java.util.LinkedList;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.hql.internal.CollectionSubqueryFactory;
import org.hibernate.persister.collection.CollectionPropertyMapping;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class PathExpressionParser implements Parser {
   private int dotcount;
   private String currentName;
   private String currentProperty;
   private String oneToOneOwnerName;
   private AssociationType ownerAssociationType;
   private String[] columns;
   private String collectionName;
   private String collectionOwnerName;
   private String collectionRole;
   private final StringBuilder componentPath = new StringBuilder();
   private Type type;
   private final StringBuilder path = new StringBuilder();
   private boolean ignoreInitialJoin;
   private boolean continuation;
   private JoinType joinType;
   private boolean useThetaStyleJoin;
   private PropertyMapping currentPropertyMapping;
   private JoinSequence joinSequence;
   private boolean expectingCollectionIndex;
   private LinkedList collectionElements;

   public PathExpressionParser() {
      this.joinType = JoinType.INNER_JOIN;
      this.useThetaStyleJoin = true;
      this.collectionElements = new LinkedList();
   }

   void setJoinType(JoinType joinType) {
      this.joinType = joinType;
   }

   void setUseThetaStyleJoin(boolean useThetaStyleJoin) {
      this.useThetaStyleJoin = useThetaStyleJoin;
   }

   private void addJoin(String name, AssociationType joinableType) throws QueryException {
      try {
         this.joinSequence.addJoin(joinableType, name, this.joinType, this.currentColumns());
      } catch (MappingException me) {
         throw new QueryException(me);
      }
   }

   private void addJoin(String name, AssociationType joinableType, String[] foreignKeyColumns) throws QueryException {
      try {
         this.joinSequence.addJoin(joinableType, name, this.joinType, foreignKeyColumns);
      } catch (MappingException me) {
         throw new QueryException(me);
      }
   }

   String continueFromManyToMany(String entityName, String[] joinColumns, QueryTranslatorImpl q) throws QueryException {
      this.start(q);
      this.continuation = true;
      this.currentName = q.createNameFor(entityName);
      q.addType(this.currentName, entityName);
      Queryable classPersister = q.getEntityPersister(entityName);
      this.addJoin(this.currentName, q.getFactory().getTypeResolver().getTypeFactory().manyToOne(entityName), joinColumns);
      this.currentPropertyMapping = classPersister;
      return this.currentName;
   }

   public void ignoreInitialJoin() {
      this.ignoreInitialJoin = true;
   }

   public void token(String token, QueryTranslatorImpl q) throws QueryException {
      if (token != null) {
         this.path.append(token);
      }

      String alias = q.getPathAlias(this.path.toString());
      if (alias != null) {
         this.reset(q);
         this.currentName = alias;
         this.currentPropertyMapping = q.getPropertyMapping(this.currentName);
         if (!this.ignoreInitialJoin) {
            JoinSequence ojf = q.getPathJoin(this.path.toString());

            try {
               this.joinSequence.addCondition(ojf.toJoinFragment(q.getEnabledFilters(), true).toWhereFragmentString());
            } catch (MappingException me) {
               throw new QueryException(me);
            }
         }
      } else if (".".equals(token)) {
         ++this.dotcount;
      } else if (this.dotcount == 0) {
         if (!this.continuation) {
            if (!q.isName(token)) {
               throw new QueryException("undefined alias: " + token);
            }

            this.currentName = token;
            this.currentPropertyMapping = q.getPropertyMapping(this.currentName);
         }
      } else if (this.dotcount == 1) {
         if (this.currentName != null) {
            this.currentProperty = token;
         } else {
            if (this.collectionName == null) {
               throw new QueryException("unexpected");
            }

            this.continuation = false;
         }
      } else {
         Type propertyType = this.getPropertyType();
         if (propertyType == null) {
            throw new QueryException("unresolved property: " + this.path);
         }

         if (propertyType.isComponentType()) {
            this.dereferenceComponent(token);
         } else if (propertyType.isEntityType()) {
            if (!this.isCollectionValued()) {
               this.dereferenceEntity(token, (EntityType)propertyType, q);
            }
         } else if (propertyType.isCollectionType()) {
            this.dereferenceCollection(token, ((CollectionType)propertyType).getRole(), q);
         } else if (token != null) {
            throw new QueryException("dereferenced: " + this.path);
         }
      }

   }

   private void dereferenceEntity(String propertyName, EntityType propertyType, QueryTranslatorImpl q) throws QueryException {
      boolean isIdShortcut = "id".equals(propertyName) && propertyType.isReferenceToPrimaryKey();

      String idPropertyName;
      try {
         idPropertyName = propertyType.getIdentifierOrUniqueKeyPropertyName(q.getFactory());
      } catch (MappingException me) {
         throw new QueryException(me);
      }

      boolean isNamedIdPropertyShortcut = idPropertyName != null && idPropertyName.equals(propertyName) && propertyType.isReferenceToPrimaryKey();
      if (!isIdShortcut && !isNamedIdPropertyShortcut) {
         String entityClass = propertyType.getAssociatedEntityName();
         String name = q.createNameFor(entityClass);
         q.addType(name, entityClass);
         this.addJoin(name, propertyType);
         if (propertyType.isOneToOne()) {
            this.oneToOneOwnerName = this.currentName;
         }

         this.ownerAssociationType = propertyType;
         this.currentName = name;
         this.currentProperty = propertyName;
         q.addPathAliasAndJoin(this.path.substring(0, this.path.toString().lastIndexOf(46)), name, this.joinSequence.copy());
         this.componentPath.setLength(0);
         this.currentPropertyMapping = q.getEntityPersister(entityClass);
      } else {
         if (this.componentPath.length() > 0) {
            this.componentPath.append('.');
         }

         this.componentPath.append(propertyName);
      }

   }

   private void dereferenceComponent(String propertyName) {
      if (propertyName != null) {
         if (this.componentPath.length() > 0) {
            this.componentPath.append('.');
         }

         this.componentPath.append(propertyName);
      }

   }

   private void dereferenceCollection(String propertyName, String role, QueryTranslatorImpl q) throws QueryException {
      this.collectionRole = role;
      QueryableCollection collPersister = q.getCollectionPersister(role);
      String name = q.createNameForCollection(role);
      this.addJoin(name, collPersister.getCollectionType());
      this.collectionName = name;
      this.collectionOwnerName = this.currentName;
      this.currentName = name;
      this.currentProperty = propertyName;
      this.componentPath.setLength(0);
      this.currentPropertyMapping = new CollectionPropertyMapping(collPersister);
   }

   private String getPropertyPath() {
      if (this.currentProperty == null) {
         return "id";
      } else {
         return this.componentPath.length() > 0 ? this.currentProperty + '.' + this.componentPath.toString() : this.currentProperty;
      }
   }

   private PropertyMapping getPropertyMapping() {
      return this.currentPropertyMapping;
   }

   private void setType() throws QueryException {
      if (this.currentProperty == null) {
         this.type = this.getPropertyMapping().getType();
      } else {
         this.type = this.getPropertyType();
      }

   }

   protected Type getPropertyType() throws QueryException {
      String propertyPath = this.getPropertyPath();
      Type propertyType = this.getPropertyMapping().toType(propertyPath);
      if (propertyType == null) {
         throw new QueryException("could not resolve property type: " + propertyPath);
      } else {
         return propertyType;
      }
   }

   protected String[] currentColumns() throws QueryException {
      String propertyPath = this.getPropertyPath();
      String[] propertyColumns = this.getPropertyMapping().toColumns(this.currentName, propertyPath);
      if (propertyColumns == null) {
         throw new QueryException("could not resolve property columns: " + propertyPath);
      } else {
         return propertyColumns;
      }
   }

   private void reset(QueryTranslatorImpl q) {
      this.dotcount = 0;
      this.currentName = null;
      this.currentProperty = null;
      this.collectionName = null;
      this.collectionRole = null;
      this.componentPath.setLength(0);
      this.type = null;
      this.collectionName = null;
      this.columns = null;
      this.expectingCollectionIndex = false;
      this.continuation = false;
      this.currentPropertyMapping = null;
   }

   public void start(QueryTranslatorImpl q) {
      if (!this.continuation) {
         this.reset(q);
         this.path.setLength(0);
         this.joinSequence = (new JoinSequence(q.getFactory())).setUseThetaStyle(this.useThetaStyleJoin);
      }

   }

   public void end(QueryTranslatorImpl q) throws QueryException {
      this.ignoreInitialJoin = false;
      Type propertyType = this.getPropertyType();
      if (propertyType != null && propertyType.isCollectionType()) {
         this.collectionRole = ((CollectionType)propertyType).getRole();
         this.collectionName = q.createNameForCollection(this.collectionRole);
         this.prepareForIndex(q);
      } else {
         this.columns = this.currentColumns();
         this.setType();
      }

      this.continuation = false;
   }

   private void prepareForIndex(QueryTranslatorImpl q) throws QueryException {
      QueryableCollection collPersister = q.getCollectionPersister(this.collectionRole);
      if (!collPersister.hasIndex()) {
         throw new QueryException("unindexed collection before []: " + this.path);
      } else {
         String[] indexCols = collPersister.getIndexColumnNames();
         if (indexCols.length != 1) {
            throw new QueryException("composite-index appears in []: " + this.path);
         } else {
            JoinSequence fromJoins = (new JoinSequence(q.getFactory())).setUseThetaStyle(this.useThetaStyleJoin).setRoot(collPersister, this.collectionName).setNext(this.joinSequence.copy());
            if (!this.continuation) {
               this.addJoin(this.collectionName, collPersister.getCollectionType());
            }

            this.joinSequence.addCondition(this.collectionName + '.' + indexCols[0] + " = ");
            CollectionElement elem = new CollectionElement();
            elem.elementColumns = collPersister.getElementColumnNames(this.collectionName);
            elem.elementType = collPersister.getElementType();
            elem.isOneToMany = collPersister.isOneToMany();
            elem.alias = this.collectionName;
            elem.joinSequence = this.joinSequence;
            this.collectionElements.addLast(elem);
            this.setExpectingCollectionIndex();
            q.addCollection(this.collectionName, this.collectionRole);
            q.addFromJoinOnly(this.collectionName, fromJoins);
         }
      }
   }

   public CollectionElement lastCollectionElement() {
      return (CollectionElement)this.collectionElements.removeLast();
   }

   public void setLastCollectionElementIndexValue(String value) {
      ((CollectionElement)this.collectionElements.getLast()).indexValue.append(value);
   }

   public boolean isExpectingCollectionIndex() {
      return this.expectingCollectionIndex;
   }

   protected void setExpectingCollectionIndex() throws QueryException {
      this.expectingCollectionIndex = true;
   }

   public JoinSequence getWhereJoin() {
      return this.joinSequence;
   }

   public String getWhereColumn() throws QueryException {
      if (this.columns.length != 1) {
         throw new QueryException("path expression ends in a composite value: " + this.path);
      } else {
         return this.columns[0];
      }
   }

   public String[] getWhereColumns() {
      return this.columns;
   }

   public Type getWhereColumnType() {
      return this.type;
   }

   public String getName() {
      return this.currentName == null ? this.collectionName : this.currentName;
   }

   public String getCollectionSubquery(Map enabledFilters) throws QueryException {
      return CollectionSubqueryFactory.createCollectionSubquery(this.joinSequence, enabledFilters, this.currentColumns());
   }

   public boolean isCollectionValued() throws QueryException {
      return this.collectionName != null && !this.getPropertyType().isCollectionType();
   }

   public void addAssociation(QueryTranslatorImpl q) throws QueryException {
      q.addJoin(this.getName(), this.joinSequence);
   }

   public String addFromAssociation(QueryTranslatorImpl q) throws QueryException {
      if (this.isCollectionValued()) {
         return this.addFromCollection(q);
      } else {
         q.addFrom(this.currentName, this.joinSequence);
         return this.currentName;
      }
   }

   public String addFromCollection(QueryTranslatorImpl q) throws QueryException {
      Type collectionElementType = this.getPropertyType();
      if (collectionElementType == null) {
         throw new QueryException("must specify 'elements' for collection valued property in from clause: " + this.path);
      } else if (collectionElementType.isEntityType()) {
         QueryableCollection collectionPersister = q.getCollectionPersister(this.collectionRole);
         Queryable entityPersister = (Queryable)collectionPersister.getElementPersister();
         String clazz = entityPersister.getEntityName();
         String elementName;
         if (collectionPersister.isOneToMany()) {
            elementName = this.collectionName;
            q.decoratePropertyMapping(elementName, collectionPersister);
         } else {
            q.addCollection(this.collectionName, this.collectionRole);
            elementName = q.createNameFor(clazz);
            this.addJoin(elementName, (AssociationType)collectionElementType);
         }

         q.addFrom(elementName, clazz, this.joinSequence);
         this.currentPropertyMapping = new CollectionPropertyMapping(collectionPersister);
         return elementName;
      } else {
         q.addFromCollection(this.collectionName, this.collectionRole, this.joinSequence);
         return this.collectionName;
      }
   }

   String getCollectionName() {
      return this.collectionName;
   }

   String getCollectionRole() {
      return this.collectionRole;
   }

   String getCollectionOwnerName() {
      return this.collectionOwnerName;
   }

   String getOneToOneOwnerName() {
      return this.oneToOneOwnerName;
   }

   AssociationType getOwnerAssociationType() {
      return this.ownerAssociationType;
   }

   String getCurrentProperty() {
      return this.currentProperty;
   }

   String getCurrentName() {
      return this.currentName;
   }

   public void fetch(QueryTranslatorImpl q, String entityName) throws QueryException {
      if (this.isCollectionValued()) {
         q.setCollectionToFetch(this.getCollectionRole(), this.getCollectionName(), this.getCollectionOwnerName(), entityName);
      } else {
         q.addEntityToFetch(entityName, this.getOneToOneOwnerName(), this.getOwnerAssociationType());
      }

   }

   static final class CollectionElement {
      Type elementType;
      boolean isOneToMany;
      String alias;
      String[] elementColumns;
      JoinSequence joinSequence;
      StringBuilder indexValue = new StringBuilder();
   }
}
