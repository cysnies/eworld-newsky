package com.comphenix.net.sf.cglib.proxy;

import java.lang.reflect.Method;

public interface InvocationHandler extends Callback {
   Object invoke(Object var1, Method var2, Object[] var3) throws Throwable;
}
