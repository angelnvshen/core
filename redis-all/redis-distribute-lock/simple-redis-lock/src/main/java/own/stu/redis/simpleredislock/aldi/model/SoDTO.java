package own.stu.redis.simpleredislock.aldi.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @CreateDate 2018-09-05
 */
@Data
public class SoDTO {
	private Long id;
	/**
	 * 格式：150905xxxxxxxx2657 纯数字 6位日期+8位数字+1校验位+3位用户id
	 */
	private String orderCode;
	/**
	 * 父order_code
	 */
	private String parentOrderCode;
	/**
	 * 1 子单 2 父单
	 */
	private Integer isLeaf;
	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 用户手机号
	 */
	private String userMobile;
	/**
	 * 下单用户账号
	 */
	private String userName;
	/**
	 * 商家ID
	 */
	private Long merchantId;
	/**
	 * 订单金额(不含运费/运费险)
	 */
	private BigDecimal orderAmount;
	/**
	 * 订单商品总金额
	 */
	private BigDecimal productAmount;
	/**
	 * 税费
	 */
	private BigDecimal taxAmount;
	/**
	 * 3，处理中 4，系统处理中 5，待出库 6，订单已经转do 21，订单已经出库 22，订单已经开始派送 25，用户已经收货 31，送货失败 34，订单已经取消 35，订单已经完成
	 */
	private Integer orderStatus;
	/**
	 * 0:账户支付 1:网上支付 2:货到付款(货到付)3:pos机(货到付)4:货到转账(货到付)5:货到付支票 (货到付)6:货到刷支付宝(货到付)21:邮局汇款22:银行转账 31:分期付款 32:合同账期
	 */
	private Integer orderPaymentType;
	/**
	 * 0.未支付1.已支付(部分或全部)待审核（银行转账，邮局汇款支付方式时）2.部分支付3.已支付
	 */
	private Integer orderPaymentStatus;
	/**
	 * 实际扣款金额
	 */
	private BigDecimal orderPaymentConfirmAmount;
	/**
	 * 支付确认时间
	 */
	private Date orderPaymentConfirmDate;
	/**
	 * 订单总重
	 */
	private BigDecimal totalWeight;
	/**
	 * 运费(实收)
	 */
	private BigDecimal orderDeliveryFee;
	/**
	 * 包邮券优惠金额
	 */
	private BigDecimal orderDeliveryFree;
	/**
	 * 订单赠送的积分
	 */
	private BigDecimal orderGivePoints;
	/**
	 * 取消原因ID
	 */
	private Integer orderCancelReasonId;
	/**
	 * 取消时间
	 */
	private Date orderCancelDate;
	/**
	 * 0 平台审单通过;2 待系统审核;4 待客服人工审核;6 审核不通过;10 待海关审核11 海关审核失败12 海关审核成功20 待支付海关审核21 待支付海关审核失败22 待支付海关审核成功30 待三单海关审核31 待三单海关审核失败32 待三单海关审核成功，待删除字段
	 */
	private Integer orderNeedCs;
	/**
	 * 订单取消原因
	 */
	private String orderCsCancelReason;
	/**
	 * 取消操作人类型：0:用户取消 1:系统取消 2:客服取消
	 */
	private Integer orderCanceOperateType;
	/**
	 * 取消操作人用户名
	 */
	private String orderCanceOperateId;
	/**
	 * -8: 前台取消订单锁定
	 */
	private Integer orderDataExchangeFlag;
	/**
	 * 配送方式类型
	 */
	private String orderDeliveryMethodId;
	/**
	 * 订单备注(用户)
	 */
	private String orderRemarkUser;
	/**
	 * 订单备注(商家给用户看的)
	 */
	private String orderRemarkMerchant2user;
	/**
	 * 订单备注(商家自己看的)
	 */
	private String orderRemarkMerchant;
	/**
	 * 订单来源  0：普通 1：团购 2：询价 3：租赁  4 拼单。
	 */
	private Integer orderSource;
	/**
	 * 订单渠道 : 订单来源渠道 ：1 pc   2 android    3  微信    4 ios    5 h5
	 */
	private Integer orderChannel;
	/**
	 * 订单促销状态：1001 拼团中，1002拼团成功，1003拼团失败，1004 参团失败，1006 参团成功，1005 取消参团；3001 待补货，3002、补货中 3003 补货成功， 3004 已取消
	 */
	private Integer orderPromotionStatus;
	/**
	 * 收货人地址
	 */
	private String goodReceiverAddress;
	/**
	 * 收货人地址邮编
	 */
	private String goodReceiverPostcode;
	/**
	 * 收货人姓名
	 */
	private String goodReceiverName;
	/**
	 * 收货人手机
	 */
	private String goodReceiverMobile;
	/**
	 * 收货人国家
	 */
	private String goodReceiverCountry;
	/**
	 * 收货人省份
	 */
	private String goodReceiverProvince;
	/**
	 * 收货人城市
	 */
	private String goodReceiverCity;
	/**
	 * 收货人地区
	 */
	private String goodReceiverCounty;
	/**
	 * 收货人四级区域
	 */
	private String goodReceiverArea;
	/**
	 * 精确地址
	 */
	private String exactAddress;

	/**
	 * 用户下单纬度信息
	 */
	private BigDecimal latitude;

	/**
	 * 用户下单经度信息
	 */
	private BigDecimal longitude;

	/**
	 * 身份证号码
	 */
	private String identityCardNumber;
	/**
	 * 订单出库时间
	 */
	private Date orderLogisticsTime;
	/**
	 * 订单收货时间
	 */
	private Date orderReceiveDate;
	/**
	 * 0：未删除1：回收站-用户可恢复到02：用户完全删除(客服可协助恢复到0或1)
	 */
	private Integer orderDeleteStatus;
	/**
	 * 是否可用:默认0否;1是
	 */
	private Integer isAvailable;
	/**
	 * 改价前订单金额(不含运费/运费险)
	 */
	private BigDecimal orderBeforeAmount;
	/**
	 * 改价前运费(实收)
	 */
	private BigDecimal orderBeforeDeliveryFee;
	/**
	 * 订单来源系统
	 */
	private String sysSource;
	/**
	 * 外部系统订单编号
	 */
	private String outOrderCode;
	/**
	 * 二级支付，多种支付类型已英文逗号隔开：,1,2,
	 */
	private String orderPaymentTwoType;
	/**
	 * 评论状态 0 :未评论 1 已评论
	 */
	private Integer commentStatus;
	/**
	 * 商家名称
	 */
	private String merchantName;
	/**
	 * 平台备注
	 */
	private String orderRemarkCompany;
	/**
	 * 订单完成时间
	 */
	private Date orderCompleteDate;
	/**
	 * 0:正常；1 免单
	 */
	private Integer orderFreeFlag;
	/**
	 * 订单类型（商品维度）: 0 普通 1 生鲜类 2 服务类 3 虚拟
	 */
	private Integer orderType;
	/**
	 * 收货人国家code
	 */
	private String goodReceiverCountryCode;
	/**
	 * 收货人省份code
	 */
	private String goodReceiverProvinceCode;
	/**
	 * 收货人城市code
	 */
	private String goodReceiverCityCode;
	/**
	 * 收货人四级区域code
	 */
	private String goodReceiverAreaCode;
	/**
	 * 店铺Id
	 */
	private Long storeId;
	/**
	 * 店铺Id
	 */
	private String storeCode;
	/**
	 * 店铺名称
	 */
	private String storeName;
	/**
	 * 订单流程类型
	 */
	private String orderTypeProcessCode;
	/**
	 * 订单标签
	 */
	private String orderLabel;
	/**
	 * 预计发货日期
	 */
	private Date expectDeliverDate;
	/**
	 * 流程运行状态 0 未运行 1运行中 2运行结束 3运行异常
	 */
//	@ApiModelProperty(value="流程运行状态 0 未运行 1运行中 2运行结束 3运行异常", notes="最大长度：3")
//	private Integer flowStatus;

	/**
	 * 收银员
	 */
	private String cashier;

	private String sourceCode;

	/**
	 * 包装费
	 */
	private BigDecimal packingExpense;

	/**
	 * 冷冻包装费
	 */
	private BigDecimal frozenPackingExpense;

	/**
	 * 预计送达时间
	 */
	private String estimatedTimeOfArrival;

	/**
	 * 配送时间段的统计信息id
	 */
	private Long storeCalendarItemThresholdId;

	/*是否立即送出*/
	private Integer deliveryNow;

	private Date expectReceiveDateStart;
	private Date expectReceiveDateEnd;
	private Integer deliveryRange;

	/**
	 * 异动收银，是否会员订单 1 会员；0 非会员；
	 */
	private Integer isMember;

	/**
	 * 礼品卡金额
	 */
	private BigDecimal giftCardAmount;
}
