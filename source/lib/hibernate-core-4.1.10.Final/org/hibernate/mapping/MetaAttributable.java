package org.hibernate.mapping;

public interface MetaAttributable {
   java.util.Map getMetaAttributes();

   void setMetaAttributes(java.util.Map var1);

   MetaAttribute getMetaAttribute(String var1);
}
