webpackJsonp([31],{"5Khx":function(a,t){},qAyW:function(a,t,e){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var i=e("//Fk"),n=e.n(i),s=e("T0gc"),o=e("fo4W"),l=e("95hR"),d={name:"namespace",components:{myTable:s.a,myDialog:o.a},mixins:[l.a],data:function(){return{searchData:{keyword:""},searchRules:{},tableData:{rowData:[],colData:[{title:"ID",key:"id"},{title:"代码",key:"code"},{title:"名称",key:"name"}],btns:[{txt:"编辑",method:"on-edit"},{txt:"删除",method:"on-del"}]},multipleSelection:[],addDialog:{visible:!1,title:"新建命名空间",showFooter:!0},addData:{code:"",name:""},editDialog:{visible:!1,title:"编辑命名空间",showFooter:!0},editData:{}}},methods:{openDialog:function(a){this[a].visible=!0,this.addData.code="",this.addData.name=""},beforeEdit:function(){var a=this;return new n.a(function(t,e){t({id:a.editData.id,code:a.editData.code,name:a.editData.name})})}},mounted:function(){this.getList()}},c={render:function(){var a=this,t=a.$createElement,e=a._self._c||t;return e("div",[e("div",{staticClass:"ml20 mt30"},[e("d-input",{staticClass:"left mr10",staticStyle:{width:"20%"},attrs:{placeholder:"请输入代码/值"},model:{value:a.searchData.keyword,callback:function(t){a.$set(a.searchData,"keyword",t)},expression:"searchData.keyword"}},[e("icon",{attrs:{slot:"suffix",name:"search",size:"14",color:"#CACACA"},on:{click:a.getList},slot:"suffix"})],1),a._v(" "),e("d-button",{attrs:{type:"primary"},on:{click:function(t){return a.openDialog("addDialog")}}},[a._v("新建 Namespace"),e("icon",{staticStyle:{"margin-left":"5px"},attrs:{name:"plus-circle"}})],1)],1),a._v(" "),e("my-table",{attrs:{data:a.tableData,showPin:a.showTablePin,page:a.page},on:{"on-size-change":a.handleSizeChange,"on-current-change":a.handleCurrentChange,"on-selection-change":a.handleSelectionChange,"on-edit":a.edit,"on-del":a.del}}),a._v(" "),e("my-dialog",{attrs:{dialog:a.addDialog},on:{"on-dialog-confirm":function(t){return a.addConfirm()},"on-dialog-cancel":function(t){return a.addCancel()}}},[e("grid-row",{staticClass:"mb10"},[e("grid-col",{staticClass:"label",attrs:{span:3}},[a._v("代码:")]),a._v(" "),e("grid-col",{attrs:{span:1}}),a._v(" "),e("grid-col",{staticClass:"val",attrs:{span:14}},[e("d-input",{model:{value:a.addData.code,callback:function(t){a.$set(a.addData,"code",t)},expression:"addData.code"}})],1)],1),a._v(" "),e("grid-row",{staticClass:"mb10"},[e("grid-col",{staticClass:"label",attrs:{span:3}},[a._v("名称:")]),a._v(" "),e("grid-col",{attrs:{span:1}}),a._v(" "),e("grid-col",{staticClass:"val",attrs:{span:14}},[e("d-input",{model:{value:a.addData.name,callback:function(t){a.$set(a.addData,"name",t)},expression:"addData.name"}})],1)],1)],1),a._v(" "),e("my-dialog",{attrs:{dialog:a.editDialog},on:{"on-dialog-confirm":function(t){return a.editConfirm()},"on-dialog-cancel":function(t){return a.editCancel()}}},[e("grid-row",{staticClass:"mb10"},[e("grid-col",{staticClass:"label",attrs:{span:5}},[a._v("代码:")]),a._v(" "),e("grid-col",{attrs:{span:1}}),a._v(" "),e("grid-col",{staticClass:"val",attrs:{span:14}},[e("d-input",{attrs:{disabled:""},model:{value:a.editData.code,callback:function(t){a.$set(a.editData,"code",t)},expression:"editData.code"}})],1)],1),a._v(" "),e("grid-row",{staticClass:"mb10"},[e("grid-col",{staticClass:"label",attrs:{span:5}},[a._v("名称:")]),a._v(" "),e("grid-col",{attrs:{span:1}}),a._v(" "),e("grid-col",{staticClass:"val",attrs:{span:14}},[e("d-input",{model:{value:a.editData.name,callback:function(t){a.$set(a.editData,"name",t)},expression:"editData.name"}})],1)],1)],1)],1)},staticRenderFns:[]};var r=e("VU/8")(d,c,!1,function(a){e("5Khx")},"data-v-3f3566d2",null);t.default=r.exports}});
//# sourceMappingURL=31.9960db73c71eeebdcc85.js.map