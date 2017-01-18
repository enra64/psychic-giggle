package de.ovgu.softwareprojekt.servers.kuka;

import java.util.EnumMap;

/**
 * Created by arne on 18.01.17.
 */
public class LbrJointRanges {

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

    public static EnumMap<LbrJoint, Integer> RANGE_MAP = new EnumMap<LbrJoint, Integer>(LbrJoint.class);

    // this static block is executed when this class is referenced for the first time
    static {
        RANGE_MAP.put(LbrJoint.BaseRotator, 340);
        RANGE_MAP.put(LbrJoint.SecondRotator, 340);
        RANGE_MAP.put(LbrJoint.ThirdRotator, 340);
        RANGE_MAP.put(LbrJoint.ToolRotator, 350);

        RANGE_MAP.put(LbrJoint.BaseTilter, 240);
        RANGE_MAP.put(LbrJoint.BaseTilter, 240);
        RANGE_MAP.put(LbrJoint.BaseTilter, 240);
        RANGE_MAP.put(LbrJoint.BaseTilter, 240);
    }
}
