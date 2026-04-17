package com.comphenix.protocol.events;

import com.comphenix.protocol.injector.GamePhase;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ListeningWhitelist {
   public static final ListeningWhitelist EMPTY_WHITELIST;
   private final ListenerPriority priority;
   private final Set whitelist;
   private final GamePhase gamePhase;
   private final Set options;

   private ListeningWhitelist(Builder builder) {
      this.priority = builder.priority;
      this.whitelist = builder.whitelist;
      this.gamePhase = builder.gamePhase;
      this.options = builder.options;
   }

   public ListeningWhitelist(ListenerPriority priority, Set whitelist) {
      this(priority, whitelist, GamePhase.PLAYING);
   }

   public ListeningWhitelist(ListenerPriority priority, Set whitelist, GamePhase gamePhase) {
      this.priority = priority;
      this.whitelist = safeSet(whitelist);
      this.gamePhase = gamePhase;
      this.options = EnumSet.noneOf(ListenerOptions.class);
   }

   public ListeningWhitelist(ListenerPriority priority, Integer... whitelist) {
      this.priority = priority;
      this.whitelist = Sets.newHashSet(whitelist);
      this.gamePhase = GamePhase.PLAYING;
      this.options = EnumSet.noneOf(ListenerOptions.class);
   }

   public ListeningWhitelist(ListenerPriority priority, Integer[] whitelist, GamePhase gamePhase) {
      this.priority = priority;
      this.whitelist = Sets.newHashSet(whitelist);
      this.gamePhase = gamePhase;
      this.options = EnumSet.noneOf(ListenerOptions.class);
   }

   public ListeningWhitelist(ListenerPriority priority, Integer[] whitelist, GamePhase gamePhase, ListenerOptions... options) {
      this.priority = priority;
      this.whitelist = Sets.newHashSet(whitelist);
      this.gamePhase = gamePhase;
      this.options = safeEnumSet(Arrays.asList(options), ListenerOptions.class);
   }

   public boolean isEnabled() {
      return this.whitelist != null && this.whitelist.size() > 0;
   }

   public ListenerPriority getPriority() {
      return this.priority;
   }

   public Set getWhitelist() {
      return this.whitelist;
   }

   public GamePhase getGamePhase() {
      return this.gamePhase;
   }

   public Set getOptions() {
      return Collections.unmodifiableSet(this.options);
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.priority, this.whitelist, this.gamePhase, this.options});
   }

   public static boolean containsAny(ListeningWhitelist whitelist, int... idList) {
      if (whitelist != null) {
         for(int i = 0; i < idList.length; ++i) {
            if (whitelist.getWhitelist().contains(idList[i])) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isEmpty(ListeningWhitelist whitelist) {
      if (whitelist == EMPTY_WHITELIST) {
         return true;
      } else {
         return whitelist == null ? true : whitelist.getWhitelist().isEmpty();
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof ListeningWhitelist)) {
         return false;
      } else {
         ListeningWhitelist other = (ListeningWhitelist)obj;
         return Objects.equal(this.priority, other.priority) && Objects.equal(this.whitelist, other.whitelist) && Objects.equal(this.gamePhase, other.gamePhase) && Objects.equal(this.options, other.options);
      }
   }

   public String toString() {
      return this == EMPTY_WHITELIST ? "EMPTY_WHITELIST" : Objects.toStringHelper(this).add("priority", this.priority).add("packets", this.whitelist).add("gamephase", this.gamePhase).add("options", this.options).toString();
   }

   public static Builder newBuilder() {
      return new Builder((ListeningWhitelist)null);
   }

   public static Builder newBuilder(ListeningWhitelist template) {
      return new Builder(template);
   }

   private static EnumSet safeEnumSet(Collection options, Class enumClass) {
      return options != null && !options.isEmpty() ? EnumSet.copyOf(options) : EnumSet.noneOf(enumClass);
   }

   private static Set safeSet(Collection set) {
      return (Set)(set != null ? Sets.newHashSet(set) : Collections.emptySet());
   }

   static {
      EMPTY_WHITELIST = new ListeningWhitelist(ListenerPriority.LOW, new Integer[0]);
   }

   public static class Builder {
      private ListenerPriority priority;
      private Set whitelist;
      private GamePhase gamePhase;
      private Set options;

      private Builder(ListeningWhitelist template) {
         if (template != null) {
            this.priority(template.getPriority());
            this.gamePhase(template.getGamePhase());
            this.whitelist(template.getWhitelist());
            this.options(template.getOptions());
         }

      }

      public Builder priority(ListenerPriority priority) {
         this.priority = priority;
         return this;
      }

      public Builder whitelist(Collection whitelist) {
         this.whitelist = ListeningWhitelist.safeSet(whitelist);
         return this;
      }

      public Builder gamePhase(GamePhase gamePhase) {
         this.gamePhase = gamePhase;
         return this;
      }

      public Builder options(Set options) {
         this.options = ListeningWhitelist.safeSet(options);
         return this;
      }

      public ListeningWhitelist build() {
         return new ListeningWhitelist(this);
      }
   }
}
