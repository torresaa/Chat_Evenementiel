/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

/**
 * Message send to notify operation success between client - server
 * PAYLOAD = byte[1]
 */
public class AckServer extends Message{
    
    public AckServer (){
        this.setType(Message.ACK_SERVER);
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        byte[] payload = new byte[1];
        setPayload(payload);
    }
    
}
