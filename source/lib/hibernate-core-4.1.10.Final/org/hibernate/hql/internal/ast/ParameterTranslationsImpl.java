package org.hibernate.hql.internal.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.param.PositionalParameterSpecification;
import org.hibernate.type.Type;

public class ParameterTranslationsImpl implements ParameterTranslations {
   private final Map namedParameters;
   private final ParameterInfo[] ordinalParameters;

   public boolean supportsOrdinalParameterMetadata() {
      return true;
   }

   public int getOrdinalParameterCount() {
      return this.ordinalParameters.length;
   }

   public ParameterInfo getOrdinalParameterInfo(int ordinalPosition) {
      return this.ordinalParameters[ordinalPosition - 1];
   }

   public int getOrdinalParameterSqlLocation(int ordinalPosition) {
      return this.getOrdinalParameterInfo(ordinalPosition).getSqlLocations()[0];
   }

   public Type getOrdinalParameterExpectedType(int ordinalPosition) {
      return this.getOrdinalParameterInfo(ordinalPosition).getExpectedType();
   }

   public Set getNamedParameterNames() {
      return this.namedParameters.keySet();
   }

   public ParameterInfo getNamedParameterInfo(String name) {
      return (ParameterInfo)this.namedParameters.get(name);
   }

   public int[] getNamedParameterSqlLocations(String name) {
      return this.getNamedParameterInfo(name).getSqlLocations();
   }

   public Type getNamedParameterExpectedType(String name) {
      return this.getNamedParameterInfo(name).getExpectedType();
   }

   public ParameterTranslationsImpl(List parameterSpecifications) {
      int size = parameterSpecifications.size();
      List ordinalParameterList = new ArrayList();
      Map namedParameterMap = new HashMap();

      class NamedParamTempHolder {
         String name;
         Type type;
         List positions = new ArrayList();
      }

      for(int i = 0; i < size; ++i) {
         ParameterSpecification spec = (ParameterSpecification)parameterSpecifications.get(i);
         if (PositionalParameterSpecification.class.isAssignableFrom(spec.getClass())) {
            PositionalParameterSpecification ordinalSpec = (PositionalParameterSpecification)spec;
            ordinalParameterList.add(new ParameterInfo(i, ordinalSpec.getExpectedType()));
         } else if (NamedParameterSpecification.class.isAssignableFrom(spec.getClass())) {
            NamedParameterSpecification namedSpec = (NamedParameterSpecification)spec;
            NamedParamTempHolder paramHolder = (NamedParamTempHolder)namedParameterMap.get(namedSpec.getName());
            if (paramHolder == null) {
               paramHolder = new NamedParamTempHolder();
               paramHolder.name = namedSpec.getName();
               paramHolder.type = namedSpec.getExpectedType();
               namedParameterMap.put(namedSpec.getName(), paramHolder);
            }

            paramHolder.positions.add(i);
         }
      }

      this.ordinalParameters = (ParameterInfo[])ordinalParameterList.toArray(new ParameterInfo[ordinalParameterList.size()]);
      if (namedParameterMap.isEmpty()) {
         this.namedParameters = Collections.EMPTY_MAP;
      } else {
         Map namedParametersBacking = new HashMap(namedParameterMap.size());

         for(NamedParamTempHolder holder : namedParameterMap.values()) {
            namedParametersBacking.put(holder.name, new ParameterInfo(ArrayHelper.toIntArray(holder.positions), holder.type));
         }

         this.namedParameters = Collections.unmodifiableMap(namedParametersBacking);
      }

   }

   public static class ParameterInfo implements Serializable {
      private final int[] sqlLocations;
      private final Type expectedType;

      public ParameterInfo(int[] sqlPositions, Type expectedType) {
         this.sqlLocations = sqlPositions;
         this.expectedType = expectedType;
      }

      public ParameterInfo(int sqlPosition, Type expectedType) {
         this.sqlLocations = new int[]{sqlPosition};
         this.expectedType = expectedType;
      }

      public int[] getSqlLocations() {
         return this.sqlLocations;
      }

      public Type getExpectedType() {
         return this.expectedType;
      }
   }
}
