<template>
  <div style="height: 600px;">
    <d-steps :current="current">
      <d-step title="步骤1" description="批量创建任务"></d-step>
      <d-step title="步骤2" description="调整及分析"></d-step>
    </d-steps>
    <hr style="border: 1px solid #e9e9e9;"/>
    <div class='step1' v-show="current===0">
      <d-form ref="form1" :model="formData" label-width="120px">
        <grid-row>
          <grid-col span="14">
            <d-form-item label="选择源Broker" prop="srcBroker.ip">
              <d-autocomplete
                v-model="formData.srcBroker.ip"
                :fetch-suggestions="queryBroker"
                placeholder="请输入broker ip"
                @select="handleBrokerInput"
                oninput="value = value.trim()"
                style="width: 90%"
              >
                <icon name="chevron-down" slot="suffix"></icon>
                <template slot-scope="{ item }">
                  <div
                    class="code"
                    style="text-overflow: ellipsis;overflow: hidden;"
                  >{{ item.ip }}</div>
                  <span
                    class="id"
                    style="font-size: 12px;color: #b4b4b4;"
                  >{{ 'id:   '+item.id}}</span>
                </template>
              </d-autocomplete>
            </d-form-item>
          </grid-col>
          <grid-col span="8">
            <d-button type="primary" icon='plus' color="warning" @click="openTargetDialog">
              添加目标broker
            </d-button>
          </grid-col>
  <!--        <grid-col span="8">-->
  <!--          <d-checkbox v-model="formData.removeFirst" size="large" name="Checkbox" label="option1" style="">先全部摘除，后补齐-->
  <!--          </d-checkbox>-->
  <!--        </grid-col>-->
        </grid-row>
        <grid-row>
          <d-form-item label="源Broker" prop="srcBroker.ip1" >
            <span class="demonstration">{{formData.srcBroker.ip}}</span>
          </d-form-item>
        </grid-row>
        <grid-row>
          <d-form-item label="目标Broker" prop="target" style="width: 90%">
            <my-table :data="targetTableData" :showPin="false" :showPagination=false />
          </d-form-item>
        </grid-row>
        <grid-row>
          <grid-col span="24">
            <d-button type="primary" @click="next" class="right" style="margin-right: 100px;margin-top: 50px">下一步
            </d-button>
          </grid-col>
        </grid-row>
      </d-form>
    </div>
    <div class='step2' v-show="current===1">
      <d-form ref="form1" :model="formData" label-width="120px">
        <grid-row>
          <grid-col span="12">
            <d-form-item label="源Broker" prop="srcBrokerIp2">
              <d-input rows="1" readonly=true type="textarea" disabled v-model="formData.srcBroker.ip"></d-input>
            </d-form-item>
          </grid-col>
          <grid-col span="12">
            <d-form-item label="目标Broker" prop="desBrokerIp">
              <d-input rows="2" readonly=true type="textarea" disabled v-model="formData.desBrokerIps"></d-input>
            </d-form-item>
          </grid-col>
  <!--        <grid-col span="6">-->
  <!--          <d-button type="primary" color="#87CEFA" style="margin-left: 30px">生成迁移策略</d-button>-->
  <!--        </grid-col>-->
        </grid-row>
        <grid-row>
          <d-form-item label="迁移范围" prop="scope">
            <d-radio-group v-model="formData.scopeType">
              <d-radio :label="0">全部主题</d-radio>
              <d-radio :label="1">部分主题</d-radio>
            </d-radio-group>
            <d-button type="primary" v-if="formData.scopeType" color="#87CEFA" @click="openTopicDialog">
              选择topic
            </d-button>
          </d-form-item>
        </grid-row>
        <grid-row v-if="formData.scopeType">
          <d-form-item label="迁移主题" prop="scope">
            <d-input v-model="formData.scopes" readonly=true></d-input>
          </d-form-item>
        </grid-row>
        <grid-row>
          <d-form-item>
            <d-button class="primary" @click="analysis(0)" color="#87CEFA" style="margin-right: 50px">分析</d-button>
          </d-form-item>
        </grid-row>
        <grid-row>
  <!--        <grid-col span="=12">-->
  <!--          <my-table :optional="true" :data="tableData" :showPin="showTablePin" :show-pagination="false"></my-table>-->
  <!--        </grid-col>-->
          <grid-col span="18">
            <d-form-item label="迁移问题报告" prop="report">
              <d-input rows="8" readonly=true type="textarea" v-model="report"></d-input>
            </d-form-item>
          </grid-col>
        </grid-row>
        <grid-row>
          <grid-col span="24">
            <d-button type="primary" class="right" style="margin-right: 100px;margin-top: 50px" @click="analysis(1)" >确定</d-button>
            <d-button class="right mr10" @click="prev" style="margin-top: 50px">上一步</d-button>
          </grid-col>
        </grid-row>
      </d-form>
    </div>

    <my-dialog :dialog="addTargetDialog.dialog" visible="false" @on-dialog-confirm="confirmTargetDialog"
               @on-dialog-cancel="cancelTargetDialog">
      <add-broker ref="addBroker"
                  @on-dialog-cancel="cancelTargetDialog"
                  @on-choosed-broker="handleTargetSelectionChange"
                  :urls="addTargetDialog.urls"
                  :colData="addTargetDialog.tableData.colData"
                  :btns="addTargetDialog.tableData.btns"/>
    </my-dialog>

    <my-dialog :dialog="addTopicDialog.dialog" :visible="false" @on-dialog-confirm="confirmTopicDialog"
               @on-dialog-cancel="cancelTopicDialog">
      <grid-col span="4">
        <d-input v-model="searchTopicData.keyword" placeholder="请输入英文名"
                 oninput="value = value.trim()"
                 @on-enter="getList">
          <d-button type="borderless" slot="suffix" @click="getTopics"><icon name="search" size="14" color="#CACACA" ></icon></d-button>
        </d-input>
      </grid-col>
      <my-table :optional="true" :data="addTopicDialog.tableData" :showPin="false" :show-pagination="false"
                @on-selection-change="handleTopicSelectionChange">
      </my-table>
    </my-dialog>

  </div>
</template>

<script>
import addBroker from '../topic/addBroker.vue'
import crud from '../../mixins/crud.js'
import myDialog from '../../components/common/myDialog.vue'
import myTable from '../../components/common/myTable.vue'
import apiRequest from '../../utils/apiRequest'
import {getTopicCodeByCode} from '../../utils/common.js'

export default {
  name: 'migrate-form',
  mixins: [crud],
  components: {
    addBroker,
    myDialog,
    myTable
  },
  props: {
  },
  data () {
    return {
      current: 0,
      timeout: null,
      multipleSelection: [],
      searchTopicData: {
        brokerId: '',
        keyword: ''
      },
      formData: {
        srcBroker: {
          id: 0,
          ip: ''
        },

        desBrokerIps: '',
        desBrokerIds: [],
        removeFirst: false,
        // targets: [],
        scopeType: 1,
        scopes: ''
      },
      targetTableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'brokerId',
            width: '30%'
          },
          {
            title: 'IP',
            key: 'brokerIp',
            width: '30%'
          },
          {
            title: '权重',
            key: 'weight',
            width: '30%'
          }
        ]
      },
      report: '',
      allBrokers: [],
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic',
            width: '20%'
          },
          {
            title: 'group',
            key: 'group',
            width: '10%'
          },
          {
            title: '迁移批次',
            key: 'migrateBatch',
            width: '10%'
          }
        ],
        btns: this.btns
      },
      addTargetDialog: {
        dialog: {
          visible: false,
          title: '选择目标broker',
          width: 700,
          showFooter: true
        },
        urls: {
          search: '/broker/search'
        },
        tableData: {
          rowData: [],
          colData: [
            {
              title: 'Broker分组',
              key: 'group.code'
            },
            {
              title: 'ID',
              key: 'id'
            },
            {
              title: 'IP',
              key: 'ip'
            },
            {
              title: '机房',
              key: 'dataCenterCode'
            },
            {
              title: '主题数',
              key: 'topicCount',
              width: '3%'
            }
          ],
          btns: []
        }
      },
      addTopicDialog: {
        dialog: {
          visible: false,
          title: '选择目标主题',
          width: 700,
          showFooter: true
        },
        urls: {
          search: '/topic/search'
        },
        page: {
          page: 1,
          size: 10,
          total: 0
        },
        multipleSelection: [],
        tableData: {
          rowData: [],
          colData: [
            {
              title: '英文名',
              key: 'code',
              width: '40%'
            },
            {
              title: '命名空间',
              key: 'namespace.code',
              width: '20%'
            },
            {
              title: '类型',
              key: 'type',
              width: '30%'
            }
          ],
          btns: [
            // {
            //   txt: '迁移到其他目标broker',
            //   method: 'on-migrate-another'
            // },
            // {
            //   txt: '移除',
            //   method: 'on-remove'
            // }
          ]
        }
      }
    }
  },
  watch: {},
  methods: {
    openTargetDialog () {
      this.addTargetDialog.dialog.visible = true
    },
    openTopicDialog () {
      this.addTopicDialog.dialog.visible = true
      this.getTopics()
    },
    confirmTargetDialog () {
      this.addTargetDialog.dialog.visible = false
    },
    confirmTopicDialog () {
      this.addTopicDialog.dialog.visible = false
    },
    cancelTargetDialog () {
      this.addTargetDialog.visible = false
    },
    cancelTopicDialog () {
      this.addTopicDialog.visible = false
    },
    handleTargetSelectionChange (val) {
      this.targetTableData.rowData = []
      this.formData.desBrokerIps = ''
      this.formData.desBrokerIds = []
      for (let r of val) {
        let broker = {
          brokerId: r.id,
          brokerIp: r.ip,
          weight: 1
        }
        this.targetTableData.rowData.push(broker)
        this.formData.desBrokerIps = this.formData.desBrokerIps ? this.formData.desBrokerIps + ', ' + r.ip : r.ip
        this.formData.desBrokerIds.push(r.id)
      }
    },
    prev () {
      this.current = this.current - 1
    },
    next () {
      // 还需要校验数据
      this.current = this.current + 1
    },
    handleBrokerInput (item) {
      this.searchTopicData.brokerId = item.id
      this.formData.srcBroker.id = item.id
      this.formData.srcBroker.ip = item.ip
    },
    handleTopicSelectionChange (val) {
      this.formData.scopes = ''
      for (let r of val) {
        this.formData.scopes = this.formData.scopes === '' ? getTopicCodeByCode(r.code, r.namespace)
          : this.formData.scopes + ',' + getTopicCodeByCode(r.code, r.namespace)
      }
    },
    queryBroker (queryString, callback) {
      if (queryString) {
        clearTimeout(this.timeout)
        this.getBrokers(queryString).then(data => {
          this.timeout = setTimeout(() => {
            callback(data)
          }, 500 * Math.random())
        })
      }
    },
    getBrokers (keyword) {
      return new Promise((resolve, reject) => {
        if (!keyword && this.allBrokers.length < 1) {
          let obj = {
            pagination: {
              page: 1,
              size: 10000000
            },
            query: {
              keyword: keyword
            }
          }
          apiRequest.post(this.urlOrigin.searchBroker, {}, obj).then((data) => {
            if (data.code === this.$store.getters.successCode) {
              this.allBrokers = (data.data || [])
              resolve(data.data || [])
            } else {
              resolve([])
            }
          }).catch(error => {
            reject(error)
          })
        } else {
          let data1 = this.allBrokers.filter(this.createFilter(keyword))
          console.log(data1)
          resolve(data1)
        }
      })
    },
    createFilter (queryString) {
      return (broker) => {
        console.log(broker.ip.indexOf(queryString))
        return (broker.ip.indexOf(queryString) > -1)
      }
    },
    analysis (add) {
      let targets = []
      this.formData.desBrokerIds.forEach(r => {
        let target = {
          brokerId: r
        }
        targets.push(target)
      })
      let params = {
        srcBrokerId: this.formData.srcBroker.id,
        scopeType: this.formData.scopeType,
        scopes: this.formData.scopes,
        targets: targets
      }
      // 获取所有broker ip 放入源broker选择框
      let url
      console.log(add)
      if (add === 0) {
        url = this.urlOrigin.analysis
      } else {
        url = this.urlOrigin.add
      }
      apiRequest.post(url, {}, params)
        .then(data => {
          if (add === 0) {
            data.data = data.data || ''
            this.report = data.data
          } else {
            this.$Message('创建成功')
          }
        }).catch(error => {
          console.error(error)
        })
    },
    getTopics () {
      this.showTablePin = true
      apiRequest.post(this.urlOrigin.searchTopic, {}, this.searchTopicData).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        this.addTopicDialog.tableData.rowData = data.data
        this.showTablePin = false
      })
    }
  },
  mounted () {
    this.allBrokers = this.getBrokers()
  }
}
</script>
