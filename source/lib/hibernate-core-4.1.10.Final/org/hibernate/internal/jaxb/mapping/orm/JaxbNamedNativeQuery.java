package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "named-native-query",
   propOrder = {"description", "query", "hint"}
)
public class JaxbNamedNativeQuery {
   protected String description;
   @XmlElement(
      required = true
   )
   protected String query;
   protected List hint;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "result-class"
   )
   protected String resultClass;
   @XmlAttribute(
      name = "result-set-mapping"
   )
   protected String resultSetMapping;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public String getQuery() {
      return this.query;
   }

   public void setQuery(String value) {
      this.query = value;
   }

   public List getHint() {
      if (this.hint == null) {
         this.hint = new ArrayList();
      }

      return this.hint;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getResultClass() {
      return this.resultClass;
   }

   public void setResultClass(String value) {
      this.resultClass = value;
   }

   public String getResultSetMapping() {
      return this.resultSetMapping;
   }

   public void setResultSetMapping(String value) {
      this.resultSetMapping = value;
   }
}
