/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Send this message when event connected was fire after contactUser
 * PAYLOAD = remoteUserIndex
 *              int
 *              4B
 */
public class MyIndexMsg extends Message{
    
    private int index;
    
    public MyIndexMsg(int index){
        this.setType(Message.MY_INDEX_MSG);
        this.index = index;
        formatMsg();
    }

    @Override
    public void formatPayload() {
        setPayload(Utils.intToByteArray(index));
    }
    
}
