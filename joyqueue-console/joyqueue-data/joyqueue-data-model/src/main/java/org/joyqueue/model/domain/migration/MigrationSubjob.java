package org.joyqueue.model.domain.migration;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.model.domain.BaseModel;

import static org.joyqueue.domain.TopicName.DEFAULT_NAMESPACE;

public class MigrationSubjob extends BaseModel {
    /**
     * 删除
     */
    public static final int DELETED = -1;
    /**
     * 新增
     */
    public static final int NEW = 0;
    /**
     * 派发
     */
    public static final int DISPATCHED = 1;
    /**
     * 执行中
     */
    public static final int RUNNING = 2;
    /**
     * 执行成功
     */
    public static final int SUCCESSED = 3;
    /**
     * 失败，不需要重试
     */
    public static final int FAILED_NO_RETRY = 4;
    /**
     * 失败,需要重试
     */
    public static final int FAILED_RETRY = 5;

    private long migrationId;

    private String topicCode;

    private String namespaceCode;

    private int pgNo;

    private int srcBrokerId;

    private int tgtBrokerId;

    private int version;

    private String executor;

    private String exception;

    public long getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(long migrationId) {
        this.migrationId = migrationId;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getNamespaceCode() {
        if (StringUtils.isEmpty(this.namespaceCode)) {
            return DEFAULT_NAMESPACE;
        }
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

    public int getSrcBrokerId() {
        return srcBrokerId;
    }

    public void setSrcBrokerId(int srcBrokerId) {
        this.srcBrokerId = srcBrokerId;
    }

    public int getTgtBrokerId() {
        return tgtBrokerId;
    }

    public void setTgtBrokerId(int tgtBrokerId) {
        this.tgtBrokerId = tgtBrokerId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
