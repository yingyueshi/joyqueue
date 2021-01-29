package org.joyqueue.service.impl;

import org.joyqueue.model.domain.migration.MigrationReport;
import org.joyqueue.repository.MigrationReportRepository;
import org.joyqueue.service.MigrationReportService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("migrationReportService")
public class MigrationReportServiceImpl extends ServiceSupport<MigrationReport, MigrationReportRepository> implements MigrationReportService {

    @Override
    public List<MigrationReport> findByMigrationId(long migrationId) {
        return repository.findByMigrationId(migrationId);
    }

}
