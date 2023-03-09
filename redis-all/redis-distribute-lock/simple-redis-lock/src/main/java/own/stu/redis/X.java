package own.stu.redis;

public class X {

    public static void main(String[] args) {
        int p = 11;
        int n = (p > 1) ? p - 1 : 1;
        n |= n >>> 1; n |= n >>> 2;  n |= n >>> 4;
        n |= n >>> 8; n |= n >>> 16;

        System.out.println(n);
        n = (n + 1) << 1;
        System.out.println(n);
    }
}
