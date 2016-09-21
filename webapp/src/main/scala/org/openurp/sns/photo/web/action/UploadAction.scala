package org.openurp.sns.photo.web.action

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.PreparedStatement
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.IOUtils
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.SystemInfo
import org.beangle.commons.lang.Throwables
import org.beangle.data.jdbc.query.JdbcExecutor
import org.beangle.webmvc.api.action.ActionSupport
import javax.servlet.http.Part
import javax.sql.DataSource
import org.beangle.commons.lang.ClassLoaders

object A {
  def main(args: Array[String]) {
    println(ClassLoaders.getResources("META-INF/beangle/mvc.xml"))
  }
}
class UploadAction extends ActionSupport {

  var datasource: DataSource = _

  var jdbcExecutor: JdbcExecutor = _

  def index(): String = {
    forward()
  }

  def upload(): String = {
    getAll("zipfile", classOf[Part]) foreach { zipFile =>
      val tmpFile = new File(SystemInfo.tmpDir + "/photo" + System.currentTimeMillis())
      IOs.copy(zipFile.getInputStream, new FileOutputStream(tmpFile))
      put("fileNames", unzip(tmpFile, "GBK"))
    }
    forward()
  }

  def unzip(zipfile: File, encoding: String): List[String] = {
    val file: ZipFile = if (null == encoding) new ZipFile(zipfile)
    else new ZipFile(zipfile, encoding)
    val fileNames = new collection.mutable.ListBuffer[String]
    try {

      val en = file.getEntries()
      var i = 0
      import scala.collection.JavaConversions._
      en.foreach { ze =>
        fileNames += ze.getName()
        println(ze.getName())
        i = i + 1
        if (!ze.isDirectory()) {
          println("file:" + ze.getName())

          val photoname = if (ze.getName().contains("/")) Strings.substringAfterLast(ze.getName(), "/") else ze.getName()

          if (photoname.indexOf(".") < 10) {
            logger.warn(photoname + " format is error")
          } else {

            val usercode = Strings.substringBeforeLast(photoname, ".")
            val userIds = jdbcExecutor.query("select id from base.users u where u.code=?", usercode)
            var userId: Long = 0
            if (userIds.isEmpty) {
              println("Cannot find user info of " + usercode);
            } else {
              userId = userIds.head.head.asInstanceOf[Long]
            }
            val year = if (userId > 0) Integer.valueOf(userId.toString.substring(0, 4)).intValue() else 0
            if (userId > 0 && year <= 2016 && year > 2000) {
              print(String.format("%05d", new Integer(i)) + ":" + photoname)
              val bos = new ByteArrayOutputStream()
              IOUtils.copy(file.getInputStream(ze), bos)
              val bytes = bos.toByteArray()

              saveOrUpdate(usercode, userId, bytes)
            }
          }
        } else {
          println("dir:" + ze.getName())
        }
      }
      put("total", i)
      file.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
    fileNames.toList
  }

  def saveOrUpdate(usercode: String, userid: Long, bytes: Array[Byte]) {
    logger.debug("saveOrUpdate => " + userid)
    val conn = datasource.getConnection()
    if (conn.getAutoCommit()) conn.setAutoCommit(false)
    val stmt: PreparedStatement = conn.prepareStatement("select id from photo.photo_infos where user_id = ?")
    stmt.setLong(1, userid)
    val useridrs = stmt.executeQuery()
    var stmt1: PreparedStatement = null
    var stmt2: PreparedStatement = null
    val year = userid.toString.substring(0, 4)
    val photo_id = Digests.md5Hex(usercode + "@sfu.edu.cn")
    if (useridrs.next()) {
      val id = useridrs.getLong("id")
      stmt1 = conn.prepareStatement("update photo.photos" + year + " set id=?, small_image=?, digest=? where id = ?")
      stmt2 = conn.prepareStatement("update photo.photo_infos set user_id = ?, updated_at = ?, photo_id = ? where id = ?")
      stmt1.setString(4, photo_id)
      stmt2.setLong(4, id)
    } else {
      stmt1 = conn.prepareStatement("insert into photo.photos" + year + "(id,small_image,digest) values(?,?,?)")
      stmt2 = conn.prepareStatement("insert into photo.photo_infos(user_id,updated_At,photo_id, id) values(?,?,?,?)")
      stmt2.setLong(4, jdbcExecutor.query("select nextval('photo.seq_photo_info')").head.head.asInstanceOf[Long])
    }

    stmt1.setString(1, photo_id)
    stmt1.setBinaryStream(2, new ByteArrayInputStream(bytes), bytes.length)
    stmt1.setString(3, Digests.md5Hex(bytes))
    stmt1.executeUpdate()

    stmt2.setLong(1, userid)
    stmt2.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()))
    stmt2.setString(3, photo_id)
    stmt2.executeUpdate()
    conn.commit()
    conn.close()
  }
}
