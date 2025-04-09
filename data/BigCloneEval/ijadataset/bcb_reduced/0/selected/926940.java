package cunei.lm;

import java.util.Arrays;
import cunei.bits.ResizableUnsignedArray;
import cunei.bits.UnsignedArray;
import cunei.config.Configuration;

public class BackoffLanguageModelTrie extends BackoffLanguageModel {

    private static final long serialVersionUID = 1L;

    private UnsignedArray[] typeBounds;

    private UnsignedArray[] historyIds;

    public BackoffLanguageModelTrie(final Configuration config) {
        super(config);
    }

    protected final int getNgramTotal(int indexId) {
        if (indexId == 0) return types.size();
        if (historyIds == null || indexId > historyIds.length) return 0;
        return historyIds[indexId - 1].size();
    }

    protected final int getNgramId(final int historyIndexId, final int indexHistoryId, final int typeId) {
        if (historyIds == null || typeBounds == null) return -1;
        if (historyIndexId < 0 || historyIndexId >= historyIds.length) return -1;
        final UnsignedArray subHistoryIds = historyIds[historyIndexId];
        if (subHistoryIds == null) return -1;
        final UnsignedArray subTypeBounds = typeBounds[historyIndexId];
        if (subTypeBounds == null || typeId >= subTypeBounds.size()) return -1;
        int lowerBound = typeId == 0 ? 0 : (int) subTypeBounds.get(typeId - 1);
        int upperBound = (int) subTypeBounds.get(typeId);
        while (lowerBound != upperBound) {
            final int indexNgramId = lowerBound + (upperBound - lowerBound) / 2;
            final long cmp = subHistoryIds.get(indexNgramId) - indexHistoryId;
            if (cmp > 0) upperBound = indexNgramId; else if (cmp < 0) lowerBound = indexNgramId + 1; else return indexNgramId;
        }
        return -1;
    }

    protected final int addNgramId(final int historyIndexId, final int indexHistoryId, final int typeId) {
        assert indexHistoryId != -1;
        if (historyIds == null) historyIds = new UnsignedArray[historyIndexId + 1]; else if (historyIds.length < historyIndexId + 1) historyIds = Arrays.copyOf(historyIds, historyIndexId + 1);
        final int indexNgramId;
        UnsignedArray subHistoryIds = historyIds[historyIndexId];
        if (subHistoryIds == null) {
            indexNgramId = 0;
            subHistoryIds = new ResizableUnsignedArray();
            historyIds[historyIndexId] = subHistoryIds;
        } else {
            indexNgramId = subHistoryIds.size();
        }
        subHistoryIds.set(indexNgramId, indexHistoryId);
        return indexNgramId;
    }

    protected UnsignedArray getHistoryIds(final int indexId) {
        return historyIds[indexId - 1];
    }

    protected void setTypeBounds(final int indexId, final UnsignedArray typeIds) {
        final int totalHistoryIds = typeIds.size();
        final UnsignedArray subTypeBounds = new UnsignedArray(types.size(), totalHistoryIds);
        int typeId = 0;
        for (int bound = 0; bound < totalHistoryIds; ) {
            if (typeId == typeIds.get(bound)) {
                bound++;
            } else {
                assert typeId < typeIds.get(bound);
                subTypeBounds.set(typeId, bound);
                typeId++;
            }
        }
        subTypeBounds.set(typeId, totalHistoryIds);
        if (typeBounds == null) typeBounds = new UnsignedArray[indexId]; else if (typeBounds.length < indexId) typeBounds = Arrays.copyOf(typeBounds, indexId);
        typeBounds[indexId - 1] = subTypeBounds;
    }

    protected void complete(final int size) {
        super.complete(size);
        if (size == 1) return;
        typeBounds[size - 2].compress();
        historyIds[size - 2].compress();
    }

    public void load(String path) {
        super.load(path);
        for (int i = 0; i < typeBounds.length; i++) {
            if (typeBounds[i] != null) typeBounds[i].load(path);
        }
        for (int i = 0; i < historyIds.length; i++) {
            if (historyIds[i] != null) historyIds[i].load(path);
        }
    }

    public BackoffLanguageModelTrie save(String path, String name) {
        super.save(path, name);
        for (int i = 0; i < typeBounds.length; i++) {
            String subName = name + "-" + (i + 2) + "-types";
            if (typeBounds[i] != null) typeBounds[i] = typeBounds[i].save(path, subName);
        }
        for (int i = 0; i < historyIds.length; i++) {
            String subName = name + "-" + (i + 2) + "-history-ids";
            if (historyIds[i] != null) historyIds[i] = historyIds[i].save(path, subName);
        }
        return this;
    }
}
