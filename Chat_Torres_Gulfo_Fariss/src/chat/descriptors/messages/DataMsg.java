/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Message that contains the text to send
 * PAYLOAD = lcMessage | data
 *              long     String
 *               16B      ---
 */
public class DataMsg extends Message{
    
    private long lcMessage;
    private String data;

    public DataMsg(long lcMessage, String data){
        setType(Message.DATA_MSG);
        this.lcMessage = lcMessage;
        this.data = data;
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.concat(Utils.longToBytes(lcMessage), data.getBytes()));
    }
    
    public long getLcMessage(){
        return this.lcMessage;
    }
    
    public String getData(){
        return data;
    }
    
}
