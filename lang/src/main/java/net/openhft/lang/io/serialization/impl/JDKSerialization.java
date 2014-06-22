package net.openhft.lang.io.serialization.impl;

import java.io.*;

/**
 * Created by ruedi on 22.06.14.
 */
public class JDKSerialization implements ObjectStreamFactory {
    @Override
    public ObjectOutput getObjectOutput(OutputStream out) throws IOException {
        return new ObjectOutputStream(out);
    }

    @Override
    public ObjectInput getObjectInput(InputStream in) throws IOException {
        return new ObjectInputStream(in);
    }
}
