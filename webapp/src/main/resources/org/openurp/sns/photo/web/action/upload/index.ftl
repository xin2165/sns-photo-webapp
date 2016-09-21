[#ftl]
[#include "/template/head.ftl"]
[@head "照片管理"/]
<body>
  <div class="container" style="width:600px">
    <div class="page-header">
      <h1>照片管理 <small>照片上传</small></h1>
    </div>
[@b.form action="!upload"   enctype="multipart/form-data" class="form-inline" role="form"]
    <label for="exampleInputPassword1" class="ontrol-label">选择zip文件：</label>
    <div class="form-group">
      <input type="file" name="zipfile" class="form-control">
    </div>
    <div class="form-group">
        <input type="submit" class="btn btn-primary" value="上传">
    </div>
[/@]
  </div>

</body>
</html>