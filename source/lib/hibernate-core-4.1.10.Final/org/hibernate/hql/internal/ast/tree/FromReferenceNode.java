package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public abstract class FromReferenceNode extends AbstractSelectExpression implements ResolvableNode, DisplayableNode, InitializeableNode, PathNode {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FromReferenceNode.class.getName());
   private FromElement fromElement;
   private boolean resolved = false;
   public static final int ROOT_LEVEL = 0;

   public FromElement getFromElement() {
      return this.fromElement;
   }

   public void setFromElement(FromElement fromElement) {
      this.fromElement = fromElement;
   }

   public void resolveFirstChild() throws SemanticException {
   }

   public String getPath() {
      return this.getOriginalText();
   }

   public boolean isResolved() {
      return this.resolved;
   }

   public void setResolved() {
      this.resolved = true;
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Resolved : %s -> %s", this.getPath(), this.getText());
      }

   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      buf.append("{").append(this.fromElement == null ? "no fromElement" : this.fromElement.getDisplayText());
      buf.append("}");
      return buf.toString();
   }

   public void recursiveResolve(int level, boolean impliedAtRoot, String classAlias) throws SemanticException {
      this.recursiveResolve(level, impliedAtRoot, classAlias, this);
   }

   public void recursiveResolve(int level, boolean impliedAtRoot, String classAlias, AST parent) throws SemanticException {
      AST lhs = this.getFirstChild();
      int nextLevel = level + 1;
      if (lhs != null) {
         FromReferenceNode n = (FromReferenceNode)lhs;
         n.recursiveResolve(nextLevel, impliedAtRoot, (String)null, this);
      }

      this.resolveFirstChild();
      boolean impliedJoin = true;
      if (level == 0 && !impliedAtRoot) {
         impliedJoin = false;
      }

      this.resolve(true, impliedJoin, classAlias, parent);
   }

   public boolean isReturnableEntity() throws SemanticException {
      return !this.isScalar() && this.fromElement.isEntity();
   }

   public void resolveInFunctionCall(boolean generateJoin, boolean implicitJoin) throws SemanticException {
      this.resolve(generateJoin, implicitJoin);
   }

   public void resolve(boolean generateJoin, boolean implicitJoin) throws SemanticException {
      this.resolve(generateJoin, implicitJoin, (String)null);
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias) throws SemanticException {
      this.resolve(generateJoin, implicitJoin, classAlias, (AST)null);
   }

   public void prepareForDot(String propertyName) throws SemanticException {
   }

   public FromElement getImpliedJoin() {
      return null;
   }
}
