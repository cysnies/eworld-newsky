package org.dom4j.io;

public class OutputFormat implements Cloneable {
   protected static final String STANDARD_INDENT = "  ";
   private boolean suppressDeclaration = false;
   private boolean newLineAfterDeclaration = true;
   private String encoding = "UTF-8";
   private boolean omitEncoding = false;
   private String indent = null;
   private boolean expandEmptyElements = false;
   private boolean newlines = false;
   private String lineSeparator = "\n";
   private boolean trimText = false;
   private boolean padText = false;
   private boolean doXHTML = false;
   private int newLineAfterNTags = 0;
   private char attributeQuoteChar = '"';

   public OutputFormat() {
   }

   public OutputFormat(String indent) {
      this.indent = indent;
   }

   public OutputFormat(String indent, boolean newlines) {
      this.indent = indent;
      this.newlines = newlines;
   }

   public OutputFormat(String indent, boolean newlines, String encoding) {
      this.indent = indent;
      this.newlines = newlines;
      this.encoding = encoding;
   }

   public String getLineSeparator() {
      return this.lineSeparator;
   }

   public void setLineSeparator(String separator) {
      this.lineSeparator = separator;
   }

   public boolean isNewlines() {
      return this.newlines;
   }

   public void setNewlines(boolean newlines) {
      this.newlines = newlines;
   }

   public String getEncoding() {
      return this.encoding;
   }

   public void setEncoding(String encoding) {
      if (encoding != null) {
         this.encoding = encoding;
      }

   }

   public boolean isOmitEncoding() {
      return this.omitEncoding;
   }

   public void setOmitEncoding(boolean omitEncoding) {
      this.omitEncoding = omitEncoding;
   }

   public void setSuppressDeclaration(boolean suppressDeclaration) {
      this.suppressDeclaration = suppressDeclaration;
   }

   public boolean isSuppressDeclaration() {
      return this.suppressDeclaration;
   }

   public void setNewLineAfterDeclaration(boolean newLineAfterDeclaration) {
      this.newLineAfterDeclaration = newLineAfterDeclaration;
   }

   public boolean isNewLineAfterDeclaration() {
      return this.newLineAfterDeclaration;
   }

   public boolean isExpandEmptyElements() {
      return this.expandEmptyElements;
   }

   public void setExpandEmptyElements(boolean expandEmptyElements) {
      this.expandEmptyElements = expandEmptyElements;
   }

   public boolean isTrimText() {
      return this.trimText;
   }

   public void setTrimText(boolean trimText) {
      this.trimText = trimText;
   }

   public boolean isPadText() {
      return this.padText;
   }

   public void setPadText(boolean padText) {
      this.padText = padText;
   }

   public String getIndent() {
      return this.indent;
   }

   public void setIndent(String indent) {
      if (indent != null && indent.length() <= 0) {
         indent = null;
      }

      this.indent = indent;
   }

   public void setIndent(boolean doIndent) {
      if (doIndent) {
         this.indent = "  ";
      } else {
         this.indent = null;
      }

   }

   public void setIndentSize(int indentSize) {
      StringBuffer indentBuffer = new StringBuffer();

      for(int i = 0; i < indentSize; ++i) {
         indentBuffer.append(" ");
      }

      this.indent = indentBuffer.toString();
   }

   public boolean isXHTML() {
      return this.doXHTML;
   }

   public void setXHTML(boolean xhtml) {
      this.doXHTML = xhtml;
   }

   public int getNewLineAfterNTags() {
      return this.newLineAfterNTags;
   }

   public void setNewLineAfterNTags(int tagCount) {
      this.newLineAfterNTags = tagCount;
   }

   public char getAttributeQuoteCharacter() {
      return this.attributeQuoteChar;
   }

   public void setAttributeQuoteCharacter(char quoteChar) {
      if (quoteChar != '\'' && quoteChar != '"') {
         throw new IllegalArgumentException("Invalid attribute quote character (" + quoteChar + ")");
      } else {
         this.attributeQuoteChar = quoteChar;
      }
   }

   public int parseOptions(String[] args, int i) {
      for(int size = args.length; i < size; ++i) {
         if (args[i].equals("-suppressDeclaration")) {
            this.setSuppressDeclaration(true);
         } else if (args[i].equals("-omitEncoding")) {
            this.setOmitEncoding(true);
         } else if (args[i].equals("-indent")) {
            ++i;
            this.setIndent(args[i]);
         } else if (args[i].equals("-indentSize")) {
            ++i;
            this.setIndentSize(Integer.parseInt(args[i]));
         } else if (args[i].startsWith("-expandEmpty")) {
            this.setExpandEmptyElements(true);
         } else if (args[i].equals("-encoding")) {
            ++i;
            this.setEncoding(args[i]);
         } else if (args[i].equals("-newlines")) {
            this.setNewlines(true);
         } else if (args[i].equals("-lineSeparator")) {
            ++i;
            this.setLineSeparator(args[i]);
         } else if (args[i].equals("-trimText")) {
            this.setTrimText(true);
         } else if (args[i].equals("-padText")) {
            this.setPadText(true);
         } else {
            if (!args[i].startsWith("-xhtml")) {
               return i;
            }

            this.setXHTML(true);
         }
      }

      return i;
   }

   public static OutputFormat createPrettyPrint() {
      OutputFormat format = new OutputFormat();
      format.setIndentSize(2);
      format.setNewlines(true);
      format.setTrimText(true);
      format.setPadText(true);
      return format;
   }

   public static OutputFormat createCompactFormat() {
      OutputFormat format = new OutputFormat();
      format.setIndent(false);
      format.setNewlines(false);
      format.setTrimText(true);
      return format;
   }
}
