package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.LinkedList;
import java.util.List;

public class SdRmnSyHandler extends RmnSdSyHandler {
   public Span handle(List tokens, Options options) {
      List<Token> newTokens = new LinkedList();
      newTokens.add((Token)tokens.get(1));
      newTokens.add((Token)tokens.get(0));
      newTokens.add((Token)tokens.get(2));
      newTokens.addAll(tokens.subList(3, tokens.size()));
      return super.handle(newTokens, options);
   }
}
