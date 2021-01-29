package org.joyqueue.service;

import org.joyqueue.model.domain.migration.MigrationSubjob;

import java.util.List;

public interface MigrationSubjobService extends Service<MigrationSubjob> {

    List<MigrationSubjob> findByMigrationId(long migrationId);

    List<MigrationSubjob> findByExecutor(String executor, int status);

    List<MigrationSubjob> findByStatus(int status);

    void updateExecutor(long id, String executor, int status);

    void updateByTask(long taskId, int status, long updateBy);

    void state(long id, int status);

    void fail(long id, String exception, int status);

    void success(long id, int status);

}
