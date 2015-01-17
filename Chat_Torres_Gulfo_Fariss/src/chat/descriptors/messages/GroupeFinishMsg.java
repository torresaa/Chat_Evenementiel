/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors.messages;

import chat.descriptors.Utils;

/**
 * Msg send when the number of members in a group has been completed
 * Server informs to its clients that the group is complete, so the
 * chat can start.
 * PAYLOAD = groupName | numberOfMembers
 *           String[20]      int 
 *             20B            4B
 */
public class GroupeFinishMsg extends Message{
    
    private String groupName;
    private int numberOfMembers;
    
    public GroupeFinishMsg (String name, int numberOfMembers){
        this.setType(Message.GROUP_FINISH_MSG);
        this.numberOfMembers = numberOfMembers;
        this.groupName = name;
        formatMsg();
    }
    
    @Override
    public void formatPayload() {
        setPayload(Utils.concat(this.groupName.getBytes(), 
                Utils.intToByteArray(this.numberOfMembers)));
    }
    
}
