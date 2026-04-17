package org.hibernate.internal.jaxb.cfg;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "type-attribute"
)
@XmlEnum
public enum JaxbTypeAttribute {
   @XmlEnumValue("auto-flush")
   AUTO_FLUSH("auto-flush"),
   @XmlEnumValue("create")
   CREATE("create"),
   @XmlEnumValue("create-onflush")
   CREATE_ONFLUSH("create-onflush"),
   @XmlEnumValue("delete")
   DELETE("delete"),
   @XmlEnumValue("dirty-check")
   DIRTY_CHECK("dirty-check"),
   @XmlEnumValue("evict")
   EVICT("evict"),
   @XmlEnumValue("flush")
   FLUSH("flush"),
   @XmlEnumValue("flush-entity")
   FLUSH_ENTITY("flush-entity"),
   @XmlEnumValue("load")
   LOAD("load"),
   @XmlEnumValue("load-collection")
   LOAD_COLLECTION("load-collection"),
   @XmlEnumValue("lock")
   LOCK("lock"),
   @XmlEnumValue("merge")
   MERGE("merge"),
   @XmlEnumValue("post-collection-recreate")
   POST_COLLECTION_RECREATE("post-collection-recreate"),
   @XmlEnumValue("post-collection-remove")
   POST_COLLECTION_REMOVE("post-collection-remove"),
   @XmlEnumValue("post-collection-update")
   POST_COLLECTION_UPDATE("post-collection-update"),
   @XmlEnumValue("post-commit-delete")
   POST_COMMIT_DELETE("post-commit-delete"),
   @XmlEnumValue("post-commit-insert")
   POST_COMMIT_INSERT("post-commit-insert"),
   @XmlEnumValue("post-commit-update")
   POST_COMMIT_UPDATE("post-commit-update"),
   @XmlEnumValue("post-delete")
   POST_DELETE("post-delete"),
   @XmlEnumValue("post-insert")
   POST_INSERT("post-insert"),
   @XmlEnumValue("post-load")
   POST_LOAD("post-load"),
   @XmlEnumValue("post-update")
   POST_UPDATE("post-update"),
   @XmlEnumValue("pre-collection-recreate")
   PRE_COLLECTION_RECREATE("pre-collection-recreate"),
   @XmlEnumValue("pre-collection-remove")
   PRE_COLLECTION_REMOVE("pre-collection-remove"),
   @XmlEnumValue("pre-collection-update")
   PRE_COLLECTION_UPDATE("pre-collection-update"),
   @XmlEnumValue("pre-delete")
   PRE_DELETE("pre-delete"),
   @XmlEnumValue("pre-insert")
   PRE_INSERT("pre-insert"),
   @XmlEnumValue("pre-load")
   PRE_LOAD("pre-load"),
   @XmlEnumValue("pre-update")
   PRE_UPDATE("pre-update"),
   @XmlEnumValue("refresh")
   REFRESH("refresh"),
   @XmlEnumValue("replicate")
   REPLICATE("replicate"),
   @XmlEnumValue("save")
   SAVE("save"),
   @XmlEnumValue("save-update")
   SAVE_UPDATE("save-update"),
   @XmlEnumValue("update")
   UPDATE("update");

   private final String value;

   private JaxbTypeAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbTypeAttribute fromValue(String v) {
      for(JaxbTypeAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
