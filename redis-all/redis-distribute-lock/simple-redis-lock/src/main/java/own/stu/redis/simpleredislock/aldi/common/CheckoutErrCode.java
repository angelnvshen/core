package own.stu.redis.simpleredislock.aldi.common;

public enum CheckoutErrCode {


    CURRENT_DELIVERY_TIME_NO_REGISTER("215", "当前时间段已约满，请返回购物车重新提交订单", ""),
    CURRENT_DELIVERY_TIME_TO_MUCH_REGISTER("216", "当前时间段下单多数过多，请重新提交订单", ""),
    ;

    private String code;
    private String msg;
    private String englishMessage;

    CheckoutErrCode(String code, String msg, String englishMessage) {
        this.code = code;
        this.msg = msg;
        this.englishMessage = englishMessage;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}