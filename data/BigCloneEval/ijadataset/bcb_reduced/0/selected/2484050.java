package diff.misc;

import java.util.*;
import com.qarks.util.*;

public class MyersDiff {

    public static FileDiffResult diff(ParsedFile orig, ParsedFile rev, Cancellable cancellable) {
        return diff(orig.getLines(), rev.getLines(), cancellable);
    }

    public static FileDiffResult diff(ParsedFile orig, ParsedFile rev) {
        return diff(orig.getLines(), rev.getLines(), new CancellableImpl());
    }

    public static FileDiffResult diff(FileLine[] orig, FileLine[] rev, Cancellable cancellable) {
        FileDiffResult result = null;
        PathNode path = buildPath(orig, rev, cancellable);
        if (path != null) {
            result = buildRevision(path, orig, rev);
        }
        return result;
    }

    public static PathNode buildPath(FileLine[] orig, FileLine[] rev, Cancellable cancellable) {
        if (orig == null) {
            throw new IllegalArgumentException("original sequence is null");
        }
        if (rev == null) {
            throw new IllegalArgumentException("revised sequence is null");
        }
        final int N = orig.length;
        final int M = rev.length;
        final int MAX = N + M + 1;
        final int size = 1 + 2 * MAX;
        final int middle = (size + 1) / 2;
        final PathNode diagonal[] = new PathNode[size];
        diagonal[middle + 1] = new Snake(0, -1, null);
        for (int d = 0; d < MAX; d++) {
            if (cancellable.isCancelled()) {
                return null;
            }
            for (int k = -d; k <= d; k += 2) {
                if (cancellable.isCancelled()) {
                    return null;
                }
                final int kmiddle = middle + k;
                final int kplus = kmiddle + 1;
                final int kminus = kmiddle - 1;
                PathNode prev = null;
                int i;
                if ((k == -d) || (k != d && diagonal[kminus].i < diagonal[kplus].i)) {
                    i = diagonal[kplus].i;
                    prev = diagonal[kplus];
                } else {
                    i = diagonal[kminus].i + 1;
                    prev = diagonal[kminus];
                }
                diagonal[kminus] = null;
                int j = i - k;
                PathNode node = new DiffNode(i, j, prev);
                while (i < N && j < M && orig[i].matches(rev[j])) {
                    i++;
                    j++;
                }
                if (i > node.i) node = new Snake(i, j, node);
                diagonal[kmiddle] = node;
                if (i >= N && j >= M) {
                    return diagonal[kmiddle];
                }
            }
            diagonal[middle + d - 1] = null;
        }
        return null;
    }

    public static FileDiffResult buildRevision(PathNode path, FileLine[] orig, FileLine[] rev) {
        ArrayList<Delta> deltas = new ArrayList<Delta>();
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        if (orig == null) {
            throw new IllegalArgumentException("original sequence is null");
        }
        if (rev == null) {
            throw new IllegalArgumentException("revised sequence is null");
        }
        if (path.isSnake()) {
            path = path.prev;
        }
        while (path != null && path.prev != null && path.prev.j >= 0) {
            if (path.isSnake()) {
                throw new IllegalStateException("bad diffpath: found snake when looking for diff");
            }
            int i = path.i;
            int j = path.j;
            path = path.prev;
            int ianchor = path.i;
            int janchor = path.j;
            Delta delta = new Delta(new Chunk(ianchor, i - ianchor), new Chunk(janchor, j - janchor));
            deltas.add(0, delta);
            if (path.isSnake()) {
                path = path.prev;
            }
        }
        int ioffset = 0;
        int joffset = 0;
        for (Delta delta : deltas) {
            Chunk left = delta.getOriginal();
            Chunk right = delta.getRevised();
            int status = (left.count == 0 || right.count == 0) ? FileLine.NO_MATCH : FileLine.MODIFIED;
            for (int i = ioffset; i < left.first(); i++) {
                if (i < orig.length) {
                    FileLine line = orig[i];
                    line.setStatus(FileLine.UNCHANGED);
                }
            }
            ioffset = left.first();
            for (int i = ioffset; i < (ioffset + left.count); i++) {
                if (i < orig.length) {
                    FileLine line = orig[i];
                    line.setStatus(status);
                }
            }
            ioffset += left.count;
            for (int i = joffset; i < right.first(); i++) {
                if (i < rev.length) {
                    FileLine line = rev[i];
                    line.setStatus(FileLine.UNCHANGED);
                }
            }
            joffset = right.first();
            for (int i = joffset; i < (joffset + right.count); i++) {
                if (i < rev.length) {
                    FileLine line = rev[i];
                    line.setStatus(status);
                }
            }
            joffset += right.count;
        }
        for (int i = ioffset; i < orig.length; i++) {
            orig[i].setStatus(FileLine.UNCHANGED);
        }
        for (int i = joffset; i < rev.length; i++) {
            rev[i].setStatus(FileLine.UNCHANGED);
        }
        FileDiffResult diffResult = new FileDiffResult(new ParsedFile(orig), new ParsedFile(rev));
        return diffResult;
    }
}
