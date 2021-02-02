package org.joyqueue.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joyqueue.domain.TopicName;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.domain.migration.MigrationReport;
import org.joyqueue.model.domain.migration.MigrationSubjob;
import org.joyqueue.model.domain.migration.MigrationTarget;
import org.joyqueue.model.domain.migration.MigrationTask;
import org.joyqueue.model.exception.BusinessException;
import org.joyqueue.model.exception.NotFoundException;
import org.joyqueue.model.query.QMigrationTask;
import org.joyqueue.repository.MigrationTaskRepository;
import org.joyqueue.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.joyqueue.model.domain.migration.MigrationReport.ErrorType.*;

@Service("migrationTaskService")
public class MigrationTaskServiceImpl extends PageServiceSupport<MigrationTask, QMigrationTask, MigrationTaskRepository>
        implements MigrationTaskService {
    private final Logger logger = LoggerFactory.getLogger(MigrationTaskServiceImpl.class);
    @Autowired
    private MigrationTargetService migrationTargetService;
    @Autowired
    private MigrationSubjobService migrationSubjobService;
    @Autowired
    private MigrationReportService migrationReportService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private TopicPartitionGroupService topicPartitionGroupReplicaService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int add(final MigrationTask task) {
        int result = super.add(task);
        if (result < 1) {
            return result;
        }
        if (CollectionUtils.isNotEmpty(task.getTargets())) {
            task.getTargets().forEach(target -> {
                target.setMigrationId(task.getId());
                target.setCreateBy(task.getCreateBy());
                target.setStatus(MigrationTask.NEW);
                migrationTargetService.add(target);
            });
        }
        return 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public String analysis(MigrationTask task, boolean add) {
        List<MigrationTarget> targets = task.getTargets();
        if (CollectionUtils.isEmpty(targets)) {
            throw new NotFoundException("没有目标Broker!");
        }

        if (targets.stream().map(t -> t.getBrokerId()).collect(Collectors.toList()).contains(task.getSrcBrokerId())) {
            throw new NotFoundException("目标Broker不能包含源Broker!");
        }

        final List<MigrationSubjob> subjobs = new ArrayList<>();
        final List<MigrationReport> errSubjobs = new ArrayList<>();

        final Map<MigrationTarget, Integer> targetPgCount = new HashMap<>();
        targets.forEach(target -> targetPgCount.put(target, 0));

        try {
            final List<TopicName> allTopics = topicService.findTopic(String.valueOf(task.getSrcBrokerId()));
            List<TopicName> topicNames = new ArrayList<>();
            switch (task.getScopeType()) {
                case TOPICS:
                    String scopes = task.getScopes();
                    if (StringUtils.isEmpty(scopes)) {
                        throw new BusinessException("迁移范围为指定主题，但是指定主题为空。");
                    }
                    topicNames = Arrays.stream(scopes.split(",")).map(topic-> StringUtils.isBlank(topic) ? null : TopicName.parse(topic.trim()))
                            .filter(topic -> {
                                if (topic == null || !allTopics.contains(topic)) {
                                    MigrationReport report = new MigrationReport(Not_in_source_broker, topic.getCode(), topic.getNamespace(), -1);
                                    errSubjobs.add(report);
                                    return false;
                                }
                                return true;
                            }).collect(Collectors.toList());
                    break;
                case EXCLUDE_TOPICS:
                    scopes = task.getScopes();
                    if (!StringUtils.isEmpty(scopes)) {
                        List<TopicName> excludeTopicNames = Arrays.stream(scopes.split(",")).map(topic ->
                                TopicName.parse(topic)).collect(Collectors.toList());
                        topicNames = allTopics;
                        topicNames.removeAll(excludeTopicNames);
                    }
                    break;
                default:
                    break;
            }

            topicNames.forEach(topic ->
                    topicPartitionGroupReplicaService.findByTopic(topic.getCode(), topic.getNamespace()).forEach(pg -> {
                if (!pg.getReplicas().contains(task.getSrcBrokerId())) {
                    return;
                }

                // 单副本
                int replicaSize = pg.getReplicas().size();
                if (replicaSize == 1) {
                    MigrationReport report = new MigrationReport(Single_replica, pg.getTopic().getCode(), pg.getNamespace().getCode(), pg.getGroupNo());
                    errSubjobs.add(report);
                    return;
                }
                // 偶数副本2, 4,6...
                if (replicaSize % 2 == 0) {
                    MigrationReport report = new MigrationReport(Even_replica, pg.getTopic().getCode(), pg.getNamespace().getCode(), pg.getGroupNo());
                    errSubjobs.add(report);
                    return;
                }
                // 目标Broker已经在分区组里面存在
                List<MigrationTarget> filteredTargets = targets.stream().filter(target -> !pg.getReplicas().contains(target.getBrokerId()))
                        .collect(Collectors.toList());
                if (filteredTargets.size() == 0) {
                    MigrationReport report = new MigrationReport(No_target, pg.getTopic().getCode(), pg.getNamespace().getCode(), pg.getGroupNo());
                    errSubjobs.add(report);
                    return;
                }

                if (filteredTargets.size() == targets.size()) {
                    MigrationTarget target = getMinCountTarget(targetPgCount);
                    subjobs.add(fillSubjob(task.getSrcBrokerId(), target.getBrokerId(), pg, MigrationTask.NEW));
                } else {
                    MigrationTarget target = getMinCountTargetFromList(filteredTargets, targetPgCount);
                    subjobs.add(fillSubjob(task.getSrcBrokerId(), target.getBrokerId(), pg, MigrationTask.NEW));
                }
            }));

            if (add) {
                add(task);
                subjobs.forEach(subjob -> {
                    subjob.setMigrationId(task.getId());
                    subjob.setCreateBy(task.getCreateBy());
                    subjob.setStatus(MigrationSubjob.NEW);
                    migrationSubjobService.add(subjob);
                });
                errSubjobs.forEach(errSubjob -> {
                    errSubjob.setMigrationId(task.getId());
                    errSubjob.setCreateBy(task.getCreateBy());
                    migrationReportService.add(errSubjob);
                });
            }
            if (errSubjobs.size() > 0) {
                return convertReport(errSubjobs);
            } else {
                return "没有发现异常";
            }
        } catch (Exception e) {
            logger.error("分析过程内部异常.", e);
            throw new NotFoundException("分析过程内部异常", e);
        }

    }

    @Override
    public List<MigrationTask> findUnfinishedBySrcBrokerId(int srcBrokerId) {
        return repository.findUnfinishedBySrcBrokerId(srcBrokerId);
    }

    protected MigrationSubjob fillSubjob(int srcBrokerId, int tgtBrokerId, TopicPartitionGroup pg, int status) {
        MigrationSubjob subjob = new MigrationSubjob();
        subjob.setSrcBrokerId(srcBrokerId);
        subjob.setTopicCode(pg.getTopic().getCode());
        subjob.setNamespaceCode(pg.getNamespace().getCode());
        subjob.setPgNo(pg.getGroupNo());
        subjob.setTgtBrokerId(tgtBrokerId);
        subjob.setStatus(status);
        return subjob;
    }

    protected MigrationTarget getMinCountTargetFromList(List<MigrationTarget> filteredTargets, Map<MigrationTarget, Integer> targetPgCount) {
        int minCount = -1;
        MigrationTarget target = null;
        for (MigrationTarget target1 : filteredTargets) {
            int count = targetPgCount.get(target1);
            if (minCount == -1 || count < minCount) {
                minCount = count;
                target = target1;
            }
        }
        return target;
    }

    protected MigrationTarget getMinCountTarget(Map<MigrationTarget, Integer> targetPgCount) {
        Iterator iterator = targetPgCount.entrySet().iterator();
        int minCount = -1;
        MigrationTarget target = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (minCount == -1 || (Integer) entry.getValue() < minCount) {
                minCount = (int) entry.getValue();
                target = (MigrationTarget) entry.getKey();
            }
        }
        return target;
    }

    public String convertReport(List<MigrationReport> reports) {
        return reports.stream().collect(Collectors.groupingBy(MigrationReport::getType,
                Collectors.mapping(MigrationReport::getPgDescriptor, Collectors.joining(","))))
                .entrySet().stream().map(entry -> entry.getKey().value() + ": " + entry.getValue() + ".")
                .collect(Collectors.joining("\n"));
    }


}