package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "entity-listeners",
   propOrder = {"entityListener"}
)
public class JaxbEntityListeners {
   @XmlElement(
      name = "entity-listener"
   )
   protected List entityListener;

   public List getEntityListener() {
      if (this.entityListener == null) {
         this.entityListener = new ArrayList();
      }

      return this.entityListener;
   }
}
