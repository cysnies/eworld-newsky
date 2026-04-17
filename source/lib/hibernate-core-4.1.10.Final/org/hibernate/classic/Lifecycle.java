package org.hibernate.classic;

import java.io.Serializable;
import org.hibernate.CallbackException;
import org.hibernate.Session;

public interface Lifecycle {
   boolean VETO = true;
   boolean NO_VETO = false;

   boolean onSave(Session var1) throws CallbackException;

   boolean onUpdate(Session var1) throws CallbackException;

   boolean onDelete(Session var1) throws CallbackException;

   void onLoad(Session var1, Serializable var2);
}
