[#ftl]
[#include "/template/head.ftl"]
[@head "照片浏览"/]
<body>
  <div class="container" style="width:1000px">
    <div class="page-header">
      <h1>照片管理 <small>照片浏览</small></h1>
    </div>
    <style>
      .imgdiv{width:88px; float:left; height:145px; overflow: hidden; margin:5px;}
      .imgdiv img{width:100%;}
      .imgdiv .text{text-align: center;}
    </style>
    <nav class="navbar navbar-default" role="navigation">
      <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        [@b.form action="!view" name="photoSearchForm" id = "photoSearchForm" class="navbar-form navbar-left" role="form"]
        <div class="form-group ">
            <input type="text" id="xh" name="xh" value="${Parameters['xh']!}" placeholder="学号" class="form-control">
        </div>
          <input type="hidden" id="page" name="page" value="1">
          <button type="submit" id="isubmit" class="btn btn-primary" onclick="$('#page').val(1)">搜索</button>
        [/@]
        <div class="navbar-form navbar-right">
          [@b.a href="upload" class="btn btn-default"]<span class="glyphicon glyphicon-plus"></span>上传照片[/@]
        </div>
      </div>
    </nav>
  
    [#if total > 0]
      <div style="margin:-10px -5px 0 -5px;">
         [#list page.data as pi]
            <div class="imgdiv">
              <img class="img-thumbnail" src="${b.url('!info?photoId='+pi.photoid)}"/>
              <div class="text">${pi.userid}</div>
            </div>
         [/#list]
        <div style="clear:both"></div>
      </div>
       <ul class="pagination pull-right">
         [#if page.prev ??]
             <li><a href="#" onclick="gotoPage(${page.prev})">&laquo;</a></li>
             [#if page.page > 1]
               <li><a onclick="gotoPage(1)">1</a></li>
             [/#if]
             [#if page.page > 3]
               <li><a>...</a></li>
             [/#if]
         [#else] <li class="disabled"><a href="#">&laquo;</a></li>
         [/#if]
         [#list (page.page - 3)..(page.page + 3) as p]
           [#if p > 0 && (p > 1 || page.page == 1) && p <= page.last && (p < page.last || page.page == page.last)]
             <li class="[#if p == page.page]active[/#if]"><a onclick="gotoPage(${p})">${p}</a></li>
           [/#if]
         [/#list]
         [#if page.page != page.last && page.last > 0]
           [#if page.last - page.page > 3]
             <li><a>...</a></li>
           [/#if]
           [#if page.page < page.last]
             <li><a onclick="gotoPage(${page.last})">${page.last}</a></li>
           [/#if]
           <li><a onclick="gotoPage(${page.next}">&raquo;</a></li>
         [#else]
           <li class="disabled"><a href="#">&raquo;</a></li>
         [/#if]
       </ul>
       <script>
         function gotoPage(page){
           $("#page").val(page);
           $("#photoSearchForm").submit();
         }
       </script>
    [#else]
      [#if page ??]
        <div class="jumbotron">
          <h1>没有找到图片</h1>
        </div>
      [#else]
        <div class="jumbotron">
          <h1>请输入用户ID前缀查询</h1>
        </div>
      [/#if]
    [/#if]
  </div>
</body>
</html>