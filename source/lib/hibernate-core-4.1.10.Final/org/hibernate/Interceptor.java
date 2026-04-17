package org.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import org.hibernate.type.Type;

public interface Interceptor {
   boolean onLoad(Object var1, Serializable var2, Object[] var3, String[] var4, Type[] var5) throws CallbackException;

   boolean onFlushDirty(Object var1, Serializable var2, Object[] var3, Object[] var4, String[] var5, Type[] var6) throws CallbackException;

   boolean onSave(Object var1, Serializable var2, Object[] var3, String[] var4, Type[] var5) throws CallbackException;

   void onDelete(Object var1, Serializable var2, Object[] var3, String[] var4, Type[] var5) throws CallbackException;

   void onCollectionRecreate(Object var1, Serializable var2) throws CallbackException;

   void onCollectionRemove(Object var1, Serializable var2) throws CallbackException;

   void onCollectionUpdate(Object var1, Serializable var2) throws CallbackException;

   void preFlush(Iterator var1) throws CallbackException;

   void postFlush(Iterator var1) throws CallbackException;

   Boolean isTransient(Object var1);

   int[] findDirty(Object var1, Serializable var2, Object[] var3, Object[] var4, String[] var5, Type[] var6);

   Object instantiate(String var1, EntityMode var2, Serializable var3) throws CallbackException;

   String getEntityName(Object var1) throws CallbackException;

   Object getEntity(String var1, Serializable var2) throws CallbackException;

   void afterTransactionBegin(Transaction var1);

   void beforeTransactionCompletion(Transaction var1);

   void afterTransactionCompletion(Transaction var1);

   String onPrepareStatement(String var1);
}
