package org.soybeanMilk.web.vp;

import java.util.Arrays;
import java.util.Collection;

/**
 * 变量路径匹配器。
 * @author earthAngry@gmail.com
 * @date 2010-12-16
 */
public class VariablePathMatcher {

    private VariablePath[] variablePaths;

    public VariablePathMatcher(Collection<String> paths) {
        if (paths != null) {
            VariablePath[] variablePaths = new VariablePath[paths.size()];
            int idx = 0;
            for (String str : paths) variablePaths[idx++] = new VariablePath(str);
            setVariablePaths(variablePaths);
        }
    }

    public VariablePath[] getVariablePaths() {
        return variablePaths;
    }

    protected void setVariablePaths(VariablePath[] variablePaths) {
        this.variablePaths = variablePaths;
        Arrays.sort(this.variablePaths);
    }

    /**
	 * 取得与给定字符串路径匹配的变量路径，如果没有则返回null。
	 * @param path 字符串路径，比如“a/b/c/d”、“{aaa}/bbb/{ccc}”
	 * @return 匹配结果。
	 */
    public VariablePath getMatched(String path) {
        VariablePath vp = new VariablePath(path);
        return getMatched(vp);
    }

    /**
	 * 取得与给定变量路径匹配的变量路径，如果没有则返回null。
	 * @param path 变量路径，比如“a/b/c/d”、“{aaa}/bbb/{ccc}”
	 * @return 匹配结果。
	 */
    public VariablePath getMatched(VariablePath path) {
        VariablePath[] localPaths = getVariablePaths();
        if (localPaths == null || localPaths.length == 0 || path == null) return null;
        int len = path.getPathNodeLength();
        int[] range = null;
        if (len > 0) {
            range = new int[] { 0, localPaths.length - 1 };
            for (int i = 0; i < len; i++) {
                if (range == null) break; else range = getMatchRowRange(path, i, range[0], range[1]);
            }
        }
        return range == null ? null : localPaths[range[0]];
    }

    /**
	 * 在某一列上查找与路径节点匹配的行范围
	 * @param v
	 * @param column
	 * @param rowRangeFrom 查找范围-行首
	 * @param rowRangeTo 查找范围-行末
	 * @return
	 */
    protected int[] getMatchRowRange(VariablePath v, int column, int rowRangeFrom, int rowRangeTo) {
        int[] re = new int[] { -1, -1 };
        re[0] = findEdgeEqualRowIndex(v, column, rowRangeFrom, rowRangeTo, true);
        if (re[0] != -1) re[1] = findEdgeEqualRowIndex(v, column, re[0], rowRangeTo, false);
        if (re[0] == -1) {
            PathNode pn = v.getPathNode(column);
            if (pn != null && !pn.isVariable()) {
                pn.setVariable(true);
                re[0] = findEdgeEqualRowIndex(v, column, rowRangeFrom, rowRangeTo, true);
                if (re[0] != -1) re[1] = findEdgeEqualRowIndex(v, column, re[0], rowRangeTo, false);
                pn.setVariable(false);
            }
        }
        return re[0] == -1 || re[1] == -1 ? null : re;
    }

    /**
	 * 在给定的行范围内，查找与给定变量路径<code>nodeIdx</code>位置的路径节点相等行号。
	 * @param v 比较目标
	 * @param nodeIdx 变量路径的节点位置
	 * @param rowFrom 行范围-首行号
	 * @param rowTo 行范围-末行号
	 * @param first true 查找相等的第一个行号， false 查找相等的最后行号
	 * @return 结果行号，如果查找不到，则返回-1。
	 */
    protected int findEdgeEqualRowIndex(VariablePath v, int nodeIdx, int rowFrom, int rowTo, boolean first) {
        int centerRow = rowFrom + (rowTo - rowFrom) / 2;
        PathNode[] currentRowNodes = getVariablePaths()[centerRow].getPathNodes();
        int valueNodeLen = v.getPathNodeLength();
        if (rowFrom >= rowTo) {
            if (currentRowNodes.length != valueNodeLen || currentRowNodes[nodeIdx].compareTo(v.getPathNode(nodeIdx)) != 0) return -1; else return rowFrom;
        } else {
            if (currentRowNodes.length > valueNodeLen) return findEdgeEqualRowIndex(v, nodeIdx, rowFrom, centerRow - 1, first); else if (currentRowNodes.length < valueNodeLen) return findEdgeEqualRowIndex(v, nodeIdx, centerRow + 1, rowTo, first); else {
                int compare = currentRowNodes[nodeIdx].compareTo(v.getPathNode(nodeIdx));
                if (compare < 0) return findEdgeEqualRowIndex(v, nodeIdx, centerRow + 1, rowTo, first); else if (compare > 0) return findEdgeEqualRowIndex(v, nodeIdx, rowFrom, centerRow - 1, first); else {
                    int moreCheckIdx = -1;
                    if (first) moreCheckIdx = findEdgeEqualRowIndex(v, nodeIdx, rowFrom, centerRow - 1, first); else moreCheckIdx = findEdgeEqualRowIndex(v, nodeIdx, centerRow + 1, rowTo, first);
                    return moreCheckIdx == -1 ? centerRow : moreCheckIdx;
                }
            }
        }
    }
}
