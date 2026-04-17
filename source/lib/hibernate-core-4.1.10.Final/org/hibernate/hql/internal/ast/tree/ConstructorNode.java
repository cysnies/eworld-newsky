package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.QueryException;
import org.hibernate.hql.internal.ast.DetailedSemanticException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.Type;

public class ConstructorNode extends SelectExpressionList implements AggregatedSelectExpression {
   private Class resultType;
   private Constructor constructor;
   private Type[] constructorArgumentTypes;
   private boolean isMap;
   private boolean isList;
   private String[] aggregatedAliases;

   public ResultTransformer getResultTransformer() {
      if (this.constructor != null) {
         return new AliasToBeanConstructorResultTransformer(this.constructor);
      } else if (this.isMap) {
         return Transformers.ALIAS_TO_ENTITY_MAP;
      } else if (this.isList) {
         return Transformers.TO_LIST;
      } else {
         throw new QueryException("Unable to determine proper dynamic-instantiation tranformer to use.");
      }
   }

   public String[] getAggregatedAliases() {
      if (this.aggregatedAliases == null) {
         this.aggregatedAliases = this.buildAggregatedAliases();
      }

      return this.aggregatedAliases;
   }

   private String[] buildAggregatedAliases() {
      SelectExpression[] selectExpressions = this.collectSelectExpressions();
      String[] aliases = new String[selectExpressions.length];

      for(int i = 0; i < selectExpressions.length; ++i) {
         String alias = selectExpressions[i].getAlias();
         aliases[i] = alias == null ? Integer.toString(i) : alias;
      }

      return aliases;
   }

   public void setScalarColumn(int i) throws SemanticException {
      SelectExpression[] selectExpressions = this.collectSelectExpressions();

      for(int j = 0; j < selectExpressions.length; ++j) {
         SelectExpression selectExpression = selectExpressions[j];
         selectExpression.setScalarColumn(j);
      }

   }

   public int getScalarColumnIndex() {
      return -1;
   }

   public void setScalarColumnText(int i) throws SemanticException {
      SelectExpression[] selectExpressions = this.collectSelectExpressions();

      for(int j = 0; j < selectExpressions.length; ++j) {
         SelectExpression selectExpression = selectExpressions[j];
         selectExpression.setScalarColumnText(j);
      }

   }

   protected AST getFirstSelectExpression() {
      return this.getFirstChild().getNextSibling();
   }

   public Class getAggregationResultType() {
      return this.resultType;
   }

   /** @deprecated */
   @Deprecated
   public Type getDataType() {
      throw new UnsupportedOperationException("getDataType() is not supported by ConstructorNode!");
   }

   public void prepare() throws SemanticException {
      this.constructorArgumentTypes = this.resolveConstructorArgumentTypes();
      String path = ((PathNode)this.getFirstChild()).getPath();
      if ("map".equals(path.toLowerCase())) {
         this.isMap = true;
         this.resultType = Map.class;
      } else if ("list".equals(path.toLowerCase())) {
         this.isList = true;
         this.resultType = List.class;
      } else {
         this.constructor = this.resolveConstructor(path);
         this.resultType = this.constructor.getDeclaringClass();
      }

   }

   private Type[] resolveConstructorArgumentTypes() throws SemanticException {
      SelectExpression[] argumentExpressions = this.collectSelectExpressions();
      if (argumentExpressions == null) {
         return new Type[0];
      } else {
         Type[] types = new Type[argumentExpressions.length];

         for(int x = 0; x < argumentExpressions.length; ++x) {
            types[x] = argumentExpressions[x].getDataType();
         }

         return types;
      }
   }

   private Constructor resolveConstructor(String path) throws SemanticException {
      String importedClassName = this.getSessionFactoryHelper().getImportedClassName(path);
      String className = StringHelper.isEmpty(importedClassName) ? path : importedClassName;
      if (className == null) {
         throw new SemanticException("Unable to locate class [" + path + "]");
      } else {
         try {
            Class holderClass = ReflectHelper.classForName(className);
            return ReflectHelper.getConstructor(holderClass, this.constructorArgumentTypes);
         } catch (ClassNotFoundException e) {
            throw new DetailedSemanticException("Unable to locate class [" + className + "]", e);
         } catch (PropertyNotFoundException e) {
            throw new DetailedSemanticException("Unable to locate appropriate constructor on class [" + className + "]", e);
         }
      }
   }

   public Constructor getConstructor() {
      return this.constructor;
   }

   public List getConstructorArgumentTypeList() {
      return Arrays.asList(this.constructorArgumentTypes);
   }

   public List getAggregatedSelectionTypeList() {
      return this.getConstructorArgumentTypeList();
   }

   public FromElement getFromElement() {
      return null;
   }

   public boolean isConstructor() {
      return true;
   }

   public boolean isReturnableEntity() throws SemanticException {
      return false;
   }

   public boolean isScalar() {
      return true;
   }

   public void setAlias(String alias) {
      throw new UnsupportedOperationException("constructor may not be aliased");
   }

   public String getAlias() {
      throw new UnsupportedOperationException("constructor may not be aliased");
   }
}
