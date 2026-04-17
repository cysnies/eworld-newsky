package org.hibernate.metamodel.source.annotations.xml;

import org.jboss.jandex.DotName;

public interface PseudoJpaDotNames {
   DotName DEFAULT_ACCESS = DotName.createSimple("default.access");
   DotName DEFAULT_DELIMITED_IDENTIFIERS = DotName.createSimple("default.delimited.identifiers");
   DotName DEFAULT_ENTITY_LISTENERS = DotName.createSimple("default.entity.listeners");
   DotName DEFAULT_POST_LOAD = DotName.createSimple("default.entity.listener.post.load");
   DotName DEFAULT_POST_PERSIST = DotName.createSimple("default.entity.listener.post.persist");
   DotName DEFAULT_POST_REMOVE = DotName.createSimple("default.entity.listener.post.remove");
   DotName DEFAULT_POST_UPDATE = DotName.createSimple("default.entity.listener.post.update");
   DotName DEFAULT_PRE_PERSIST = DotName.createSimple("default.entity.listener.pre.persist");
   DotName DEFAULT_PRE_REMOVE = DotName.createSimple("default.entity.listener.pre.remove");
   DotName DEFAULT_PRE_UPDATE = DotName.createSimple("default.entity.listener.pre.update");
}
