package org.joyqueue.repository;

import org.apache.ibatis.annotations.Param;
import org.joyqueue.model.domain.migration.MigrationSubjob;

import java.util.List;

/**
 * Hosts repository
 * Created by chenyanying3 on 2018-10-15
 */
@org.springframework.stereotype.Repository
public interface MigrationSubjobRepository extends Repository<MigrationSubjob> {

    List<MigrationSubjob> findByMigrationId(long migrationId);

    List<MigrationSubjob> findByExecutor(@Param("executor") String executor, @Param("status") int status);

    List<MigrationSubjob> findByStatus(int status);

    void updateExecutor(MigrationSubjob subjob);

    void updateByTask(@Param("taskId") long taskId, @Param("status") int status, @Param("updateBy") long updateBy);

    void updateStatus(MigrationSubjob subjob);

    void fail(MigrationSubjob subjob);

    void success(MigrationSubjob subjob);

}
