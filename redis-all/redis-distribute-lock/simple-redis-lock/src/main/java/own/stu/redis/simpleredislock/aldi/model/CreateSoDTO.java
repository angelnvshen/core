package own.stu.redis.simpleredislock.aldi.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by byp on 2018/10/10.
 */
@Data
public class CreateSoDTO extends SoDTO {

    /**
     * 收银员
     */
    private String cashier;

    /**
     * 线下支付金额
     */
    private BigDecimal orderPaidByOffline;

    /******** 积分相关 *******************/

    /**
     * 使用(消费)积分对应规则id
     */
    private Long usedRuleId;

    /**
     * 赠送积分对应规则id
     */
    private Long giveRuleId;

    /**
     * 积分有效期结束时间
     */
    private Date deadline;
    /**
     * 冻结到期时间
     */
    private Date freezeDeadline;

    /**
     * ??????
     */
    private BigDecimal orderDeliveryFeeAccounting;

    /**
     * 配送服务类型
     */
    private String orderDeliveryServiceType;

    /**
     * 人工订单状态
     */
    private Integer manualType;

    // 订单对应的运费模板id
    private Long freightTemplateId;

    /**
     * 预售信息对象
     */
//    private SoPresellDTO soPresellDTO;

    private String promotionIds;// 促销ID

    /**
     * 发票类型 0:未开票 1:电子发票 2:纸质发票
     */
    private Integer orderInvoiceType;

    /**
     * 收发货地址是否都为中国
     */
    private Boolean deliveryAndReceiveCountryIsChina;

    // 配送公司ID
    private String deliveryCompanyId;

//    private List<CreateSoItemDTO> orderItemList;

//    private List<SoAttachmentDTO> soAttachmentDTOList;//附件

    private List<CreateSoDTO> childOrderList;

    /**
     * 订单分摊信息（订单维度）
     */
//    private SoShareAmountDTO soShareAmountDTO;

    /**
     * 订单优惠券信息
     */
//    private List<SoCouponDTO> orderCouponList;
//    /**
//     * 订单包邮券信息
//     */
//    private List<OrderCoupon> orderFreeCouponList;

    /**
     * 组合品信息
     */
//    private List<OrderItemRelationInputDTO> orderItemRelationInputDTOList;

    /**
     * 发票信息
     */
//    private List<SoInvoiceDTO> soInvoiceDTOList;

    /**
     * 订单返利信息（优惠码、佣金）
     */
//    private List<SoRebateDTO> soRebateDTOList;
//    /**优惠码*/
    private String referralCode;

    private Date orderCreateTime;

    private Long companyId;

    private Integer warehouseType;

    private Integer isDeleted;

    private Integer versionNo;
    private Integer mealType;
    private String equipCode;
    private String tableNo;
    private String tableName;
    private Integer mealsNum;
    private Integer isTemporary;
    private String seqNo;

    /**
     * 是否部分发货
     */
    private Integer isAcceptPartialShipment;

    /**
     * 餐具份数字典ID
     */
    private Long dictionaryId;
    //拼单标识
    private Integer collageType;

    /**
     * 选择小区
     */
    private String storeCommunity;

    /**
     * 小区code
     */
    private String storeCommunityCode;

    private String storeCommunityUS;


}
