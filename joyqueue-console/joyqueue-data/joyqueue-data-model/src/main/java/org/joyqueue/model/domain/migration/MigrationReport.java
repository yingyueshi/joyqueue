package org.joyqueue.model.domain.migration;

import org.joyqueue.domain.TopicName;
import org.joyqueue.model.domain.BaseModel;
import org.joyqueue.model.domain.EnumItem;

public class MigrationReport extends BaseModel {

    private long migrationId;
    private ErrorType type;
    private String topicCode;
    private String namespaceCode;
    private int pgNo;

    public MigrationReport() {}

    public MigrationReport(ErrorType type, String topicCode, String namespaceCode, int pgNo) {
        this.type = type;
        this.topicCode = topicCode;
        this.namespaceCode = namespaceCode;
        this.pgNo = pgNo;
        this.type = type;
    }

    public long getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(long migrationId) {
        this.migrationId = migrationId;
    }

    public ErrorType getType() {
        return type;
    }

    public void setType(ErrorType type) {
        this.type = type;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getNamespaceCode() {
        return namespaceCode;
    }

    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    public int getPgNo() {
        return pgNo;
    }

    public void setPgNo(int pgNo) {
        this.pgNo = pgNo;
    }

    public String getPgDescriptor() {
        return TopicName.parse(this.topicCode, this.namespaceCode).getFullName() + "-" + this.pgNo;
    }

    public enum ErrorType implements EnumItem {
        Single_replica(1, "单主题"),
        Lack_replica(2, "缺少副本"),
        No_target(3, "没有可替换的Broker"),
        Even_replica(4, "偶数副本"),
        Only_add(5, "只添加不摘除"),
        Not_in_source_broker(6, "不在源目标中");

        private int value;
        private String desc;
        ErrorType(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }


        @Override
        public int value() {
            return this.value;
        }

        @Override
        public String description() {
            return this.desc;
        }
    }
}
