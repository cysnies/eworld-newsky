package org.hibernate.hql.internal.ast;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;
import java.lang.reflect.Constructor;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.tree.AggregateNode;
import org.hibernate.hql.internal.ast.tree.BetweenOperatorNode;
import org.hibernate.hql.internal.ast.tree.BinaryArithmeticOperatorNode;
import org.hibernate.hql.internal.ast.tree.BinaryLogicOperatorNode;
import org.hibernate.hql.internal.ast.tree.BooleanLiteralNode;
import org.hibernate.hql.internal.ast.tree.Case2Node;
import org.hibernate.hql.internal.ast.tree.CaseNode;
import org.hibernate.hql.internal.ast.tree.CollectionFunction;
import org.hibernate.hql.internal.ast.tree.ConstructorNode;
import org.hibernate.hql.internal.ast.tree.CountNode;
import org.hibernate.hql.internal.ast.tree.DeleteStatement;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.FromClause;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.IdentNode;
import org.hibernate.hql.internal.ast.tree.ImpliedFromElement;
import org.hibernate.hql.internal.ast.tree.InLogicOperatorNode;
import org.hibernate.hql.internal.ast.tree.IndexNode;
import org.hibernate.hql.internal.ast.tree.InitializeableNode;
import org.hibernate.hql.internal.ast.tree.InsertStatement;
import org.hibernate.hql.internal.ast.tree.IntoClause;
import org.hibernate.hql.internal.ast.tree.IsNotNullLogicOperatorNode;
import org.hibernate.hql.internal.ast.tree.IsNullLogicOperatorNode;
import org.hibernate.hql.internal.ast.tree.JavaConstantNode;
import org.hibernate.hql.internal.ast.tree.LiteralNode;
import org.hibernate.hql.internal.ast.tree.MapEntryNode;
import org.hibernate.hql.internal.ast.tree.MapKeyNode;
import org.hibernate.hql.internal.ast.tree.MapValueNode;
import org.hibernate.hql.internal.ast.tree.MethodNode;
import org.hibernate.hql.internal.ast.tree.OrderByClause;
import org.hibernate.hql.internal.ast.tree.ParameterNode;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.tree.ResultVariableRefNode;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.hql.internal.ast.tree.SelectExpressionImpl;
import org.hibernate.hql.internal.ast.tree.SessionFactoryAwareNode;
import org.hibernate.hql.internal.ast.tree.SqlFragment;
import org.hibernate.hql.internal.ast.tree.SqlNode;
import org.hibernate.hql.internal.ast.tree.UnaryArithmeticNode;
import org.hibernate.hql.internal.ast.tree.UnaryLogicOperatorNode;
import org.hibernate.hql.internal.ast.tree.UpdateStatement;

public class SqlASTFactory extends ASTFactory implements HqlSqlTokenTypes {
   private HqlSqlWalker walker;

   public SqlASTFactory(HqlSqlWalker walker) {
      this.walker = walker;
   }

   public Class getASTNodeType(int tokenType) {
      switch (tokenType) {
         case 10:
         case 82:
            return BetweenOperatorNode.class;
         case 11:
         case 14:
         case 16:
         case 18:
         case 21:
         case 23:
         case 24:
         case 25:
         case 28:
         case 31:
         case 32:
         case 33:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 42:
         case 43:
         case 44:
         case 46:
         case 47:
         case 48:
         case 50:
         case 52:
         case 53:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 72:
         case 75:
         case 76:
         case 77:
         case 85:
         case 87:
         case 88:
         case 89:
         case 92:
         case 93:
         case 94:
         case 101:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 109:
         case 114:
         case 120:
         case 121:
         case 122:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 136:
         case 138:
         case 139:
         case 141:
         case 143:
         case 145:
         case 146:
         case 147:
         case 149:
         default:
            return SqlNode.class;
         case 12:
            return CountNode.class;
         case 13:
            return DeleteStatement.class;
         case 15:
            return DotNode.class;
         case 17:
         case 27:
            return CollectionFunction.class;
         case 19:
            return UnaryLogicOperatorNode.class;
         case 20:
         case 49:
            return BooleanLiteralNode.class;
         case 22:
            return FromClause.class;
         case 26:
         case 83:
            return InLogicOperatorNode.class;
         case 29:
            return InsertStatement.class;
         case 30:
            return IntoClause.class;
         case 34:
         case 84:
         case 102:
         case 108:
         case 110:
         case 111:
         case 112:
         case 113:
            return BinaryLogicOperatorNode.class;
         case 41:
            return OrderByClause.class;
         case 45:
         case 86:
            return QueryNode.class;
         case 51:
            return UpdateStatement.class;
         case 54:
            return CaseNode.class;
         case 68:
            return MapKeyNode.class;
         case 69:
            return MapValueNode.class;
         case 70:
            return MapEntryNode.class;
         case 71:
            return AggregateNode.class;
         case 73:
            return ConstructorNode.class;
         case 74:
            return Case2Node.class;
         case 78:
            return IndexNode.class;
         case 79:
            return IsNotNullLogicOperatorNode.class;
         case 80:
            return IsNullLogicOperatorNode.class;
         case 81:
            return MethodNode.class;
         case 90:
         case 91:
            return UnaryArithmeticNode.class;
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 124:
         case 125:
            return LiteralNode.class;
         case 100:
            return JavaConstantNode.class;
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
            return BinaryArithmeticOperatorNode.class;
         case 123:
         case 148:
            return ParameterNode.class;
         case 126:
         case 140:
            return IdentNode.class;
         case 134:
            return FromElement.class;
         case 135:
            return ImpliedFromElement.class;
         case 137:
            return SelectClause.class;
         case 142:
            return SqlFragment.class;
         case 144:
            return SelectExpressionImpl.class;
         case 150:
            return ResultVariableRefNode.class;
      }
   }

   protected AST createUsingCtor(Token token, String className) {
      try {
         Class c = Class.forName(className);
         Class[] tokenArgType = new Class[]{Token.class};
         Constructor ctor = c.getConstructor(tokenArgType);
         AST t;
         if (ctor != null) {
            t = (AST)ctor.newInstance(token);
            this.initializeSqlNode(t);
         } else {
            t = this.create(c);
         }

         return t;
      } catch (Exception var7) {
         throw new IllegalArgumentException("Invalid class or can't make instance, " + className);
      }
   }

   private void initializeSqlNode(AST t) {
      if (t instanceof InitializeableNode) {
         InitializeableNode initializeableNode = (InitializeableNode)t;
         initializeableNode.initialize(this.walker);
      }

      if (t instanceof SessionFactoryAwareNode) {
         ((SessionFactoryAwareNode)t).setSessionFactory(this.walker.getSessionFactoryHelper().getFactory());
      }

   }

   protected AST create(Class c) {
      try {
         AST t = (AST)c.newInstance();
         this.initializeSqlNode(t);
         return t;
      } catch (Exception var4) {
         this.error("Can't create AST Node " + c.getName());
         return null;
      }
   }
}
