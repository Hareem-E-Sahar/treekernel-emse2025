package com.google.gwt.user.linker.rpc;

import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.dev.util.DiskCache;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This artifact holds a log of the reasoning for which types are considered
 * serializable for a particular RPC interface.
 */
public class RpcLogArtifact extends Artifact<RpcLogArtifact> {

    /**
   * This strong name indicates that the artifact doesn't really have its own
   * strong name.
   */
    public static final String UNSPECIFIED_STRONGNAME = "UNSPECIFIED";

    private static DiskCache diskCache = DiskCache.INSTANCE;

    private long diskCacheToken;

    private final String qualifiedSourceName;

    private final String serializationPolicyStrongName;

    public RpcLogArtifact(String qualifiedSourceName, String serializationPolicyStrongName, String rpcLog) {
        super(RpcLogLinker.class);
        this.qualifiedSourceName = qualifiedSourceName;
        this.serializationPolicyStrongName = serializationPolicyStrongName;
        diskCacheToken = diskCache.writeString(rpcLog);
    }

    public byte[] getContents() {
        return diskCache.readByteArray(diskCacheToken);
    }

    public String getQualifiedSourceName() {
        return qualifiedSourceName;
    }

    public String getSerializationPolicyStrongName() {
        return serializationPolicyStrongName;
    }

    @Override
    public int hashCode() {
        return serializationPolicyStrongName.hashCode();
    }

    @Override
    protected int compareToComparableArtifact(RpcLogArtifact o) {
        int comp;
        comp = qualifiedSourceName.compareTo(o.getQualifiedSourceName());
        if (comp != 0) {
            return comp;
        }
        return serializationPolicyStrongName.compareTo(o.getSerializationPolicyStrongName());
    }

    @Override
    protected Class<RpcLogArtifact> getComparableArtifactType() {
        return RpcLogArtifact.class;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        diskCacheToken = diskCache.transferFromStream(stream);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        diskCache.transferToStream(diskCacheToken, stream);
    }
}
