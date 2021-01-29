package org.joyqueue.repository;

import org.joyqueue.model.domain.migration.MigrationTarget;

import java.util.List;

/**
 * Hosts repository
 * Created by chenyanying3 on 2018-10-15
 */
@org.springframework.stereotype.Repository
public interface MigrationTargetRepository extends Repository<MigrationTarget> {

    List<MigrationTarget> findByMigrationId(long migrationId);

}
