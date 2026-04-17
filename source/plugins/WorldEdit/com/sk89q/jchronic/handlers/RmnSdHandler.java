package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.repeaters.RepeaterMonthName;
import com.sk89q.jchronic.tags.ScalarDay;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class RmnSdHandler extends MDHandler {
   public Span handle(List tokens, Options options) {
      return this.handle((Repeater)((Token)tokens.get(0)).getTag(RepeaterMonthName.class), ((Token)tokens.get(1)).getTag(ScalarDay.class), tokens.subList(2, tokens.size()), options);
   }
}
