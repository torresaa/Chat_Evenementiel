/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 *
 * @author aquilest
 */
public class DeliverMsg extends Message{
    private long lcMessage;
    
    public DeliverMsg(long lc){
        setType(Message.DELIVER_MSG);
        this.lcMessage = lc;
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.longToBytes(lcMessage));
    }
    
}
