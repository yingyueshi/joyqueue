<template>
  <div>
    <div class="ml20 mt30">
      <d-select v-model="searchData.status" class="left mr10" style="width:200px" @on-change="change">
        <span slot="prepend">迁移进度</span>
        <d-option :value="0">全部</d-option>
        <d-option :value="1">新建</d-option>
        <d-option :value="2">迁移中</d-option>
        <d-option :value="3">迁移成功</d-option>
        <d-option :value="4">迁移完成，有失败</d-option>
      </d-select>
<!--      <d-input v-model="searchData.keyword" placeholder="broker id/ip" class="left mr10" style="width: 213px">-->
<!--        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>-->
<!--        <span slot="prepend">源broker</span>-->
<!--      </d-input>-->
      <d-button type="primary" icon='plus' color="danger" @click="openAddMigrateTaskDialog('addMigrateTaskDialog')">
        批量迁移
      </d-button>
      <my-table :optional="false" :data="tableData" :showPin="showTablePin" :page="page" @on-detail="openDetailDialog"></my-table>
      <my-dialog class="maxDialogHeight" :dialog="addMigrateTaskDialog" visible="false" @on-dialog-confirm="addConfirm()"
                 @on-dialog-cancel="dialogCancel('addMigrateTaskDialog')">
        <broker-migration-form ref="migrateTaskCreationForm" @on-dialog-cancel="dialogCancel('addMigrateTaskDialog')"/>
      </my-dialog>
      <my-dialog class="maxDialogHeight" :dialog="detailDialog" visible="false" @on-dialog-cancel="dialogCancel('detailDialog')">
        <my-table :optional="false" :data="detailDialog.tableData" :showPin="false" :show-pagination="false" >
        </my-table>
      </my-dialog>
    </div>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import {timeStampToString} from '../../utils/dateTimeUtils'
import brokerMigrationForm from './brokerMigrationForm'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'brokerMigrate',
  components: {
    myTable,
    myDialog,
    brokerMigrationForm
  },
  mixins: [crud],
  data () {
    return {
      searchData: {
        keyword: '',
        status: 0
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id',
            width: '7%'
          },
          {
            title: '来源Broker',
            key: 'srcBrokerId',
            width: '10%'
          },
          {
            title: '目标Broker',
            key: 'targetsStr',
            width: '20%'
          },
          {
            title: '范围类型',
            key: 'scopeType',
            width: '10%',
            formatter (row) {
              if (row.scopeType === 'ALL') {
                return '全部'
              }
              if (row.scopeType === 'TOPICS') {
                return '指定主题'
              }
              if (row.scopeType === 'EXCLUDE_TOPICS') {
                return '排除主题'
              }
            }
          },
          {
            title: '主题',
            key: 'scopes',
            width: '20%'
          },
          {
            title: '迁移状态',
            key: 'status',
            width: '10%',
            formatter (row) {
              if (row.status === 1) {
                return '新建'
              }
              if (row.status === 2) {
                return '迁移中'
              }
              if (row.status === 3) {
                return '迁移成功'
              }
              if (row.status === 4) {
                return '迁移有失败'
              }
            }
          },
          {
            title: '创建人',
            key: 'createBy.id',
            width: '10%'
          },
          {
            title: '创建时间',
            key: 'createTime',
            width: '26%',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '详情',
            method: 'on-detail'
          }
        ]
      },
      addMigrateTaskDialog: {
        visible: false,
        title: '创建迁移任务',
        width: 900,
        showFooter: false
      },
      detailDialog: {
        visible: false,
        title: '详情',
        width: 1200,
        showFooter: false,
        tableData: {
          rowData: [],
          colData: [
            {
              title: 'ID',
              key: 'id',
              width: '9%'
            },
            {
              title: '执行器',
              key: 'executor',
              width: '15%'
            },
            {
              title: '主题',
              key: 'topicCode',
              width: '10%'
            },
            {
              title: '命名空间',
              key: 'namespaceCode',
              width: '10%'
            },
            {
              title: '分区组',
              key: 'pgNo',
              width: '6%'
            },
            {
              title: '状态',
              key: 'status',
              width: '10%',
              formatter (row) {
                if (row.status === 0) {
                  return '新建'
                }
                if (row.status === 1) {
                  return '已派发'
                }
                if (row.status === 2) {
                  return '执行中'
                }
                if (row.status === 2) {
                  return '执行中'
                }
                if (row.status === 3) {
                  return '已成功'
                }
                if (row.status === 4) {
                  return '已失败'
                }
                if (row.status === 5) {
                  return '失败，待重试'
                }
                if (row.status === -1) {
                  return '已删除'
                }
              }
            },
            {
              title: '创建人',
              key: 'createBy.id',
              width: '10%'
            },
            {
              title: '创建时间',
              key: 'createTime',
              width: '20%',
              formatter (item) {
                return timeStampToString(item.createTime)
              }
            }
          ]
        }
      }
    }
  },
  methods: {
    openAddMigrateTaskDialog (dialog) {
      this[dialog].visible = true
    },
    openDetailDialog (item) {
      this.detailDialog.visible = true
      this.getDetails(item)
    },
    change () {
      this.getList()
    },
    getDetails (item) {
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.detail + '/' + item.id, {}, data).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        this.detailDialog.tableData.rowData = data.data
        this.detailDialog.showTablePin = false
      })
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label {
    text-align: right;
    line-height: 32px;
  }
  .maxDialogHeight /deep/ .dui-dialog__body {
    height: 650px;
  }
  .val {
  }
</style>
