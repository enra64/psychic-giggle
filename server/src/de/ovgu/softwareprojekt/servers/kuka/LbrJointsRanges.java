package de.ovgu.softwareprojekt.servers.kuka;

/**
 * Created by arne on 18.01.17.
 */
public class LbrJointsRanges {
    public static float getRange(LbrJoint joint){
        switch (joint) {
            case BaseRotator:
                return 340;
            case BaseTilter:
                return 240;
            case SecondRotator:
                return 340;
            case SecondTilter:
                return 240;
            case ThirdRotator:
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
