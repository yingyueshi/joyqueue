/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.server.archive.store.query;

import org.joyqueue.server.archive.store.model.Query;
import org.joyqueue.server.archive.store.utils.ArchiveSerializer;

import java.util.Arrays;

/**
 * Created by chengzhiliang on 2018/12/4.
 */
public class QueryCondition implements Query {

    @Override
    public <T> T getQueryCondition() {
        return (T)this;
    }

    private RowKey startRowKey; // 查询开始键
    private RowKey stopRowKey; // 查询结束键
    private int count;
    private int queryBucket = 0;

    private RowKey rowKey; // 指定RowKey查询

    private byte[] startRowKeyByteArr;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public RowKey getStartRowKey() {
        return startRowKey;
    }

    public void setStartRowKey(RowKey startRowKey) {
        this.startRowKey = startRowKey;
    }

    public RowKey getStopRowKey() {
        return stopRowKey;
    }

    public void setStopRowKey(RowKey stopRowKey) {
        this.stopRowKey = stopRowKey;
    }

    public RowKey getRowKey() {
        return rowKey;
    }

    public void setRowKey(RowKey rowKey) {
        this.rowKey = rowKey;
    }

    public byte[] getStartRowKeyByteArr() {
        return startRowKeyByteArr;
    }

    public void setStartRowKeyByteArr(String startRowKeyByteArr) {
        byte[] bytes = ArchiveSerializer.hexStrToByteArray(startRowKeyByteArr);
        this.startRowKeyByteArr = bytes;
    }

    public int getQueryBucket() {
        return queryBucket;
    }

    public void setQueryBucket(int queryBucket) {
        this.queryBucket = queryBucket;
    }

    @Override
    public String toString() {
        return "QueryCondition{" +
                "startRowKey=" + startRowKey +
                ", stopRowKey=" + stopRowKey +
                ", count=" + count +
                ", queryBucket=" + queryBucket +
                ", rowKey=" + rowKey +
                ", startRowKeyByteArr=" + Arrays.toString(startRowKeyByteArr) +
                '}';
    }

    /**
     * 查询RowKey
     */
    public static class RowKey {
        private String topic;
        private long beginTime;
        private long endTime;
        private long sendTime;
        private String businessId;
        private String messageId;

        public static RowKey of(RowKey rowKey) {
            RowKey clone = new RowKey();
            clone.setTopic(rowKey.getTopic());
            clone.setBeginTime(rowKey.getBeginTime());
            clone.setEndTime(rowKey.getEndTime());
            clone.setSendTime(rowKey.getSendTime());
            clone.setBusinessId(rowKey.getBusinessId());
            clone.setMessageId(rowKey.getMessageId());
            return clone;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public long getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(long beginTime) {
            this.beginTime = beginTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getSendTime() {
            return sendTime;
        }

        public void setSendTime(long sendTime) {
            this.sendTime = sendTime;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        @Override
        public String toString() {
            return "RowKey{" +
                    "topic='" + topic + '\'' +
                    ", beginTime=" + beginTime +
                    ", endTime=" + endTime +
                    ", sendTime=" + sendTime +
                    ", businessId='" + businessId + '\'' +
                    ", messageId='" + messageId + '\'' +
                    '}';
        }
    }
}
