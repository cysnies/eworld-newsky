package com.sk89q.worldedit.expression.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Switch extends Node implements RValue {
   private final RValue parameter;
   private final Map valueMap;
   private final RValue[] caseStatements;
   private final RValue defaultCase;

   public Switch(int position, RValue parameter, List values, List caseStatements, RValue defaultCase) {
      this(position, parameter, invertList(values), caseStatements, defaultCase);
   }

   private static Map invertList(List values) {
      Map<Double, Integer> valueMap = new HashMap();

      for(int i = 0; i < values.size(); ++i) {
         valueMap.put(values.get(i), i);
      }

      return valueMap;
   }

   private Switch(int position, RValue parameter, Map valueMap, List caseStatements, RValue defaultCase) {
      super(position);
      this.parameter = parameter;
      this.valueMap = valueMap;
      this.caseStatements = (RValue[])caseStatements.toArray(new RValue[caseStatements.size()]);
      this.defaultCase = defaultCase;
   }

   public char id() {
      return 'W';
   }

   public double getValue() throws EvaluationException {
      double parameter = this.parameter.getValue();

      try {
         double ret = (double)0.0F;
         Integer index = (Integer)this.valueMap.get(parameter);
         if (index != null) {
            for(int i = index; i < this.caseStatements.length; ++i) {
               ret = this.caseStatements[i].getValue();
            }
         }

         return this.defaultCase == null ? ret : this.defaultCase.getValue();
      } catch (BreakException e) {
         if (e.doContinue) {
            throw e;
         } else {
            return (double)0.0F;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("switch (");
      sb.append(this.parameter);
      sb.append(") { ");

      for(int i = 0; i < this.caseStatements.length; ++i) {
         RValue caseStatement = this.caseStatements[i];
         sb.append("case ");

         for(Map.Entry entry : this.valueMap.entrySet()) {
            if ((Integer)entry.getValue() == i) {
               sb.append(entry.getKey());
               break;
            }
         }

         sb.append(": ");
         sb.append(caseStatement);
         sb.append(' ');
      }

      if (this.defaultCase != null) {
         sb.append("default: ");
         sb.append(this.defaultCase);
         sb.append(' ');
      }

      sb.append("}");
      return sb.toString();
   }

   public RValue optimize() throws EvaluationException {
      RValue optimizedParameter = this.parameter.optimize();
      List<RValue> newSequence = new ArrayList();
      if (optimizedParameter instanceof Constant) {
         double parameter = optimizedParameter.getValue();
         Integer index = (Integer)this.valueMap.get(parameter);
         if (index == null) {
            return (RValue)(this.defaultCase == null ? new Constant(this.getPosition(), (double)0.0F) : this.defaultCase.optimize());
         } else {
            boolean breakDetected = false;

            for(int i = index; i < this.caseStatements.length && !breakDetected; ++i) {
               RValue invokable = this.caseStatements[i].optimize();
               if (invokable instanceof Sequence) {
                  for(RValue subInvokable : ((Sequence)invokable).sequence) {
                     if (subInvokable instanceof Break) {
                        breakDetected = true;
                        break;
                     }

                     newSequence.add(subInvokable);
                  }
               } else {
                  newSequence.add(invokable);
               }
            }

            if (this.defaultCase != null && !breakDetected) {
               RValue invokable = this.defaultCase.optimize();
               if (invokable instanceof Sequence) {
                  for(RValue subInvokable : ((Sequence)invokable).sequence) {
                     newSequence.add(subInvokable);
                  }
               } else {
                  newSequence.add(invokable);
               }
            }

            return new Switch(this.getPosition(), optimizedParameter, Collections.singletonMap(parameter, 0), newSequence, (RValue)null);
         }
      } else {
         Map<Double, Integer> newValueMap = new HashMap();
         Map<Integer, Double> backMap = new HashMap();

         for(Map.Entry entry : this.valueMap.entrySet()) {
            backMap.put(entry.getValue(), entry.getKey());
         }

         for(int i = 0; i < this.caseStatements.length; ++i) {
            RValue invokable = this.caseStatements[i].optimize();
            Double caseValue = (Double)backMap.get(i);
            if (caseValue != null) {
               newValueMap.put(caseValue, newSequence.size());
            }

            if (invokable instanceof Sequence) {
               for(RValue subInvokable : ((Sequence)invokable).sequence) {
                  newSequence.add(subInvokable);
               }
            } else {
               newSequence.add(invokable);
            }
         }

         return new Switch(this.getPosition(), optimizedParameter, newValueMap, newSequence, this.defaultCase.optimize());
      }
   }
}
