package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Chronic;
import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.tags.Scalar;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class SRPHandler implements IHandler {
   public Span handle(List tokens, Span span, Options options) {
      int distance = (Integer)((Scalar)((Token)tokens.get(0)).getTag(Scalar.class)).getType();
      Repeater<?> repeater = (Repeater)((Token)tokens.get(1)).getTag(Repeater.class);
      Pointer.PointerType pointer = (Pointer.PointerType)((Pointer)((Token)tokens.get(2)).getTag(Pointer.class)).getType();
      return repeater.getOffset(span, distance, pointer);
   }

   public Span handle(List tokens, Options options) {
      Repeater<?> repeater = (Repeater)((Token)tokens.get(1)).getTag(Repeater.class);
      Span span = Chronic.parse("this second", new Options(options.getNow(), false));
      return this.handle(tokens, span, options);
   }
}
