package com.comphenix.net.sf.cglib.transform.impl;

import com.comphenix.net.sf.cglib.asm.Type;

public interface InterceptFieldFilter {
   boolean acceptRead(Type var1, String var2);

   boolean acceptWrite(Type var1, String var2);
}
