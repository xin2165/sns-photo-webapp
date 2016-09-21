package org.openurp.sns.photo.model


case class Page[T](data: Seq[T], page:Int, pageSize:Int, total:Long) {
	val prev = Option(page - 1).filter(_ > 0)
	val last = Math.ceil(total / pageSize).longValue()
	val next = Option(page + 1).filter(_ * pageSize < total)
}

class PageLimit(_page:Option[Int], _pageSize:Option[Int]){
  val page = _page.getOrElse(1)
  val pageSize = _pageSize.getOrElse(30)
  val offset = (page - 1) * pageSize
}