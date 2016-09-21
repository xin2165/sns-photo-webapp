package org.openurp.sns.photo.model

import java.util.Date

case class PhotoInfo(id: Long, userid: String, updatedAt: Date, photoid: String, remark: Option[String])

//object PhotoInfo {
//
//  private val simple = {
//    get[Long]("id") ~ get[String]("code") ~ get[Date]("updated_at") ~
//      get[String]("photo_id") ~ get[Option[String]]("remark") map {
//        case id ~  code ~ updatedAt ~ photoid ~ remark =>
//          PhotoInfo(id, code, updatedAt, photoid, remark)
//      }
//  }

//  def list(iform: PhotoSearchForm) = {
//    DB.withConnection { implicit conn =>
//      val page = new PageLimit(iform.page, iform.pageSize)
//      val list = SQL("""
//        select p.id, u.code, p.updated_at, p.photo_id, p.remark 
//        from photo.photo_infos p,base.users u
//        where u.code like {xh} and u.id=user_id
//        offset {offset} limit {pageSize}
//        """).on('xh -> (iform.xh + "%"), 'offset -> page.offset,
//        'pageSize -> page.pageSize).as(simple *)
//      val total = SQL("""
//           select count(*)
//        from photo.photo_infos p,base.users u
//        where u.id=p.user_id and u.code like {xh}
//      """).on('xh -> (iform.xh + "%")).as(scalar[Long].single)
//      new Page(list, page.page, page.pageSize, total)
//    }
//  }

//}