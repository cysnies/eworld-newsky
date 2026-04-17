package com.comphenix.protocol;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import java.util.ArrayList;
import java.util.List;

class RangeParser {
   public static List getRanges(String text, Range legalRange) {
      return getRanges(new String[]{text}, 0, 0, legalRange);
   }

   public static List getRanges(String[] args, int offset, int lastIndex, Range legalRange) {
      List<String> tokens = tokenizeInput(args, offset, lastIndex);
      List<Range<Integer>> ranges = new ArrayList();

      for(int i = 0; i < tokens.size(); ++i) {
         String current = (String)tokens.get(i);
         String next = i + 1 < tokens.size() ? (String)tokens.get(i + 1) : null;
         if ("-".equals(current)) {
            throw new IllegalArgumentException("A hyphen must appear between two numbers.");
         }

         Range<Integer> range;
         if ("-".equals(next)) {
            if (i + 2 >= tokens.size()) {
               throw new IllegalArgumentException("Cannot form a range without a upper limit.");
            }

            range = Ranges.closed(Integer.parseInt(current), Integer.parseInt((String)tokens.get(i + 2)));
            ranges.add(range);
            i += 2;
         } else {
            range = Ranges.singleton(Integer.parseInt(current));
            ranges.add(range);
         }

         if (!legalRange.encloses(range)) {
            throw new IllegalArgumentException(range + " is not in the range " + range.toString());
         }
      }

      return simplify(ranges, (Integer)legalRange.upperEndpoint());
   }

   private static List simplify(List ranges, int maximum) {
      List<Range<Integer>> result = new ArrayList();
      boolean[] set = new boolean[maximum + 1];
      int start = -1;

      for(Range range : ranges) {
         for(int id : range.asSet(DiscreteDomains.integers())) {
            set[id] = true;
         }
      }

      for(int i = 0; i <= set.length; ++i) {
         if (i < set.length && set[i]) {
            if (start < 0) {
               start = i;
            }
         } else if (start >= 0) {
            result.add(Ranges.closed(start, i - 1));
            start = -1;
         }
      }

      return result;
   }

   private static List tokenizeInput(String[] args, int offset, int lastIndex) {
      List<String> tokens = new ArrayList();

      for(int i = offset; i <= lastIndex; ++i) {
         String text = args[i];
         StringBuilder number = new StringBuilder();

         for(int j = 0; j < text.length(); ++j) {
            char current = text.charAt(j);
            if (Character.isDigit(current)) {
               number.append(current);
            } else if (!Character.isWhitespace(current)) {
               if (current != '-') {
                  throw new IllegalArgumentException("Illegal character '" + current + "' found.");
               }

               if (number.length() > 0) {
                  tokens.add(number.toString());
                  number.setLength(0);
               }

               tokens.add(Character.toString(current));
            }
         }

         if (number.length() > 0) {
            tokens.add(number.toString());
         }
      }

      return tokens;
   }
}
