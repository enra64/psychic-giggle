// PUT_VREP_REMOTEAPI_COPYRIGHT_NOTICE_HERE

package de.ovgu.softwareprojekt.servers.kuka.vrep.coppelia;

public class StringW
{
    String w;

    public StringW(String s)
    {
        w = new String(s);
    }

    public void setValue(String s)
    {
        w = new String(s);
    }

    public String getValue()
    {
        return w;
    }
}