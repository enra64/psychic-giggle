package de.ovgu.softwareprojekt.networking;

/**
 * This interface is used to signal the {@link AbstractPsychicServer} of the loss of a client, so it can
 * decide on further server advertisement.
 */
interface ClientLossListener {
    /**
     * Called when a client is lost.
     */
    void onClientLoss();
}
