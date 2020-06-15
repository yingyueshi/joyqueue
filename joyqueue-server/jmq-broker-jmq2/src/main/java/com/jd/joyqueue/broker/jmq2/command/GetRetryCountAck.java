package com.jd.joyqueue.broker.jmq2.command;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;

/**
 * 获取重试条数应答
 */
public class GetRetryCountAck extends JMQ2Payload {
    // 主题
    private String topic;
    // 应用
    private String app;
    // 数量
    private int count;

    public GetRetryCountAck topic(final String topic) {
        setTopic(topic);
        return this;
    }

    public GetRetryCountAck app(final String app) {
        setApp(app);
        return this;
    }

    public GetRetryCountAck count(final int count) {
        setCount(count);
        return this;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(topic != null && !topic.isEmpty(), "topic can not be empty");
        Preconditions.checkArgument(app != null && !app.isEmpty(), "app can not be empty");
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_RETRY_COUNT_ACK.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetRetryPayload{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GetRetryCountAck getRetry = (GetRetryCountAck) o;

        if (count != getRetry.count) {
            return false;
        }
        if (app != null ? !app.equals(getRetry.app) : getRetry.app != null) {
            return false;
        }
        if (topic != null ? !topic.equals(getRetry.topic) : getRetry.topic != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + count;
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (app != null ? app.hashCode() : 0);
        return result;
    }
}