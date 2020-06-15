package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import com.jd.joyqueue.broker.jmq2.command.ResetConsumeOffset;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
@Deprecated
public class ResetConsumeHandler implements JMQ2CommandHandler, Type, BrokerContextAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Consume consume;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ResetConsumeOffset resetConsumeOffset = (ResetConsumeOffset) command.getPayload();

        String app = resetConsumeOffset.getApp();
        String topic = resetConsumeOffset.getTopic();

        try {
            this.consume.resetPullIndex(topic, app);
        } catch (JoyQueueException e) {
            logger.info("Reset Pull index error[{}]", e);
            return BooleanAck.build(JoyQueueCode.CONSUME_POSITION_UPDATE_ERROR, "");
        }

        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JMQ2CommandType.RESET_CONSUMER_OFFSET.getCode();
    }
}