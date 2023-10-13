/*
 */
package nl.infcomtec.pubsub;

import java.io.Serializable;

/**
 *
 * @author walter
 */
public interface Serialization {

    /**
     * Deserializer.
     *
     * @param data Hopefully from serialize.
     * @return Anything, optimized handling of CharSequence instances.
     */
    public Serializable deserialize(byte[] data);

    /**
     * Serializer.
     *
     * @param content Anything, optimized handling of CharSequence instances.
     * @return Bytes.
     */
    public byte[] serialize(Serializable content);

}
