package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class ORGRHandler extends ORRHandler {
   public Span handle(List tokens, Options options) {
      Span outerSpan = Handler.getAnchor(tokens.subList(2, 4), options);
      return this.handle(tokens.subList(0, 2), outerSpan, options);
   }
}
