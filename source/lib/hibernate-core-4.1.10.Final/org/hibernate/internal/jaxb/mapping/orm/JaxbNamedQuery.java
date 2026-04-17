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
   name = "named-query",
   propOrder = {"description", "query", "lockMode", "hint"}
)
public class JaxbNamedQuery {
   protected String description;
   @XmlElement(
      required = true
   )
   protected String query;
   @XmlElement(
      name = "lock-mode"
   )
   protected JaxbLockModeType lockMode;
   protected List hint;
   @XmlAttribute(
      required = true
   )
   protected String name;

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

   public JaxbLockModeType getLockMode() {
      return this.lockMode;
   }

   public void setLockMode(JaxbLockModeType value) {
      this.lockMode = value;
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
}
