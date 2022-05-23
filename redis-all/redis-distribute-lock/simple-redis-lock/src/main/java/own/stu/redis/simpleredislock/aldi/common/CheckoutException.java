package own.stu.redis.simpleredislock.aldi.common;

public class CheckoutException extends RuntimeException {

    public CheckoutException(CheckoutErrCode errCode) {
        super(errCode.getCode());
    }
}