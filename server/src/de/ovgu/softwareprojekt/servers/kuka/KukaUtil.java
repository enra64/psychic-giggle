package de.ovgu.softwareprojekt.servers.kuka;

/**
 * Small helper class for common robot needs.
 */
class KukaUtil {
    /**
     * This is a somewhat custom value clamping function. It uses the negative of maxDisplacement as minimum.
     *
     * @param value           the value that should be capped
     * @param maxDisplacement the maximum and negative minimum the value should be after this
     * @return the capped value
     */
    static float clampToJointRange(float value, float maxDisplacement) {
        if (value > maxDisplacement)
            return maxDisplacement;
        if (value < -maxDisplacement)
            return maxDisplacement;
        return value;
    }

    /**
     * Get the range of a joint as given in V-REP.
     *
     * @param joint the joint of which the range should be returned
     * @return the maximum range of the joint. the robot can move half of it in positive and negative direction.
     */
    static float getRange(LbrJoint joint) {
        switch (joint) {
            case BaseRotator:
                return 340;
            case BaseTilter:
                return 240;
            case SecondRotator:
                return 340;
            case SecondTilter:
                return 240;
            case ToolArmRotator:
                return 340;
            case ToolTilter:
                return 240;
            case ToolRotator:
                return 350;
            default:
                throw new RuntimeException("Unknown joint");
        }
    }
}
