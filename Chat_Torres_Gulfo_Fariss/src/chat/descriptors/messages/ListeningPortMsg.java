/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Msg send from client to server after connection
 * to inform listening port
 * PAYLOAD = ListeningPort (4B) | name
 */
public class ListeningPortMsg extends Message{
    
    private int listenigPort = -1;
    private String name;
    
    public ListeningPortMsg (int port){
        this.setType(Message.LISTENING_PORT_MSG);
        this.listenigPort = port;
        //formatPayload();
        this.formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.intToByteArray(this.listenigPort));
    }
    
}
