package org.hibernate;

import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.util.xml.XmlDocument;

public class InvalidMappingException extends MappingException {
   private final String path;
   private final String type;

   public InvalidMappingException(String customMessage, String type, String path, Throwable cause) {
      super(customMessage, cause);
      this.type = type;
      this.path = path;
   }

   public InvalidMappingException(String customMessage, String type, String path) {
      super(customMessage);
      this.type = type;
      this.path = path;
   }

   public InvalidMappingException(String customMessage, XmlDocument xmlDocument, Throwable cause) {
      this(customMessage, xmlDocument.getOrigin().getType(), xmlDocument.getOrigin().getName(), cause);
   }

   public InvalidMappingException(String customMessage, XmlDocument xmlDocument) {
      this(customMessage, xmlDocument.getOrigin().getType(), xmlDocument.getOrigin().getName());
   }

   public InvalidMappingException(String customMessage, Origin origin) {
      this(customMessage, origin.getType().toString(), origin.getName());
   }

   public InvalidMappingException(String type, String path) {
      this("Could not parse mapping document from " + type + (path == null ? "" : " " + path), type, path);
   }

   public InvalidMappingException(String type, String path, Throwable cause) {
      this("Could not parse mapping document from " + type + (path == null ? "" : " " + path), type, path, cause);
   }

   public String getType() {
      return this.type;
   }

   public String getPath() {
      return this.path;
   }
}
