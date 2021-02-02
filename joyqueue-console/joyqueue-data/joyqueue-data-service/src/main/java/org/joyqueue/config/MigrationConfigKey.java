package org.joyqueue.config;

import org.joyqueue.toolkit.config.PropertyDef;

public enum MigrationConfigKey implements PropertyDef {

    CHANGE_LEADER_MAX_RETRY_COUNT("change.leader.max.retry.count", 2, Type.INT),
    NSR_UPDATE_CHECK_TIMEOUT("nsr.update.timeout", 60 * 1000L, Type.LONG),
    NSR_UPDATE_CHECK_INTERVAL("nsr.update.check.interval", 3 * 1000L, Type.LONG),
    REPLICATION_CHECK_TIMEOUT("replication.check.timeout", 5 * 60 * 1000L, Type.LONG),
    REPLICATION_CHECK_INTERVAL("replication.check.interval", 10 * 1000L, Type.LONG),
    SOURCE_LIMIT_TYPE("source.limit.type", 0, Type.INT) //0: 源Broker控制所有可能成为leader的副本并发数， 1：源broker只控制leader并发数
    ;

    private final String name;
    private final Object value;
    private final Type type;

    MigrationConfigKey(String name, Object value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public Type getType() {
        return this.type;
    }
}
