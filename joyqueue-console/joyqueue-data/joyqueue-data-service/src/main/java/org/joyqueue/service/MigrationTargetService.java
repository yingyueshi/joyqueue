package org.joyqueue.service;

import org.joyqueue.model.domain.migration.MigrationTarget;

import java.util.List;

public interface MigrationTargetService extends Service<MigrationTarget> {

    List<MigrationTarget> findByMigrationId(long migrationId);

}
