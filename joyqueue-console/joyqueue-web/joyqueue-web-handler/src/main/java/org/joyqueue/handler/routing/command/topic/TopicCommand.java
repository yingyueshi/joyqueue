/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.routing.command.topic;

import com.google.common.base.Preconditions;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.domain.TopicName;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.*;
import org.joyqueue.model.keyword.TopicKeyword;
import org.joyqueue.model.query.QTopic;
import org.joyqueue.service.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;

/**
 * 主题 处理器
 * Created by chenyanying3 on 2018-10-18.
 */
public class TopicCommand extends NsrCommandSupport<Topic, TopicService, QTopic> {
    @Value(nullable = false)
    protected BrokerService brokerService;
    @Value(nullable = false)
    protected TopicPartitionGroupService topicPartitionGroupService;
    @Value(nullable = false)
    protected ConsumerService consumerService;
    @Value(nullable = false)
    protected ProducerService producerService;
    @Value
    protected DataCenterService dataCenterService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QTopic> qPageQuery) throws Exception {
        PageResult<Topic> result = service.search(qPageQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("addWithBrokerGroup")
    public Response addWithBrokerGroup(@Body Topic topic) throws Exception {
        //参数校验
        if (topic == null || topic.getBrokerGroup() == null || topic.getBrokers() == null || topic.getBrokers().isEmpty()) {
            new ConfigException(ErrorCode.BadRequest);
        }
        if (topic.getReplica() > topic.getBrokers().size()) topic.setReplica(topic.getBrokers().size());
        service.addWithBrokerGroup(topic);
        return Responses.success(topic);
    }

    @Path("delete")
    public Response delete(@Body(type = Body.BodyType.TEXT) String id) throws Exception {
        return super.delete(id);
    }

    @Path("/api/mqtt/topic/add")
    public Response addTopic(@Body Topic topic) throws Exception {
        //参数校验
        if (topic == null || topic.getBrokerGroup() == null) {
            new ConfigException(ErrorCode.BadRequest);
        }
        //新建主题
        List<Broker> brokerList = brokerService.findByGroup(topic.getBrokerGroup().getId());
        Preconditions.checkArgument(null == brokerList || brokerList.size() < 1, topic.getBrokerGroup().getCode() + "分组暂时无可用broker");
        topic.setBrokers(brokerList);
        if (topic.getReplica() > brokerList.size()) topic.setReplica(brokerList.size());
        service.addWithBrokerGroup(topic);
        return Responses.success(topic);
    }

    @Path("searchUnsubscribed")
    public Response searchUnsubscribed(@PageQuery QPageQuery<QTopic> qPageQuery) throws Exception {
        QTopic qTopic = qPageQuery.getQuery();
        if (qTopic == null) {
            throw new ConfigException(ErrorCode.BadRequest);
        }
        qTopic.setUserId(Long.valueOf(String.valueOf(session.getId())));
        qTopic.setAdmin(session.getRole() == User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
        qTopic.setKeyword(qTopic.getKeyword() == null ? null : qTopic.getKeyword().trim());
        PageResult<AppUnsubscribedTopic> result = service.findAppUnsubscribedByQuery(qPageQuery);

        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("getById")
    public Response getById(@Body Topic model) {
        try {
            Topic topic = service.findById(model.getId());
            List<Broker> brokers = brokerService.findByTopic(topic.getId());
            if (CollectionUtils.isEmpty(brokers)) {
                brokers = Collections.emptyList();
            }
            List<String> centers = dataCenterService.findByIps(brokers.stream().map(Broker::getIp).collect(Collectors.toList()))
                    .stream().map(DataCenter::getName).distinct().sorted(String::compareTo).collect(Collectors.toList());
            topic.setDataCenters(centers);
            return Responses.success(topic);
        } catch (Exception e) {
            return Responses.error(e);
        }
    }

    @Path("findByBroker")
    public Response findByBroker(@Body @PageQuery QPageQuery<TopicKeyword> qTopic) {
        try {
            if (StringUtils.isEmpty(qTopic.getQuery().getBrokerId())) {
                return Responses.error(HTTP_BAD_REQUEST, HTTP_BAD_REQUEST, "BrokerId不能为空！");
            }
            PageResult<TopicName> result = new PageResult<>();
            result.setPagination(qTopic.getPagination());
            String brokerId = qTopic.getQuery().getBrokerId();
            String keyWord = qTopic.getQuery().getKeyword();
            List<TopicName> topicNames = service.findTopic(brokerId).stream().filter(topicName ->
                    topicName.getCode().contains(keyWord)).collect(Collectors.toList());
            int start;
            int end;
            int page;

            if (topicNames.size() <= qTopic.getPagination().getStart()) {
                start = 0;
                end = Math.min(topicNames.size(), qTopic.getPagination().getSize());
                page = 1;
            } else {
                start = qTopic.getPagination().getStart();
                end = Math.min(topicNames.size(), qTopic.getPagination().getStart() + qTopic.getPagination().getSize());
                page = qTopic.getPagination().getPage();
            }
            result.getPagination().setStart(start);
            result.getPagination().setPage(page);
            result.getPagination().setTotalRecord(topicNames.size());
            List<TopicName> pageTopics = topicNames.subList(start, end);
            result.setResult(pageTopics);
            return Responses.success(result);
        } catch (Exception e) {
            return Responses.error(e);
        }
    }

}
