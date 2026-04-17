package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class SRPAHandler extends SRPHandler {
   public Span handle(List tokens, Options options) {
      Span anchorSpan = Handler.getAnchor(tokens.subList(3, tokens.size()), options);
      return super.handle(tokens, anchorSpan, options);
   }
}
