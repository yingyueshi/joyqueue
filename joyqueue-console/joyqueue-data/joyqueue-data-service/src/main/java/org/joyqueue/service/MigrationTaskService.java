package org.joyqueue.service;

import org.joyqueue.model.domain.migration.MigrationTask;
import org.joyqueue.model.query.QMigrationTask;

import java.util.List;

public interface MigrationTaskService extends PageService<MigrationTask, QMigrationTask> {

    String analysis(MigrationTask task, boolean add);

    List<MigrationTask> findUnfinishedBySrcBrokerId(int srcBrokerId);

}
