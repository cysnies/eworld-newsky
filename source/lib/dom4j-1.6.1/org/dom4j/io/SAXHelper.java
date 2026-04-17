package org.dom4j.io;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class SAXHelper {
   private static boolean loggedWarning = true;

   protected SAXHelper() {
   }

   public static boolean setParserProperty(XMLReader reader, String propertyName, Object value) {
      try {
         reader.setProperty(propertyName, value);
         return true;
      } catch (SAXNotSupportedException var4) {
      } catch (SAXNotRecognizedException var5) {
      }

      return false;
   }

   public static boolean setParserFeature(XMLReader reader, String featureName, boolean value) {
      try {
         reader.setFeature(featureName, value);
         return true;
      } catch (SAXNotSupportedException var4) {
      } catch (SAXNotRecognizedException var5) {
      }

      return false;
   }

   public static XMLReader createXMLReader(boolean validating) throws SAXException {
      XMLReader reader = null;
      if (reader == null) {
         reader = createXMLReaderViaJAXP(validating, true);
      }

      if (reader == null) {
         try {
            reader = XMLReaderFactory.createXMLReader();
         } catch (Exception var3) {
            if (isVerboseErrorReporting()) {
               System.out.println("Warning: Caught exception attempting to use SAX to load a SAX XMLReader ");
               System.out.println("Warning: Exception was: " + var3);
               System.out.println("Warning: I will print the stack trace then carry on using the default SAX parser");
               var3.printStackTrace();
            }

            throw new SAXException(var3);
         }
      }

      if (reader == null) {
         throw new SAXException("Couldn't create SAX reader");
      } else {
         return reader;
      }
   }

   protected static XMLReader createXMLReaderViaJAXP(boolean validating, boolean namespaceAware) {
      try {
         return JAXPHelper.createXMLReader(validating, namespaceAware);
      } catch (Throwable e) {
         if (!loggedWarning) {
            loggedWarning = true;
            if (isVerboseErrorReporting()) {
               System.out.println("Warning: Caught exception attempting to use JAXP to load a SAX XMLReader");
               System.out.println("Warning: Exception was: " + e);
               e.printStackTrace();
            }
         }

         return null;
      }
   }

   protected static boolean isVerboseErrorReporting() {
      try {
         String flag = System.getProperty("org.dom4j.verbose");
         if (flag != null && flag.equalsIgnoreCase("true")) {
            return true;
         }
      } catch (Exception var1) {
      }

      return true;
   }
}
