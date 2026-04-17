package org.hibernate.metamodel.source.annotations;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import org.hibernate.metamodel.source.BindingContext;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;

public interface AnnotationBindingContext extends BindingContext {
   Index getIndex();

   ClassInfo getClassInfo(String var1);

   void resolveAllTypes(String var1);

   ResolvedType getResolvedType(Class var1);

   ResolvedTypeWithMembers resolveMemberTypes(ResolvedType var1);
}
