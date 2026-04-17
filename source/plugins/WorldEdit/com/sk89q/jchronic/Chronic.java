package com.sk89q.jchronic;

import com.sk89q.jchronic.handlers.Handler;
import com.sk89q.jchronic.numerizer.Numerizer;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.tags.Grabber;
import com.sk89q.jchronic.tags.Ordinal;
import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.tags.Scalar;
import com.sk89q.jchronic.tags.Separator;
import com.sk89q.jchronic.tags.TimeZone;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.LinkedList;
import java.util.List;

public class Chronic {
   public static final String VERSION = "0.2.3";

   private Chronic() {
   }

   public static Span parse(String text) {
      return parse(text, new Options());
   }

   public static Span parse(String text, Options options) {
      String normalizedText = preNormalize(text);
      List<Token> tokens = baseTokenize(normalizedText);
      List<Class> optionScannerClasses = new LinkedList();
      optionScannerClasses.add(Repeater.class);

      for(Class optionScannerClass : optionScannerClasses) {
         try {
            tokens = (List)optionScannerClass.getMethod("scan", List.class, Options.class).invoke((Object)null, tokens, options);
         } catch (Throwable e) {
            throw new RuntimeException("Failed to scan tokens.", e);
         }
      }

      List<Class> scannerClasses = new LinkedList();
      scannerClasses.add(Grabber.class);
      scannerClasses.add(Pointer.class);
      scannerClasses.add(Scalar.class);
      scannerClasses.add(Ordinal.class);
      scannerClasses.add(Separator.class);
      scannerClasses.add(TimeZone.class);

      for(Class scannerClass : scannerClasses) {
         try {
            tokens = (List)scannerClass.getMethod("scan", List.class, Options.class).invoke((Object)null, tokens, options);
         } catch (Throwable e) {
            throw new RuntimeException("Failed to scan tokens.", e);
         }
      }

      List<Token> taggedTokens = new LinkedList();

      for(Token token : tokens) {
         if (token.isTagged()) {
            taggedTokens.add(token);
         }
      }

      if (options.isDebug()) {
         System.out.println("Chronic.parse: " + taggedTokens);
      }

      Span span = Handler.tokensToSpan(taggedTokens, options);
      if (options.isGuess()) {
         span = guess(span);
      }

      return span;
   }

   protected static String preNormalize(String text) {
      String normalizedText = text.toLowerCase();
      normalizedText = numericizeNumbers(normalizedText);
      normalizedText = normalizedText.replaceAll("['\"\\.]", "");
      normalizedText = normalizedText.replaceAll("([/\\-,@])", " $1 ");
      normalizedText = normalizedText.replaceAll("\\btoday\\b", "this day");
      normalizedText = normalizedText.replaceAll("\\btomm?orr?ow\\b", "next day");
      normalizedText = normalizedText.replaceAll("\\byesterday\\b", "last day");
      normalizedText = normalizedText.replaceAll("\\bnoon\\b", "12:00");
      normalizedText = normalizedText.replaceAll("\\bmidnight\\b", "24:00");
      normalizedText = normalizedText.replaceAll("\\bbefore now\\b", "past");
      normalizedText = normalizedText.replaceAll("\\bnow\\b", "this second");
      normalizedText = normalizedText.replaceAll("\\b(ago|before)\\b", "past");
      normalizedText = normalizedText.replaceAll("\\bthis past\\b", "last");
      normalizedText = normalizedText.replaceAll("\\bthis last\\b", "last");
      normalizedText = normalizedText.replaceAll("\\b(?:in|during) the (morning)\\b", "$1");
      normalizedText = normalizedText.replaceAll("\\b(?:in the|during the|at) (afternoon|evening|night)\\b", "$1");
      normalizedText = normalizedText.replaceAll("\\btonight\\b", "this night");
      normalizedText = normalizedText.replaceAll("(?=\\w)([ap]m|oclock)\\b", " $1");
      normalizedText = normalizedText.replaceAll("\\b(hence|after|from)\\b", "future");
      normalizedText = numericizeOrdinals(normalizedText);
      return normalizedText;
   }

   protected static String numericizeNumbers(String text) {
      return Numerizer.numerize(text);
   }

   protected static String numericizeOrdinals(String text) {
      return text;
   }

   protected static List baseTokenize(String text) {
      String[] words = text.split(" ");
      List<Token> tokens = new LinkedList();

      for(String word : words) {
         tokens.add(new Token(word));
      }

      return tokens;
   }

   public static Span guess(Span span) {
      if (span == null) {
         return null;
      } else {
         long guessValue;
         if (span.getWidth() > 1L) {
            guessValue = span.getBegin() + span.getWidth() / 2L;
         } else {
            guessValue = span.getBegin();
         }

         Span guess = new Span(guessValue, guessValue);
         return guess;
      }
   }
}
