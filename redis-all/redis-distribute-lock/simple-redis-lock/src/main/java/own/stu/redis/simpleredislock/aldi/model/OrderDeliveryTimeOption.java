package own.stu.redis.simpleredislock.aldi.model;

/**
 * Created with IntelliJ IDEA.
 * User: gaoyu
 * Date: 17/11/21
 * Time: 下午2:02
 */
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDeliveryTimeOption implements Serializable {

	/**
	 * store_calendar_item.id
	 */
	private Long id;

	/**
	 * store_calendar_item_threshold.id
	 */
	private Long thresholdId;

	/**
	 * 是否选中
	 */
	private Integer selected;

	/**
	 * 日期名称(今天、明天这种)
	 */
	private String dateName;

	/**
	 * 日期字符串
	 */
	private String dateStr;

	/**
	 * 开始配送时间:08:00
	 */
	private String timeStartStr;

	/**
	 * 结束配送时间:09:00
	 */
	private String timeEndStr;

	/**
	 * 是否立即送达 0否 1是
	 */
	private Integer deliveryNow;

	/**
	 * 立即送达文字
	 */
	private String deliveryNowStr;

	/**
	 * 订单下单上限，-1代表无上限'
	 */
	private Integer registerThreshold;

	/**
	 * 是否可以下单，0 默认可以
	 */
	private Integer canRegister;

	private Long storeId;

	private String storeCode;
}
