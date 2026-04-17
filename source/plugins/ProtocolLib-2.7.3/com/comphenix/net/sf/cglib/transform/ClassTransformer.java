package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;

public interface ClassTransformer extends ClassVisitor {
   void setTarget(ClassVisitor var1);
}
