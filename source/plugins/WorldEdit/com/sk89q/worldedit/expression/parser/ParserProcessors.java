package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.Identifiable;
import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.runtime.Conditional;
import com.sk89q.worldedit.expression.runtime.Operators;
import com.sk89q.worldedit.expression.runtime.RValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public final class ParserProcessors {
   private static final Map unaryOpMap = new HashMap();
   private static final Map[] binaryOpMapsLA;
   private static final Map[] binaryOpMapsRA;

   static RValue processExpression(LinkedList input) throws ParserException {
      return processBinaryOpsRA(input, binaryOpMapsRA.length - 1);
   }

   private static RValue processBinaryOpsLA(LinkedList input, int level) throws ParserException {
      if (level < 0) {
         return processUnaryOps(input);
      } else {
         LinkedList<Identifiable> lhs = new LinkedList();
         LinkedList<Identifiable> rhs = new LinkedList();
         String operator = null;
         Iterator<Identifiable> it = input.descendingIterator();

         while(it.hasNext()) {
            Identifiable identifiable = (Identifiable)it.next();
            if (operator == null) {
               rhs.addFirst(identifiable);
               if (identifiable instanceof OperatorToken) {
                  operator = (String)binaryOpMapsLA[level].get(((OperatorToken)identifiable).operator);
                  if (operator != null) {
                     rhs.removeFirst();
                  }
               }
            } else {
               lhs.addFirst(identifiable);
            }
         }

         RValue rhsInvokable = processBinaryOpsLA(rhs, level - 1);
         if (operator == null) {
            return rhsInvokable;
         } else {
            RValue lhsInvokable = processBinaryOpsLA(lhs, level);

            try {
               return Operators.getOperator(((Identifiable)input.get(0)).getPosition(), operator, lhsInvokable, rhsInvokable);
            } catch (NoSuchMethodException var9) {
               Token operatorToken = (Token)input.get(lhs.size());
               throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
            }
         }
      }
   }

   private static RValue processBinaryOpsRA(LinkedList input, int level) throws ParserException {
      if (level < 0) {
         return processTernaryOps(input);
      } else {
         LinkedList<Identifiable> lhs = new LinkedList();
         LinkedList<Identifiable> rhs = new LinkedList();
         String operator = null;

         for(Identifiable identifiable : input) {
            if (operator == null) {
               lhs.addLast(identifiable);
               if (identifiable instanceof OperatorToken) {
                  operator = (String)binaryOpMapsRA[level].get(((OperatorToken)identifiable).operator);
                  if (operator != null) {
                     lhs.removeLast();
                  }
               }
            } else {
               rhs.addLast(identifiable);
            }
         }

         RValue lhsInvokable = processBinaryOpsRA(lhs, level - 1);
         if (operator == null) {
            return lhsInvokable;
         } else {
            RValue rhsInvokable = processBinaryOpsRA(rhs, level);

            try {
               return Operators.getOperator(((Identifiable)input.get(0)).getPosition(), operator, lhsInvokable, rhsInvokable);
            } catch (NoSuchMethodException var9) {
               Token operatorToken = (Token)input.get(lhs.size());
               throw new ParserException(operatorToken.getPosition(), "Couldn't find operator '" + operator + "'");
            }
         }
      }
   }

   private static RValue processTernaryOps(LinkedList input) throws ParserException {
      LinkedList<Identifiable> lhs = new LinkedList();
      LinkedList<Identifiable> mhs = new LinkedList();
      LinkedList<Identifiable> rhs = new LinkedList();
      int partsFound = 0;
      int conditionalsFound = 0;

      for(Identifiable identifiable : input) {
         char character = identifiable.id();
         switch (character) {
            case ':':
               --conditionalsFound;
               break;
            case '?':
               ++conditionalsFound;
         }

         if (conditionalsFound < 0) {
            throw new ParserException(identifiable.getPosition(), "Unexpected ':'");
         }

         switch (partsFound) {
            case 0:
               if (character == '?') {
                  partsFound = 1;
               } else {
                  lhs.addLast(identifiable);
               }
               break;
            case 1:
               if (conditionalsFound == 0 && character == ':') {
                  partsFound = 2;
                  break;
               }

               mhs.addLast(identifiable);
               break;
            case 2:
               rhs.addLast(identifiable);
         }
      }

      if (partsFound < 2) {
         return processBinaryOpsLA(input, binaryOpMapsLA.length - 1);
      } else {
         RValue lhsInvokable = processBinaryOpsLA(lhs, binaryOpMapsLA.length - 1);
         RValue mhsInvokable = processTernaryOps(mhs);
         RValue rhsInvokable = processTernaryOps(rhs);
         return new Conditional(((Identifiable)input.get(lhs.size())).getPosition(), lhsInvokable, mhsInvokable, rhsInvokable);
      }
   }

   private static RValue processUnaryOps(LinkedList input) throws ParserException {
      LinkedList<UnaryOperator> postfixes = new LinkedList();

      while(!input.isEmpty()) {
         Identifiable last = (Identifiable)input.removeLast();
         if (last instanceof OperatorToken) {
            postfixes.addLast(new UnaryOperator(last.getPosition(), "x" + ((OperatorToken)last).operator));
         } else {
            if (!(last instanceof UnaryOperator)) {
               if (!(last instanceof RValue)) {
                  throw new ParserException(last.getPosition(), "Expected expression, found " + last);
               }

               input.addAll(postfixes);
               last = (RValue)last;

               Identifiable last;
               int lastPosition;
               while(true) {
                  if (input.isEmpty()) {
                     return last;
                  }

                  last = (Identifiable)input.removeLast();
                  lastPosition = last.getPosition();
                  if (!(last instanceof UnaryOperator)) {
                     break;
                  }

                  String operator = ((UnaryOperator)last).operator;
                  if (!operator.equals("+")) {
                     String opName = (String)unaryOpMap.get(operator);
                     if (opName == null) {
                        break;
                     }

                     try {
                        last = Operators.getOperator(lastPosition, opName, last);
                     } catch (NoSuchMethodException var9) {
                        throw new ParserException(lastPosition, "No such prefix operator: " + operator);
                     }
                  }
               }

               if (last instanceof Token) {
                  throw new ParserException(lastPosition, "Extra token found in expression: " + last);
               }

               if (last instanceof RValue) {
                  throw new ParserException(lastPosition, "Extra expression found: " + last);
               }

               throw new ParserException(lastPosition, "Extra element found: " + last);
            }

            postfixes.addLast(new UnaryOperator(last.getPosition(), "x" + ((UnaryOperator)last).operator));
         }
      }

      throw new ParserException(-1, "Expression missing.");
   }

   static {
      unaryOpMap.put("-", "neg");
      unaryOpMap.put("!", "not");
      unaryOpMap.put("~", "inv");
      unaryOpMap.put("++", "inc");
      unaryOpMap.put("--", "dec");
      unaryOpMap.put("x++", "postinc");
      unaryOpMap.put("x--", "postdec");
      unaryOpMap.put("x!", "fac");
      Object[][][] binaryOpsLA = new Object[][][]{{{"^", "pow"}, {"**", "pow"}}, {{"*", "mul"}, {"/", "div"}, {"%", "mod"}}, {{"+", "add"}, {"-", "sub"}}, {{"<<", "shl"}, {">>", "shr"}}, {{"<", "lth"}, {">", "gth"}, {"<=", "leq"}, {">=", "geq"}}, {{"==", "equ"}, {"!=", "neq"}, {"~=", "near"}}, {{"&&", "and"}}, {{"||", "or"}}};
      Object[][][] binaryOpsRA = new Object[][][]{{{"=", "ass"}, {"+=", "aadd"}, {"-=", "asub"}, {"*=", "amul"}, {"/=", "adiv"}, {"%=", "amod"}, {"^=", "aexp"}}};
      Map<String, String>[] lBinaryOpMapsLA = binaryOpMapsLA = new Map[binaryOpsLA.length];

      for(int i = 0; i < binaryOpsLA.length; ++i) {
         Object[][] a = binaryOpsLA[i];
         switch (a.length) {
            case 0:
               lBinaryOpMapsLA[i] = Collections.emptyMap();
               break;
            case 1:
               Object[] first = a[0];
               lBinaryOpMapsLA[i] = Collections.singletonMap((String)first[0], (String)first[1]);
               break;
            default:
               Map<String, String> m = lBinaryOpMapsLA[i] = new HashMap();

               for(int j = 0; j < a.length; ++j) {
                  Object[] element = a[j];
                  m.put((String)element[0], (String)element[1]);
               }
         }
      }

      Map<String, String>[] lBinaryOpMapsRA = binaryOpMapsRA = new Map[binaryOpsRA.length];

      for(int i = 0; i < binaryOpsRA.length; ++i) {
         Object[][] a = binaryOpsRA[i];
         switch (a.length) {
            case 0:
               lBinaryOpMapsRA[i] = Collections.emptyMap();
               break;
            case 1:
               Object[] first = a[0];
               lBinaryOpMapsRA[i] = Collections.singletonMap((String)first[0], (String)first[1]);
               break;
            default:
               Map<String, String> m = lBinaryOpMapsRA[i] = new HashMap();

               for(int j = 0; j < a.length; ++j) {
                  Object[] element = a[j];
                  m.put((String)element[0], (String)element[1]);
               }
         }
      }

   }
}
