package own.stu.netty.rpcsim.registry;

public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);

    void registerAllEphemeralServiceNameTo(String serviceAddress) throws Exception;
}