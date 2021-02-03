package org.joyqueue.model.query;

import org.joyqueue.model.Query;

public class QMigrationTask implements Query {

    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
