package own.stu.redis.logAop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 使用@Aspect注解声明一个切面
@Aspect
// @Component表明此类交给了Spring管理  
@Component
public class AopAspect {
    //注入我们需要的业务对象
//    @Autowired
//    private LogService logService;

    /**
     * 这里我们使用注解的形式
     * 当然，我们也可以通过切点表达式直接指定需要拦截的package,需要拦截的class 以及 method
     * 切点表达式:   execution(...)确定作用的方法的范围
     */
    //我们使用的是关联到上面的注解类中，例如：com.test.Annotation
    //@Pointcut("@annotation(包名.类名)")
    @Pointcut("@annotation(LogAnnotation)")
    public void pointCut() {
    }

    /**
     * 环绕通知 @Around  ， 当然也可以使用 @Before (前置通知)  @After (后置通知)
     *
     * @param point
     * @return
     * @throws Throwable
     */
     //@Around注解后添加切点方法名，进行关联
    @Around("pointCut()")
    //参数ProceedingJoinPoint的作用：
    //其他切面编程使用JoinPoint就可以了，ProceedingJoinPoint继承了JoinPoint类，增加了proceed方法。
    //环绕通知=前置+目标方法执行+后置通知，proceed方法就是用于启动目标方法执行的
    public Object around(ProceedingJoinPoint point) throws Throwable {

        System.out.println(" ===== around ======= ");
        //获取方法签名(通过此签名获取目标方法信息)
        MethodSignature ms=(MethodSignature)point.getSignature();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start(ms.getName());

        //执行目标方法，获得目标方法的返回值
        try {

            Object result = point.proceed();
            /*try {
            //方法执行后进行log保存的方法调用
            //同类中不走代理，需要创建代理对象
                        SysLogAspect logAspect = (SysLogAspect) AopContext.currentProxy();
            logAspect.saveLog(point, time, result);
        } catch (Exception e) {
        }*/
            return result;
        }finally {
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        }
    }

    @AfterThrowing(value = "pointCut()", throwing = "ex")
    public void afterThrowing(Throwable ex) throws Throwable {
        System.out.println(" ===== afterThrowing ======= ");
        // TODO 方法发生了异常，怎么处理？ 是否加日志
        System.out.println("afterThrowing : ======" + ex.getMessage());
    }

    /**
     * 保存日志
     *
     * @param joinPoint
     * @param time
     */
     //环绕通知所执行的方法
     //设置异步，不影响响应速度
    /* @Async
    public void saveLog(ProceedingJoinPoint joinPoint, long time, Object result) {

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获得此次请求的对象
        HttpServletRequest request = attributes.getRequest();


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        //创建业务对象
        StSystemLog systemLog = new StSystemLog();
        //获得切点目标方法的包名.类名
        String className = joinPoint.getTarget().getClass().getName();
        //获得目标方法的方法名
        String methodName = signature.getName();
        systemLog.setClassName(className);
        systemLog.setMethodName(className.substring(className.lastIndexOf(".") + 1) + "." + methodName);
        //获得请求的IP地址
        systemLog.setReqIp(request.getRemoteAddr());
        //获得请求的相应时长
        systemLog.setExeuTime(time);
        //log的创建时间
        systemLog.setCreateTime(LocalDateTime.now());
        //根据signature.getMethod()方法获得的方法对象，获得注解类的对象
        Annotation annotation = method.getAnnotation(Annotation.class);
        //如果注解类对象不为空
        if (annotation != null) {
            //获得注解的属性上的值
            systemLog.setRemark(annotation.value());
        }

        //请求的参数转为数组
        Object[] args = joinPoint.getArgs();
        try {
            List<String> list = new ArrayList<String>();
            //循环请求参数数组
            for (Object o : args) {
                //判断对象o是否为入参验证的信息，不需要入参的验证信息
                if (!(o instanceof BindingResult)) {
                    //将每个参数都转换为Json
                    //为什么不用fastJson，因为fastJson转换不了列表，会显示空{}
                    list.add(new Gson().toJson(o));
                }
            }
            //获得请求参数的JSON字符串
            systemLog.setReqParam(list.toString());
        } catch (Exception e) {
        }
        //出参
        //获得出参的JSON字符串
        systemLog.setRespParam(JSONObject.toJSONString(result));
        //进行日志保存，一般为异步方法
        logService.save(systemLog);
    }*/
}
