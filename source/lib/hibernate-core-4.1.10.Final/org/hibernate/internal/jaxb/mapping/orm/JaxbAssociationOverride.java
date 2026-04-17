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
   name = "association-override",
   propOrder = {"description", "joinColumn", "joinTable"}
)
public class JaxbAssociationOverride {
   protected String description;
   @XmlElement(
      name = "join-column"
   )
   protected List joinColumn;
   @XmlElement(
      name = "join-table"
   )
   protected JaxbJoinTable joinTable;
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

   public List getJoinColumn() {
      if (this.joinColumn == null) {
         this.joinColumn = new ArrayList();
      }

      return this.joinColumn;
   }

   public JaxbJoinTable getJoinTable() {
      return this.joinTable;
   }

   public void setJoinTable(JaxbJoinTable value) {
      this.joinTable = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
