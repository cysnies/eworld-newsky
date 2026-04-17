package org.hibernate.engine.query.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.internal.util.collections.ArrayHelper;

public class ParamLocationRecognizer implements ParameterParser.Recognizer {
   private Map namedParameterDescriptions = new HashMap();
   private List ordinalParameterLocationList = new ArrayList();

   public static ParamLocationRecognizer parseLocations(String query) {
      ParamLocationRecognizer recognizer = new ParamLocationRecognizer();
      ParameterParser.parse(query, recognizer);
      return recognizer;
   }

   public Map getNamedParameterDescriptionMap() {
      return this.namedParameterDescriptions;
   }

   public List getOrdinalParameterLocationList() {
      return this.ordinalParameterLocationList;
   }

   public void ordinalParameter(int position) {
      this.ordinalParameterLocationList.add(position);
   }

   public void namedParameter(String name, int position) {
      this.getOrBuildNamedParameterDescription(name, false).add(position);
   }

   public void jpaPositionalParameter(String name, int position) {
      this.getOrBuildNamedParameterDescription(name, true).add(position);
   }

   private NamedParameterDescription getOrBuildNamedParameterDescription(String name, boolean jpa) {
      NamedParameterDescription desc = (NamedParameterDescription)this.namedParameterDescriptions.get(name);
      if (desc == null) {
         desc = new NamedParameterDescription(jpa);
         this.namedParameterDescriptions.put(name, desc);
      }

      return desc;
   }

   public void other(char character) {
   }

   public void outParameter(int position) {
   }

   public static class NamedParameterDescription {
      private final boolean jpaStyle;
      private final List positions = new ArrayList();

      public NamedParameterDescription(boolean jpaStyle) {
         this.jpaStyle = jpaStyle;
      }

      public boolean isJpaStyle() {
         return this.jpaStyle;
      }

      private void add(int position) {
         this.positions.add(position);
      }

      public int[] buildPositionsArray() {
         return ArrayHelper.toIntArray(this.positions);
      }
   }
}
