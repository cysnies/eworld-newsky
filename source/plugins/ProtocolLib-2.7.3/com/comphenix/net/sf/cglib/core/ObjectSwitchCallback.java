package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.Label;

public interface ObjectSwitchCallback {
   void processCase(Object var1, Label var2) throws Exception;

   void processDefault() throws Exception;
}
