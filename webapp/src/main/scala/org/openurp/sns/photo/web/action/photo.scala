package org.openurp.sns.photo.web.action;
//package org.openurp.photo.web.action
//
//import java.io.ByteArrayInputStream
//import java.io.File
//import java.io.FileInputStream
//import java.sql.PreparedStatement
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//
//import scala.collection.JavaConversions.asScalaBuffer
//
//import org.apache.commons.codec.digest.DigestUtils
//import org.beangle.commons.cache.Cache
//import org.beangle.commons.cache.CacheManager
//import org.beangle.commons.lang.ClassLoaders
//import org.beangle.data.jdbc.query.JdbcExecutor
//import org.beangle.data.jdbc.util.PoolingDataSourceFactory
//import org.joda.time.DateTimeZone
//import org.joda.time.format.DateTimeFormat
//import org.joda.time.format.DateTimeFormatter
//
//import net.sf.ehcache.{Cache => EHCache}
//import net.sf.ehcache.{CacheManager => EHCacheManager}
//import net.sf.ehcache.Element
//import play.api.Logger
//import play.api.Play
//import play.api.mvc.Action
//import play.api.mvc.Controller
//import play.api.mvc.SimpleResult
//
//object photo extends Controller {
//
//  val ImageResolution = 144
//  var cacheManager = new EhcacheManager()
//  private val dfp: DateTimeFormatter =
//    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").withLocale(java.util.Locale.ENGLISH).withZone(DateTimeZone.forID("GMT"))
//  val etagformat = new SimpleDateFormat("yyyyMMddHHmmss")
//  val MimeType = "image/jpg"
//  def photoCache: Cache[String, (String, Array[Byte])] = cacheManager.getCache("photo")
//  val factory = new PoolingDataSourceFactory(Play.current.configuration.getString("db.default.driver").get,
//    Play.current.configuration.getString("db.default.url").get,
//    Play.current.configuration.getString("db.default.user").get,
//    Play.current.configuration.getString("db.default.password").get, new java.util.Properties())
//  val datasource = factory.getObject
//
//  val executor = new JdbcExecutor(datasource)
//
//  def index(xh: String) = Action { request =>
//    val size = request.getQueryString("size").getOrElse("small")
//    println(size)
//    if (xh != null) {
//      val photoCachekey = xh +"@"+size
//      photoCache.get(photoCachekey) match {
//        case Some((updatedAt, image)) => {
//          request.headers.get(IF_NONE_MATCH) match {
//            case Some(etag) => {
//              if (etag == updatedAt) NotModified.withHeaders(DATE -> dfp.print({ new java.util.Date }.getTime))
//              else loadFromCache(updatedAt, image)
//            }
//            case None => loadFromCache(updatedAt, image)
//          }
//        }
//        case None => {
//          loadFromDB(xh,size)
//        }
//      }
//    } else {
//      BadRequest("Couldn't find xh")
//    }
//  }
//
// 
//  def loadFromDB(photoId: String,size:String): SimpleResult = {
//    val rs = executor.query("select user_id, updated_at from photo.photo_infos where photo_id =?", photoId)
//    val sdf = new SimpleDateFormat("yyyyMMddHHmmss")
//    val image = if (rs.isEmpty) {
//      val nfile = new File(this.getClass().getClassLoader().getResource("DefaultPhoto.gif").getFile())
//      val in = new FileInputStream(nfile)
//      val bytes = new Array[Byte](nfile.length.toInt)
//      in.read(bytes)
//      in.close()
//      (bytes, sdf.format(new Date()))
//    } else {
//      val row = rs.head
//      val year = row(0).toString.substring(0, 4)
//      val updatedAt = sdf.format(row(1).asInstanceOf[Date])
//      val image = if (size == "small"){
//         executor.query("select small_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
//      }else{
//         executor.query("select origin_image from photo.photos" + year + " where id =?", photoId).head.head.asInstanceOf[Array[Byte]]
//      }
//      photoCache.put(photoId, (updatedAt, image))
//      (image, updatedAt)
//    }
//    Ok(image._1).as(MimeType)
//      .withHeaders(
//        "Date" -> dfp.print({ new java.util.Date }.getTime),
//        "Expires" -> dfp.print(expires(etagformat.parse(image._2), 10).getTime),
//        "Cache-Control" -> "public",
//        "ETag" -> image._2)
//  }
//  def expires(date: Date, days: Int): Date = {
//    val expireAt = Calendar.getInstance
//    expireAt.setTime(date)
//    expireAt.add(Calendar.DAY_OF_YEAR, days)
//    expireAt.getTime
//  }
//
//  def loadFromCache(updated: String, image: Array[Byte]): SimpleResult = {
//    val updatedAt = etagformat.parse(updated)
//    Ok(image).as(MimeType)
//      .withHeaders(
//        "Date" -> dfp.print({ new java.util.Date }.getTime),
//        "Expires" -> dfp.print(expires(updatedAt, 10).getTime),
//        "Cache-Control" -> "public",
//        "ETag" -> etagformat.format(updatedAt))
//
//  }
//}
//
///**
// * EHCache 管理器
// *
// * @author chaostone
// * @version 1.0, 2014/03/22
// * @since 0.0.1
// */
//class EhcacheManager extends CacheManager {
//
//  val manager = EHCacheManager.newInstance(ClassLoaders.getResource("ehcache.xml", getClass))
//
//  /**
//   * Return the cache associated with the given name.
//   */
//  def getCache[K, V](name: String): Cache[K, V] = {
//    new EhCache(manager.getCache(name))
//  }
//
//  /**
//   * Return a collection of the caches known by this cache manager.
//   */
//  def cacheNames: Set[String] = {
//    manager.getCacheNames().toSet
//  }
//}
//
//class EhCache[K, V](val inner: EHCache) extends Cache[K, V] {
//  /**
//   * Return the cache name.
//   */
//  def name(): String = inner.getName()
//
//  /**
//   * Get Some(T) or None
//   */
//  def get(key: K): Option[V] = {
//    val ele = inner.get(key)
//    if (null == ele) None
//    else Some(ele.getObjectValue.asInstanceOf[V])
//  }
//
//  /**
//   * Put a new Value
//   */
//  def put(key: K, value: V) {
//    inner.put(new Element(key, value, 1))
//  }
//
//  /**
//   * Evict specified key
//   */
//  def evict(key: K) {
//    inner.remove(key)
//  }
//
//  /**
//   * Return cached keys
//   */
//  def keys(): Set[K] = {
//    import scala.collection.JavaConversions._
//    inner.getKeys().toSet.asInstanceOf[Set[K]]
//  }
//
//  /**
//   * Remove all mappings from the cache.
//   */
//  def clear() {
//    keys foreach { key =>
//      evict(key)
//    }
//  }
//}
