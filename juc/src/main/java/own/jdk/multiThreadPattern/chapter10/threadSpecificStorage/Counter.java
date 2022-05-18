package own.jdk.multiThreadPattern.chapter10.threadSpecificStorage;

public class Counter {
    private int i = 0;

    public int getAndIncrement() {
        return i++;
    }
}
