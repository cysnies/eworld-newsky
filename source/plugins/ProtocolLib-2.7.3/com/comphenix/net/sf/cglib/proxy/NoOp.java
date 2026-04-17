package com.comphenix.net.sf.cglib.proxy;

public interface NoOp extends Callback {
   NoOp INSTANCE = new NoOp() {
   };
}
