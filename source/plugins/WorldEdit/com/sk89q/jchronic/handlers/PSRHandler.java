package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.LinkedList;
import java.util.List;

public class PSRHandler extends SRPHandler {
   public Span handle(List tokens, Options options) {
      List<Token> newTokens = new LinkedList();
      newTokens.add((Token)tokens.get(1));
      newTokens.add((Token)tokens.get(2));
      newTokens.add((Token)tokens.get(0));
      return super.handle(newTokens, options);
   }
}
