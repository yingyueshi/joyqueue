/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.joyqueue0.network.codec;

import com.google.common.collect.Lists;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.PutRetry;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

import java.util.List;

/**
 * putRetry
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class PutRetryCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        PutRetry payload = new PutRetry();
        payload.setTopic(Serializer.readString(buffer, 2));
        payload.setApp(Serializer.readString(buffer, 2));
        payload.setException(Serializer.readString(buffer, 2));

        int count = buffer.readShort();
        List<BrokerMessage> messages = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            messages.add(Serializer.readBrokerMessage(buffer));
        }

        payload.setMessages(messages);
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.PUT_RETRY.getCode();
    }
}