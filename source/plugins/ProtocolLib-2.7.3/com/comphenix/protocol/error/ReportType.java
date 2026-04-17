package com.comphenix.protocol.error;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReportType {
   private final String errorFormat;
   protected String reportName;

   public ReportType(String errorFormat) {
      this.errorFormat = errorFormat;
   }

   public String getMessage(Object[] parameters) {
      return parameters != null && parameters.length != 0 ? String.format(this.errorFormat, parameters) : this.toString();
   }

   public String toString() {
      return this.errorFormat;
   }

   public static Class getSenderClass(Object sender) {
      if (sender == null) {
         throw new IllegalArgumentException("sender cannot be NUll.");
      } else {
         return sender instanceof Class ? (Class)sender : sender.getClass();
      }
   }

   public static String getReportName(Object sender, ReportType type) {
      if (sender == null) {
         throw new IllegalArgumentException("sender cannot be NUll.");
      } else {
         return getReportName(getSenderClass(sender), type);
      }
   }

   private static String getReportName(Class sender, ReportType type) {
      if (sender == null) {
         throw new IllegalArgumentException("sender cannot be NUll.");
      } else if (type.reportName != null) {
         return type.reportName;
      } else {
         for(Field field : getReportFields(sender)) {
            try {
               field.setAccessible(true);
               if (field.get((Object)null) == type) {
                  return type.reportName = field.getDeclaringClass().getCanonicalName() + "#" + field.getName();
               }
            } catch (IllegalAccessException e) {
               throw new FieldAccessException("Unable to read field " + field, e);
            }
         }

         throw new IllegalArgumentException("Cannot find report name for " + type);
      }
   }

   public static ReportType[] getReports(Class sender) {
      if (sender == null) {
         throw new IllegalArgumentException("sender cannot be NULL.");
      } else {
         List<ReportType> result = new ArrayList();

         for(Field field : getReportFields(sender)) {
            try {
               field.setAccessible(true);
               result.add((ReportType)field.get((Object)null));
            } catch (IllegalAccessException e) {
               throw new FieldAccessException("Unable to read field " + field, e);
            }
         }

         return (ReportType[])result.toArray(new ReportType[0]);
      }
   }

   private static List getReportFields(Class clazz) {
      return FuzzyReflection.fromClass(clazz).getFieldList(FuzzyFieldContract.newBuilder().requireModifier(8).typeDerivedOf(ReportType.class).build());
   }
}
