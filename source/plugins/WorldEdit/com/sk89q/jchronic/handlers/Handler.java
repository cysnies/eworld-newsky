package com.sk89q.jchronic.handlers;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.repeaters.EnumRepeaterDayPortion;
import com.sk89q.jchronic.repeaters.IntegerRepeaterDayPortion;
import com.sk89q.jchronic.repeaters.Repeater;
import com.sk89q.jchronic.repeaters.RepeaterDayName;
import com.sk89q.jchronic.repeaters.RepeaterDayPortion;
import com.sk89q.jchronic.repeaters.RepeaterMonthName;
import com.sk89q.jchronic.repeaters.RepeaterTime;
import com.sk89q.jchronic.tags.Grabber;
import com.sk89q.jchronic.tags.Ordinal;
import com.sk89q.jchronic.tags.OrdinalDay;
import com.sk89q.jchronic.tags.Pointer;
import com.sk89q.jchronic.tags.Scalar;
import com.sk89q.jchronic.tags.ScalarDay;
import com.sk89q.jchronic.tags.ScalarMonth;
import com.sk89q.jchronic.tags.ScalarYear;
import com.sk89q.jchronic.tags.Separator;
import com.sk89q.jchronic.tags.SeparatorAt;
import com.sk89q.jchronic.tags.SeparatorComma;
import com.sk89q.jchronic.tags.SeparatorIn;
import com.sk89q.jchronic.tags.SeparatorSlashOrDash;
import com.sk89q.jchronic.tags.Tag;
import com.sk89q.jchronic.tags.TimeZone;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Tick;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.jchronic.utils.Token;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Handler {
   private static Map _definitions;
   private HandlerPattern[] _patterns;
   private IHandler _handler;
   private boolean _compatible;

   public Handler(IHandler handler, HandlerPattern... patterns) {
      this(handler, true, patterns);
   }

   public Handler(IHandler handler, boolean compatible, HandlerPattern... patterns) {
      this._handler = handler;
      this._compatible = compatible;
      this._patterns = patterns;
   }

   public boolean isCompatible(Options options) {
      return !options.isCompatibilityMode() || this._compatible;
   }

   public IHandler getHandler() {
      return this._handler;
   }

   public boolean match(List tokens, Map definitions) {
      int tokenIndex = 0;

      HandlerPattern[] var7;
      for(HandlerPattern pattern : var7 = this._patterns) {
         boolean optional = pattern.isOptional();
         if (pattern instanceof TagPattern) {
            boolean match = tokenIndex < tokens.size() && ((Token)tokens.get(tokenIndex)).getTags(((TagPattern)pattern).getTagClass()).size() > 0;
            if (!match && !optional) {
               return false;
            }

            if (match) {
               ++tokenIndex;
            }
         } else if (pattern instanceof HandlerTypePattern) {
            if (optional && tokenIndex == tokens.size()) {
               return true;
            }

            for(Handler subHandler : (List)definitions.get(((HandlerTypePattern)pattern).getType())) {
               if (subHandler.match(tokens.subList(tokenIndex, tokens.size()), definitions)) {
                  return true;
               }
            }

            return false;
         }
      }

      if (tokenIndex != tokens.size()) {
         return false;
      } else {
         return true;
      }
   }

   public String toString() {
      return "[Handler: " + this._handler + "]";
   }

   public static synchronized Map definitions() {
      if (_definitions == null) {
         Map<HandlerType, List<Handler>> definitions = new HashMap();
         List<Handler> timeHandlers = new LinkedList();
         timeHandlers.add(new Handler((IHandler)null, new HandlerPattern[]{new TagPattern(RepeaterTime.class), new TagPattern(RepeaterDayPortion.class, true)}));
         definitions.put(Handler.HandlerType.TIME, timeHandlers);
         List<Handler> dateHandlers = new LinkedList();
         dateHandlers.add(new Handler(new RdnRmnSdTTzSyHandler(), new HandlerPattern[]{new TagPattern(RepeaterDayName.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(RepeaterTime.class), new TagPattern(TimeZone.class), new TagPattern(ScalarYear.class)}));
         dateHandlers.add(new Handler(new RmnSdSyHandler(), new HandlerPattern[]{new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class)}));
         dateHandlers.add(new Handler(new RmnSdSyHandler(), new HandlerPattern[]{new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new RmnSdHandler(), new HandlerPattern[]{new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new RmnOdHandler(), new HandlerPattern[]{new TagPattern(RepeaterMonthName.class), new TagPattern(OrdinalDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new RmnSyHandler(), new HandlerPattern[]{new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class)}));
         dateHandlers.add(new Handler(new SdRmnSyHandler(), new HandlerPattern[]{new TagPattern(ScalarDay.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new SmSdSyHandler(), new HandlerPattern[]{new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new SdSmSyHandler(), new HandlerPattern[]{new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new SySmSdHandler(), new HandlerPattern[]{new TagPattern(ScalarYear.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)}));
         dateHandlers.add(new Handler(new SmSdHandler(), false, new HandlerPattern[]{new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class)}));
         dateHandlers.add(new Handler(new SmSyHandler(), new HandlerPattern[]{new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class)}));
         definitions.put(Handler.HandlerType.DATE, dateHandlers);
         List<Handler> anchorHandlers = new LinkedList();
         anchorHandlers.add(new Handler(new RHandler(), new HandlerPattern[]{new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)}));
         anchorHandlers.add(new Handler(new RHandler(), new HandlerPattern[]{new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)}));
         anchorHandlers.add(new Handler(new RGRHandler(), new HandlerPattern[]{new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)}));
         definitions.put(Handler.HandlerType.ANCHOR, anchorHandlers);
         List<Handler> arrowHandlers = new LinkedList();
         arrowHandlers.add(new Handler(new SRPHandler(), new HandlerPattern[]{new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class)}));
         arrowHandlers.add(new Handler(new PSRHandler(), new HandlerPattern[]{new TagPattern(Pointer.class), new TagPattern(Scalar.class), new TagPattern(Repeater.class)}));
         arrowHandlers.add(new Handler(new SRPAHandler(), new HandlerPattern[]{new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class), new HandlerTypePattern(Handler.HandlerType.ANCHOR)}));
         definitions.put(Handler.HandlerType.ARROW, arrowHandlers);
         List<Handler> narrowHandlers = new LinkedList();
         narrowHandlers.add(new Handler(new ORSRHandler(), new HandlerPattern[]{new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(SeparatorIn.class), new TagPattern(Repeater.class)}));
         narrowHandlers.add(new Handler(new ORGRHandler(), new HandlerPattern[]{new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)}));
         definitions.put(Handler.HandlerType.NARROW, narrowHandlers);
         _definitions = definitions;
      }

      return _definitions;
   }

   public static Span tokensToSpan(List tokens, Options options) {
      if (options.isDebug()) {
         System.out.println("Chronic.tokensToSpan: " + tokens);
      }

      Map<HandlerType, List<Handler>> definitions = definitions();

      for(Handler handler : (List)definitions.get(Handler.HandlerType.DATE)) {
         if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
            if (options.isDebug()) {
               System.out.println("Chronic.tokensToSpan: date");
            }

            List<Token> goodTokens = new LinkedList();

            for(Token token : tokens) {
               if (token.getTag(Separator.class) == null) {
                  goodTokens.add(token);
               }
            }

            return handler.getHandler().handle(goodTokens, options);
         }
      }

      for(Handler handler : (List)definitions.get(Handler.HandlerType.ANCHOR)) {
         if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
            if (options.isDebug()) {
               System.out.println("Chronic.tokensToSpan: anchor");
            }

            List<Token> goodTokens = new LinkedList();

            for(Token token : tokens) {
               if (token.getTag(Separator.class) == null) {
                  goodTokens.add(token);
               }
            }

            return handler.getHandler().handle(goodTokens, options);
         }
      }

      for(Handler handler : (List)definitions.get(Handler.HandlerType.ARROW)) {
         if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
            if (options.isDebug()) {
               System.out.println("Chronic.tokensToSpan: arrow");
            }

            List<Token> goodTokens = new LinkedList();

            for(Token token : tokens) {
               if (token.getTag(SeparatorAt.class) == null && token.getTag(SeparatorSlashOrDash.class) == null && token.getTag(SeparatorComma.class) == null) {
                  goodTokens.add(token);
               }
            }

            return handler.getHandler().handle(goodTokens, options);
         }
      }

      for(Handler handler : (List)definitions.get(Handler.HandlerType.NARROW)) {
         if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
            if (options.isDebug()) {
               System.out.println("Chronic.tokensToSpan: narrow");
            }

            return handler.getHandler().handle(tokens, options);
         }
      }

      if (options.isDebug()) {
         System.out.println("Chronic.tokensToSpan: none");
      }

      return null;
   }

   public static List getRepeaters(List tokens) {
      List<Repeater<?>> repeaters = new LinkedList();

      for(Token token : tokens) {
         Repeater<?> tag = (Repeater)token.getTag(Repeater.class);
         if (tag != null) {
            repeaters.add(tag);
         }
      }

      Collections.sort(repeaters);
      Collections.reverse(repeaters);
      return repeaters;
   }

   public static Span getAnchor(List tokens, Options options) {
      Grabber grabber = new Grabber(Grabber.Relative.THIS);
      Pointer.PointerType pointer = Pointer.PointerType.FUTURE;
      List<Repeater<?>> repeaters = getRepeaters(tokens);

      for(int i = 0; i < repeaters.size(); ++i) {
         tokens.remove(tokens.size() - 1);
      }

      if (!tokens.isEmpty() && ((Token)tokens.get(0)).getTag(Grabber.class) != null) {
         grabber = (Grabber)((Token)tokens.get(0)).getTag(Grabber.class);
         tokens.remove(tokens.size() - 1);
      }

      Repeater<?> head = (Repeater)repeaters.remove(0);
      head.setStart((Calendar)options.getNow().clone());
      Grabber.Relative grabberType = (Grabber.Relative)grabber.getType();
      Span outerSpan;
      if (grabberType == Grabber.Relative.LAST) {
         outerSpan = head.nextSpan(Pointer.PointerType.PAST);
      } else if (grabberType == Grabber.Relative.THIS) {
         if (repeaters.size() > 0) {
            outerSpan = head.thisSpan(Pointer.PointerType.NONE);
         } else {
            outerSpan = head.thisSpan(options.getContext());
         }
      } else {
         if (grabberType != Grabber.Relative.NEXT) {
            throw new IllegalArgumentException("Invalid grabber type " + grabberType + ".");
         }

         outerSpan = head.nextSpan(Pointer.PointerType.FUTURE);
      }

      if (options.isDebug()) {
         System.out.println("Chronic.getAnchor: outerSpan = " + outerSpan + "; repeaters = " + repeaters);
      }

      Span anchor = findWithin(repeaters, outerSpan, pointer, options);
      return anchor;
   }

   public static Span dayOrTime(Calendar dayStart, List timeTokens, Options options) {
      Span outerSpan = new Span(dayStart, Time.cloneAndAdd(dayStart, 5, 1L));
      if (!timeTokens.isEmpty()) {
         options.setNow(outerSpan.getBeginCalendar());
         Span time = getAnchor(dealiasAndDisambiguateTimes(timeTokens, options), options);
         return time;
      } else {
         return outerSpan;
      }
   }

   public static Span findWithin(List tags, Span span, Pointer.PointerType pointer, Options options) {
      if (options.isDebug()) {
         System.out.println("Chronic.findWithin: " + tags + " in " + span);
      }

      if (tags.isEmpty()) {
         return span;
      } else {
         Repeater<?> head = (Repeater)tags.get(0);
         List<Repeater<?>> rest = (List<Repeater<?>>)(tags.size() > 1 ? tags.subList(1, tags.size()) : new LinkedList());
         head.setStart(pointer == Pointer.PointerType.FUTURE ? span.getBeginCalendar() : span.getEndCalendar());
         Span h = head.thisSpan(Pointer.PointerType.NONE);
         return !span.contains(h.getBegin()) && !span.contains(h.getEnd()) ? null : findWithin(rest, h, pointer, options);
      }
   }

   public static List dealiasAndDisambiguateTimes(List tokens, Options options) {
      int dayPortionIndex = -1;
      int tokenSize = tokens.size();

      for(int i = 0; dayPortionIndex == -1 && i < tokenSize; ++i) {
         Token t = (Token)tokens.get(i);
         if (t.getTag(RepeaterDayPortion.class) != null) {
            dayPortionIndex = i;
         }
      }

      int timeIndex = -1;

      for(int i = 0; timeIndex == -1 && i < tokenSize; ++i) {
         Token t = (Token)tokens.get(i);
         if (t.getTag(RepeaterTime.class) != null) {
            timeIndex = i;
         }
      }

      if (dayPortionIndex != -1 && timeIndex != -1) {
         Token t1 = (Token)tokens.get(dayPortionIndex);
         Tag<RepeaterDayPortion<?>> t1Tag = t1.getTag(RepeaterDayPortion.class);
         Object t1TagType = t1Tag.getType();
         if (RepeaterDayPortion.DayPortion.MORNING.equals(t1TagType)) {
            if (options.isDebug()) {
               System.out.println("Chronic.dealiasAndDisambiguateTimes: morning->am");
            }

            t1.untag(RepeaterDayPortion.class);
            t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AM));
         } else if (RepeaterDayPortion.DayPortion.AFTERNOON.equals(t1TagType) || RepeaterDayPortion.DayPortion.EVENING.equals(t1TagType) || RepeaterDayPortion.DayPortion.NIGHT.equals(t1TagType)) {
            if (options.isDebug()) {
               System.out.println("Chronic.dealiasAndDisambiguateTimes: " + t1TagType + "->pm");
            }

            t1.untag(RepeaterDayPortion.class);
            t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.PM));
         }
      }

      if (options.getAmbiguousTimeRange() != 0) {
         List<Token> ttokens = new LinkedList();

         for(int i = 0; i < tokenSize; ++i) {
            Token t0 = (Token)tokens.get(i);
            ttokens.add(t0);
            Token t1 = null;
            if (i < tokenSize - 1) {
               t1 = (Token)tokens.get(i + 1);
            }

            if (t0.getTag(RepeaterTime.class) != null && ((Tick)((RepeaterTime)t0.getTag(RepeaterTime.class)).getType()).isAmbiguous() && (t1 == null || t1.getTag(RepeaterDayPortion.class) == null)) {
               Token distoken = new Token("disambiguator");
               distoken.tag(new IntegerRepeaterDayPortion(options.getAmbiguousTimeRange()));
               ttokens.add(distoken);
            }
         }

         tokens = ttokens;
      }

      if (options.isDebug()) {
         System.out.println("Chronic.dealiasAndDisambiguateTimes: " + tokens);
      }

      return tokens;
   }

   public static enum HandlerType {
      TIME,
      DATE,
      ANCHOR,
      ARROW,
      NARROW;
   }
}
