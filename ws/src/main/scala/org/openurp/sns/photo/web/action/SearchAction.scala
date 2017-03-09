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
import org.beangle.cache.ehcache.EhCacheManager
import org.beangle.webmvc.api.view.StreamView

class SearchAction(ehCacheManager: EhCacheManager) extends ActionSupport {

  var jdbcExecutor: JdbcExecutor = _

  val cache = ehCacheManager.getCache("photo", classOf[String], classOf[Object])

  @mapping("{photoId}")
  def index(@param("photoId") photoId: String): View = {
    cache.get(photoId) match {
      case Some(image) => buildStream(image.asInstanceOf[Array[Byte]], photoId)
      case None =>
        loadFromDB(photoId) match {
          case Some(image) =>
            cache.put(photoId, image)
            buildStream(image, photoId)
          case None =>
            val nfile = new File(this.getClass().getClassLoader().getResource("DefaultPhoto.gif").getFile())
            val in = new FileInputStream(nfile)
            buildStream(new Array[Byte](nfile.length.toInt), photoId)
        }
    }
  }

  private def buildStream(bytes: Array[Byte], photoId: String): StreamView = {
    Stream(new ByteArrayInputStream(bytes), "image/jpg", photoId + ".jpg")
  }

  def loadFromDB(photoId: String, size: String = "small"): Option[Array[Byte]] = {
    val rs = jdbcExecutor.query("select user_id, updated_at from photo.photo_infos where photo_id =?", photoId)
    if (!rs.isEmpty) {
      val row = rs.head
      val year = row(0).toString.substring(0, 4)
      val image = if (size == "small") {
        jdbcExecutor.query("select small_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
      } else {
        jdbcExecutor.query("select origin_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
      }
      Some(image)
    } else {
      None
    }
  }
}
