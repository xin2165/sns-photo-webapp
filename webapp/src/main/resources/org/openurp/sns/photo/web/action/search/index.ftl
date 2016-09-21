[#ftl]
[#include "/template/head.ftl"]
[@head "照片管理"/]
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
    [@b.form action="!view"  name="photoSearchForm" class="navbar-form navbar-left" role="form"]
      <div class="form-group ">
        <input type="text" id="xh" name="xh" value="${Parameters['xh']!}" placeholder="学号" class="form-control">
      </div>
      <input type="hidden" id="page" name="page" value="">
      <button type="submit" id="isubmit" class="btn btn-primary" onclick="$('#page').val(1)">搜索</button>
    [/@]
    <div class="navbar-form navbar-right">
      [@b.a href="upload" class="btn btn-default"]<span class="glyphicon glyphicon-plus"></span>上传照片[/@]
    </div>
    </div>
    </nav>
    <div class="jumbotron">
      <h1>请输入用户ID前缀查询</h1>
    </div>
  </div>
</body>
</html>