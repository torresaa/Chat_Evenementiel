/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 *  Ack message for the specific lcMessage
 *  PAYLOAD = lcMessage
 *              long
 *               16B
 * 
 */
public class DataAckMsg extends Message{
    
    private long lcMessage; 

    public DataAckMsg(long lc){
        setType(Message.DATA_ACK_MSG);
        this.lcMessage = lc;
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.longToBytes(lcMessage));
    }
    
    
}
