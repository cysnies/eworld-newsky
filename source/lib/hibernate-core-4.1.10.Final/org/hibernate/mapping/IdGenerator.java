package org.hibernate.mapping;

import java.io.Serializable;
import java.util.Properties;

public class IdGenerator implements Serializable {
   private String name;
   private String identifierGeneratorStrategy;
   private Properties params = new Properties();

   public String getIdentifierGeneratorStrategy() {
      return this.identifierGeneratorStrategy;
   }

   public String getName() {
      return this.name;
   }

   public Properties getParams() {
      return this.params;
   }

   public void setIdentifierGeneratorStrategy(String string) {
      this.identifierGeneratorStrategy = string;
   }

   public void setName(String string) {
      this.name = string;
   }

   public void addParam(String key, String value) {
      this.params.setProperty(key, value);
   }
}
