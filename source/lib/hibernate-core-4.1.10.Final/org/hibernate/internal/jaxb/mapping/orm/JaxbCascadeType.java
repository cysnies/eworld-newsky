package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "cascade-type",
   propOrder = {"cascadeAll", "cascadePersist", "cascadeMerge", "cascadeRemove", "cascadeRefresh", "cascadeDetach"}
)
public class JaxbCascadeType {
   @XmlElement(
      name = "cascade-all"
   )
   protected JaxbEmptyType cascadeAll;
   @XmlElement(
      name = "cascade-persist"
   )
   protected JaxbEmptyType cascadePersist;
   @XmlElement(
      name = "cascade-merge"
   )
   protected JaxbEmptyType cascadeMerge;
   @XmlElement(
      name = "cascade-remove"
   )
   protected JaxbEmptyType cascadeRemove;
   @XmlElement(
      name = "cascade-refresh"
   )
   protected JaxbEmptyType cascadeRefresh;
   @XmlElement(
      name = "cascade-detach"
   )
   protected JaxbEmptyType cascadeDetach;

   public JaxbEmptyType getCascadeAll() {
      return this.cascadeAll;
   }

   public void setCascadeAll(JaxbEmptyType value) {
      this.cascadeAll = value;
   }

   public JaxbEmptyType getCascadePersist() {
      return this.cascadePersist;
   }

   public void setCascadePersist(JaxbEmptyType value) {
      this.cascadePersist = value;
   }

   public JaxbEmptyType getCascadeMerge() {
      return this.cascadeMerge;
   }

   public void setCascadeMerge(JaxbEmptyType value) {
      this.cascadeMerge = value;
   }

   public JaxbEmptyType getCascadeRemove() {
      return this.cascadeRemove;
   }

   public void setCascadeRemove(JaxbEmptyType value) {
      this.cascadeRemove = value;
   }

   public JaxbEmptyType getCascadeRefresh() {
      return this.cascadeRefresh;
   }

   public void setCascadeRefresh(JaxbEmptyType value) {
      this.cascadeRefresh = value;
   }

   public JaxbEmptyType getCascadeDetach() {
      return this.cascadeDetach;
   }

   public void setCascadeDetach(JaxbEmptyType value) {
      this.cascadeDetach = value;
   }
}
