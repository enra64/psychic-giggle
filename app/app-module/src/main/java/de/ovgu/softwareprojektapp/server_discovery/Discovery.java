package de.ovgu.softwareprojektapp.server_discovery;

import java.util.List;

public class Discovery {
    public class Server {
        public String name;
        public String address;
        public String port;
    }

    public interface OnDiscoveryListener {
        void onServerListUpdated(List<Server> serverNames);
    }

    public Discovery(int port){

    }

    public void startDiscovery(OnDiscoveryListener listener){

    }

    public void stopDiscovery() {

    }
}