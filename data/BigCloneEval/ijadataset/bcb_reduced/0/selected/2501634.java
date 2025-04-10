package jpcsp.HLE.kernel.types;

import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.managers.SceUidManager;
import jpcsp.HLE.modules150.SysMemUserForUser.SysMemInfo;

public class SceKernelFplInfo extends pspAbstractMemoryMappedStructureVariableLength {

    public final String name;

    public final int attr;

    public final int blockSize;

    public final int numBlocks;

    public int freeBlocks;

    public int numWaitThreads;

    private final SysMemInfo sysMemInfo;

    public final int uid;

    public final int partitionid;

    public int[] blockAddress;

    public boolean[] blockAllocated;

    /** do not instantiate unless there is enough free mem.
     * use the static helper function tryCreateFpl. */
    private SceKernelFplInfo(String name, int partitionid, int attr, int blockSize, int numBlocks, int memType, int memAlign) {
        this.name = name;
        this.attr = attr;
        this.blockSize = blockSize;
        this.numBlocks = numBlocks;
        freeBlocks = numBlocks;
        numWaitThreads = 0;
        uid = SceUidManager.getNewUid("ThreadMan-Fpl");
        this.partitionid = partitionid;
        blockAddress = new int[numBlocks];
        blockAllocated = new boolean[numBlocks];
        for (int i = 0; i < numBlocks; i++) {
            blockAllocated[i] = false;
        }
        int alignedBlockSize = (blockSize + (memAlign - 1)) & (~(memAlign - 1));
        int totalFplSize = alignedBlockSize * numBlocks;
        sysMemInfo = Modules.SysMemUserForUserModule.malloc(partitionid, String.format("ThreadMan-Fpl-0x%x-%s", uid, name), memType, totalFplSize, 0);
        if (sysMemInfo == null) {
            throw new RuntimeException("SceKernelFplInfo: not enough free mem");
        }
        for (int i = 0; i < numBlocks; i++) {
            blockAddress[i] = sysMemInfo.addr + alignedBlockSize * i;
        }
    }

    public static SceKernelFplInfo tryCreateFpl(String name, int partitionid, int attr, int blockSize, int numBlocks, int memType, int memAlign) {
        SceKernelFplInfo info = null;
        int alignedBlockSize = (blockSize + (memAlign - 1)) & (~(memAlign - 1));
        int totalFplSize = alignedBlockSize * numBlocks;
        int maxFreeSize = Modules.SysMemUserForUserModule.maxFreeMemSize();
        if (totalFplSize <= maxFreeSize) {
            info = new SceKernelFplInfo(name, partitionid, attr, blockSize, numBlocks, memType, memAlign);
        } else {
            Modules.log.warn("tryCreateFpl not enough free mem (want=" + totalFplSize + ", free=" + maxFreeSize + ", diff=" + (totalFplSize - maxFreeSize) + ")");
        }
        return info;
    }

    @Override
    protected void write() {
        super.write();
        writeStringNZ(32, name);
        write32(attr);
        write32(blockSize);
        write32(numBlocks);
        write32(freeBlocks);
        write32(numWaitThreads);
    }

    public boolean isBlockAllocated(int blockId) {
        return blockAllocated[blockId];
    }

    public void freeBlock(int blockId) {
        if (!isBlockAllocated(blockId)) throw new IllegalArgumentException("Block " + blockId + " is not allocated");
        blockAllocated[blockId] = false;
        freeBlocks++;
    }

    /** @return the address of the allocated block */
    public int allocateBlock(int blockId) {
        if (isBlockAllocated(blockId)) throw new IllegalArgumentException("Block " + blockId + " is already allocated");
        blockAllocated[blockId] = true;
        freeBlocks--;
        return blockAddress[blockId];
    }

    /** @return the block index or -1 on failure */
    public int findFreeBlock() {
        for (int i = 0; i < numBlocks; i++) if (!isBlockAllocated(i)) return i;
        return -1;
    }

    /** @return the block index or -1 on failure */
    public int findBlockByAddress(int addr) {
        for (int i = 0; i < numBlocks; i++) if (blockAddress[i] == addr) return i;
        return -1;
    }

    public void deleteSysMemInfo() {
        Modules.SysMemUserForUserModule.free(sysMemInfo);
    }
}
