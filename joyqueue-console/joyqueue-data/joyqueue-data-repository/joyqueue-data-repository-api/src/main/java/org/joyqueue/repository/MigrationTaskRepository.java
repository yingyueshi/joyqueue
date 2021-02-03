package org.joyqueue.repository;

import org.joyqueue.model.domain.migration.MigrationTask;
import org.joyqueue.model.query.QMigrationTask;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hosts repository
 * Created by chenyanying3 on 2018-10-15
 */
@Repository
public interface MigrationTaskRepository extends PageRepository<MigrationTask, QMigrationTask> {

    List<MigrationTask> findUnfinishedBySrcBrokerId(int srcBrokerId);
}
