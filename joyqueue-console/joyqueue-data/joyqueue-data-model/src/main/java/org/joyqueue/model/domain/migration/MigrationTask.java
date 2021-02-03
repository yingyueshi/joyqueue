package org.joyqueue.model.domain.migration;

import org.joyqueue.model.domain.BaseModel;
import org.joyqueue.model.domain.EnumItem;

import java.util.List;

public class MigrationTask extends BaseModel {

    /**
     * 删除
     */
    public static final int DELETED = -1;
    /**
     * 新增
     */
    public static final int NEW = 1;
    /**
     * 派发
     */
    public static final int RUNNING = 2;
    /**
     * 成功
     */
    public static final int SUCCESSED = 3;
    /**
     * 失败
     */
    public static final int PART_FAILED = 4;

    private int srcBrokerId;

    private boolean removeFirst;

    private List<MigrationTarget> targets;

    private String targetsStr;

    private ScopeType scopeType;

    private String scopes;

    public int getSrcBrokerId() {
        return srcBrokerId;
    }

    public void setSrcBrokerId(int srcBrokerId) {
        this.srcBrokerId = srcBrokerId;
    }

    public boolean isRemoveFirst() {
        return removeFirst;
    }

    public void setRemoveFirst(boolean removeFirst) {
        this.removeFirst = removeFirst;
    }

    public String getTargetsStr() {
        return targetsStr;
    }

    public void setTargetsStr(String targetsStr) {
        this.targetsStr = targetsStr;
    }

    public List<MigrationTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<MigrationTarget> targets) {
        this.targets = targets;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public enum ScopeType implements EnumItem {
        ALL(0, "全部"),
        TOPICS(1, "指定主题"),
        EXCLUDE_TOPICS(2, "排除主题");

        private int value;
        private String desc;

        ScopeType(int value, String desc) {
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
