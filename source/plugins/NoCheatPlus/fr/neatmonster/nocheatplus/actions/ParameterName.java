package fr.neatmonster.nocheatplus.actions;

public enum ParameterName {
   BLOCK_ID("blockid"),
   CHECK("check"),
   TAGS("tags"),
   DISTANCE("distance"),
   FALL_DISTANCE("falldistance"),
   FOOD("food"),
   IP("ip"),
   LIMIT("limit"),
   LOCATION_FROM("locationfrom"),
   LOCATION_TO("locationto"),
   PACKETS("packets"),
   PLAYER("player"),
   REACH_DISTANCE("reachdistance"),
   VIOLATIONS("violations"),
   WORLD("world");

   private final String text;

   public static final ParameterName get(String text) {
      for(ParameterName parameterName : values()) {
         if (parameterName.text.equals(text)) {
            return parameterName;
         }
      }

      return null;
   }

   private ParameterName(String text) {
      this.text = text;
   }
}
