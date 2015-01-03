package implementation.engine;

public interface AcceptCallback {
  /**
   * Callback to notify about an accepted connection.
   * @param server
   * @param channel
   */
  public void accepted(NioServerImpl server, NioChannelImpl channel);
  
  /**
   * Callback to notify that a previously accepted channel 
   * has been closed.
   * @param channel
   */
  public void closed(NioChannelImpl channel);
}
