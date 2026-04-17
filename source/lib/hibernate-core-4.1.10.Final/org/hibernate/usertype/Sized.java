package org.hibernate.usertype;

import org.hibernate.metamodel.relational.Size;

public interface Sized {
   Size[] dictatedSizes();

   Size[] defaultSizes();
}
