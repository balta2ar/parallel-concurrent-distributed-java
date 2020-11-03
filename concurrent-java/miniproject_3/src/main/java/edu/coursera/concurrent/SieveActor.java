package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
//        throw new UnsupportedOperationException();

        SieveActorActor sieveActorActor = new SieveActorActor(2);
        finish(()->{
            for (int i = 3; i <= limit; i++){
                sieveActorActor.send(i);
            }
            sieveActorActor.send(0);
        });
        int result = 0;
        SieveActorActor current = sieveActorActor;
        while (current!=null){
            result += 1;
            current = current.nextActor;
        }
        return result;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        SieveActorActor nextActor = null;
        int localPrime; // the only prime this Actor hold

        public SieveActorActor(int localPrime) {
            this.localPrime = localPrime;
        }

        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            int candidate = (int)msg;

            if (candidate <= 0){
                if (nextActor != null){
                    nextActor.send(msg);
                }
            }

            // if the candidate is locally prime
            if (isLocalPrime(candidate)){
                // if nextActor is null, then add a new Actor to hold candidate
                if (nextActor == null){
                    nextActor = new SieveActorActor(candidate);
                } else { // let nextActor to decide
                    nextActor.send(msg);
                }
            }

        }

        boolean isLocalPrime(int candidate) {
            return candidate % localPrime != 0;
        }
    }
}
