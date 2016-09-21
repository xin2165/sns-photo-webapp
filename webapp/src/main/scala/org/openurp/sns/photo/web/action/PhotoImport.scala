package org.openurp.sns.photo.web.action;
//package org.openurp.photo.web.action
//
//import java.io.ByteArrayInputStream
//import java.sql.PreparedStatement
//import org.apache.commons.codec.digest.DigestUtils
//import play.api.mvc._
//import services.JdbcDao._
//import play.api.data._
//import play.api.data.Forms._
//import java.io.File
//import java.io.FileInputStream
//import org.beangle.commons.lang.Strings
//import services.ImageResizer
//import play.Logger
//import org.beangle.data.jdbc.query.JdbcExecutor
//import scala.collection.mutable.ListBuffer
//
//case class PhotoImportForm(path: String)
//
//object PhotoImport extends Controller {
//
//  val iform = Form(mapping("path" -> text.verifying("目录不存在", path => {
//    new File(path).exists()
//  }))(PhotoImportForm.apply)(PhotoImportForm.unapply))
//
//  def index = Action { implicit request =>
//    Ok(views.html.photoImport.index(iform))
//  }
//
//  def importFile = Action { implicit request =>
//    iform.bindFromRequest.fold(
//      hasErrors => {
//        Ok(views.html.photoImport.index(hasErrors))
//      },
//      success => {
//        val photoNum = importPhoto(success.path)
//        val dir = new File(success.path)
//        Ok(views.html.photoImport.imported(photoNum))
//      })
//  }
//
//  def importPhoto(path: String): Int = {
//    val dir = new File(path)
//    var i = 0
//    Logger.info("importFile files " + dir.list().length)
//    dir.listFiles() foreach { file =>
//      if (file.isDirectory()) {
//        i = i + importPhoto(file.getAbsolutePath())
//      } else {
//        val ophotoname = file.getName()
//        try {
//          val photoname = {
//            if (ophotoname.indexOf("_") > 0) {
//              ophotoname.replaceAll("_", "")
//            } else ophotoname
//          }
//          val executor = new JdbcExecutor(datasource)
//          //sfu userid policy
//          val users = executor.query("select id from base.users u where u.code=?", Strings.substringBeforeLast(photoname, "."))
//          val userid: Long =
//            if (users.isEmpty && photoname.length() == 8) {
//              photoname match {
//                case "3498.jpg" => 2008098
//                case _ => {
//                  if (photoname.startsWith("0") || photoname.startsWith("1")) java.lang.Long.valueOf("20" + Strings.substringBeforeLast(photoname, "."))
//                  else java.lang.Long.valueOf("19" + Strings.substringBeforeLast(photoname, "."))
//                }
//              }
//            } else users.head.head.asInstanceOf[Number].longValue()
//          val in = new FileInputStream(file)
//          val bytes = new Array[Byte](file.length.toInt)
//          in.read(bytes)
//          in.close()
//
//          saveOrUpdate(userid, bytes)
//          i = i + 1
//          println(String.format("%05d", new Integer(i)) + ":" + photoname)
//
//        } catch {
//          case t: Throwable => Logger.error("file import error for:" + ophotoname, t) // todo: handle error
//        }
//      }
//    }
//    i
//  }
//
//  def saveOrUpdate(userid: Long, bytes: Array[Byte]) {
//    val year = userid.toString.substring(0, 4)
//    val photosTableName = "photo.photos" + year
//    val digest = DigestUtils.md5Hex(bytes)
//    val executor = new JdbcExecutor(datasource)
//    val digestrs = executor.query("select digest from " + photosTableName + " where digest = ?", digest)
//    if (digestrs.isEmpty) {
//      val conn = datasource.getConnection()
//      val sbytes = ImageResizer.resize(bytes, 144)
//      val bbytes = ImageResizer.resize(bytes, 1024)
//      val photoinfos = executor.query("select id from photo.photo_infos where user_id = ?", userid)
//      val stmt: PreparedStatement = conn.prepareStatement("select id from photo.photo_infos where user_id = ?")
//      if (conn.getAutoCommit()) conn.setAutoCommit(false)
//      val photo_id = DigestUtils.md5Hex(userid + "@sfu.edu.cn")
//      val stmts = new collection.mutable.ListBuffer[PreparedStatement]
//      if (!photoinfos.isEmpty) {
//        val id = photoinfos.head.head.asInstanceOf[Number].longValue()
//        val stmt1 = conn.prepareStatement("update " + photosTableName + " set origin_image=?, small_image=?, digest=? where id = ?")
//        stmt1.setBinaryStream(1, new ByteArrayInputStream(bbytes), bbytes.length)
//        stmt1.setBinaryStream(2, new ByteArrayInputStream(sbytes), sbytes.length)
//        stmt1.setString(3, digest)
//        stmt1.setString(4, photo_id)
//        stmts += stmt1
//        val stmt2 = conn.prepareStatement("update photo.photo_infos set user_id = ?, updated_at = ?, photo_id = ? where id = ?")
//        stmt2.setLong(1, userid)
//        stmt2.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()))
//        stmt2.setString(3, photo_id)
//        stmt2.setLong(4, id)
//        stmts += stmt2
//      } else {
//        val stmt1 = conn.prepareStatement("insert into " + photosTableName + "(id,origin_image,small_image,digest) values(?,?,?,?)")
//        stmt1.setString(1, photo_id)
//        stmt1.setBinaryStream(2, new ByteArrayInputStream(bbytes), bbytes.length)
//        stmt1.setBinaryStream(3, new ByteArrayInputStream(sbytes), sbytes.length)
//        stmt1.setString(4, digest)
//        stmts += stmt1
//        val stmt2 = conn.prepareStatement("insert into photo.photo_infos(user_id,updated_At,photo_id,id) values(?,?,?,?)")
//        stmt2.setLong(1, userid)
//        stmt2.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()))
//        stmt2.setString(3, photo_id)
//        stmt2.setLong(4, executor.query("select nextval('photo.seq_photo_info')").head.head.asInstanceOf[Long])
//        stmts += stmt2
//      }
//      stmts foreach { stmt =>
//        stmt.executeUpdate()
//        stmt.close();
//      }
//      conn.commit()
//      conn.close()
//    } else {
//      println("Ignore exists." + userid)
//    }
//  }
//}
//
//
