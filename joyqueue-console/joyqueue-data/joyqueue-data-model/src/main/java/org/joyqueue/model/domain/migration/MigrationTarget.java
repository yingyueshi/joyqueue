package org.joyqueue.model.domain.migration;

import org.joyqueue.model.domain.BaseModel;

public class MigrationTarget extends BaseModel {

    private long migrationId;
    private int brokerId;
    private String brokerIp;
    private int weight;

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public long getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(long migrationId) {
        this.migrationId = migrationId;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
