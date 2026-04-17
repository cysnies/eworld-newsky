package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.hql.internal.CollectionProperties;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.JoinType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class DotNode extends FromReferenceNode implements DisplayableNode, SelectExpression {
   public static boolean useThetaStyleImplicitJoins = false;
   public static boolean REGRESSION_STYLE_JOIN_SUPPRESSION = false;
   public static final IllegalCollectionDereferenceExceptionBuilder DEF_ILLEGAL_COLL_DEREF_EXCP_BUILDER = new IllegalCollectionDereferenceExceptionBuilder() {
      public QueryException buildIllegalCollectionDereferenceException(String propertyName, FromReferenceNode lhs) {
         String lhsPath = ASTUtil.getPathText(lhs);
         return new QueryException("illegal attempt to dereference collection [" + lhsPath + "] with element property reference [" + propertyName + "]");
      }
   };
   public static IllegalCollectionDereferenceExceptionBuilder ILLEGAL_COLL_DEREF_EXCP_BUILDER;
   private static final CoreMessageLogger LOG;
   private static final int DEREF_UNKNOWN = 0;
   private static final int DEREF_ENTITY = 1;
   private static final int DEREF_COMPONENT = 2;
   private static final int DEREF_COLLECTION = 3;
   private static final int DEREF_PRIMITIVE = 4;
   private static final int DEREF_IDENTIFIER = 5;
   private static final int DEREF_JAVA_CONSTANT = 6;
   private String propertyName;
   private String path;
   private String propertyPath;
   private String[] columns;
   private JoinType joinType;
   private boolean fetch;
   private int dereferenceType;
   private FromElement impliedJoin;

   public DotNode() {
      this.joinType = JoinType.INNER_JOIN;
      this.fetch = false;
      this.dereferenceType = 0;
   }

   public void setJoinType(JoinType joinType) {
      this.joinType = joinType;
   }

   private String[] getColumns() throws QueryException {
      if (this.columns == null) {
         String tableAlias = this.getLhs().getFromElement().getTableAlias();
         this.columns = this.getFromElement().toColumns(tableAlias, this.propertyPath, false);
      }

      return this.columns;
   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      FromElement fromElement = this.getFromElement();
      buf.append("{propertyName=").append(this.propertyName);
      buf.append(",dereferenceType=").append(this.getWalker().getASTPrinter().getTokenTypeName(this.dereferenceType));
      buf.append(",propertyPath=").append(this.propertyPath);
      buf.append(",path=").append(this.getPath());
      if (fromElement != null) {
         buf.append(",tableAlias=").append(fromElement.getTableAlias());
         buf.append(",className=").append(fromElement.getClassName());
         buf.append(",classAlias=").append(fromElement.getClassAlias());
      } else {
         buf.append(",no from element");
      }

      buf.append('}');
      return buf.toString();
   }

   public void resolveFirstChild() throws SemanticException {
      FromReferenceNode lhs = (FromReferenceNode)this.getFirstChild();
      SqlNode property = (SqlNode)lhs.getNextSibling();
      String propName = property.getText();
      this.propertyName = propName;
      if (this.propertyPath == null) {
         this.propertyPath = propName;
      }

      lhs.resolve(true, true, (String)null, this);
      this.setFromElement(lhs.getFromElement());
      this.checkSubclassOrSuperclassPropertyReference(lhs, propName);
   }

   public void resolveInFunctionCall(boolean generateJoin, boolean implicitJoin) throws SemanticException {
      if (!this.isResolved()) {
         Type propertyType = this.prepareLhs();
         if (propertyType != null && propertyType.isCollectionType()) {
            this.resolveIndex((AST)null);
         } else {
            this.resolveFirstChild();
            super.resolve(generateJoin, implicitJoin);
         }

      }
   }

   public void resolveIndex(AST parent) throws SemanticException {
      if (!this.isResolved()) {
         Type propertyType = this.prepareLhs();
         this.dereferenceCollection((CollectionType)propertyType, true, true, (String)null, parent);
      }
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias, AST parent) throws SemanticException {
      if (!this.isResolved()) {
         Type propertyType = this.prepareLhs();
         if (propertyType == null) {
            if (parent == null) {
               this.getWalker().getLiteralProcessor().lookupConstant(this);
            }

         } else {
            if (propertyType.isComponentType()) {
               this.checkLhsIsNotCollection();
               this.dereferenceComponent(parent);
               this.initText();
            } else if (propertyType.isEntityType()) {
               this.checkLhsIsNotCollection();
               this.dereferenceEntity((EntityType)propertyType, implicitJoin, classAlias, generateJoin, parent);
               this.initText();
            } else if (propertyType.isCollectionType()) {
               this.checkLhsIsNotCollection();
               this.dereferenceCollection((CollectionType)propertyType, implicitJoin, false, classAlias, parent);
            } else {
               if (!CollectionProperties.isAnyCollectionProperty(this.propertyName)) {
                  this.checkLhsIsNotCollection();
               }

               this.dereferenceType = 4;
               this.initText();
            }

            this.setResolved();
         }
      }
   }

   private void initText() {
      String[] cols = this.getColumns();
      String text = StringHelper.join(", ", cols);
      if (cols.length > 1 && this.getWalker().isComparativeExpressionClause()) {
         text = "(" + text + ")";
      }

      this.setText(text);
   }

   private Type prepareLhs() throws SemanticException {
      FromReferenceNode lhs = this.getLhs();
      lhs.prepareForDot(this.propertyName);
      return this.getDataType();
   }

   private void dereferenceCollection(CollectionType collectionType, boolean implicitJoin, boolean indexed, String classAlias, AST parent) throws SemanticException {
      this.dereferenceType = 3;
      String role = collectionType.getRole();
      boolean isSizeProperty = this.getNextSibling() != null && CollectionProperties.isAnyCollectionProperty(this.getNextSibling().getText());
      if (isSizeProperty) {
         indexed = true;
      }

      QueryableCollection queryableCollection = this.getSessionFactoryHelper().requireQueryableCollection(role);
      String propName = this.getPath();
      FromClause currentFromClause = this.getWalker().getCurrentFromClause();
      if (this.getWalker().getStatementType() != 45 && indexed && classAlias == null) {
         String alias = this.getLhs().getFromElement().getQueryable().getTableName();
         this.columns = this.getFromElement().toColumns(alias, this.propertyPath, false, true);
      }

      FromElementFactory factory = new FromElementFactory(currentFromClause, this.getLhs().getFromElement(), propName, classAlias, this.getColumns(), implicitJoin);
      FromElement elem = factory.createCollection(queryableCollection, role, this.joinType, this.fetch, indexed);
      LOG.debugf("dereferenceCollection() : Created new FROM element for %s : %s", propName, elem);
      this.setImpliedJoin(elem);
      this.setFromElement(elem);
      if (isSizeProperty) {
         elem.setText("");
         elem.setUseWhereFragment(false);
      }

      if (!implicitJoin) {
         EntityPersister entityPersister = elem.getEntityPersister();
         if (entityPersister != null) {
            this.getWalker().addQuerySpaces(entityPersister.getQuerySpaces());
         }
      }

      this.getWalker().addQuerySpaces(queryableCollection.getCollectionSpaces());
   }

   private void dereferenceEntity(EntityType entityType, boolean implicitJoin, String classAlias, boolean generateJoin, AST parent) throws SemanticException {
      this.checkForCorrelatedSubquery("dereferenceEntity");
      DotNode parentAsDotNode = null;
      String property = this.propertyName;
      boolean joinIsNeeded;
      if (this.isDotNode(parent)) {
         parentAsDotNode = (DotNode)parent;
         property = parentAsDotNode.propertyName;
         joinIsNeeded = generateJoin && !this.isReferenceToPrimaryKey(parentAsDotNode.propertyName, entityType);
      } else if (!this.getWalker().isSelectStatement()) {
         joinIsNeeded = this.getWalker().getCurrentStatementType() == 45 && this.getWalker().isInFrom();
      } else if (REGRESSION_STYLE_JOIN_SUPPRESSION) {
         joinIsNeeded = generateJoin && (!this.getWalker().isInSelect() || !this.getWalker().isShallowQuery());
      } else {
         joinIsNeeded = generateJoin || this.getWalker().isInSelect() || this.getWalker().isInFrom();
      }

      if (joinIsNeeded) {
         this.dereferenceEntityJoin(classAlias, entityType, implicitJoin, parent);
      } else {
         this.dereferenceEntityIdentifier(property, parentAsDotNode);
      }

   }

   private boolean isDotNode(AST n) {
      return n != null && n.getType() == 15;
   }

   private void dereferenceEntityJoin(String classAlias, EntityType propertyType, boolean impliedJoin, AST parent) throws SemanticException {
      this.dereferenceType = 1;
      if (LOG.isDebugEnabled()) {
         LOG.debugf("dereferenceEntityJoin() : generating join for %s in %s (%s) parent = %s", new Object[]{this.propertyName, this.getFromElement().getClassName(), classAlias == null ? "<no alias>" : classAlias, ASTUtil.getDebugString(parent)});
      }

      String associatedEntityName = propertyType.getAssociatedEntityName();
      String tableAlias = this.getAliasGenerator().createName(associatedEntityName);
      String[] joinColumns = this.getColumns();
      String joinPath = this.getPath();
      if (impliedJoin && this.getWalker().isInFrom()) {
         this.joinType = this.getWalker().getImpliedJoinType();
      }

      FromClause currentFromClause = this.getWalker().getCurrentFromClause();
      FromElement elem = currentFromClause.findJoinByPath(joinPath);
      boolean found = elem != null;
      boolean useFoundFromElement = found && (elem.isImplied() || this.areSame(classAlias, elem.getClassAlias()));
      if (!useFoundFromElement) {
         JoinSequence joinSequence = this.getSessionFactoryHelper().createJoinSequence(impliedJoin, propertyType, tableAlias, this.joinType, joinColumns);

         FromElement lhsFromElement;
         for(lhsFromElement = this.getLhs().getFromElement(); lhsFromElement != null && ComponentJoin.class.isInstance(lhsFromElement); lhsFromElement = lhsFromElement.getOrigin()) {
         }

         if (lhsFromElement == null) {
            throw new QueryException("Unable to locate appropriate lhs");
         }

         FromElementFactory factory = new FromElementFactory(currentFromClause, lhsFromElement, joinPath, classAlias, joinColumns, impliedJoin);
         elem = factory.createEntityJoin(associatedEntityName, tableAlias, joinSequence, this.fetch, this.getWalker().isInFrom(), propertyType);
      } else {
         currentFromClause.addDuplicateAlias(classAlias, elem);
      }

      this.setImpliedJoin(elem);
      this.getWalker().addQuerySpaces(elem.getEntityPersister().getQuerySpaces());
      this.setFromElement(elem);
   }

   private boolean areSame(String alias1, String alias2) {
      return !StringHelper.isEmpty(alias1) && !StringHelper.isEmpty(alias2) && alias1.equals(alias2);
   }

   private void setImpliedJoin(FromElement elem) {
      this.impliedJoin = elem;
      if (this.getFirstChild().getType() == 15) {
         DotNode dotLhs = (DotNode)this.getFirstChild();
         if (dotLhs.getImpliedJoin() != null) {
            this.impliedJoin = dotLhs.getImpliedJoin();
         }
      }

   }

   public FromElement getImpliedJoin() {
      return this.impliedJoin;
   }

   private boolean isReferenceToPrimaryKey(String propertyName, EntityType owningType) {
      EntityPersister persister = this.getSessionFactoryHelper().getFactory().getEntityPersister(owningType.getAssociatedEntityName());
      if (persister.getEntityMetamodel().hasNonIdentifierPropertyNamedId()) {
         return propertyName.equals(persister.getIdentifierPropertyName()) && owningType.isReferenceToPrimaryKey();
      } else if ("id".equals(propertyName)) {
         return owningType.isReferenceToPrimaryKey();
      } else {
         String keyPropertyName = this.getSessionFactoryHelper().getIdentifierOrUniqueKeyPropertyName(owningType);
         return keyPropertyName != null && keyPropertyName.equals(propertyName) && owningType.isReferenceToPrimaryKey();
      }
   }

   private void checkForCorrelatedSubquery(String methodName) {
      if (this.isCorrelatedSubselect()) {
         LOG.debugf("%s() : correlated subquery", methodName);
      }

   }

   private boolean isCorrelatedSubselect() {
      return this.getWalker().isSubQuery() && this.getFromElement().getFromClause() != this.getWalker().getCurrentFromClause();
   }

   private void checkLhsIsNotCollection() throws SemanticException {
      if (this.getLhs().getDataType() != null && this.getLhs().getDataType().isCollectionType()) {
         throw ILLEGAL_COLL_DEREF_EXCP_BUILDER.buildIllegalCollectionDereferenceException(this.propertyName, this.getLhs());
      }
   }

   private void dereferenceComponent(AST parent) {
      this.dereferenceType = 2;
      this.setPropertyNameAndPath(parent);
   }

   private void dereferenceEntityIdentifier(String propertyName, DotNode dotParent) {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("dereferenceShortcut() : property %s in %s does not require a join.", propertyName, this.getFromElement().getClassName());
      }

      this.initText();
      this.setPropertyNameAndPath(dotParent);
      if (dotParent != null) {
         dotParent.dereferenceType = 5;
         dotParent.setText(this.getText());
         dotParent.columns = this.getColumns();
      }

   }

   private void setPropertyNameAndPath(AST parent) {
      if (this.isDotNode(parent)) {
         DotNode dotNode = (DotNode)parent;
         AST lhs = dotNode.getFirstChild();
         AST rhs = lhs.getNextSibling();
         this.propertyName = rhs.getText();
         this.propertyPath = this.propertyPath + "." + this.propertyName;
         dotNode.propertyPath = this.propertyPath;
         LOG.debugf("Unresolved property path is now '%s'", dotNode.propertyPath);
      } else {
         LOG.debugf("Terminal propertyPath = [%s]", this.propertyPath);
      }

   }

   public Type getDataType() {
      if (super.getDataType() == null) {
         FromElement fromElement = this.getLhs().getFromElement();
         if (fromElement == null) {
            return null;
         }

         Type propertyType = fromElement.getPropertyType(this.propertyName, this.propertyPath);
         LOG.debugf("getDataType() : %s -> %s", this.propertyPath, propertyType);
         super.setDataType(propertyType);
      }

      return super.getDataType();
   }

   public void setPropertyPath(String propertyPath) {
      this.propertyPath = propertyPath;
   }

   public String getPropertyPath() {
      return this.propertyPath;
   }

   public FromReferenceNode getLhs() {
      FromReferenceNode lhs = (FromReferenceNode)this.getFirstChild();
      if (lhs == null) {
         throw new IllegalStateException("DOT node with no left-hand-side!");
      } else {
         return lhs;
      }
   }

   public String getPath() {
      if (this.path == null) {
         FromReferenceNode lhs = this.getLhs();
         if (lhs == null) {
            this.path = this.getText();
         } else {
            SqlNode rhs = (SqlNode)lhs.getNextSibling();
            this.path = lhs.getPath() + "." + rhs.getOriginalText();
         }
      }

      return this.path;
   }

   public void setFetch(boolean fetch) {
      this.fetch = fetch;
   }

   public void setScalarColumnText(int i) throws SemanticException {
      String[] sqlColumns = this.getColumns();
      ColumnHelper.generateScalarColumns(this, sqlColumns, i);
   }

   public void resolveSelectExpression() throws SemanticException {
      if (!this.getWalker().isShallowQuery() && !this.getWalker().getCurrentFromClause().isSubQuery()) {
         this.resolve(true, false);
         Type type = this.getDataType();
         if (type.isEntityType()) {
            FromElement fromElement = this.getFromElement();
            fromElement.setIncludeSubclasses(true);
            if (useThetaStyleImplicitJoins) {
               fromElement.getJoinSequence().setUseThetaStyle(true);
               FromElement origin = fromElement.getOrigin();
               if (origin != null) {
                  ASTUtil.makeSiblingOfParent(origin, fromElement);
               }
            }
         }
      } else {
         this.resolve(false, true);
      }

      for(FromReferenceNode lhs = this.getLhs(); lhs != null; lhs = (FromReferenceNode)lhs.getFirstChild()) {
         this.checkSubclassOrSuperclassPropertyReference(lhs, lhs.getNextSibling().getText());
      }

   }

   public void setResolvedConstant(String text) {
      this.path = text;
      this.dereferenceType = 6;
      this.setResolved();
   }

   private boolean checkSubclassOrSuperclassPropertyReference(FromReferenceNode lhs, String propertyName) {
      if (lhs != null && !(lhs instanceof IndexNode)) {
         FromElement source = lhs.getFromElement();
         if (source != null) {
            source.handlePropertyBeingDereferenced(lhs.getDataType(), propertyName);
         }
      }

      return false;
   }

   static {
      ILLEGAL_COLL_DEREF_EXCP_BUILDER = DEF_ILLEGAL_COLL_DEREF_EXCP_BUILDER;
      LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DotNode.class.getName());
   }

   public interface IllegalCollectionDereferenceExceptionBuilder {
      QueryException buildIllegalCollectionDereferenceException(String var1, FromReferenceNode var2);
   }
}
