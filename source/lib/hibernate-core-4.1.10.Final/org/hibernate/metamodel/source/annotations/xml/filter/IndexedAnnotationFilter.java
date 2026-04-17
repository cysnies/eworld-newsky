package org.hibernate.metamodel.source.annotations.xml.filter;

import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.xml.mocker.IndexBuilder;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

public interface IndexedAnnotationFilter extends JPADotNames {
   IndexedAnnotationFilter[] ALL_FILTERS = new IndexedAnnotationFilter[]{ExclusiveAnnotationFilter.INSTANCE, NameAnnotationFilter.INSTANCE, NameTargetAnnotationFilter.INSTANCE};

   void beforePush(IndexBuilder var1, DotName var2, AnnotationInstance var3);
}
