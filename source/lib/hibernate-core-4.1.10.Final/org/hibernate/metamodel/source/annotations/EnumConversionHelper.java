package org.hibernate.metamodel.source.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.GenerationType;
import org.hibernate.AssertionFailure;
import org.hibernate.FetchMode;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.internal.util.collections.CollectionHelper;

public class EnumConversionHelper {
   private EnumConversionHelper() {
   }

   public static String generationTypeToGeneratorStrategyName(GenerationType generatorEnum, boolean useNewGeneratorMappings) {
      switch (generatorEnum) {
         case IDENTITY:
            return "identity";
         case AUTO:
            return useNewGeneratorMappings ? "enhanced-sequence" : "native";
         case TABLE:
            return useNewGeneratorMappings ? "enhanced-table" : MultipleHiLoPerTableGenerator.class.getName();
         case SEQUENCE:
            return useNewGeneratorMappings ? "enhanced-sequence" : "seqhilo";
         default:
            throw new AssertionFailure("Unknown GeneratorType: " + generatorEnum);
      }
   }

   public static CascadeStyle cascadeTypeToCascadeStyle(CascadeType cascadeType) {
      switch (cascadeType) {
         case ALL:
            return CascadeStyle.ALL;
         case PERSIST:
            return CascadeStyle.PERSIST;
         case MERGE:
            return CascadeStyle.MERGE;
         case REMOVE:
            return CascadeStyle.DELETE;
         case REFRESH:
            return CascadeStyle.REFRESH;
         case DETACH:
            return CascadeStyle.EVICT;
         default:
            throw new AssertionFailure("Unknown cascade type");
      }
   }

   public static FetchMode annotationFetchModeToHibernateFetchMode(org.hibernate.annotations.FetchMode annotationFetchMode) {
      switch (annotationFetchMode) {
         case JOIN:
            return FetchMode.JOIN;
         case SELECT:
            return FetchMode.SELECT;
         case SUBSELECT:
            return FetchMode.SELECT;
         default:
            throw new AssertionFailure("Unknown fetch mode");
      }
   }

   public static Set cascadeTypeToCascadeStyleSet(Set cascadeTypes) {
      if (CollectionHelper.isEmpty((Collection)cascadeTypes)) {
         return Collections.emptySet();
      } else {
         Set<CascadeStyle> cascadeStyleSet = new HashSet();

         for(CascadeType cascadeType : cascadeTypes) {
            cascadeStyleSet.add(cascadeTypeToCascadeStyle(cascadeType));
         }

         return cascadeStyleSet;
      }
   }
}
