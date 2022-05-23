package own.stu.redis.simpleredislock.aldi.service;

import org.springframework.stereotype.Component;

@Component
public class OrderCommunityCountFlagContext {

    private static ThreadLocal<Boolean> mapThreadLocal = ThreadLocal.withInitial(() -> false);

    //获取当前线程的存的变量
    public Boolean get() {
        return mapThreadLocal.get();
    }

    //设置当前线程的存的变量
    public void set(Boolean countFlag) {
        mapThreadLocal.set(countFlag);
    }

    //清空当前线程的变量信息，并设置当前线程的存的变量
    public void reset(Boolean countFlag) {
        remove();
        set(countFlag);
    }

    //移除当前线程的存的变量
    public void remove() {
        this.mapThreadLocal.remove();
    }

}