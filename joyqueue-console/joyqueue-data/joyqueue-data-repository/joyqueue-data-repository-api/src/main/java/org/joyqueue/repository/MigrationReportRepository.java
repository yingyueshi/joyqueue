package org.joyqueue.repository;

import org.joyqueue.model.domain.migration.MigrationReport;

import java.util.List;

/**
 * Hosts repository
 * Created by chenyanying3 on 2018-10-15
 */
@org.springframework.stereotype.Repository
public interface MigrationReportRepository extends Repository<MigrationReport> {

    List<MigrationReport> findByMigrationId(long migrationId);

}
