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
import org.openurp.sns.photo.model.PhotoInfo
import org.openurp.sns.photo.model.Page
import org.openurp.sns.photo.model.PageLimit

class SearchAction extends ActionSupport {

  var jdbcExecutor: JdbcExecutor = _

  def index(): String = {
    forward()
  }

  def view(): String = {
    val code = get("xh").getOrElse("") + "%"
    val pageLimit = new PageLimit(get("page", classOf[Int]), get("pageSize", classOf[Int]))
    val userInfos = jdbcExecutor.query("""
        select p.id, u.code, p.updated_at, p.photo_id, p.remark 
        from photo.photo_infos p,base.users u
        where u.code like ? and u.id=p.user_id
        offset ? limit ?
        """, code, pageLimit.offset, pageLimit.pageSize);

    val total = jdbcExecutor.query("""
           select count(*) from photo.photo_infos p,base.users u  where u.id=p.user_id and u.code like ?
          """, code)

    val photoInfos = userInfos.map { userInfo =>
      new PhotoInfo(userInfo(0).asInstanceOf[Long], userInfo(1).asInstanceOf[String], userInfo(2).asInstanceOf[Date], userInfo(3).asInstanceOf[String], userInfo(4).asInstanceOf[Option[String]])
    }

    put("total", total.head.head)
    put("page", new Page(photoInfos, pageLimit.page, pageLimit.pageSize, total.head.head.asInstanceOf[Long]));
    forward()
  }

  @mapping("{photoId}")
  def info(@param("photoId") photoId: String): View = {
    Stream(new ByteArrayInputStream(loadFromDB(photoId)), "image/jpg", photoId)
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
