package org.openurp.sns.photo.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.data.jdbc.query.JdbcExecutor
import org.openurp.sns.photo.web.action.SearchAction
import org.openurp.sns.photo.web.action.UploadAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[UploadAction],classOf[SearchAction])
    bind(classOf[JdbcExecutor])
  }
}