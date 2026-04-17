package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.hibernate.HibernateException;

public abstract class AbstractProxyHandler implements InvocationHandler {
   private boolean valid = true;
   private final int hashCode;

   public AbstractProxyHandler(int hashCode) {
      this.hashCode = hashCode;
   }

   protected abstract Object continueInvocation(Object var1, Method var2, Object[] var3) throws Throwable;

   public String toString() {
      return super.toString() + "[valid=" + this.valid + "]";
   }

   public final int hashCode() {
      return this.hashCode;
   }

   protected final boolean isValid() {
      return this.valid;
   }

   protected final void invalidate() {
      this.valid = false;
   }

   protected final void errorIfInvalid() {
      if (!this.isValid()) {
         throw new HibernateException("proxy handle is no longer valid");
      }
   }

   public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      if ("toString".equals(methodName)) {
         return this.toString();
      } else if ("hashCode".equals(methodName)) {
         return this.hashCode();
      } else {
         return "equals".equals(methodName) ? this.equals(args[0]) : this.continueInvocation(proxy, method, args);
      }
   }
}
