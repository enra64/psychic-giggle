package de.ovgu.softwareprojekt.servers.kuka;

/**
 * This interface makes the kuka and the vrep marble labyrinth implementation interchangeable.
 */
public interface LbrIiwa {
    /**
     * Initialises the control method. All other interface methods should be safely callable after calling start()
     */
    void start();

    /**
     * Rotate a joint.
     *
     * @param joint  which joint
     * @param degree how much.
     */
    void rotateJoint(Joint joint, float degree);

    /**
     * Set the velocity of a joint
     * @param joint which joint
     * @param velocity how fast
     */
    void setJointVelocity(Joint joint, float velocity);

    /**
     * Rotate a joint to a position.
     *
     * @param joint  which joint
     * @param degree where to
     */
    void rotateJointTarget(Joint joint, float degree);

    /**
     * Finalize operations. The other interface methods, except for start(), need not be callable after calling stop().
     */
    void stop();
}
