package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.Label;

public interface ProcessSwitchCallback {
   void processCase(int var1, Label var2) throws Exception;

   void processDefault() throws Exception;
}
