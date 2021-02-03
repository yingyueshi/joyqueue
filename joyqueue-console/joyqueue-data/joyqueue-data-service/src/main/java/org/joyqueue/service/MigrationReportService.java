package org.joyqueue.service;

import org.joyqueue.model.domain.migration.MigrationReport;

import java.util.List;

public interface MigrationReportService extends Service<MigrationReport> {
    List<MigrationReport> findByMigrationId(long migrationId);
}
