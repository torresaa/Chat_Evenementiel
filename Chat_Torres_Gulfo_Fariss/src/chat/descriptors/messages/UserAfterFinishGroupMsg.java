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
    private String name;

    public UserAfterFinishGroupMsg(int index, InetAddress ip, int listeningPort, String name) {
        setType(Message.USER_AFTER_FINISH_GROUP);
        this.index = index;
        this.ip = ip;
        this.listeningPort = listeningPort;
        this.name = name;
        formatMsg();
    }

    @Override
    public void formatPayload() {
        byte[] ipBytes = this.ip.getAddress();
        setPayload(Utils.concat(Utils.intToByteArray(this.index),
                Utils.concat(ipBytes, 
                        Utils.concat(Utils.intToByteArray(listeningPort), this.name.getBytes()))));
    }

}
