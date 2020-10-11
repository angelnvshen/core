package own.jdk.multiThreadInAction.chapter4.demo2;

import lombok.Data;
import lombok.NoArgsConstructor;
import own.jdk.multiThreadInAction.util.Tools;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RecordProcessor implements StatProcessor {

    private final Map<Long, DelayItem> summaryResult;
    private static final FastTimeStampParser FAST_TIMESTAMP_PARSER = new FastTimeStampParser();
    private static final DecimalFormat df = new DecimalFormat("0000");

    private static final int INDEX_TIMESTAMP = 0;
    private static final int INDEX_TRACE_ID = 7;
    private static final int INDEX_MESSAGE_TYPE = 2;
    private static final int INDEX_OPERATION_NAME = 4;
    private static final int SRC_DEVICE = 5;
    private static final int DEST_DEVICE = 6;

    private static final int FIELDS_COUNT = 11;

    private final Map<String, DelayData> immediateResult;

    private final int traceIdDiff;
    private final String expectedOperationName;
    private String selfDevice = "ESB";

    private long currRecordDate;

    // 采样周期，单位：s
    private final int sampleInterval;
    private final String expectedExternalDeviceList;

    public RecordProcessor(int sampleInterval, int traceIdDiff,
                           String expectedOperationName, String expectedExternalDeviceList) {
        summaryResult = new TreeMap<Long, DelayItem>();

        this.immediateResult = new HashMap<String, DelayData>();
        this.sampleInterval = sampleInterval;
        this.traceIdDiff = traceIdDiff;
        this.expectedOperationName = expectedOperationName;
        this.expectedExternalDeviceList = expectedExternalDeviceList;
    }

    @Override
    public void process(String record) {
        String[] recordParts = filterRecord(record);
        if (recordParts == null || recordParts.length == 0) {
            return;
        }

        process(recordParts);
    }

    private String[] filterRecord(String record) {
        String[] recordParts = new String[FIELDS_COUNT];
        Tools.split(record, recordParts, '|');
        if (recordParts.length < 7) {
            return null;
        }

        String recordType = recordParts[INDEX_MESSAGE_TYPE];
        String operationName = recordParts[INDEX_OPERATION_NAME];
        String srcDevice = recordParts[SRC_DEVICE];
        String destDevice = recordParts[DEST_DEVICE];

        if ("response".equals(recordType)) {
            operationName = operationName.substring(0,
                    operationName.length() - "Rsp".length());
            recordParts[INDEX_OPERATION_NAME] = operationName;
        }

        if (!expectedOperationName.equals(operationName)) {
            recordParts = null;
        }

        if ("*".equals(expectedExternalDeviceList)) {
            if ("request".equals(recordType)) {
                if (!selfDevice.equals(srcDevice)) {
                    recordParts = null;
                }
            } else {
                if (!selfDevice.equals(destDevice)) {
                    recordParts = null;
                }
            }
        } else {
            if ("request".equals(recordType)) {
                // 仅考虑表示当前设备发送给指定列表中的其他设备的请求记录
                if (!(selfDevice.equals(srcDevice) && expectedExternalDeviceList
                        .contains(destDevice))) {
                    recordParts = null;
                }
            } else {
                // 仅考虑表示指定列表中的其他设备发生给读取设备的响应记录
                if (!(selfDevice.equals(destDevice) && expectedExternalDeviceList
                        .contains(srcDevice))) {
                    recordParts = null;
                }
            }
        }

        return recordParts;
    }

    public void process(String[] recordParts) {
        String traceId;
        String matchingReqTraceId;
        String recordType;
        String interfaceName;
        String operationName;
        String timeStamp;
        String strRspTimeStamp;
        String strReqTimeStamp;
        DelayData delayData;

        traceId = recordParts[INDEX_TRACE_ID];
        recordType = recordParts[INDEX_MESSAGE_TYPE];
        timeStamp = recordParts[INDEX_TIMESTAMP];

        if ("response".equals(recordType)) {
            int nonSeqLen = traceId.length() - 4;
            String traceIdSeq = traceId.substring(nonSeqLen);

            // 获取这条响应记录相应的请求记录中的traceId
            matchingReqTraceId = traceId.substring(0, nonSeqLen)
                    + df.format(Integer.valueOf(traceIdSeq).intValue()
                    - Integer.valueOf(traceIdDiff).intValue());

            delayData = immediateResult.remove(matchingReqTraceId);
            if (null == delayData) {
                // 不可能到这里，除非日志记录有错误
                return;
            }

            delayData.setRspTime(timeStamp);
            strRspTimeStamp = timeStamp;
            strReqTimeStamp = delayData.getReqTime();

            // 仅在读取到表示相应的请求记录时才统计数据
            long reqTimeStamp = parseTimeStamp(strReqTimeStamp);
            long rspTimeStamp = parseTimeStamp(strRspTimeStamp);
            long delay = rspTimeStamp - reqTimeStamp;
            DelayItem delayStatData;

            if (reqTimeStamp - currRecordDate < sampleInterval * 1000) {
                delayStatData = summaryResult.get(currRecordDate);
            } else {
                currRecordDate = reqTimeStamp;
                delayStatData = new DelayItem(currRecordDate);
//                delayStatData.getTotalDelay().addAndGet(delay);
                summaryResult.put(currRecordDate, delayStatData);
            }

            delayStatData.getSampleCount().incrementAndGet();
            delayStatData.getTotalDelay().addAndGet(delay);
        } else {
            // 记录请求数据
            delayData = new DelayData();
            delayData.setTraceId(traceId);
            delayData.setReqTime(timeStamp);

            interfaceName = recordParts[1];
            operationName = recordParts[INDEX_OPERATION_NAME];
            delayData.setOperationName(interfaceName + '.' + operationName);
            immediateResult.put(traceId, delayData);
        }
    }

    private static long parseTimeStamp(String timeStamp) {
        String[] parts = new String[2];
        Tools.split(timeStamp, parts, '.');

        long part1 = FAST_TIMESTAMP_PARSER.parseTimeStamp(parts[0]);
        String millisecond = parts[1];
        int part2 = 0;
        if (null != millisecond) {
            part2 = Integer.valueOf(millisecond);
        }

        return part1 + part2;
    }

    @Override
    public Map<Long, DelayItem> getResult() {
        return summaryResult;
    }

    //    @Data
    @NoArgsConstructor
    class DelayData {
        private String traceId;
        private String operationName;
        private String reqTime;
        private String rspTime;

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getReqTime() {
            return reqTime;
        }

        public void setReqTime(String reqTime) {
            this.reqTime = reqTime;
        }

        public String getRspTime() {
            return rspTime;
        }

        public void setRspTime(String rspTime) {
            this.rspTime = rspTime;
        }
    }

    public static void main(String[] args) {

        // 一对请求与响应之间的消息唯一标识的后3位值之差
        int traceIdDiff;
        // 待统计的操作名称
        String expectedOperationName;
        // 可选参数：采样周期（单位：秒）
        int sampleInterval;
        /*
         * 可选参数：指定一个以逗号分割的列表，仅发送给该列表中的设备的请求才会被统计在内。 默认值"*"表示不对外部设备做要求。
         */
        String expectedExternalDeviceList;

        traceIdDiff = 3;
        expectedOperationName = "sendSms";
        sampleInterval = 10;
        expectedExternalDeviceList = "*";
        StatProcessor processor = new RecordProcessor(sampleInterval, traceIdDiff, expectedOperationName, expectedExternalDeviceList);
//        String data = "2016-03-30 08:48:16.569|REST|request|Location|getLocation|OSG|ESB|00200000791|192.168.1.102|13612345678|136712345670";
        String data = "2016-03-30 08:47:56.654|SOAP|request|SMS|sendSms|ESB|NIG|00210000001|192.168.1.102|13612345678|136712345670";
        processor.process(data);

        data = "2016-03-30 08:47:56.677|SOAP|response|SMS|sendSmsRsp|NIG|ESB|00210000004|192.168.1.102|13612345678|136712345670";
        processor.process(data);
        System.out.println(processor.getResult());
    }
}
