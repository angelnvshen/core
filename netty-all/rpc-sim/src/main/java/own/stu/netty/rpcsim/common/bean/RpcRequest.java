package own.stu.netty.rpcsim.common.bean;

import lombok.Data;

@Data
public class RpcRequest {

    private String requestId;

    private String interfaceName;

    private String serviceVersion;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;
}
