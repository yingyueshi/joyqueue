package org.joyqueue.config;

import org.joyqueue.toolkit.config.PropertyDef;

public enum MigrationConfigKey implements PropertyDef {

    CHANGE_LEADER_MAX_RETRY_COUNT("change.leader.max.retry.count", 4, Type.INT),
    NSR_UPDATE_CHECK_TIMEOUT("nsr.update.timeout", 40 * 1000L, Type.LONG),
    NSR_UPDATE_CHECK_INTERVAL("nsr.update.check.interval", 10 * 60 * 1000L, Type.LONG),
    REPLICATION_CHECK_TIMEOUT("replication.check.timeout", 10 * 60 * 1000L, Type.LONG),
    REPLICATION_CHECK_INTERVAL("replication.check.interval", 30 * 1000L, Type.LONG)
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
