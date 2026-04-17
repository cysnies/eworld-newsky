package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CharPrefixTree extends PrefixTree {
   public CharPrefixTree(PrefixTree.NodeFactory nodeFactory, PrefixTree.LookupEntryFactory resultFactory) {
      super(nodeFactory, resultFactory);
   }

   public static final List toCharacterList(char[] chars) {
      List<Character> characters = new ArrayList(chars.length);

      for(int i = 0; i < chars.length; ++i) {
         characters.add(chars[i]);
      }

      return characters;
   }

   public CharLookupEntry lookup(char[] chars, boolean create) {
      return (CharLookupEntry)this.lookup((List)toCharacterList(chars), create);
   }

   public CharLookupEntry lookup(String input, boolean create) {
      return this.lookup(input.toCharArray(), create);
   }

   public boolean feed(String input) {
      return this.feed(input.toCharArray());
   }

   public boolean feed(char[] chars) {
      return this.feed((List)toCharacterList(chars));
   }

   public void feedAll(Collection inputs, boolean trim, boolean lowerCase) {
      for(String input : inputs) {
         if (trim) {
            input = input.toLowerCase();
         }

         if (lowerCase) {
            input = input.toLowerCase();
         }

         this.feed(input);
      }

   }

   public boolean hasPrefix(char[] chars) {
      return this.hasPrefix((List)toCharacterList(chars));
   }

   public boolean hasPrefix(String input) {
      return this.hasPrefix(input.toCharArray());
   }

   public boolean hasPrefixWords(String input) {
      L result = (L)this.lookup(input, false);
      if (!result.hasPrefix) {
         return false;
      } else if (input.length() == result.depth) {
         return true;
      } else {
         return Character.isWhitespace(input.charAt(result.depth));
      }
   }

   public boolean isPrefix(char[] chars) {
      return this.isPrefix((List)toCharacterList(chars));
   }

   public boolean isPrefix(String input) {
      return this.isPrefix(input.toCharArray());
   }

   public boolean matches(char[] chars) {
      return this.matches((List)toCharacterList(chars));
   }

   public boolean matches(String input) {
      return this.matches(input.toCharArray());
   }

   public static CharPrefixTree newCharPrefixTree() {
      return new CharPrefixTree(new PrefixTree.NodeFactory() {
         public final SimpleCharNode newNode(SimpleCharNode parent) {
            return new SimpleCharNode();
         }
      }, new PrefixTree.LookupEntryFactory() {
         public final CharLookupEntry newLookupEntry(SimpleCharNode node, SimpleCharNode insertion, int depth, boolean hasPrefix) {
            return new CharLookupEntry(node, insertion, depth, hasPrefix);
         }
      });
   }

   public static class CharNode extends PrefixTree.Node {
   }

   public static class SimpleCharNode extends CharNode {
   }

   public static class CharLookupEntry extends PrefixTree.LookupEntry {
      public CharLookupEntry(CharNode node, CharNode insertion, int depth, boolean hasPrefix) {
         super(node, insertion, depth, hasPrefix);
      }
   }
}
