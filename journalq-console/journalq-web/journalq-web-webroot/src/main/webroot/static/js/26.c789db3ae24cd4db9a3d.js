webpackJsonp([26],{IA1d:function(e,t){},rRBE:function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=r("lcoF"),o=r("X2Oc"),d={name:"add-app-form",mixins:[a.a],props:{type:0,data:{type:Object,default:function(){return{code:"",owner:{code:""}}}}},methods:{getFormData:function(){return this.formData.name=this.formData.code,this.formData.system=0,this.formData}},data:function(){return{formData:this.data,rules:{code:Object(o.f)(),"owner.code":[{required:!0,message:"请选择应用负责人",trigger:"change"}]},error:{code:""}}}},c={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("d-form",{ref:"form",attrs:{model:e.formData,rules:e.rules,"label-width":"100px"}},[r("d-form-item",{attrs:{label:"英文名称：",prop:"code",error:e.error.code}},[r("d-input",{staticStyle:{width:"80%"},attrs:{placeholder:"仅支持英文字母大小写和数字，首字母"},model:{value:e.formData.code,callback:function(t){e.$set(e.formData,"code",t)},expression:"formData.code"}})],1),e._v(" "),r("d-form-item",{attrs:{label:"应用负责人：",prop:"owner.code"}},[r("d-input",{staticStyle:{width:"80%"},attrs:{placeholder:"user code"},model:{value:e.formData.owner.code,callback:function(t){e.$set(e.formData.owner,"code",t)},expression:"formData.owner.code"}})],1)],1)},staticRenderFns:[]};var n=r("VU/8")(d,c,!1,function(e){r("IA1d")},"data-v-6cd96aad",null);t.default=n.exports}});
//# sourceMappingURL=26.c789db3ae24cd4db9a3d.js.map