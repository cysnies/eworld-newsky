package com.mysql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
   private static final int BYTE_RANGE = 256;
   private static byte[] allBytes = new byte[256];
   private static char[] byteToChars = new char[256];
   private static Method toPlainStringMethod;
   static final int WILD_COMPARE_MATCH_NO_WILD = 0;
   static final int WILD_COMPARE_MATCH_WITH_WILD = 1;
   static final int WILD_COMPARE_NO_MATCH = -1;
   // $FF: synthetic field
   static Class class$java$math$BigDecimal;

   public static String consistentToString(BigDecimal decimal) {
      if (decimal == null) {
         return null;
      } else {
         if (toPlainStringMethod != null) {
            try {
               return (String)toPlainStringMethod.invoke(decimal, (Object[])null);
            } catch (InvocationTargetException var2) {
            } catch (IllegalAccessException var3) {
            }
         }

         return decimal.toString();
      }
   }

   public static final String dumpAsHex(byte[] byteBuffer, int length) {
      StringBuffer outputBuf = new StringBuffer(length * 4);
      int p = 0;
      int rows = length / 8;

      for(int i = 0; i < rows && p < length; ++i) {
         int ptemp = p;

         for(int j = 0; j < 8; ++j) {
            String hexVal = Integer.toHexString(byteBuffer[ptemp] & 255);
            if (hexVal.length() == 1) {
               hexVal = "0" + hexVal;
            }

            outputBuf.append(hexVal + " ");
            ++ptemp;
         }

         outputBuf.append("    ");

         for(int j = 0; j < 8; ++j) {
            int b = 255 & byteBuffer[p];
            if (b > 32 && b < 127) {
               outputBuf.append((char)b + " ");
            } else {
               outputBuf.append(". ");
            }

            ++p;
         }

         outputBuf.append("\n");
      }

      int n = 0;

      for(int i = p; i < length; ++i) {
         String hexVal = Integer.toHexString(byteBuffer[i] & 255);
         if (hexVal.length() == 1) {
            hexVal = "0" + hexVal;
         }

         outputBuf.append(hexVal + " ");
         ++n;
      }

      for(int i = n; i < 8; ++i) {
         outputBuf.append("   ");
      }

      outputBuf.append("    ");

      for(int i = p; i < length; ++i) {
         int b = 255 & byteBuffer[i];
         if (b > 32 && b < 127) {
            outputBuf.append((char)b + " ");
         } else {
            outputBuf.append(". ");
         }
      }

      outputBuf.append("\n");
      return outputBuf.toString();
   }

   private static boolean endsWith(byte[] dataFrom, String suffix) {
      for(int i = 1; i <= suffix.length(); ++i) {
         int dfOffset = dataFrom.length - i;
         int suffixOffset = suffix.length() - i;
         if (dataFrom[dfOffset] != suffix.charAt(suffixOffset)) {
            return false;
         }
      }

      return true;
   }

   public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes, String origString, int offset, int length) {
      if (origBytes != null && origBytes.length != 0) {
         int bytesLen = origBytes.length;
         int bufIndex = 0;
         int strIndex = 0;
         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);

         while(true) {
            if (origString.charAt(strIndex) == '\\') {
               bytesOut.write(origBytes[bufIndex++]);
            } else {
               int loByte = origBytes[bufIndex];
               if (loByte < 0) {
                  loByte += 256;
               }

               bytesOut.write(loByte);
               if (loByte >= 128) {
                  if (bufIndex < bytesLen - 1) {
                     int hiByte = origBytes[bufIndex + 1];
                     if (hiByte < 0) {
                        hiByte += 256;
                     }

                     bytesOut.write(hiByte);
                     ++bufIndex;
                     if (hiByte == 92) {
                        bytesOut.write(hiByte);
                     }
                  }
               } else if (loByte == 92 && bufIndex < bytesLen - 1) {
                  int hiByte = origBytes[bufIndex + 1];
                  if (hiByte < 0) {
                     hiByte += 256;
                  }

                  if (hiByte == 98) {
                     bytesOut.write(92);
                     bytesOut.write(98);
                     ++bufIndex;
                  }
               }

               ++bufIndex;
            }

            if (bufIndex >= bytesLen) {
               return bytesOut.toByteArray();
            }

            ++strIndex;
         }
      } else {
         return origBytes;
      }
   }

   public static char firstNonWsCharUc(String searchIn) {
      return firstNonWsCharUc(searchIn, 0);
   }

   public static char firstNonWsCharUc(String searchIn, int startAt) {
      if (searchIn == null) {
         return '\u0000';
      } else {
         int length = searchIn.length();

         for(int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (!Character.isWhitespace(c)) {
               return Character.toUpperCase(c);
            }
         }

         return '\u0000';
      }
   }

   public static char firstAlphaCharUc(String searchIn, int startAt) {
      if (searchIn == null) {
         return '\u0000';
      } else {
         int length = searchIn.length();

         for(int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (Character.isLetter(c)) {
               return Character.toUpperCase(c);
            }
         }

         return '\u0000';
      }
   }

   public static final String fixDecimalExponent(String dString) {
      int ePos = dString.indexOf("E");
      if (ePos == -1) {
         ePos = dString.indexOf("e");
      }

      if (ePos != -1 && dString.length() > ePos + 1) {
         char maybeMinusChar = dString.charAt(ePos + 1);
         if (maybeMinusChar != '-' && maybeMinusChar != '+') {
            StringBuffer buf = new StringBuffer(dString.length() + 1);
            buf.append(dString.substring(0, ePos + 1));
            buf.append('+');
            buf.append(dString.substring(ePos + 1, dString.length()));
            dString = buf.toString();
         }
      }

      return dString;
   }

   public static final byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
      try {
         byte[] b = null;
         if (converter != null) {
            b = converter.toBytes(c);
         } else if (encoding == null) {
            b = (new String(c)).getBytes();
         } else {
            String s = new String(c);
            b = s.getBytes(encoding);
            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
               b = escapeEasternUnicodeByteStream(b, s, 0, s.length());
            }
         }

         return b;
      } catch (UnsupportedEncodingException var7) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009");
      }
   }

   public static final byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode) throws SQLException {
      try {
         byte[] b = null;
         if (converter != null) {
            b = converter.toBytes(c, offset, length);
         } else if (encoding == null) {
            byte[] temp = (new String(c, offset, length)).getBytes();
            length = temp.length;
            b = new byte[length];
            System.arraycopy(temp, 0, b, 0, length);
         } else {
            String s = new String(c, offset, length);
            byte[] temp = s.getBytes(encoding);
            length = temp.length;
            b = new byte[length];
            System.arraycopy(temp, 0, b, 0, length);
            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
               b = escapeEasternUnicodeByteStream(b, s, offset, length);
            }
         }

         return b;
      } catch (UnsupportedEncodingException var10) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.10") + encoding + Messages.getString("StringUtils.11"), "S1009");
      }
   }

   public static final byte[] getBytes(char[] c, String encoding, String serverEncoding, boolean parserKnowsUnicode, ConnectionImpl conn) throws SQLException {
      try {
         SingleByteCharsetConverter converter = null;
         if (conn != null) {
            converter = conn.getCharsetConverter(encoding);
         } else {
            converter = SingleByteCharsetConverter.getInstance(encoding, (Connection)null);
         }

         return getBytes(c, converter, encoding, serverEncoding, parserKnowsUnicode);
      } catch (UnsupportedEncodingException var6) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009");
      }
   }

   public static final byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
      try {
         byte[] b = null;
         if (converter != null) {
            b = converter.toBytes(s);
         } else if (encoding == null) {
            b = s.getBytes();
         } else {
            b = s.getBytes(encoding);
            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
               b = escapeEasternUnicodeByteStream(b, s, 0, s.length());
            }
         }

         return b;
      } catch (UnsupportedEncodingException var6) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009");
      }
   }

   public static final byte[] getBytesWrapped(String s, char beginWrap, char endWrap, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
      try {
         byte[] b = null;
         if (converter != null) {
            b = converter.toBytesWrapped(s, beginWrap, endWrap);
         } else if (encoding == null) {
            StringBuffer buf = new StringBuffer(s.length() + 2);
            buf.append(beginWrap);
            buf.append(s);
            buf.append(endWrap);
            b = buf.toString().getBytes();
         } else {
            StringBuffer buf = new StringBuffer(s.length() + 2);
            buf.append(beginWrap);
            buf.append(s);
            buf.append(endWrap);
            b = buf.toString().getBytes(encoding);
            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
               b = escapeEasternUnicodeByteStream(b, s, 0, s.length());
            }
         }

         return b;
      } catch (UnsupportedEncodingException var9) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009");
      }
   }

   public static final byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode) throws SQLException {
      try {
         byte[] b = null;
         if (converter != null) {
            b = converter.toBytes(s, offset, length);
         } else if (encoding == null) {
            byte[] temp = s.substring(offset, offset + length).getBytes();
            length = temp.length;
            b = new byte[length];
            System.arraycopy(temp, 0, b, 0, length);
         } else {
            byte[] temp = s.substring(offset, offset + length).getBytes(encoding);
            length = temp.length;
            b = new byte[length];
            System.arraycopy(temp, 0, b, 0, length);
            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
               b = escapeEasternUnicodeByteStream(b, s, offset, length);
            }
         }

         return b;
      } catch (UnsupportedEncodingException var9) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.10") + encoding + Messages.getString("StringUtils.11"), "S1009");
      }
   }

   public static final byte[] getBytes(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, ConnectionImpl conn) throws SQLException {
      try {
         SingleByteCharsetConverter converter = null;
         if (conn != null) {
            converter = conn.getCharsetConverter(encoding);
         } else {
            converter = SingleByteCharsetConverter.getInstance(encoding, (Connection)null);
         }

         return getBytes(s, converter, encoding, serverEncoding, parserKnowsUnicode);
      } catch (UnsupportedEncodingException var6) {
         throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009");
      }
   }

   public static int getInt(byte[] buf, int offset, int endPos) throws NumberFormatException {
      int base = 10;

      int s;
      for(s = offset; Character.isWhitespace((char)buf[s]) && s < endPos; ++s) {
      }

      if (s == endPos) {
         throw new NumberFormatException(new String(buf));
      } else {
         boolean negative = false;
         if ((char)buf[s] == '-') {
            negative = true;
            ++s;
         } else if ((char)buf[s] == '+') {
            ++s;
         }

         int save = s;
         int cutoff = Integer.MAX_VALUE / base;
         int cutlim = Integer.MAX_VALUE % base;
         if (negative) {
            ++cutlim;
         }

         boolean overflow = false;

         int i;
         for(i = 0; s < endPos; ++s) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
               c = (char)(c - 48);
            } else {
               if (!Character.isLetter(c)) {
                  break;
               }

               c = (char)(Character.toUpperCase(c) - 65 + 10);
            }

            if (c >= base) {
               break;
            }

            if (i <= cutoff && (i != cutoff || c <= cutlim)) {
               i *= base;
               i += c;
            } else {
               overflow = true;
            }
         }

         if (s == save) {
            throw new NumberFormatException(new String(buf));
         } else if (overflow) {
            throw new NumberFormatException(new String(buf));
         } else {
            return negative ? -i : i;
         }
      }
   }

   public static int getInt(byte[] buf) throws NumberFormatException {
      return getInt(buf, 0, buf.length);
   }

   public static long getLong(byte[] buf) throws NumberFormatException {
      return getLong(buf, 0, buf.length);
   }

   public static long getLong(byte[] buf, int offset, int endpos) throws NumberFormatException {
      int base = 10;

      int s;
      for(s = offset; Character.isWhitespace((char)buf[s]) && s < endpos; ++s) {
      }

      if (s == endpos) {
         throw new NumberFormatException(new String(buf));
      } else {
         boolean negative = false;
         if ((char)buf[s] == '-') {
            negative = true;
            ++s;
         } else if ((char)buf[s] == '+') {
            ++s;
         }

         int save = s;
         long cutoff = Long.MAX_VALUE / (long)base;
         long cutlim = (long)((int)(Long.MAX_VALUE % (long)base));
         if (negative) {
            ++cutlim;
         }

         boolean overflow = false;

         long i;
         for(i = 0L; s < endpos; ++s) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
               c = (char)(c - 48);
            } else {
               if (!Character.isLetter(c)) {
                  break;
               }

               c = (char)(Character.toUpperCase(c) - 65 + 10);
            }

            if (c >= base) {
               break;
            }

            if (i <= cutoff && (i != cutoff || (long)c <= cutlim)) {
               i *= (long)base;
               i += (long)c;
            } else {
               overflow = true;
            }
         }

         if (s == save) {
            throw new NumberFormatException(new String(buf));
         } else if (overflow) {
            throw new NumberFormatException(new String(buf));
         } else {
            return negative ? -i : i;
         }
      }
   }

   public static short getShort(byte[] buf) throws NumberFormatException {
      short base = 10;

      int s;
      for(s = 0; Character.isWhitespace((char)buf[s]) && s < buf.length; ++s) {
      }

      if (s == buf.length) {
         throw new NumberFormatException(new String(buf));
      } else {
         boolean negative = false;
         if ((char)buf[s] == '-') {
            negative = true;
            ++s;
         } else if ((char)buf[s] == '+') {
            ++s;
         }

         int save = s;
         short cutoff = (short)(32767 / base);
         short cutlim = (short)(32767 % base);
         if (negative) {
            ++cutlim;
         }

         boolean overflow = false;

         short i;
         for(i = 0; s < buf.length; ++s) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
               c = (char)(c - 48);
            } else {
               if (!Character.isLetter(c)) {
                  break;
               }

               c = (char)(Character.toUpperCase(c) - 65 + 10);
            }

            if (c >= base) {
               break;
            }

            if (i <= cutoff && (i != cutoff || c <= cutlim)) {
               i = (short)(i * base);
               i = (short)(i + c);
            } else {
               overflow = true;
            }
         }

         if (s == save) {
            throw new NumberFormatException(new String(buf));
         } else if (overflow) {
            throw new NumberFormatException(new String(buf));
         } else {
            return negative ? (short)(-i) : i;
         }
      }
   }

   public static final int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
      if (searchIn != null && searchFor != null && startingPosition <= searchIn.length()) {
         int patternLength = searchFor.length();
         int stringLength = searchIn.length();
         int stopSearchingAt = stringLength - patternLength;
         int i = startingPosition;
         if (patternLength == 0) {
            return -1;
         } else {
            char firstCharOfPatternUc = Character.toUpperCase(searchFor.charAt(0));
            char firstCharOfPatternLc = Character.toLowerCase(searchFor.charAt(0));

            while(true) {
               label41:
               while(i >= stopSearchingAt || Character.toUpperCase(searchIn.charAt(i)) == firstCharOfPatternUc || Character.toLowerCase(searchIn.charAt(i)) == firstCharOfPatternLc) {
                  if (i > stopSearchingAt) {
                     return -1;
                  }

                  int j = i + 1;
                  int end = j + patternLength - 1;
                  int k = 1;

                  while(j < end) {
                     int searchInPos = j++;
                     int searchForPos = k++;
                     if (Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(searchFor.charAt(searchForPos))) {
                        ++i;
                        continue label41;
                     }

                     if (Character.toLowerCase(searchIn.charAt(searchInPos)) != Character.toLowerCase(searchFor.charAt(searchForPos))) {
                        ++i;
                        continue label41;
                     }
                  }

                  return i;
               }

               ++i;
            }
         }
      } else {
         return -1;
      }
   }

   public static final int indexOfIgnoreCase(String searchIn, String searchFor) {
      return indexOfIgnoreCase(0, searchIn, searchFor);
   }

   public static int indexOfIgnoreCaseRespectMarker(int startAt, String src, String target, String marker, String markerCloses, boolean allowBackslashEscapes) {
      char contextMarker = 0;
      boolean escaped = false;
      int markerTypeFound = 0;
      int srcLength = src.length();
      int ind = 0;

      for(int i = startAt; i < srcLength; ++i) {
         char c = src.charAt(i);
         if (allowBackslashEscapes && c == '\\') {
            escaped = !escaped;
         } else if (c == markerCloses.charAt(markerTypeFound) && !escaped) {
            contextMarker = 0;
         } else if ((ind = marker.indexOf(c)) != -1 && !escaped && contextMarker == 0) {
            markerTypeFound = ind;
            contextMarker = c;
         } else if (c == target.charAt(0) && !escaped && contextMarker == 0 && indexOfIgnoreCase(i, src, target) != -1) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOfIgnoreCaseRespectQuotes(int startAt, String src, String target, char quoteChar, boolean allowBackslashEscapes) {
      char contextMarker = 0;
      boolean escaped = false;
      int srcLength = src.length();

      for(int i = startAt; i < srcLength; ++i) {
         char c = src.charAt(i);
         if (allowBackslashEscapes && c == '\\') {
            escaped = !escaped;
         } else if (c == contextMarker && !escaped) {
            contextMarker = 0;
         } else if (c == quoteChar && !escaped && contextMarker == 0) {
            contextMarker = c;
         } else if ((Character.toUpperCase(c) == Character.toUpperCase(target.charAt(0)) || Character.toLowerCase(c) == Character.toLowerCase(target.charAt(0))) && !escaped && contextMarker == 0 && startsWithIgnoreCase(src, i, target)) {
            return i;
         }
      }

      return -1;
   }

   public static final List split(String stringToSplit, String delimitter, boolean trim) {
      if (stringToSplit == null) {
         return new ArrayList();
      } else if (delimitter == null) {
         throw new IllegalArgumentException();
      } else {
         StringTokenizer tokenizer = new StringTokenizer(stringToSplit, delimitter, false);

         List splitTokens;
         String token;
         for(splitTokens = new ArrayList(tokenizer.countTokens()); tokenizer.hasMoreTokens(); splitTokens.add(token)) {
            token = tokenizer.nextToken();
            if (trim) {
               token = token.trim();
            }
         }

         return splitTokens;
      }
   }

   public static final List split(String stringToSplit, String delimiter, String markers, String markerCloses, boolean trim) {
      if (stringToSplit == null) {
         return new ArrayList();
      } else if (delimiter == null) {
         throw new IllegalArgumentException();
      } else {
         int delimPos = 0;
         int currentPos = 0;

         List splitTokens;
         for(splitTokens = new ArrayList(); (delimPos = indexOfIgnoreCaseRespectMarker(currentPos, stringToSplit, delimiter, markers, markerCloses, false)) != -1; currentPos = delimPos + 1) {
            String token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
               token = token.trim();
            }

            splitTokens.add(token);
         }

         if (currentPos < stringToSplit.length()) {
            String token = stringToSplit.substring(currentPos);
            if (trim) {
               token = token.trim();
            }

            splitTokens.add(token);
         }

         return splitTokens;
      }
   }

   private static boolean startsWith(byte[] dataFrom, String chars) {
      for(int i = 0; i < chars.length(); ++i) {
         if (dataFrom[i] != chars.charAt(i)) {
            return false;
         }
      }

      return true;
   }

   public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
      return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
   }

   public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
      return startsWithIgnoreCase(searchIn, 0, searchFor);
   }

   public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(String searchIn, String searchFor) {
      if (searchIn == null) {
         return searchFor == null;
      } else {
         int beginPos = 0;
         int inLength = searchIn.length();

         for(beginPos = 0; beginPos < inLength; ++beginPos) {
            char c = searchIn.charAt(beginPos);
            if (Character.isLetterOrDigit(c)) {
               break;
            }
         }

         return startsWithIgnoreCase(searchIn, beginPos, searchFor);
      }
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
      return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
      if (searchIn == null) {
         return searchFor == null;
      } else {
         for(int inLength = searchIn.length(); beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos)); ++beginPos) {
         }

         return startsWithIgnoreCase(searchIn, beginPos, searchFor);
      }
   }

   public static byte[] stripEnclosure(byte[] source, String prefix, String suffix) {
      if (source.length >= prefix.length() + suffix.length() && startsWith(source, prefix) && endsWith(source, suffix)) {
         int totalToStrip = prefix.length() + suffix.length();
         int enclosedLength = source.length - totalToStrip;
         byte[] enclosed = new byte[enclosedLength];
         int startPos = prefix.length();
         int numToCopy = enclosed.length;
         System.arraycopy(source, startPos, enclosed, 0, numToCopy);
         return enclosed;
      } else {
         return source;
      }
   }

   public static final String toAsciiString(byte[] buffer) {
      return toAsciiString(buffer, 0, buffer.length);
   }

   public static final String toAsciiString(byte[] buffer, int startPos, int length) {
      char[] charArray = new char[length];
      int readpoint = startPos;

      for(int i = 0; i < length; ++i) {
         charArray[i] = (char)buffer[readpoint];
         ++readpoint;
      }

      return new String(charArray);
   }

   public static int wildCompare(String searchIn, String searchForWildcard) {
      if (searchIn != null && searchForWildcard != null) {
         if (searchForWildcard.equals("%")) {
            return 1;
         } else {
            int result = -1;
            char wildcardMany = '%';
            char wildcardOne = '_';
            char wildcardEscape = '\\';
            int searchForPos = 0;
            int searchForEnd = searchForWildcard.length();
            int searchInPos = 0;
            int searchInEnd = searchIn.length();

            while(searchForPos != searchForEnd) {
               for(char wildstrChar = searchForWildcard.charAt(searchForPos); searchForWildcard.charAt(searchForPos) != wildcardMany && wildstrChar != wildcardOne; result = 1) {
                  if (searchForWildcard.charAt(searchForPos) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                     ++searchForPos;
                  }

                  if (searchInPos == searchInEnd || Character.toUpperCase(searchForWildcard.charAt(searchForPos++)) != Character.toUpperCase(searchIn.charAt(searchInPos++))) {
                     return 1;
                  }

                  if (searchForPos == searchForEnd) {
                     return searchInPos != searchInEnd ? 1 : 0;
                  }
               }

               if (searchForWildcard.charAt(searchForPos) == wildcardOne) {
                  do {
                     if (searchInPos == searchInEnd) {
                        return result;
                     }

                     ++searchInPos;
                     ++searchForPos;
                  } while(searchForPos < searchForEnd && searchForWildcard.charAt(searchForPos) == wildcardOne);

                  if (searchForPos == searchForEnd) {
                     break;
                  }
               }

               if (searchForWildcard.charAt(searchForPos) == wildcardMany) {
                  ++searchForPos;

                  for(; searchForPos != searchForEnd; ++searchForPos) {
                     if (searchForWildcard.charAt(searchForPos) != wildcardMany) {
                        if (searchForWildcard.charAt(searchForPos) != wildcardOne) {
                           break;
                        }

                        if (searchInPos == searchInEnd) {
                           return -1;
                        }

                        ++searchInPos;
                     }
                  }

                  if (searchForPos == searchForEnd) {
                     return 0;
                  }

                  if (searchInPos == searchInEnd) {
                     return -1;
                  }

                  char cmp;
                  if ((cmp = searchForWildcard.charAt(searchForPos)) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                     ++searchForPos;
                     cmp = searchForWildcard.charAt(searchForPos);
                  }

                  ++searchForPos;

                  do {
                     while(searchInPos != searchInEnd && Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(cmp)) {
                        ++searchInPos;
                     }

                     if (searchInPos++ == searchInEnd) {
                        return -1;
                     }

                     int tmp = wildCompare(searchIn, searchForWildcard);
                     if (tmp <= 0) {
                        return tmp;
                     }
                  } while(searchInPos != searchInEnd && searchForWildcard.charAt(0) != wildcardMany);

                  return -1;
               }
            }

            return searchInPos != searchInEnd ? 1 : 0;
         }
      } else {
         return -1;
      }
   }

   static byte[] s2b(String s, ConnectionImpl conn) throws SQLException {
      if (s == null) {
         return null;
      } else if (conn != null && conn.getUseUnicode()) {
         try {
            String encoding = conn.getEncoding();
            if (encoding == null) {
               return s.getBytes();
            } else {
               SingleByteCharsetConverter converter = conn.getCharsetConverter(encoding);
               return converter != null ? converter.toBytes(s) : s.getBytes(encoding);
            }
         } catch (UnsupportedEncodingException var4) {
            return s.getBytes();
         }
      } else {
         return s.getBytes();
      }
   }

   public static int lastIndexOf(byte[] s, char c) {
      if (s == null) {
         return -1;
      } else {
         for(int i = s.length - 1; i >= 0; --i) {
            if (s[i] == c) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int indexOf(byte[] s, char c) {
      if (s == null) {
         return -1;
      } else {
         int length = s.length;

         for(int i = 0; i < length; ++i) {
            if (s[i] == c) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean isNullOrEmpty(String toTest) {
      return toTest == null || toTest.length() == 0;
   }

   public static String stripComments(String src, String stringOpens, String stringCloses, boolean slashStarComments, boolean slashSlashComments, boolean hashComments, boolean dashDashComments) {
      if (src == null) {
         return null;
      } else {
         StringBuffer buf = new StringBuffer(src.length());
         StringReader sourceReader = new StringReader(src);
         int contextMarker = 0;
         boolean escaped = false;
         int markerTypeFound = -1;
         int ind = 0;
         int currentChar = 0;

         try {
            label141:
            while((currentChar = sourceReader.read()) != -1) {
               if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
                  contextMarker = 0;
                  markerTypeFound = -1;
               } else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped && contextMarker == 0) {
                  markerTypeFound = ind;
                  contextMarker = currentChar;
               }

               if (contextMarker == 0 && currentChar == 47 && (slashSlashComments || slashStarComments)) {
                  currentChar = sourceReader.read();
                  if (currentChar == 42 && slashStarComments) {
                     for(int prevChar = 0; (currentChar = sourceReader.read()) != 47 || prevChar != 42; prevChar = currentChar) {
                        if (currentChar == 13) {
                           currentChar = sourceReader.read();
                           if (currentChar == 10) {
                              currentChar = sourceReader.read();
                           }
                        } else if (currentChar == 10) {
                           currentChar = sourceReader.read();
                        }

                        if (currentChar < 0) {
                           continue label141;
                        }
                     }
                     continue;
                  }

                  if (currentChar == 47 && slashSlashComments) {
                     while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                     }
                  }
               } else if (contextMarker == 0 && currentChar == 35 && hashComments) {
                  while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                  }
               } else if (contextMarker == 0 && currentChar == 45 && dashDashComments) {
                  label156: {
                     currentChar = sourceReader.read();
                     if (currentChar != -1 && currentChar == 45) {
                        while(true) {
                           if ((currentChar = sourceReader.read()) == 10 || currentChar == 13 || currentChar < 0) {
                              break label156;
                           }
                        }
                     }

                     buf.append('-');
                     if (currentChar != -1) {
                        buf.append(currentChar);
                     }
                     continue;
                  }
               }

               if (currentChar != -1) {
                  buf.append((char)currentChar);
               }
            }
         } catch (IOException var15) {
         }

         return buf.toString();
      }
   }

   public static final boolean isEmptyOrWhitespaceOnly(String str) {
      if (str != null && str.length() != 0) {
         int length = str.length();

         for(int i = 0; i < length; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      for(int i = -128; i <= 127; ++i) {
         allBytes[i - -128] = (byte)i;
      }

      String allBytesString = new String(allBytes, 0, 255);
      int allBytesStringLen = allBytesString.length();

      for(int i = 0; i < 255 && i < allBytesStringLen; ++i) {
         byteToChars[i] = allBytesString.charAt(i);
      }

      try {
         toPlainStringMethod = (class$java$math$BigDecimal == null ? (class$java$math$BigDecimal = class$("java.math.BigDecimal")) : class$java$math$BigDecimal).getMethod("toPlainString");
      } catch (NoSuchMethodException var3) {
      }

   }
}
