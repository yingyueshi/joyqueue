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
package org.joyqueue.broker.helper;

import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.transport.Transport;

/**
 * session帮助类
 *
 * author: gaohaoxiang
 * date: 2018/9/10
 */
public class SessionHelper {

    public static void setConnection(Transport transport, Connection connection) {
        transport.attr().set(SessionManager.CONNECTION_KEY, connection);
    }

    public static boolean putIfAbsentConnection(Transport transport, Connection connection) {
        return transport.attr().putIfAbsent(SessionManager.CONNECTION_KEY, connection) == null;
    }

    public static Connection getConnection(Transport transport) {
        return transport.attr().get(SessionManager.CONNECTION_KEY);
    }
}