package own.stu.redis.simpleredislock.aldi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import own.stu.redis.simpleredislock.aldi.common.CheckoutException;
import own.stu.redis.simpleredislock.aldi.model.CreateSoDTO;

@Service
public class OrderRedisCountManageImpl {

    protected static final Logger logger = LoggerFactory.getLogger(OrderRedisCountManageImpl.class);

    @Autowired
    private OrderNumThresholdManageImp orderNumThresholdManage;

    @Autowired
    private OrderCommunityCountManageImp orderCommunityCountManage;

    @Autowired
    private OrderCommunityCountFlagContext countFlagContext;

    public void increaseThreshold(CreateSoDTO userOrder) {

        orderCommunityCountManage.increaseThreshold(userOrder);
        try {
            orderNumThresholdManage.increaseThreshold(userOrder);
        }catch (CheckoutException checkoutException){
            Boolean communityCountFlag = countFlagContext.get();
            if(communityCountFlag != null && communityCountFlag){
                orderCommunityCountManage.communityDecreaseThreshold(userOrder);
            }
            throw checkoutException;
        }
    }

    public void decreaseThreshold(CreateSoDTO userOrder) {
        orderNumThresholdManage.decreaseThreshold(userOrder);

        Boolean communityCountFlag = countFlagContext.get();
        if(communityCountFlag != null && communityCountFlag) {
            orderCommunityCountManage.communityDecreaseThreshold(userOrder);
        }
    }
}
