package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class DummyHandler implements IHandler {
   public Span handle(List tokens, Options options) {
      return null;
   }
}
