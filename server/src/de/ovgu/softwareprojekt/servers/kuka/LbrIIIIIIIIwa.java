package de.ovgu.softwareprojekt.servers.kuka;

/**
 * This interface is designed to make the kuka and the vrep implementation interchangeable
 */
public interface LbrIIIIIIIIwa {
    void start();
    void rotateJoint(LbrJoint joint, float degree);
    void setJointVelocity(LbrJoint joint, float velocity);
    void rotateJointTarget(LbrJoint joint, float degree);
    void stop();
}
