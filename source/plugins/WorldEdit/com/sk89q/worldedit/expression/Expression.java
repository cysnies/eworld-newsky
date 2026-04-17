package com.sk89q.worldedit.expression;

import com.sk89q.worldedit.expression.lexer.Lexer;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.parser.Parser;
import com.sk89q.worldedit.expression.runtime.Constant;
import com.sk89q.worldedit.expression.runtime.EvaluationException;
import com.sk89q.worldedit.expression.runtime.RValue;
import com.sk89q.worldedit.expression.runtime.ReturnException;
import com.sk89q.worldedit.expression.runtime.Variable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Expression {
   private static final ThreadLocal instance = new ThreadLocal();
   private final Map variables;
   private final String[] variableNames;
   private RValue root;
   private final Map megabuf;

   public static Expression compile(String expression, String... variableNames) throws ExpressionException {
      return new Expression(expression, variableNames);
   }

   private Expression(String expression, String... variableNames) throws ExpressionException {
      this(Lexer.tokenize(expression), variableNames);
   }

   private Expression(List tokens, String... variableNames) throws ExpressionException {
      this.variables = new HashMap();
      this.megabuf = new HashMap();
      this.variableNames = variableNames;
      this.variables.put("e", new Constant(-1, Math.E));
      this.variables.put("pi", new Constant(-1, Math.PI));
      this.variables.put("true", new Constant(-1, (double)1.0F));
      this.variables.put("false", new Constant(-1, (double)0.0F));

      for(String variableName : variableNames) {
         if (this.variables.containsKey(variableName)) {
            throw new ExpressionException(-1, "Tried to overwrite identifier '" + variableName + "'");
         }

         this.variables.put(variableName, new Variable((double)0.0F));
      }

      this.root = Parser.parse(tokens, this);
   }

   public double evaluate(double... values) throws EvaluationException {
      for(int i = 0; i < values.length; ++i) {
         String variableName = this.variableNames[i];
         RValue invokable = (RValue)this.variables.get(variableName);
         if (!(invokable instanceof Variable)) {
            throw new EvaluationException(invokable.getPosition(), "Tried to assign constant " + variableName + ".");
         }

         ((Variable)invokable).value = values[i];
      }

      this.pushInstance();

      double var11;
      try {
         double var10 = this.root.getValue();
         return var10;
      } catch (ReturnException e) {
         var11 = e.getValue();
      } finally {
         this.popInstance();
      }

      return var11;
   }

   public void optimize() throws EvaluationException {
      this.root = this.root.optimize();
   }

   public String toString() {
      return this.root.toString();
   }

   public RValue getVariable(String name, boolean create) {
      RValue variable = (RValue)this.variables.get(name);
      if (variable == null && create) {
         this.variables.put(name, variable = new Variable((double)0.0F));
      }

      return variable;
   }

   public static Expression getInstance() {
      return (Expression)((Stack)instance.get()).peek();
   }

   private void pushInstance() {
      Stack<Expression> foo = (Stack)instance.get();
      if (foo == null) {
         instance.set(foo = new Stack());
      }

      foo.push(this);
   }

   private void popInstance() {
      Stack<Expression> foo = (Stack)instance.get();
      foo.pop();
      if (foo.isEmpty()) {
         instance.set((Object)null);
      }

   }

   public Map getMegabuf() {
      return this.megabuf;
   }
}
