/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import java.util.BitSet;

/**
 *
 * @author aquilest
 */
public class ReceivedDeliverMsg implements Comparable<ReceivedDeliverMsg>{
    public static final long DELIVER_TIMEOUT_MILLIS = 50000;
    
    private long lcMessage; 
    private BitSet activeUsers; 
    private BitSet incomingDeliver; 
    
    private long firsDeliverTime; 
    
    public ReceivedDeliverMsg(long lc, BitSet activeUsers, int index){
        this.activeUsers = (BitSet) activeUsers.clone();
        this.lcMessage = lc;
        this.incomingDeliver = new BitSet();
        this.firsDeliverTime = System.currentTimeMillis();
        this.incomingDeliver.set(index);
    }
    
    public void setDeliver(int index){
        this.incomingDeliver.set(index);
    }
    
    public boolean allDeliverComplete(){
        BitSet temp_deliver = (BitSet)this.incomingDeliver.clone();
        temp_deliver.and(activeUsers);
        return temp_deliver.cardinality() == this.activeUsers.cardinality();
    }
    
    public boolean timeOutComplete(){
        return System.currentTimeMillis() - this.firsDeliverTime
                > ReceivedDeliverMsg.DELIVER_TIMEOUT_MILLIS;
    }
    
    public long getLcMessage (){
        return this.lcMessage;
    } 
   
    @Override
    public String toString(){
        return "Message with lc: "+this.lcMessage;
    }

    @Override
    public int compareTo(ReceivedDeliverMsg t) {
        if (this.lcMessage == t.getLcMessage()){
            return 0;
        }else{
            if(this.lcMessage < t.getLcMessage()){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
