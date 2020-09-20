package own.stu.highConcurrence.disruptor.common;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyHandlerException implements ExceptionHandler {

    /*
     * (non-Javadoc) 运行过程中发生时的异常
     *
     * @see
     * com.lmax.disruptor.ExceptionHandler#handleEventException(java.lang.Throwable
     * , long, java.lang.Object)
     */
    public void handleEventException(Throwable ex, long sequence, Object event) {
        ex.printStackTrace();
        log.error("process data error sequence ==[{}] event==[{}] ,ex ==[{}]", sequence, event.toString(), ex.getMessage());
    }

    /*
     * (non-Javadoc) 启动时的异常
     *
     * @see
     * com.lmax.disruptor.ExceptionHandler#handleOnStartException(java.lang.
     * Throwable)
     */
    public void handleOnStartException(Throwable ex) {
        log.error("start disruptor error ==[{}]!", ex.getMessage());
    }

    /*
     * (non-Javadoc) 关闭时的异常
     *
     * @see
     * com.lmax.disruptor.ExceptionHandler#handleOnShutdownException(java.lang
     * .Throwable)
     */
    public void handleOnShutdownException(Throwable ex) {
        log.error("shutdown disruptor error ==[{}]!", ex.getMessage());
    }
}
 
