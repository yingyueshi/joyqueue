package org.joyqueue.server.archive.store.utils;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.server.archive.store.model.SendLog;
import org.joyqueue.server.archive.store.query.QueryCondition;
import org.joyqueue.toolkit.lang.Pair;
import org.joyqueue.toolkit.security.Md5;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class ArchiveSerializer {
    private static ByteBufferPool byteBufferPool = new ByteBufferPool();

    public static void release(ByteBuffer byteBuffer) {
        byteBufferPool.release(byteBuffer);
    }

    /**
     * from jetty
     * <p>
     * ByteBuffer池
     */
    static class ByteBufferPool {

        private final ConcurrentMap<Integer, Queue<ByteBuffer>> directBuffers = new ConcurrentHashMap<>();
        private final ConcurrentMap<Integer, Queue<ByteBuffer>> heapBuffers = new ConcurrentHashMap<>();
        private final int factor;

        ByteBufferPool() {
            this(1024);
        }

        ByteBufferPool(int factor) {
            this.factor = factor;
        }

        public ByteBuffer acquire(int size, boolean direct) {
            int bucket = bucketFor(size);
            ConcurrentMap<Integer, Queue<ByteBuffer>> buffers = buffersFor(direct);

            ByteBuffer result = null;
            Queue<ByteBuffer> byteBuffers = buffers.get(bucket);
            if (byteBuffers != null) {
                result = byteBuffers.poll();
            }

            if (result == null) {
                int capacity = bucket * factor;
                result = newByteBuffer(capacity, direct);
            }

            result.clear();
            return result;
        }

        protected ByteBuffer newByteBuffer(int capacity, boolean direct) {
            return direct ? ByteBuffer.allocateDirect(capacity)
                    : ByteBuffer.allocate(capacity);
        }

        public void release(ByteBuffer buffer) {
            if (buffer == null) {
                return;
            }

            int bucket = bucketFor(buffer.capacity());
            ConcurrentMap<Integer, Queue<ByteBuffer>> buffers = buffersFor(buffer.isDirect());

            Queue<ByteBuffer> byteBuffers = buffers.get(bucket);
            if (byteBuffers == null) {
                byteBuffers = new ConcurrentLinkedQueue<>();
                Queue<ByteBuffer> existing = buffers.putIfAbsent(bucket, byteBuffers);
                if (existing != null) {
                    byteBuffers = existing;
                }
            }

            buffer.clear();
            byteBuffers.offer(buffer);
        }

        public void clear() {
            directBuffers.clear();
            heapBuffers.clear();
        }

        private int bucketFor(int size) {
            int bucket = size / factor;
            if (size % factor > 0) {
                ++bucket;
            }
            return bucket;
        }

        ConcurrentMap<Integer, Queue<ByteBuffer>> buffersFor(boolean direct) {
            return direct ? directBuffers : heapBuffers;
        }

    }

    /**
     * Message unique id
     **/
    public static String messageId(String topic, short partition, long messageIndex) {
        return topic + partition + messageIndex;
    }

    /**
     * MD5 for content with key
     **/
    public static byte[] md5(String content, byte[] key) throws GeneralSecurityException {
        return Md5.INSTANCE.encrypt(content.getBytes(Charset.forName("utf-8")), key);
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static byte[] reverse(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return byteArray;
        }
        byte[] reverseArray = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            reverseArray[i] = byteArray[byteArray.length - i - 1];
        }
        return reverseArray;
    }

    public static byte[] reverse(ByteBuffer buffer) {
        return reverse(buffer.array());
    }

    public static String reverse(String reverseStr) {
        return new StringBuffer(reverseStr).reverse().toString();
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] array) {
        return array[0] & 255 | (array[1] & 255) << 8 | (array[2] & 255) << 16 | (array[3] & 255) << 24;
    }

    public static byte intToByte(int i) {
        byte[] result = new byte[1];
        result[0] = (byte)(i & 0xFF);
        return result[0];
    }

    public static String Byte2String(byte nByte) {
        StringBuilder nStr = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            int j = (int) nByte & (int) (Math.pow(2, (double) i));
            if (j > 0) {
                nStr.append("1");
            } else {
                nStr.append("0");
            }
        }
        return nStr.toString();
    }

    public static int generateMurmurHash(String seed) {
        return Hashing.murmur3_32().hashString(seed, Charset.forName("utf-8")).hashCode();
    }

    public static byte[] xorOperation(byte[] source1, byte[] source2) {
        if (source1.length != source2.length) {
            return null;
        }
        byte[] xorResult = new byte[source1.length];
        for (int i = 0; i < source1.length; i++) {
            xorResult[i] = (byte) (source1[i] ^ source2[i]);
        }
        return xorResult;
    }

    public static class ProduceArchiveSerializer {
        public static final int MAGIC_SALT = 16;
        public static final int MAGIC_SALT_START = 0;
        public static final int MAGIC_SALT_STOP = 15;

        /**
         * randomkey:   salt(1) + topic(16) + sendTime(8) + businessId(16) + messageId(16) 总长度：57
         * businesskey: (topic xor businessId)(16) + businessId(16) + sendTime(8) + messageId(16) 总长度：56
         * value: brokerId(4) + topic(变长) + app(变长) + clientIp(16) + compassType(2) + messageBody(变长) + businessId(变长)
         *
         * @param sendLog
         * @return
         */
        public static Pair<Pair<byte[], byte[]>, Pair<byte[], byte[]>> convertSendLogToKVBytes(SendLog sendLog) throws GeneralSecurityException {
            final byte[] messageIdBytes = md5(sendLog.getMessageId(),null);

            //key: salt(1) + topic(16) + sendTime(8) + businessId(16) + messageId(16) 总长度：57
            ByteBuffer bufferKey = ByteBuffer.allocate(57);

            final String key = sendLog.getTopic() + sendLog.getSendTime() + sendLog.getBusinessId() + byteArrayToHexStr(messageIdBytes);
            final int hashcode = generateMurmurHash(key);
            final byte salt = intToByte(Math.abs(hashcode) % MAGIC_SALT);
            bufferKey.put(salt);
            bufferKey.put(md5(sendLog.getTopic(), null));
            bufferKey.putLong(sendLog.getSendTime());
            bufferKey.put(md5(sendLog.getBusinessId(), null));
            bufferKey.put(messageIdBytes);

            // key: (topic xor businessId)(16) + businessId(16) + sendTime(8) + messageId(16) 总长度：56
            ByteBuffer bizIdKey = ByteBuffer.allocate(56);
            final byte[] topicMd5 = md5(sendLog.getTopic(), null);
            final byte[] businessMd5 = md5(sendLog.getBusinessId(), null);
            final byte[] xorMd5 = xorOperation(topicMd5, businessMd5);
            bizIdKey.put(xorMd5);
            bizIdKey.put(businessMd5);
            bizIdKey.putLong(sendLog.getSendTime());
            bizIdKey.put(messageIdBytes);

            // value
            final byte[] topic = sendLog.getTopic().getBytes(Charset.forName("utf-8"));
            final byte[] app = sendLog.getApp().getBytes(Charset.forName("utf-8"));
            final byte[] messageBody = sendLog.getMessageBody();
            final byte[] businessIdBytes = sendLog.getBusinessId().getBytes(Charset.forName("utf-8"));
            final int size = 4 + (4 + topic.length) + (4 + app.length) + 16 + 2 + (4 + messageBody.length) + (4 + businessIdBytes.length);
            ByteBuffer bufferVal = ByteBuffer.allocate(size);
            bufferVal.putInt(sendLog.getBrokerId());

            bufferVal.putInt(topic.length);
            bufferVal.put(topic);
            bufferVal.putInt(app.length);
            bufferVal.put(app);

            // clientIP
            byte[] clientIpBytes16 = new byte[16];
            byte[] clientIpBytes = sendLog.getClientIp();
            System.arraycopy(clientIpBytes, 0, clientIpBytes16,0, Math.min(clientIpBytes.length, clientIpBytes16.length));
            bufferVal.put(clientIpBytes16);

            bufferVal.putShort(sendLog.getCompressType());

            bufferVal.putInt(messageBody.length);
            bufferVal.put(messageBody);
            bufferVal.putInt(businessIdBytes.length);
            bufferVal.put(businessIdBytes);

            return new Pair<>(new Pair<>(bufferKey.array(), bufferVal.array()),
                    new Pair<>(bizIdKey.array(), new byte[] {salt}));
        }

        public static SendLog readSendLog(Pair<byte[], byte[]> pair) {
            if (pair == null) {
                return new SendLog();
            }
            SendLog log = new SendLog();

            byte[] key = pair.getKey();
            ByteBuffer wrap = ByteBuffer.wrap(key);
            //salt
            wrap.get();
            // 主题（MD5后的）
            byte[] topicMD5 = new byte[16];
            wrap.get(topicMD5);
            // 发送时间
            log.setSendTime(wrap.getLong());
            // 业务主键（MD5后的）
            byte[] businessId = new byte[16];
            wrap.get(businessId);
            // 消息ID（MD5后的）
            byte[] messageId = new byte[16];
            wrap.get(messageId);
            log.setBytesMessageId(messageId);
            log.setMessageId(byteArrayToHexStr(messageId));

            //int size = 4 + (4 + topic.length) + (4 + app.length) + 16 + 2 + (4 + messageBody.length) + (4 + businessIdBytes.length);
            byte[] value = pair.getValue();
            ByteBuffer valWrap = ByteBuffer.wrap(value);
            // brokerID
            log.setBrokerId(valWrap.getInt());

            int topicLen = valWrap.getInt();
            byte[] topic = new byte[topicLen];
            valWrap.get(topic);
            log.setTopic(new String(topic, Charset.forName("utf-8")));

            int appLen = valWrap.getInt();
            byte[] app = new byte[appLen];
            valWrap.get(app);
            log.setApp(new String(app, Charset.forName("utf-8")));

            // 客户端IP
            byte[] clientIp = new byte[16];
            valWrap.get(clientIp);
            log.setClientIp(clientIp);
            // 压缩类型
            log.setCompressType(valWrap.getShort());
            // 消息体
            int msgBodySize = valWrap.getInt();
            byte[] messageBody = new byte[msgBodySize];
            valWrap.get(messageBody);
            log.setMessageBody(messageBody);
            // 业务主键
            int bizSize = valWrap.getInt();
            byte[] businessIdBytes = new byte[bizSize];
            valWrap.get(businessIdBytes);
            log.setBusinessId(new String(businessIdBytes, Charset.forName("utf-8")));

            return log;
        }

        public static byte[] convert4BizIdKey(Pair<byte[], byte[]> pair) {
            byte[] key = pair.getKey();
            ByteBuffer wrapKey = ByteBuffer.wrap(key);
            byte[] xorMd5 = new byte[16];
            wrapKey.get(xorMd5);
            byte[] businessId = new byte[16];
            wrapKey.get(businessId);
            long time = wrapKey.getLong();
            byte[] messageId = new byte[16];
            wrapKey.get(messageId);

            byte[] value = pair.getValue();
            ByteBuffer wrapValue = ByteBuffer.wrap(value);
            byte salt = wrapValue.get();

            // 1 + 16 + 8 + 16 + 16 = 57
            ByteBuffer allocate = ByteBuffer.allocate(57);
            allocate.put(salt);
            allocate.put(xorOperation(xorMd5, businessId));
            allocate.putLong(time);
            allocate.put(businessId);
            allocate.put(messageId);

            return allocate.array();
        }

        public static byte[] createRowKey(int salt, QueryCondition.RowKey rowKey) throws GeneralSecurityException {
            String topic = rowKey.getTopic();
            long crateTime = rowKey.getTime();
            String businessId = rowKey.getBusinessId();
            String messageId = rowKey.getMessageId();

            ByteBuffer allocate;

            if (StringUtils.isNotEmpty(businessId)) {
                // 16 + 16 + 8 + 16 = 56
                allocate = ByteBuffer.allocate(56);

                final byte[] topicMd5 = md5(topic, null);
                final byte[] businessMd5 = md5(businessId, null);
                allocate.put(xorOperation(topicMd5, businessMd5));
                allocate.put(businessMd5);
                allocate.putLong(crateTime);
                if (messageId != null) {
                    allocate.put(new BigInteger(messageId, 16).toByteArray());
                } else {
                    // 没有messageId填充16个字节
                    allocate.put(new byte[16]);
                }
            } else {
                // 1 + 16 + 8 + 16 + 16 = 57
                allocate = ByteBuffer.allocate(57);

                allocate.put(intToByte(salt));
                allocate.put(md5(topic, null));
                allocate.putLong(crateTime);
                // 没有businessId填充16个字节
                allocate.put(new byte[16]);
                if (messageId != null) {
                    allocate.put(new BigInteger(messageId, 16).toByteArray());
                } else {
                    // 没有messageId填充16个字节
                    allocate.put(new byte[16]);
                }
            }

            return allocate.array();
        }

        public static byte[] createRowKey(byte salt, QueryCondition.RowKey rowKey) throws GeneralSecurityException {
            String topic = rowKey.getTopic();
            long crateTime = rowKey.getTime();
            String businessId = rowKey.getBusinessId();
            String messageId = rowKey.getMessageId();

            ByteBuffer allocate;

            if (StringUtils.isNotEmpty(businessId)) {
                // 16 + 16 + 8 + 16 = 56
                allocate = ByteBuffer.allocate(56);

                final byte[] topicMd5 = md5(topic, null);
                final byte[] businessMd5 = md5(businessId, null);
                allocate.put(xorOperation(topicMd5, businessMd5));
                allocate.put(businessMd5);
                allocate.putLong(crateTime);
                if (messageId != null) {
                    allocate.put(new BigInteger(messageId, 16).toByteArray());
                } else {
                    // 没有messageId填充16个字节
                    allocate.put(new byte[16]);
                }
            } else {
                // 1 + 16 + 8 + 16 + 16 = 57
                allocate = ByteBuffer.allocate(57);

                allocate.put(salt);
                allocate.put(md5(topic, null));
                allocate.putLong(crateTime);
                // 没有businessId填充16个字节
                allocate.put(new byte[16]);
                if (messageId != null) {
                    allocate.put(new BigInteger(messageId, 16).toByteArray());
                } else {
                    // 没有messageId填充16个字节
                    allocate.put(new byte[16]);
                }
            }

            return allocate.array();
        }

        public static byte[] bytesRowKey(QueryCondition.RowKey rowKey) throws GeneralSecurityException {
            String key = rowKey.getTopic() + rowKey.getTime() + rowKey.getBusinessId() + rowKey.getMessageId();
            int hashcode = generateMurmurHash(key);
            // 1 + 16 + 8 + 16 + 16 = 57
            ByteBuffer allocate = ByteBuffer.allocate(57);
            allocate.put(intToByte(Math.abs(hashcode) % MAGIC_SALT));
            allocate.put(md5(rowKey.getTopic(), null));
            allocate.putLong(rowKey.getTime());
            allocate.put(md5(rowKey.getBusinessId(), null));
            allocate.put(hexStrToByteArray(rowKey.getMessageId()));
            // rowKey
            byte[] bytesRowKey = allocate.array();
            return bytesRowKey;
        }

        public static int saltByRowKey(QueryCondition.RowKey rowKey) {
            final String key = rowKey.getTopic() + rowKey.getTime() + rowKey.getBusinessId() + rowKey.getMessageId();
            final int hashcode = generateMurmurHash(key);
            return Math.abs(hashcode) % MAGIC_SALT;
        }
    }

    public static class ConsumeArchiveSerializer {

        public static ByteBuffer writeConsumeLog(ConsumeLog consumeLog) {
            int size = consumeLogSize(consumeLog);
            ByteBuffer buffer = byteBufferPool.acquire(/* 4 byte len */ 4 + size, false); // 4个字节长度
            buffer.putInt(size);
            buffer.put(consumeLog.getBytesMessageId());
            buffer.putInt(consumeLog.getBrokerId());

            byte[] clientIpBytes16 = new byte[16];
            byte[] clientIpBytes = consumeLog.getClientIp();
            System.arraycopy(clientIpBytes, 0, clientIpBytes16, 0, Math.min(clientIpBytes.length, clientIpBytes16.length));
            buffer.put(clientIpBytes16);

            buffer.putLong(consumeLog.getConsumeTime());

            byte[] appBytes = consumeLog.getApp().getBytes(Charset.forName("utf-8"));
            buffer.putShort((short) appBytes.length);
            buffer.put(appBytes);

            buffer.flip();

            return buffer;
        }

        public static int consumeLogSize(ConsumeLog consumeLog) {
            int size = 0;
            // messageId
            size += consumeLog.getBytesMessageId().length;
            // brokerId
            size += 4;
            // clientIp
            size += 16;
            // consumeTime
            size += 8;
            // app长度
            size += 2;
            // app
            size += consumeLog.getApp().getBytes(Charset.forName("utf-8")).length;
            return size;
        }

        public static ConsumeLog readConsumeLog(Pair<byte[], byte[]> pair) {
            ConsumeLog log = new ConsumeLog();

            byte[] key = pair.getKey();
            ByteBuffer wrap = ByteBuffer.wrap(key);

            byte[] messageId = new byte[16];
            wrap.get(messageId);
            log.setBytesMessageId(messageId);

            int appId = wrap.getInt();
            log.setAppId(appId);

            byte[] value = pair.getValue();
            ByteBuffer valBF = ByteBuffer.wrap(value);

            log.setBrokerId(valBF.getInt());

            byte[] clientIp = new byte[16];
            valBF.get(clientIp);
            log.setClientIp(clientIp);

            log.setConsumeTime(valBF.getLong());

            return log;
        }

        public static ConsumeLog readConsumeLog(ByteBuffer buffer) {
            ConsumeLog log = new ConsumeLog();

            byte[] byteMessageId = new byte[16];
            buffer.get(byteMessageId);
            log.setBytesMessageId(byteMessageId);

            log.setBrokerId(buffer.getInt());

            byte[] clientIp = new byte[16];
            buffer.get(clientIp);
            log.setClientIp(clientIp);

            log.setConsumeTime(buffer.getLong());

            int appLen = (int) buffer.getShort();
            byte[] appBytes = new byte[appLen];
            buffer.get(appBytes);
            log.setApp(new String(appBytes, Charset.forName("utf-8")));

            return log;
        }

        public static Pair<byte[], byte[]> convertConsumeLogToKVBytes(ConsumeLog consumeLog) {
            ByteBuffer buffer = ByteBuffer.allocate(ConsumeLog.len);
            buffer.put(consumeLog.getBytesMessageId());
            buffer.putInt(consumeLog.getAppId());
            buffer.putInt(consumeLog.getBrokerId());

            byte[] clientIpBytes16 = new byte[16];
            byte[] clientIpBytes = consumeLog.getClientIp();
            System.arraycopy(clientIpBytes, 0, clientIpBytes16, 0, Math.min(clientIpBytes.length, clientIpBytes16.length));
            buffer.put(clientIpBytes16);

            buffer.putLong(consumeLog.getConsumeTime());
            buffer.flip();

            byte[] key = new byte[ConsumeLog.keyLen];
            buffer.get(key);

            byte[] value = new byte[ConsumeLog.valLen];
            buffer.get(value);

            return new Pair<>(key, value);
        }
    }
}
