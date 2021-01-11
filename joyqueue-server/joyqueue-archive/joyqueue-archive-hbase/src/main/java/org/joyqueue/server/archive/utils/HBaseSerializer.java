package org.joyqueue.server.archive.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.joyqueue.server.archive.store.query.QueryCondition;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class HBaseSerializer {
    public static FilterList createFilterList(QueryCondition queryCondition) {
        FilterList filters = new FilterList();
        filters.addFilter(new PageFilter(queryCondition.getCount()));
        /*Filter filter = createFilter(queryCondition.getStartRowKey(), createRowKey(0, queryCondition.getStartRowKey()));
        if (filter != null) {
            filters.addFilter(filter);
        }*/

        return filters;
    }

    private static Filter createFilter(QueryCondition.RowKey rowKey, byte[] startRowKey) {
        // salt(1) + topic(16) + sendTime(8) + businessId(16) + messageId(16) 总长度：57
        String businessId = rowKey.getBusinessId();
        if (StringUtils.isNotEmpty(businessId)) {
            List<Pair<byte[], byte[]>> fuzzyKeysData = new LinkedList<>();
            org.apache.hadoop.hbase.util.Pair<byte[], byte[]> pair = new org.apache.hadoop.hbase.util.Pair<>();

            // salt
            startRowKey[0] = Bytes.toBytes("?")[0];
            // 时间任意
            for (int i = 17; i < 25; i++) {
                startRowKey[i] = Bytes.toBytes("?")[0];
            }
            // messageId任意
            for (int i = 41; i < 57; i++) {
                startRowKey[i] = Bytes.toBytes("?")[0];
            }

            pair.setFirst(startRowKey);

            byte fixed = 0x0; //必须匹配
            byte unFixed = 0x1; //不用匹配

            ByteBuffer allocate = ByteBuffer.allocate(57);
            allocate.put(unFixed);
            for (int i = 0; i < 16; i++) {
                allocate.put(fixed);
            }
            for (int i = 0; i < 8; i++) {
                allocate.put(unFixed);
            }
            for (int i = 0; i < 16; i++) {
                allocate.put(fixed);
            }
            for (int i = 0; i < 16; i++) {
                allocate.put(unFixed);
            }

            pair.setSecond(allocate.array());

            fuzzyKeysData.add(pair);

            Filter filter = new FuzzyRowFilter(fuzzyKeysData);
            return filter;
        }
        return null;
    }

    public static byte[] toBytes(String content) {
        return Bytes.toBytes(content);
    }

    public static byte[] toBytes(long value) {
        return Bytes.toBytes(value);
    }

    public static long toLong(byte[] bytes) {
        return Bytes.toLong(bytes);
    }
}
