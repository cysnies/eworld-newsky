package com.mysql.jdbc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Security {
   private static final char PVERSION41_CHAR = '*';
   private static final int SHA1_HASH_SIZE = 20;

   private static int charVal(char c) {
      return c >= '0' && c <= '9' ? c - 48 : (c >= 'A' && c <= 'Z' ? c - 65 + 10 : c - 97 + 10);
   }

   static byte[] createKeyFromOldPassword(String passwd) throws NoSuchAlgorithmException {
      passwd = makeScrambledPassword(passwd);
      int[] salt = getSaltFromPassword(passwd);
      return getBinaryPassword(salt, false);
   }

   static byte[] getBinaryPassword(int[] salt, boolean usingNewPasswords) throws NoSuchAlgorithmException {
      int val = 0;
      byte[] binaryPassword = new byte[20];
      if (usingNewPasswords) {
         int pos = 0;

         for(int i = 0; i < 4; ++i) {
            val = salt[i];

            for(int t = 3; t >= 0; --t) {
               binaryPassword[pos++] = (byte)(val & 255);
               val >>= 8;
            }
         }

         return binaryPassword;
      } else {
         int offset = 0;

         for(int i = 0; i < 2; ++i) {
            val = salt[i];

            for(int t = 3; t >= 0; --t) {
               binaryPassword[t + offset] = (byte)(val % 256);
               val >>= 8;
            }

            offset += 4;
         }

         MessageDigest md = MessageDigest.getInstance("SHA-1");
         md.update(binaryPassword, 0, 8);
         return md.digest();
      }
   }

   private static int[] getSaltFromPassword(String password) {
      int[] result = new int[6];
      if (password != null && password.length() != 0) {
         if (password.charAt(0) == '*') {
            String saltInHex = password.substring(1, 5);
            int val = 0;

            for(int i = 0; i < 4; ++i) {
               val = (val << 4) + charVal(saltInHex.charAt(i));
            }

            return result;
         } else {
            int resultPos = 0;
            int pos = 0;

            int val;
            for(int length = password.length(); pos < length; result[resultPos++] = val) {
               val = 0;

               for(int i = 0; i < 8; ++i) {
                  val = (val << 4) + charVal(password.charAt(pos++));
               }
            }

            return result;
         }
      } else {
         return result;
      }
   }

   private static String longToHex(long val) {
      String longHex = Long.toHexString(val);
      int length = longHex.length();
      if (length >= 8) {
         return longHex.substring(0, 8);
      } else {
         int padding = 8 - length;
         StringBuffer buf = new StringBuffer();

         for(int i = 0; i < padding; ++i) {
            buf.append("0");
         }

         buf.append(longHex);
         return buf.toString();
      }
   }

   static String makeScrambledPassword(String password) throws NoSuchAlgorithmException {
      long[] passwordHash = Util.newHash(password);
      StringBuffer scramble = new StringBuffer();
      scramble.append(longToHex(passwordHash[0]));
      scramble.append(longToHex(passwordHash[1]));
      return scramble.toString();
   }

   static void passwordCrypt(byte[] from, byte[] to, byte[] password, int length) {
      for(int pos = 0; pos < from.length && pos < length; ++pos) {
         to[pos] = (byte)(from[pos] ^ password[pos]);
      }

   }

   static byte[] passwordHashStage1(String password) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      StringBuffer cleansedPassword = new StringBuffer();
      int passwordLength = password.length();

      for(int i = 0; i < passwordLength; ++i) {
         char c = password.charAt(i);
         if (c != ' ' && c != '\t') {
            cleansedPassword.append(c);
         }
      }

      return md.digest(cleansedPassword.toString().getBytes());
   }

   static byte[] passwordHashStage2(byte[] hashedPassword, byte[] salt) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(salt, 0, 4);
      md.update(hashedPassword, 0, 20);
      return md.digest();
   }

   static byte[] scramble411(String password, String seed) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] passwordHashStage1 = md.digest(password.getBytes());
      md.reset();
      byte[] passwordHashStage2 = md.digest(passwordHashStage1);
      md.reset();
      byte[] seedAsBytes = seed.getBytes();
      md.update(seedAsBytes);
      md.update(passwordHashStage2);
      byte[] toBeXord = md.digest();
      int numToXor = toBeXord.length;

      for(int i = 0; i < numToXor; ++i) {
         toBeXord[i] ^= passwordHashStage1[i];
      }

      return toBeXord;
   }

   private Security() {
   }
}
