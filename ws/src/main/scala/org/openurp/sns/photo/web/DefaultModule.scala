package org.openurp.sns.photo.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.data.jdbc.query.JdbcExecutor
import org.openurp.sns.photo.web.action.SearchAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[SearchAction])
    bind(classOf[JdbcExecutor])
  }
}