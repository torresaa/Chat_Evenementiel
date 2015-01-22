/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors;

import chat.descriptors.messages.Message;
import implementation.engine.NioChannelImpl;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 *
 * @author aquilest
 */
public abstract class RemoteHost implements Serializable {

    private InetAddress ip;
    private int listenPort = -1;
    private LinkedList<byte[]> pendingMessages;

    public RemoteHost(InetAddress ip) {
        this.ip = ip;
        this.pendingMessages = new LinkedList<byte[]>();
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.listenPort = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return listenPort;
    }

    public abstract NioChannelImpl getNioChannel();

    public void addMessageToQueue(Message msg) {
        this.pendingMessages.add(msg.getMessage());
    }

    public byte[] getMessageFromQueue() {
        return this.pendingMessages.pollFirst();
    }
    
    public boolean queueIsEmpty(){
        return this.pendingMessages.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        RemoteHost t = (RemoteHost) o;
        if(this.ip.equals(t.getIp())){
            if(this.listenPort == t.getPort()){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "RemoteHost{" + "ip=" + ip + " ListenigPort: "+ this.listenPort+'}';
    }

}
