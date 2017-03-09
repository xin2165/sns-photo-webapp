package org.openurp.sns.photo.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.data.jdbc.query.JdbcExecutor
import org.openurp.sns.photo.web.action.SearchAction
import org.beangle.cache.ehcache.EhCacheManager

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[SearchAction])
    bind(classOf[JdbcExecutor])
    
    bind("cache.Ehcache", classOf[EhCacheManager]).constructor("ehcache-photo",false)
  }
}