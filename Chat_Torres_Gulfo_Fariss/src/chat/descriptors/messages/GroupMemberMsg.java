/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;
import java.net.InetAddress;

/**
 * Message to send to each client every member of the group
 * one by one.
 * PAYLOAD = index | @IP | listeningPort | Client Name
 *            int   Inet@      int           String
 *            4B     4B        4B
 */
public class GroupMemberMsg extends Message{
    
    private int index;
    private InetAddress ip;
    private int listeningPort;
    private String name;

    public GroupMemberMsg(int index, InetAddress ip, int listeningPort, String name){
        setType(Message.GROUP_MEMBER_MSG);
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
                        Utils.concat(Utils.intToByteArray(listeningPort), 
                                this.name.getBytes()))));
    }  
}
