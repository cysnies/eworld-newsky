package com.comphenix.protocol.wrappers.collection;

import com.google.common.base.Function;

public abstract class AbstractConverted {
   protected abstract Object toOuter(Object var1);

   protected abstract Object toInner(Object var1);

   protected Function getOuterConverter() {
      // $FF: Couldn't be decompiled
   }
}
