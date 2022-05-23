package own.stu.redis.simpleredislock.aldi.common;

import org.springframework.stereotype.Component;
import own.stu.redis.simpleredislock.aldi.model.OrderDeliveryTimeOption;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderNumThresholdContext {

    private static ThreadLocal<Map<Long, OrderDeliveryTimeOption>> mapThreadLocal = ThreadLocal.withInitial(() -> new HashMap<>());

    //获取当前线程的存的变量
    public OrderDeliveryTimeOption get(Long thresholdId) {
        Map<Long, OrderDeliveryTimeOption> dataMap = mapThreadLocal.get();
        return dataMap.get(thresholdId);
    }

    //设置当前线程的存的变量
    public void set(Long thresholdId, OrderDeliveryTimeOption data) {
        Map<Long, OrderDeliveryTimeOption> dataMap = this.mapThreadLocal.get();
        dataMap.put(thresholdId, data);
    }

    //清空当前线程的变量信息，并设置当前线程的存的变量
    public void reset(Long thresholdId, OrderDeliveryTimeOption data) {
        remove();
        set(thresholdId, data);
    }

    //移除当前线程的存的变量
    public void remove() {
        this.mapThreadLocal.remove();
    }

}
