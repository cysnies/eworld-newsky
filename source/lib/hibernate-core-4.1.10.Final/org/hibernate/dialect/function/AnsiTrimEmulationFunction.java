package org.hibernate.dialect.function;

import org.hibernate.type.StandardBasicTypes;

public class AnsiTrimEmulationFunction extends AbstractAnsiTrimEmulationFunction {
   public static final String LTRIM = "ltrim";
   public static final String RTRIM = "rtrim";
   public static final String REPLACE = "replace";
   public static final String SPACE_PLACEHOLDER = "${space}$";
   public static final String LEADING_SPACE_TRIM_TEMPLATE = "ltrim(?1)";
   public static final String TRAILING_SPACE_TRIM_TEMPLATE = "rtrim(?1)";
   public static final String BOTH_SPACE_TRIM_TEMPLATE = "ltrim(rtrim(?1))";
   public static final String BOTH_SPACE_TRIM_FROM_TEMPLATE = "ltrim(rtrim(?2))";
   public static final String LEADING_TRIM_TEMPLATE = "replace(replace(ltrim(replace(replace(?1,' ','${space}$'),?2,' ')),' ',?2),'${space}$',' ')";
   public static final String TRAILING_TRIM_TEMPLATE = "replace(replace(rtrim(replace(replace(?1,' ','${space}$'),?2,' ')),' ',?2),'${space}$',' ')";
   public static final String BOTH_TRIM_TEMPLATE = "replace(replace(ltrim(rtrim(replace(replace(?1,' ','${space}$'),?2,' '))),' ',?2),'${space}$',' ')";
   private final SQLFunction leadingSpaceTrim;
   private final SQLFunction trailingSpaceTrim;
   private final SQLFunction bothSpaceTrim;
   private final SQLFunction bothSpaceTrimFrom;
   private final SQLFunction leadingTrim;
   private final SQLFunction trailingTrim;
   private final SQLFunction bothTrim;

   public AnsiTrimEmulationFunction() {
      this("ltrim", "rtrim", "replace");
   }

   public AnsiTrimEmulationFunction(String ltrimFunctionName, String rtrimFunctionName, String replaceFunctionName) {
      this.leadingSpaceTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(?1)".replaceAll("ltrim", ltrimFunctionName));
      this.trailingSpaceTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "rtrim(?1)".replaceAll("rtrim", rtrimFunctionName));
      this.bothSpaceTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(rtrim(?1))".replaceAll("ltrim", ltrimFunctionName).replaceAll("rtrim", rtrimFunctionName));
      this.bothSpaceTrimFrom = new SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(rtrim(?2))".replaceAll("ltrim", ltrimFunctionName).replaceAll("rtrim", rtrimFunctionName));
      this.leadingTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "replace(replace(ltrim(replace(replace(?1,' ','${space}$'),?2,' ')),' ',?2),'${space}$',' ')".replaceAll("ltrim", ltrimFunctionName).replaceAll("rtrim", rtrimFunctionName).replaceAll("replace", replaceFunctionName));
      this.trailingTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "replace(replace(rtrim(replace(replace(?1,' ','${space}$'),?2,' ')),' ',?2),'${space}$',' ')".replaceAll("ltrim", ltrimFunctionName).replaceAll("rtrim", rtrimFunctionName).replaceAll("replace", replaceFunctionName));
      this.bothTrim = new SQLFunctionTemplate(StandardBasicTypes.STRING, "replace(replace(ltrim(rtrim(replace(replace(?1,' ','${space}$'),?2,' '))),' ',?2),'${space}$',' ')".replaceAll("ltrim", ltrimFunctionName).replaceAll("rtrim", rtrimFunctionName).replaceAll("replace", replaceFunctionName));
   }

   protected SQLFunction resolveBothSpaceTrimFunction() {
      return this.bothSpaceTrim;
   }

   protected SQLFunction resolveBothSpaceTrimFromFunction() {
      return this.bothSpaceTrimFrom;
   }

   protected SQLFunction resolveLeadingSpaceTrimFunction() {
      return this.leadingSpaceTrim;
   }

   protected SQLFunction resolveTrailingSpaceTrimFunction() {
      return this.trailingSpaceTrim;
   }

   protected SQLFunction resolveBothTrimFunction() {
      return this.bothTrim;
   }

   protected SQLFunction resolveLeadingTrimFunction() {
      return this.leadingTrim;
   }

   protected SQLFunction resolveTrailingTrimFunction() {
      return this.trailingTrim;
   }
}
