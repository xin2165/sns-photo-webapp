package org.openurp.sns.photo.web.action

import java.util.Date
import org.beangle.data.jdbc.query.JdbcExecutor
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.api.view.Stream
import org.beangle.webmvc.api.annotation.param
import java.io.File
import org.beangle.webmvc.api.annotation.mapping
import java.io.FileInputStream
import java.text.SimpleDateFormat
import javax.activation.MimeType
import java.io.ByteArrayInputStream

class SearchAction extends ActionSupport {

  var jdbcExecutor: JdbcExecutor = _

  @mapping("{photoId}")
  def index(@param("photoId") photoId: String): View = {
    Stream(new ByteArrayInputStream(loadFromDB(photoId)), "image/jpg", photoId+".jpg")
  }

  def loadFromDB(photoId: String, size: String = "small"): Array[Byte] = {
    val rs = jdbcExecutor.query("select user_id, updated_at from photo.photo_infos where photo_id =?", photoId)
    if (rs.isEmpty) {
      val nfile = new File(this.getClass().getClassLoader().getResource("DefaultPhoto.gif").getFile())
      val in = new FileInputStream(nfile)
      new Array[Byte](nfile.length.toInt)
    } else {
      val row = rs.head
      val year = row(0).toString.substring(0, 4)
      val image = if (size == "small") {
        jdbcExecutor.query("select small_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
      } else {
        jdbcExecutor.query("select origin_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
      }
      image
    }
  }
}
