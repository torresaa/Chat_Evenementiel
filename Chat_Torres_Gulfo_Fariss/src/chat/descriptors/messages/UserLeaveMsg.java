/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Send this message when a user leave the chat room, but he can come back later
 * PAYLOAD = indexOfLeaveUser (int)
 */
public class UserLeaveMsg extends Message{
    
    private int index;
    
    public UserLeaveMsg(int index){
        setType(Message.USER_LEAVE_MSG);
        this.index = index;
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.intToByteArray(index));
    }   
}
