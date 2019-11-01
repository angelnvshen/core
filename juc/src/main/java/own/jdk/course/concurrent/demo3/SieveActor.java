package own.jdk.course.concurrent.demo3;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

import java.util.ArrayList;
import java.util.List;

/**
 * PCDP is a shared-memory, pedagogical, parallel programming framework.
 * PCDP‘s design, implementation, and APIs emphasize simplicity to
 * make it straightforward to use for programmers new to parallel programming.
 * PCDP supports task parallelism, loop parallelism, actor parallelism,
 * bulk synchronization, point-to-point synchronization, and isolation.
 * PCDP is built on top of the Java Fork-Join framework, but offers more convenient APIs.
 */

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     * <p>
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        PCDP.finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        int numPrimes = 0;
        SieveActorActor loopActor = sieveActor;
        while (loopActor != null) {
            numPrimes += loopActor.numPrimes;
            loopActor = loopActor.nextActor;
        }
        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * @param msg Received message
         */

        private static final int MAX_LOCAL_PRIMES = 500;
        private List<Integer> primes;
        private int numPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor(final int localPrime) {
            primes = new ArrayList<>();
            primes.add(localPrime);
            this.nextActor = null;
            this.numPrimes = 1;
        }

        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
            } else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if (locallyPrime) {
                    if (primes.size() <= MAX_LOCAL_PRIMES) {
                        primes.add(candidate);
                        numPrimes++;
                    } else if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    } else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(final Integer candidate) {
            return primes
                    .stream()
                    //.parallel()
                    .noneMatch(prime -> candidate % prime == 0);
        }
    }
}
