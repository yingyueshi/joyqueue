package org.joyqueue.model.keyword;

import org.joyqueue.model.QKeyword;

public class TopicKeyword extends QKeyword {

    private String brokerId;

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }
}
