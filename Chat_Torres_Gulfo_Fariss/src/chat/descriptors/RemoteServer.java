/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors;

import implementation.engine.NioChannelImpl;
import java.net.InetAddress;

/**
 *
 * @author aquilest
 */
public class RemoteServer extends RemoteHost{
    private NioChannelImpl nioChannel;
    
    public RemoteServer(InetAddress ip) {
        super(ip);
    }
    
    public RemoteServer(NioChannelImpl nioChannel){
        super(nioChannel.getRemoteAddress().getAddress());
        this.nioChannel = nioChannel;
    }
   
    @Override
    public NioChannelImpl getNioChannel() {
        return this.nioChannel;
    }
    
}
