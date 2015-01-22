/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Send this msg when a user that has leaved the chat room, cames back
 * Payload: index (int)
 *      
 */
public class ComingBackMsg extends Message{
    
    private int index;
    
    public ComingBackMsg(int index){
        setType(Message.COMINGBACK_MSG);
        this.index = index;
        formatMsg();
    }

    @Override
    public void formatPayload() {
        setPayload(Utils.intToByteArray(index));
    }
    
}
