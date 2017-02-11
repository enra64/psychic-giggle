package de.ovgu.softwareprojekt.examples.kuka;

/**
 * Exception thrown when a {@link LbrIiwa} implementation could not execute a desired action.
 */
class RoboticFailure extends RuntimeException {
    /**
     * Exception thrown when a {@link LbrIiwa} implementation could not execute a desired action.
     * @param msg exception information
     */
    RoboticFailure(String msg){
        super(msg);
    }
}
