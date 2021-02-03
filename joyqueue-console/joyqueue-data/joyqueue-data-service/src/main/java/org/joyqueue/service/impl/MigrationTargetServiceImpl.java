package org.joyqueue.service.impl;

import org.joyqueue.model.domain.migration.MigrationTarget;
import org.joyqueue.repository.MigrationTargetRepository;
import org.joyqueue.service.MigrationTargetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("migrationTargetService")
public class MigrationTargetServiceImpl extends ServiceSupport<MigrationTarget, MigrationTargetRepository> implements MigrationTargetService {

    public List<MigrationTarget> findByMigrationId(long migrationId) {
        return repository.findByMigrationId(migrationId);
    }

}
