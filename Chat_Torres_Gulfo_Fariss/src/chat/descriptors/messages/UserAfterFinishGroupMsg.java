/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;
import java.net.InetAddress;

/**
 * Message send when new user is connected to the server after group finish
 * to notify the group about the new user
 * 
 */
public class UserAfterFinishGroupMsg extends Message {

    private int index;
    private InetAddress ip;
    private int listeningPort;

    public UserAfterFinishGroupMsg(int index, InetAddress ip, int listeningPort) {
        setType(Message.USER_AFTER_FINISH_GROUP);
        this.index = index;
        this.ip = ip;
        this.listeningPort = listeningPort;
        formatMsg();
    }

    @Override
    public void formatPayload() {
        byte[] ipBytes = this.ip.getAddress();
        setPayload(Utils.concat(Utils.intToByteArray(this.index),
                Utils.concat(ipBytes, Utils.intToByteArray(listeningPort))));
    }

}
