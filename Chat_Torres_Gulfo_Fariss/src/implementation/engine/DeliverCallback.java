package implementation.engine;

import java.nio.ByteBuffer;

public interface DeliverCallback {

    /**
     * Callback to notify that a message has been received. The message is
     * whole, all bytes have been accumulated.
     *
     * @param channel
     * @param bytes
     */
    public void deliver(NioChannelImpl channel, ByteBuffer bytes);

    /**
     * Callback to notify that a previously connected channel has been closed.
     *
     * @param channel
     */
    public void closed(NioChannelImpl channel);
}
