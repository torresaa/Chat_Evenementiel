/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

import java.nio.ByteBuffer;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import implementation.engine.NioServerImpl;

/**
 *
 * @author aquilest
 */
public class Client implements AcceptCallback, ConnectCallback, DeliverCallback{

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closed(NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connected(NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliver(NioChannelImpl channel, ByteBuffer bytes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
