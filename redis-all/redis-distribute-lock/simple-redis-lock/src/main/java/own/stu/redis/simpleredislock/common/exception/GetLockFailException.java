package own.stu.redis.simpleredislock.common.exception;

public class GetLockFailException extends RuntimeException{
    public GetLockFailException() {
    }

    public GetLockFailException(String message) {
        super(message);
    }
}
