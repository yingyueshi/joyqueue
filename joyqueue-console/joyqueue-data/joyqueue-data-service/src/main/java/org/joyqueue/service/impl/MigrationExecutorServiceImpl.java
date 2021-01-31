package org.joyqueue.service.impl;

import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.MigrationException;
import org.joyqueue.manage.PartitionGroupPosition;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.domain.*;
import org.joyqueue.model.domain.migration.MigrationSubjob;
import org.joyqueue.model.domain.migration.MigrationTask;
import org.joyqueue.model.query.QMigrationTask;
import org.joyqueue.service.*;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.joyqueue.config.MigrationConfigKey.*;
import static org.joyqueue.model.domain.migration.MigrationSubjob.FAILED_NO_RETRY;
import static org.joyqueue.model.domain.migration.MigrationSubjob.SUCCESSED;
import static org.joyqueue.model.domain.migration.MigrationTask.NEW;

@Service("migrationExecutorService")
public class MigrationExecutorServiceImpl implements MigrationExecutorService {

    private final Logger logger = LoggerFactory.getLogger(MigrationExecutorServiceImpl.class);

    @Autowired
    private MigrationSubjobService migrationSubjobService;
    @Autowired
    private MigrationTaskService migrationTaskService;
    @Autowired
    private TopicPartitionGroupService topicPartitionGroupService;
    @Autowired
    private PartitionGroupReplicaService replicaService;
    @Autowired
    private BrokerMonitorService brokerMonitorService;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String DOT = ".";
    private Properties config;
    private int changeLeaderMaxRetryCount;
    private long nsrUpdateCheckTimeout;
    private long nsrUpdateCheckInterval;
    private long replicationCheckTimeout;
    private long replicationCheckInterval;

    private static MigrationSubjob executeSubjob;

    @PostConstruct
    private void init(){
        config = new Properties();
        try {
            config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            logger.info("broker migration config loaded successfully. ");
        } catch (IOException e) {
            logger.error("Failed to read broker migration config , error: {}", e.getMessage());
        }

        changeLeaderMaxRetryCount = Integer.parseInt(config.getProperty(CHANGE_LEADER_MAX_RETRY_COUNT.getName(), CHANGE_LEADER_MAX_RETRY_COUNT.getValue().toString()));
        nsrUpdateCheckTimeout = Long.parseLong(config.getProperty(NSR_UPDATE_CHECK_TIMEOUT.getName(), NSR_UPDATE_CHECK_TIMEOUT.getValue().toString()));
        nsrUpdateCheckInterval = Long.parseLong(config.getProperty(NSR_UPDATE_CHECK_INTERVAL.getName(), NSR_UPDATE_CHECK_INTERVAL.getValue().toString()));
        replicationCheckTimeout = Long.parseLong(config.getProperty(REPLICATION_CHECK_TIMEOUT.getName(), REPLICATION_CHECK_TIMEOUT.getValue().toString()));
        replicationCheckInterval = Long.parseLong(config.getProperty(REPLICATION_CHECK_INTERVAL.getName(), REPLICATION_CHECK_INTERVAL.getValue().toString()));
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void dispatchTask() {
        // 因重启等，导致RUNNING状态却不在执行自动任务中，回滚
        String localIp = IpUtil.getLocalIp();
        List<MigrationSubjob> runnings = migrationSubjobService.findByExecutor(localIp, MigrationSubjob.RUNNING);
        runnings.forEach(run -> {
            if (executeSubjob == null || run.getId() != executeSubjob.getId()) {
                run.setStatus(MigrationSubjob.DISPATCHED);
                run.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
                migrationSubjobService.updateStatus(run);
                logger.warn(String.format("执行作业%s状态为执行中，但是却不在执行器中，回滚为已派发状态。", run.getId()));
            }
        });
        // 每次派发一个
        List<MigrationSubjob> waitExecutes = migrationSubjobService.findByExecutor(localIp, MigrationSubjob.DISPATCHED);
        if (waitExecutes.size() > 0) {
            return;
        }

        List<MigrationSubjob> dispatches = migrationSubjobService.findByStatus(MigrationSubjob.DISPATCHED);
        dispatches.addAll(runnings);

        List<Integer> leaders = dispatches.stream().map(dispatch -> {
            TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(dispatch.getNamespaceCode(),
                    dispatch.getTopicCode(), dispatch.getPgNo());
            if (group == null) {
                return -1;
            }
            return group.getLeader();
        }).filter(leader -> leader > 0).collect(Collectors.toList());
        List<MigrationSubjob> newSubjobs = migrationSubjobService.findByStatus(MigrationSubjob.NEW);
        A: for (MigrationSubjob subjob : newSubjobs) {
            // 分区组不能存在当前已派发的任务中
            for (MigrationSubjob dispatch : dispatches) {
                if (dispatch.getTopicCode().equals(subjob.getTopicCode()) && dispatch.getNamespaceCode().equals(subjob.getNamespaceCode())
                && dispatch.getPgNo() == subjob.getPgNo()) {
                    continue A;
                }
            }
            // 分区组副本不能已经存在当前已派发任务的leader中
            TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(subjob.getNamespaceCode(),
                    subjob.getTopicCode(), subjob.getPgNo());
            B: for (Integer replica : group.getReplicas()) {
                if (replica != subjob.getSrcBrokerId() && leaders.contains(replica)) {
                    continue A;
                }
            }
            migrationSubjobService.updateExecutor(subjob.getId(), IpUtil.getLocalIp(), MigrationSubjob.DISPATCHED);
            break A;
        }

    }

    @Scheduled(fixedDelay = 2 * 1000)
    private void executorTask() {
        List<MigrationSubjob> waitExecutes = migrationSubjobService.findByExecutor(IpUtil.getLocalIp(), MigrationSubjob.DISPATCHED);
        List<MigrationSubjob> fails = migrationSubjobService.findByExecutor(IpUtil.getLocalIp(), MigrationSubjob.FAILED_RETRY);
        waitExecutes.addAll(fails);
        if (waitExecutes.size() < 1) {
            return;
        }
        MigrationSubjob waitExecute = waitExecutes.get(0);
        executeSubjob = waitExecute;
        migrationSubjobService.state(waitExecute.getId(), MigrationSubjob.RUNNING);
        executeReplica(waitExecute);
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void updateTaskStatus() {
        migrationTaskService.findByQuery(new ListQuery<>(new QMigrationTask())).stream().forEach(task -> {
            List<MigrationSubjob> subjobs = migrationSubjobService.findByMigrationId(task.getId());
            int size = subjobs.size();
            if (size <= 0) {
                task.setStatus(MigrationTask.DELETED);
                migrationTaskService.updateStatus(task);
                return;
            }

            if (size == subjobs.stream().filter(subjob -> subjob.getStatus()==NEW).count()) {
                task.setStatus(MigrationTask.NEW);
                migrationTaskService.updateStatus(task);
                return;
            }

            if (size == subjobs.stream().filter(subjob -> subjob.getStatus()==SUCCESSED).count()) {
                task.setStatus(MigrationTask.SUCCESSED);
                migrationTaskService.updateStatus(task);
                return;
            }

            if (size == subjobs.stream().filter(subjob -> subjob.getStatus()==SUCCESSED || subjob.getStatus()==FAILED_NO_RETRY).count()) {
                task.setStatus(MigrationTask.PART_FAILED);
                migrationTaskService.updateStatus(task);
                return;
            }

            task.setStatus(MigrationTask.RUNNING);
            migrationTaskService.updateStatus(task);
        });
    }

    private void executeReplica(MigrationSubjob subjob) {
        lock.writeLock().lock();
        logger.info("Execute replica task: {}", subjob);
        try {
            TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(subjob.getNamespaceCode(),
                    subjob.getTopicCode(), subjob.getPgNo());
            // 未摘除源Broker
            if (group.getReplicas().contains(subjob.getSrcBrokerId())) {
                // 如果leader没有选出来 直接下一轮等待选举结果
                if (group.getLeader() == null || group.getLeader() < 0) {
                    logger.warn("主题分区组leader节点未选举成功,需等待下轮检查,主题[{}],分组[{}],该分区组源leader节点[{}]",
                            subjob.getTopicCode(), subjob.getPgNo(), subjob.getSrcBrokerId());
                    return;
                }
                // 源目标是leader，先切走
                boolean isLeaderChanged = true;
                if (subjob.getSrcBrokerId() == group.getLeader()) {
                    Set<Integer> replicas = group.getReplicas();
                    // 单副本
                    if (replicas.size() < 2) {
                        logger.error("replicas副本数是偶数,不满足切换leader条件,task[{}],replicas[{}]", subjob, replicas);
                        migrationSubjobService.fail(subjob.getId(), "replicas副本数是偶数,不满足切换leader条件", FAILED_NO_RETRY);
                        throw new MigrationException("replicas副本数是偶数,不满足切换leader条件.");
                    }
                    // 偶数副本
                    if (group.getReplicas().size() % 2 == 0) {
                        logger.error("replicas副本数是单副本,不满足切换leader条件, subjob[{}],replicas[{}]", subjob, replicas);
                        migrationSubjobService.fail(subjob.getId(), "replicas副本数是单副本,不满足切换leader条件", FAILED_NO_RETRY);
                        throw new MigrationException("replicas副本数是单副本,不满足切换leader条件.");
                    }
                    // 切leader
                    isLeaderChanged = leaderChange(subjob, replicas);
                }

                // leader已经切走，摘除
                if (isLeaderChanged) {
                    try {
                        removeReplica(subjob);
                    } catch (Exception e) {
                        logger.error("摘除源Broker失败,subjob[{}]", subjob);
                        migrationSubjobService.fail(subjob.getId(), "摘除源Broker失败", FAILED_NO_RETRY);
                        throw new MigrationException("摘除源Broker失败");
                    }
                } else {
                    logger.error("切Leader失败,subjob[{}]", subjob);
                    migrationSubjobService.fail(subjob.getId(), "切Leader失败", FAILED_NO_RETRY);
                    throw new MigrationException("切Leader失败");
                }
            }

            // 已摘除，添加
            try {
                addReplica(subjob);
            } catch (Exception e) {
                logger.error("添加目标Broker失败,subjob[{}]", subjob);
                migrationSubjobService.fail(subjob.getId(), "添加目标Broker失败", FAILED_NO_RETRY);
                throw new MigrationException("添加目标Broker失败");
            }

            // 监听
            long leaderPosition;
            try {
                leaderPosition = getLeaderPosition(subjob);
            } catch (Exception e) {
                logger.error(String.format("获取Leader当前位置失败, subjob: %s ", subjob), e);
                return;
            }

            long startTime = SystemClock.now();
            while (SystemClock.now() - startTime < replicationCheckTimeout) {
                if (checkReplicationPosition(subjob, leaderPosition)) {
                    logger.info(String.format("迁移执行成功, subjob: %s ", subjob));
                    migrationSubjobService.success(subjob.getId(), SUCCESSED);
                    return;
                }
                try {
                    Thread.sleep(replicationCheckInterval);
                } catch (InterruptedException e) {
                    logger.error("sleep 失败");
                }
            }

        } catch (Exception e) {
            logger.error(String.format("迁移执行失败, subjob: %s ", subjob), e);
            migrationSubjobService.fail(subjob.getId(), "调度迁移执行失败, cause:" + e.getMessage(), FAILED_NO_RETRY);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean leaderChange(MigrationSubjob subjob, Set<Integer> replicas) throws Exception {
        TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(subjob.getNamespaceCode(),
                subjob.getTopicCode(), subjob.getPgNo());
        if (subjob.getSrcBrokerId() != group.getLeader()) {
            return true;
        }
        logger.info("Replica leader change: {}", subjob);
        List<Integer> candidates = replicas.stream().filter(r -> !r.equals(subjob.getSrcBrokerId())).collect(Collectors.toList());
        if (candidates.size() < 1) {
            logger.error("replicas副本数是单副本,不满足切换leader条件,task[{}],replicas[{}]", subjob, replicas);
            migrationSubjobService.fail(subjob.getId(), "replicas副本数是单副本,不满足切换leader条件", FAILED_NO_RETRY);
            throw new MigrationException("replicas副本数是单副本,不满足切换leader条件.");
        }

        logger.info("进行迁移leader change,主题分区组变更: {}", group);
        int retryCount = 0;
        while (retryCount < changeLeaderMaxRetryCount) {
            Integer newLeader = candidates.get(retryCount % candidates.size());
            PartitionGroupReplica replica = replicaService.findById(subjob.getTopicCode() + DOT + subjob.getPgNo() + DOT + newLeader);
            group.setOutSyncReplicas(replica.getOutSyncReplicas());
            group.setLeader(newLeader);
            int count = topicPartitionGroupService.leaderChange(group);
            if (count <= 0) {
                retryCount++;
                logger.error("要更新的replica数据不存在,进行重试第{}次,subjob[{}]", retryCount, subjob);
            } else {
                long startTime = SystemClock.now();
                while (SystemClock.now() - startTime < nsrUpdateCheckTimeout) {
                    group = topicPartitionGroupService.findByTopicAndGroup(subjob.getNamespaceCode(),
                            subjob.getTopicCode(), subjob.getPgNo());
                    if (subjob.getSrcBrokerId() != group.getLeader()) {
                        return true;
                    }
                    try {
                        Thread.sleep(nsrUpdateCheckInterval);
                    } catch (InterruptedException e) {
                        logger.error("切Leader sleep 失败");
                    }
                }
                retryCount++;
                logger.error("要更新的replica数据不存在,进行重试第{}次,subjob[{}]", retryCount, subjob);
            }
        }
        return false;
    }

    private void removeReplica(MigrationSubjob subjob) throws Exception {
        logger.info("Replica delete: {}", subjob);
        String deleteId = subjob.getTopicCode() + DOT + subjob.getPgNo() + DOT + subjob.getSrcBrokerId();
        PartitionGroupReplica replica = replicaService.findById(deleteId);
        replicaService.removeWithNameservice(replica, topicPartitionGroupService.findByTopicAndGroup(
                replica.getNamespace().getCode(),replica.getTopic().getCode(),replica.getGroupNo()));
    }

    private void addReplica(MigrationSubjob subjob) {
        TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(subjob.getNamespaceCode(),
                subjob.getTopicCode(), subjob.getPgNo());
        // 已经存在，不再添加
        if (group.getReplicas().stream().filter(replica -> replica == subjob.getTgtBrokerId()).findFirst().isPresent()) {
            return;
        }
        // 不存在，添加
        PartitionGroupReplica replica = new PartitionGroupReplica();
        Namespace namespace = new Namespace(subjob.getNamespaceCode(), subjob.getNamespaceCode());
        Topic topic = new Topic(TopicName.parse(subjob.getTopicCode(), subjob.getNamespaceCode()).getFullName(), subjob.getTopicCode());
        replica.setNamespace(namespace);
        replica.setTopic(topic);
        replica.setGroupNo(subjob.getPgNo());
        replica.setBrokerId(subjob.getTgtBrokerId());
        if(group.getElectType().equals(TopicPartitionGroup.ElectType.raft.type())) {
            replica.setRole(PartitionGroupReplica.ROLE_DYNAMIC);
        } else {
            replica.setRole(PartitionGroupReplica.ROLE_SLAVE);
        }

        replicaService.addWithNameservice(replica, group);
    }

    private long getLeaderPosition(MigrationSubjob subjob) throws Exception {
        List<PartitionGroupPosition> partitionGroupPositions = brokerMonitorService.findPartitionGroupMetric(
                subjob.getNamespaceCode(), subjob.getTopicCode(), subjob.getPgNo());
        return partitionGroupPositions.stream().filter(pg -> pg.isLeader()).findFirst().get().getRightPosition();
    }

    private boolean checkReplicationPosition(MigrationSubjob subjob, long leaderStartPosition) throws Exception {
        logger.info("Check replica position, subjob: {}", subjob);
        List<PartitionGroupPosition> partitionGroupPositions = brokerMonitorService.findPartitionGroupMetric(
                subjob.getNamespaceCode(), subjob.getTopicCode(), subjob.getPgNo());
        if (partitionGroupPositions.stream().filter(pg -> Integer.parseInt(pg.getBrokerId().split("_")[0]) == subjob.getTgtBrokerId())
                .findFirst().get().getRightPosition() >= leaderStartPosition) {
            return true;
        }
        return false;
    }

}
