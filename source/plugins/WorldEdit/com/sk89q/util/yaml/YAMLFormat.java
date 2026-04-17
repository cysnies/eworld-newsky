package com.sk89q.util.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public enum YAMLFormat {
   EXTENDED(FlowStyle.BLOCK),
   COMPACT(FlowStyle.AUTO);

   private final DumperOptions.FlowStyle style;

   private YAMLFormat(DumperOptions.FlowStyle style) {
      this.style = style;
   }

   public DumperOptions.FlowStyle getStyle() {
      return this.style;
   }
}
