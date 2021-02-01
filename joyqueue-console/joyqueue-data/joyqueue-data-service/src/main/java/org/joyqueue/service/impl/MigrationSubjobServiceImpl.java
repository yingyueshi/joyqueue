package org.joyqueue.service.impl;

import org.joyqueue.model.domain.migration.MigrationSubjob;
import org.joyqueue.repository.MigrationSubjobRepository;
import org.joyqueue.service.MigrationSubjobService;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.util.LocalSession;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("migrationSubjobService")
public class MigrationSubjobServiceImpl extends ServiceSupport<MigrationSubjob, MigrationSubjobRepository> implements MigrationSubjobService {

    @Override
    public List<MigrationSubjob> findByMigrationId(long migrationId) {
        return repository.findByMigrationId(migrationId);
    }

    @Override
    public List<MigrationSubjob> findByExecutor(String executor, int status) {
        return repository.findByExecutor(executor, status);
    }

    @Override
    public List<MigrationSubjob> findByStatus(int status) {
        return repository.findByStatus(status);
    }

    @Override
    public void updateExecutor(long id, String executor, int status) {
        MigrationSubjob subjob = new MigrationSubjob();
        subjob.setId(id);
        subjob.setStatus(status);
        subjob.setExecutor(executor);
        subjob.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
        subjob.setUpdateTime(new Date());
        repository.updateExecutor(subjob);
    }

    @Override
    public void updateByTask(long taskId, int status, long updateBy) {
        repository.updateByTask(taskId, status, updateBy);
    }

    @Override
    public void state(long id, int status) {
        MigrationSubjob subjob = new MigrationSubjob();
        subjob.setId(id);
        subjob.setStatus(status);
        subjob.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
        subjob.setUpdateTime(new Date());
        repository.state(subjob);
    }

    @Override
    public void fail(long id, String exception, int status) {
        MigrationSubjob subjob = new MigrationSubjob();
        subjob.setId(id);
        subjob.setStatus(status);
        subjob.setException(exception);
        subjob.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
        subjob.setUpdateTime(new Date());
        repository.fail(subjob);
    }

    @Override
    public void success(long id, int status) {
        MigrationSubjob subjob = new MigrationSubjob();
        subjob.setId(id);
        subjob.setStatus(status);
        subjob.setException(null);
        subjob.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
        subjob.setUpdateTime(new Date());
        repository.success(subjob);
    }

}
