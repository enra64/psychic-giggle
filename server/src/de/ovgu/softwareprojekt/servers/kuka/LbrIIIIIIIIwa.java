package de.ovgu.softwareprojekt.servers.kuka;

/**
 * This interface is designed to make the kuka and the vrep implementation interchangeable
 */
public interface LbrIIIIIIIIwa {
    void start();
    void rotateJoint(LbrJoints joint, int degree);
    void setJointVelocity(LbrJoints joint, float velocity);
    void stop();
}
