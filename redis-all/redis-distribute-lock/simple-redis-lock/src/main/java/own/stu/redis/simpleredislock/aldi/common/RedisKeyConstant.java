package own.stu.redis.simpleredislock.aldi.common;

public class RedisKeyConstant {

    /**
     *  社区团购信息，redis key 前缀
     */
    public static final String  STORE_COMMUNITY_KEY_PREFIX = "frontier-trade:community:";

    public static final String  STORE_COMMUNITY_COUNT_KEY_FORMAT = "frontier-trade:communityCount:%d:%s";
    /**
     * switch 表示门店是否开启社区团购 hash key
     */
    public static final String  STORE_COMMUNITY_KEY_HASHKEY_SWITCH = "switch";

    /**
     * 社区配送阈值后缀(hash key)
     * hash key 例： 社区code:count
     */
    public static final String STORE_COMMUNITY_KEY_HASHKEY_COUNT = "count";

}