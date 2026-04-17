package com.earth2me.essentials.textreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleTextInput implements IText {
   private final transient List lines = new ArrayList();

   public SimpleTextInput(String input) {
      this.lines.addAll(Arrays.asList(input.split("\\n")));
   }

   public List getLines() {
      return this.lines;
   }

   public List getChapters() {
      return Collections.emptyList();
   }

   public Map getBookmarks() {
      return Collections.emptyMap();
   }
}
