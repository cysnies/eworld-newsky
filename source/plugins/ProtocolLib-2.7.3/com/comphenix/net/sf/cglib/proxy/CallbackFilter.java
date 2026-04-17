package com.comphenix.net.sf.cglib.proxy;

import java.lang.reflect.Method;

public interface CallbackFilter {
   int accept(Method var1);

   boolean equals(Object var1);
}
