package org.joyqueue.broker.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NameServiceStub
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class NameServiceStub extends Service implements NameService {

    private AllMetadata allMetadata;
    private EventBus eventBus = new EventBus("joyqueue-nameservice-eventBus");

    public NameServiceStub() {
        allMetadata = new AllMetadata();
        allMetadata.setTopics(Maps.newHashMap());
        allMetadata.setBrokers(Maps.newHashMap());
        allMetadata.setProducers(Lists.newArrayList());
        allMetadata.setConsumers(Lists.newArrayList());
        allMetadata.setDataCenters(Lists.newArrayList());
        allMetadata.setConfigs(Lists.newArrayList());
        allMetadata.setAppTokens(Lists.newArrayList());

    }

    @Override
    protected void doStart() throws Exception {
        eventBus.start();
    }

    @Override
    protected void doStop() {
        eventBus.stop();
    }

    @Override
    public TopicConfig subscribe(Subscription subscription, ClientType clientType) {
        if (subscription.getType().equals(Subscription.Type.CONSUMPTION)) {
            Consumer consumer = new Consumer();
            consumer.setTopic(subscription.getTopic());
            consumer.setApp(subscription.getApp());
            consumer.setClientType(clientType);
            allMetadata.getConsumers().add(consumer);
        } else if (subscription.getType().equals(Subscription.Type.PRODUCTION)) {
            Producer producer = new Producer();
            producer.setTopic(subscription.getTopic());
            producer.setApp(subscription.getApp());
            producer.setClientType(clientType);
            allMetadata.getProducers().add(producer);
        }
        return null;
    }

    @Override
    public List<TopicConfig> subscribe(List<Subscription> subscriptions, ClientType clientType) {
        return null;
    }

    @Override
    public void unSubscribe(Subscription subscription) {

    }

    @Override
    public void unSubscribe(List<Subscription> subscriptions) {

    }

    @Override
    public boolean hasSubscribe(String app, Subscription.Type subscribe) {
        return false;
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {

    }

    @Override
    public Broker getBroker(int brokerId) {
        return allMetadata.getBrokers().get(brokerId);
    }

    @Override
    public List<Broker> getAllBrokers() {
        return Lists.newArrayList(allMetadata.getBrokers().values());
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        allMetadata.getTopics().put(topic.getName(), TopicConfig.toTopicConfig(topic, partitionGroups));
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        return allMetadata.getTopics().get(topic);
    }

    @Override
    public Set<String> getAllTopicCodes() {
        Set<String> result = Sets.newHashSet();
        for (Map.Entry<TopicName, TopicConfig> entry : allMetadata.getTopics().entrySet()) {
            result.add(entry.getKey().getFullName());
        }
        return result;
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        Set<String> result = Sets.newHashSet();
        if (subscription.equals(Subscription.Type.CONSUMPTION)) {
            for (Consumer consumer : allMetadata.getConsumers()) {
                if (consumer.getApp().equals(app)) {
                    result.add(consumer.getTopic().getFullName());
                }
            }
        } else if (subscription.equals(Subscription.Type.PRODUCTION)) {
            for (Producer producer : allMetadata.getProducers()) {
                if (producer.getApp().equals(app)) {
                    result.add(producer.getTopic().getFullName());
                }
            }
        }
        return result;
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        Map<TopicName, TopicConfig> result = Maps.newHashMap();
        for (Map.Entry<TopicName, TopicConfig> entry : allMetadata.getTopics().entrySet()) {
            if (entry.getValue().isReplica(brokerId)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Broker broker = new Broker();
        broker.setId(brokerId);
        broker.setIp(brokerIp);
        broker.setPort(port);
        allMetadata.getBrokers().put(brokerId, broker);
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        for (Producer producer : allMetadata.getProducers()) {
            if (producer.getTopic().equals(topic) && producer.getApp().equals(app)) {
                return producer;
            }
        }
        return null;
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        for (Consumer consumer : allMetadata.getConsumers()) {
            if (consumer.getTopic().equals(topic) && consumer.getApp().equals(app)) {
                return consumer;
            }
        }
        return null;
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        Map<TopicName, TopicConfig> result = Maps.newHashMap();
        if (subscribe.equals(Subscription.Type.CONSUMPTION)) {
            for (Consumer consumer : allMetadata.getConsumers()) {
                if (consumer.getApp().equals(subscribeApp)) {
                    result.put(consumer.getTopic(), allMetadata.getTopics().get(consumer.getTopic()));
                }
            }
        } else if (subscribe.equals(Subscription.Type.PRODUCTION)) {
            for (Producer producer : allMetadata.getProducers()) {
                if (producer.getApp().equals(subscribeApp)) {
                    result.put(producer.getTopic(), allMetadata.getTopics().get(producer.getTopic()));
                }
            }
        }
        return result;
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        for (DataCenter dataCenter : allMetadata.getDataCenters()) {
            if (dataCenter.getUrl().equals(ip)) {
                return dataCenter;
            }
        }
        return null;
    }

    @Override
    public String getConfig(String group, String key) {
        for (Config config : allMetadata.getConfigs()) {
            if (config.getGroup().equals(group) && config.getKey().equals(key)) {
                return config.getValue();
            }
        }
        return null;
    }

    @Override
    public List<Config> getAllConfigs() {
        return allMetadata.getConfigs();
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        return Lists.newArrayList(allMetadata.getBrokers().values());
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        List<Consumer> result = Lists.newArrayList();
        for (Consumer consumer : allMetadata.getConsumers()) {
            if (consumer.getTopic().equals(topic)) {
                result.add(consumer);
            }
        }
        return result;
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        List<Producer> result = Lists.newArrayList();
        for (Producer producer : allMetadata.getProducers()) {
            if (producer.getTopic().equals(topic)) {
                result.add(producer);
            }
        }
        return result;
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        List<Replica> result = Lists.newArrayList();
        return result;
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        return null;
    }

    @Override
    public AllMetadata getAllMetadata() {
        return allMetadata;
    }

    @Override
    public void addListener(EventListener<NameServerEvent> listener) {
        eventBus.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<NameServerEvent> listener) {
        eventBus.removeListener(listener);
    }

    @Override
    public void addEvent(NameServerEvent event) {
        eventBus.inform(event);
    }
}