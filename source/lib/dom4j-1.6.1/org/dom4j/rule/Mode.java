package org.dom4j.rule;

import java.util.HashMap;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class Mode {
   private RuleSet[] ruleSets = new RuleSet[14];
   private Map elementNameRuleSets;
   private Map attributeNameRuleSets;

   public void fireRule(Node node) throws Exception {
      if (node != null) {
         Rule rule = this.getMatchingRule(node);
         if (rule != null) {
            Action action = rule.getAction();
            if (action != null) {
               action.run(node);
            }
         }
      }

   }

   public void applyTemplates(Element element) throws Exception {
      int i = 0;

      for(int size = element.attributeCount(); i < size; ++i) {
         Attribute attribute = element.attribute(i);
         this.fireRule(attribute);
      }

      i = 0;

      for(int size = element.nodeCount(); i < size; ++i) {
         Node node = element.node(i);
         this.fireRule(node);
      }

   }

   public void applyTemplates(Document document) throws Exception {
      int i = 0;

      for(int size = document.nodeCount(); i < size; ++i) {
         Node node = document.node(i);
         this.fireRule(node);
      }

   }

   public void addRule(Rule rule) {
      int matchType = rule.getMatchType();
      String name = rule.getMatchesNodeName();
      if (name != null) {
         if (matchType == 1) {
            this.elementNameRuleSets = this.addToNameMap(this.elementNameRuleSets, name, rule);
         } else if (matchType == 2) {
            this.attributeNameRuleSets = this.addToNameMap(this.attributeNameRuleSets, name, rule);
         }
      }

      if (matchType >= 14) {
         matchType = 0;
      }

      if (matchType == 0) {
         int i = 1;

         for(int size = this.ruleSets.length; i < size; ++i) {
            RuleSet ruleSet = this.ruleSets[i];
            if (ruleSet != null) {
               ruleSet.addRule(rule);
            }
         }
      }

      this.getRuleSet(matchType).addRule(rule);
   }

   public void removeRule(Rule rule) {
      int matchType = rule.getMatchType();
      String name = rule.getMatchesNodeName();
      if (name != null) {
         if (matchType == 1) {
            this.removeFromNameMap(this.elementNameRuleSets, name, rule);
         } else if (matchType == 2) {
            this.removeFromNameMap(this.attributeNameRuleSets, name, rule);
         }
      }

      if (matchType >= 14) {
         matchType = 0;
      }

      this.getRuleSet(matchType).removeRule(rule);
      if (matchType != 0) {
         this.getRuleSet(0).removeRule(rule);
      }

   }

   public Rule getMatchingRule(Node node) {
      int matchType = node.getNodeType();
      if (matchType == 1) {
         if (this.elementNameRuleSets != null) {
            String name = node.getName();
            RuleSet ruleSet = (RuleSet)this.elementNameRuleSets.get(name);
            if (ruleSet != null) {
               Rule answer = ruleSet.getMatchingRule(node);
               if (answer != null) {
                  return answer;
               }
            }
         }
      } else if (matchType == 2 && this.attributeNameRuleSets != null) {
         String name = node.getName();
         RuleSet ruleSet = (RuleSet)this.attributeNameRuleSets.get(name);
         if (ruleSet != null) {
            Rule answer = ruleSet.getMatchingRule(node);
            if (answer != null) {
               return answer;
            }
         }
      }

      if (matchType < 0 || matchType >= this.ruleSets.length) {
         matchType = 0;
      }

      Rule answer = null;
      RuleSet ruleSet = this.ruleSets[matchType];
      if (ruleSet != null) {
         answer = ruleSet.getMatchingRule(node);
      }

      if (answer == null && matchType != 0) {
         ruleSet = this.ruleSets[0];
         if (ruleSet != null) {
            answer = ruleSet.getMatchingRule(node);
         }
      }

      return answer;
   }

   protected RuleSet getRuleSet(int matchType) {
      RuleSet ruleSet = this.ruleSets[matchType];
      if (ruleSet == null) {
         ruleSet = new RuleSet();
         this.ruleSets[matchType] = ruleSet;
         if (matchType != 0) {
            RuleSet allRules = this.ruleSets[0];
            if (allRules != null) {
               ruleSet.addAll(allRules);
            }
         }
      }

      return ruleSet;
   }

   protected Map addToNameMap(Map map, String name, Rule rule) {
      if (map == null) {
         map = new HashMap();
      }

      RuleSet ruleSet = (RuleSet)map.get(name);
      if (ruleSet == null) {
         ruleSet = new RuleSet();
         map.put(name, ruleSet);
      }

      ruleSet.addRule(rule);
      return map;
   }

   protected void removeFromNameMap(Map map, String name, Rule rule) {
      if (map != null) {
         RuleSet ruleSet = (RuleSet)map.get(name);
         if (ruleSet != null) {
            ruleSet.removeRule(rule);
         }
      }

   }
}
