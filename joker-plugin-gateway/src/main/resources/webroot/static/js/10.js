webpackJsonp([10],{"+tnp":function(e,r,t){"use strict";Object.defineProperty(r,"__esModule",{value:!0});var o=t("QyUb"),a=t("/dzA"),n={name:"appCreate",data:function(){return{form:{name:"",port:8888,host:"localhost",remark:""},rules:{name:[{required:!0,message:"请输入应用名字",trigger:"blur"},{min:2,max:10,message:"长度在 2 到 10 个字符",trigger:"blur"}],port:[{required:!0,message:"请输入端口号",trigger:"blur"}],host:[{required:!0,message:"请输入主机域名",trigger:"blur"}],remark:[{required:!0,message:"请输入应用注释",trigger:"blur"}]}}},methods:{onSubmit:function(){var e=this;this.$refs.appForm.validate(function(r){if(!r)return!1;e.form.port=parseInt(e.form.port),Object(a.c)(o.a.app.create,"",e.form).then(function(r){0===r.code?e.$router.back(1):e.$message({message:r.msg,type:"warning"})})})}}},s={render:function(){var e=this,r=e.$createElement,t=e._self._c||r;return t("div",{staticClass:"dashboard"},[t("el-form",{ref:"appForm",attrs:{model:e.form,rules:e.rules,"label-width":"80px"}},[t("el-form-item",{attrs:{label:"应用名字",prop:"name"}},[t("el-input",{model:{value:e.form.name,callback:function(r){e.$set(e.form,"name",r)},expression:"form.name"}})],1),e._v(" "),t("el-row",[t("el-col",{attrs:{span:12}},[t("el-form-item",{attrs:{label:"服务域名",prop:"host"}},[t("el-input",{model:{value:e.form.host,callback:function(r){e.$set(e.form,"host",r)},expression:"form.host"}})],1)],1),e._v(" "),t("el-col",{attrs:{span:12}},[t("el-form-item",{attrs:{label:"服务端口",prop:"port"}},[t("el-input",{attrs:{type:"number"},model:{value:e.form.port,callback:function(r){e.$set(e.form,"port",r)},expression:"form.port"}})],1)],1)],1),e._v(" "),t("el-form-item",{attrs:{label:"服务标注",prop:"remark"}},[t("el-input",{model:{value:e.form.remark,callback:function(r){e.$set(e.form,"remark",r)},expression:"form.remark"}})],1),e._v(" "),t("el-form-item",[t("el-button",{attrs:{type:"primary"},on:{click:e.onSubmit}},[e._v("立即创建")]),e._v(" "),t("el-button",{on:{click:function(r){e.$router.back(1)}}},[e._v("取消")])],1)],1)],1)},staticRenderFns:[]};var l=t("VU/8")(n,s,!1,function(e){t("g40r")},"data-v-19e6bb36",null);r.default=l.exports},g40r:function(e,r){}});