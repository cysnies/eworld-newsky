package com.comphenix.protocol;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ExactMatchConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

class MultipleLinesPrompt extends StringPrompt {
   private static final String KEY = "multiple_lines_prompt";
   private static final String KEY_LAST = "multiple_lines_prompt.last_line";
   private static final String KEY_LINES = "multiple_lines_prompt.linecount";
   private final MultipleConversationCanceller endMarker;
   private final String initialPrompt;

   public String removeAccumulatedInput(ConversationContext context) {
      Object result = context.getSessionData("multiple_lines_prompt");
      if (result instanceof StringBuilder) {
         context.setSessionData("multiple_lines_prompt", (Object)null);
         context.setSessionData("multiple_lines_prompt.linecount", (Object)null);
         return ((StringBuilder)result).toString();
      } else {
         return null;
      }
   }

   public MultipleLinesPrompt(String endMarker, String initialPrompt) {
      this((ConversationCanceller)(new ExactMatchConversationCanceller(endMarker)), initialPrompt);
   }

   public MultipleLinesPrompt(ConversationCanceller endMarker, String initialPrompt) {
      this.endMarker = new MultipleWrapper(endMarker);
      this.initialPrompt = initialPrompt;
   }

   public MultipleLinesPrompt(MultipleConversationCanceller endMarker, String initialPrompt) {
      this.endMarker = endMarker;
      this.initialPrompt = initialPrompt;
   }

   public Prompt acceptInput(ConversationContext context, String in) {
      StringBuilder result = (StringBuilder)context.getSessionData("multiple_lines_prompt");
      Integer count = (Integer)context.getSessionData("multiple_lines_prompt.linecount");
      if (result == null) {
         context.setSessionData("multiple_lines_prompt", result = new StringBuilder());
      }

      if (count == null) {
         count = 0;
      }

      context.setSessionData("multiple_lines_prompt.last_line", in);
      Integer var5;
      context.setSessionData("multiple_lines_prompt.linecount", var5 = count + 1);
      result.append(in + "\n");
      return (Prompt)(this.endMarker.cancelBasedOnInput(context, in, result, var5) ? Prompt.END_OF_CONVERSATION : this);
   }

   public String getPromptText(ConversationContext context) {
      Object last = context.getSessionData("multiple_lines_prompt.last_line");
      return last instanceof String ? (String)last : this.initialPrompt;
   }

   private static class MultipleWrapper implements MultipleConversationCanceller {
      private ConversationCanceller canceller;

      public MultipleWrapper(ConversationCanceller canceller) {
         this.canceller = canceller;
      }

      public boolean cancelBasedOnInput(ConversationContext context, String currentLine) {
         return this.canceller.cancelBasedOnInput(context, currentLine);
      }

      public boolean cancelBasedOnInput(ConversationContext context, String currentLine, StringBuilder lines, int lineCount) {
         return this.cancelBasedOnInput(context, currentLine);
      }

      public void setConversation(Conversation conversation) {
         this.canceller.setConversation(conversation);
      }

      public MultipleWrapper clone() {
         return new MultipleWrapper(this.canceller.clone());
      }
   }

   public interface MultipleConversationCanceller extends ConversationCanceller {
      boolean cancelBasedOnInput(ConversationContext var1, String var2);

      boolean cancelBasedOnInput(ConversationContext var1, String var2, StringBuilder var3, int var4);
   }
}
