package edu.utdallas.cs5390.chat.common.connection.udp;

import edu.utdallas.cs5390.chat.common.util.Utils;

import java.net.DatagramSocket;

/**
 * Created by adisor on 11/1/2016.
 */
class UDPMessageSocket {

    final DatagramSocket datagramSocket;

    UDPMessageSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void close() {
        Utils.closeResource(datagramSocket);
    }
}
