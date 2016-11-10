package de.ovgu.softwareprojektapp.server_discovery;

import java.util.LinkedList;
import java.util.List;

public class Discovery implements DiscoveryThread.DiscoveryListener {
    private DiscoveryThread mDiscoveryThread;
    private OnDiscoveryListener mDiscoveryListener;
    private List<Server> mCurrentServerList = new LinkedList<>();

    public Discovery(int port) {
        mDiscoveryThread = new DiscoveryThread(this, port);
    }

    public void startDiscovery(OnDiscoveryListener listener) {
        mDiscoveryListener = listener;
        mDiscoveryThread.start();
    }

    public void stopDiscovery() {
        mDiscoveryThread.close();
    }

    @Override
    public void onDiscovery(Server newServer) {
        mCurrentServerList.add(newServer);
        //TODO: check for server equality
        mDiscoveryListener.onServerListUpdated(mCurrentServerList);
    }
}