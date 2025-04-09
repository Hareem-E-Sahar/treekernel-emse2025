package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Internal class for associating comments with AST nodes.
 * 
 * @since 3.0
 */
class DefaultCommentMapper {

    Comment[] comments;

    Scanner scanner;

    int leadingPtr;

    ASTNode[] leadingNodes;

    long[] leadingIndexes;

    int trailingPtr, lastTrailingPtr;

    ASTNode[] trailingNodes;

    long[] trailingIndexes;

    static final int STORAGE_INCREMENT = 16;

    /**
	 * @param table the given table of comments
	 */
    DefaultCommentMapper(Comment[] table) {
        this.comments = table;
    }

    boolean hasSameTable(Comment[] table) {
        return this.comments == table;
    }

    /**
	 * Get comment of the list which includes a given position
	 * 
	 * @param position The position belonging to the looked up comment
	 * @return comment which includes the given position or null if none was found
	 */
    Comment getComment(int position) {
        if (this.comments == null) {
            return null;
        }
        int size = this.comments.length;
        if (size == 0) {
            return null;
        }
        int index = getCommentIndex(0, position, 0);
        if (index < 0) {
            return null;
        }
        return this.comments[index];
    }

    private int getCommentIndex(int start, int position, int exact) {
        if (position == 0) {
            if (this.comments.length > 0 && this.comments[0].getStartPosition() == 0) {
                return 0;
            }
            return -1;
        }
        int bottom = start, top = this.comments.length - 1;
        int i = 0, index = -1;
        Comment comment = null;
        while (bottom <= top) {
            i = bottom + (top - bottom) / 2;
            comment = this.comments[i];
            int commentStart = comment.getStartPosition();
            if (position < commentStart) {
                top = i - 1;
            } else if (position >= (commentStart + comment.getLength())) {
                bottom = i + 1;
            } else {
                index = i;
                break;
            }
        }
        if (index < 0 && exact != 0) {
            comment = this.comments[i];
            if (position < comment.getStartPosition()) {
                return exact < 0 ? i - 1 : i;
            } else {
                return exact < 0 ? i : i + 1;
            }
        }
        return index;
    }

    /**
	 * Returns the extended start position of the given node. Unlike
	 * {@link ASTNode#getStartPosition()} and {@link ASTNode#getLength()},
	 * the extended source range may include comments and whitespace
	 * immediately before or after the normal source range for the node.
	 * 
	 * @param node the node
	 * @return the 0-based character index, or <code>-1</code>
	 *    if no source position information is recorded for this node
	 * @see #getExtendedLength(ASTNode)
	 * @since 3.0
	 */
    public int getExtendedStartPosition(ASTNode node) {
        if (this.leadingPtr >= 0) {
            long range = -1;
            for (int i = 0; range < 0 && i <= this.leadingPtr; i++) {
                if (this.leadingNodes[i] == node) range = this.leadingIndexes[i];
            }
            if (range >= 0) {
                return this.comments[(int) (range >> 32)].getStartPosition();
            }
        }
        return node.getStartPosition();
    }

    public final int getLineNumber(int position, int[] lineRange) {
        int[] lineEnds = this.scanner.lineEnds;
        int length = lineEnds.length;
        return Util.getLineNumber(position, lineEnds, (lineRange[0] > length ? length : lineRange[0]) - 1, (lineRange[1] > length ? length : lineRange[1]) - 1);
    }

    public int getExtendedEnd(ASTNode node) {
        int end = node.getStartPosition() + node.getLength();
        if (this.trailingPtr >= 0) {
            long range = -1;
            for (int i = 0; range < 0 && i <= this.trailingPtr; i++) {
                if (this.trailingNodes[i] == node) range = this.trailingIndexes[i];
            }
            if (range >= 0) {
                Comment lastComment = this.comments[(int) range];
                end = lastComment.getStartPosition() + lastComment.getLength();
            }
        }
        return end - 1;
    }

    /**
	 * Returns the extended source length of the given node. Unlike
	 * {@link ASTNode#getStartPosition()} and {@link ASTNode#getLength()},
	 * the extended source range may include comments and whitespace
	 * immediately before or after the normal source range for the node.
	 * 
	 * @param node the node
	 * @return a (possibly 0) length, or <code>0</code>
	 *    if no source position information is recorded for this node
	 * @see #getExtendedStartPosition(ASTNode)
	 * @see #getExtendedEnd(ASTNode)
	 * @since 3.0
	 */
    public int getExtendedLength(ASTNode node) {
        return getExtendedEnd(node) - getExtendedStartPosition(node) + 1;
    }

    /**
	 * Return index of first leading comment of a given node.
	 * 
	 * @param node
	 * @return index of first leading comment or -1 if node has no leading comment
	 */
    int firstLeadingCommentIndex(ASTNode node) {
        if (this.leadingPtr >= 0) {
            for (int i = 0; i <= this.leadingPtr; i++) {
                if (this.leadingNodes[i] == node) {
                    return (int) (this.leadingIndexes[i] >> 32);
                }
            }
        }
        return -1;
    }

    /**
	 * Return index of last trailing comment of a given node.
	 * 
	 * @param node
	 * @return index of last trailing comment or -1 if node has no trailing comment
	 */
    int lastTrailingCommentIndex(ASTNode node) {
        if (this.trailingPtr >= 0) {
            for (int i = 0; i <= this.trailingPtr; i++) {
                if (this.trailingNodes[i] == node) {
                    return (int) this.trailingIndexes[i];
                }
            }
        }
        return -1;
    }

    void initialize(CompilationUnit unit, Scanner sc) {
        this.leadingPtr = -1;
        this.trailingPtr = -1;
        this.comments = unit.optionalCommentTable;
        if (this.comments == null) {
            return;
        }
        int size = this.comments.length;
        if (size == 0) {
            return;
        }
        this.scanner = sc;
        this.scanner.tokenizeWhiteSpace = true;
        DefaultASTVisitor commentVisitor = new CommentMapperVisitor();
        unit.accept(commentVisitor);
        int leadingCount = this.leadingPtr + 1;
        if (leadingCount > 0 && leadingCount < this.leadingIndexes.length) {
            System.arraycopy(this.leadingNodes, 0, this.leadingNodes = new ASTNode[leadingCount], 0, leadingCount);
            System.arraycopy(this.leadingIndexes, 0, this.leadingIndexes = new long[leadingCount], 0, leadingCount);
        }
        if (this.trailingPtr >= 0) {
            while (this.trailingIndexes[this.trailingPtr] == -1) {
                this.trailingPtr--;
                if (this.trailingPtr < 0) {
                    this.trailingIndexes = null;
                    this.trailingNodes = null;
                    break;
                }
            }
            int trailingCount = this.trailingPtr + 1;
            if (trailingCount > 0 && trailingCount < this.trailingIndexes.length) {
                System.arraycopy(this.trailingNodes, 0, this.trailingNodes = new ASTNode[trailingCount], 0, trailingCount);
                System.arraycopy(this.trailingIndexes, 0, this.trailingIndexes = new long[trailingCount], 0, trailingCount);
            }
        }
        this.scanner = null;
    }

    /**
	 * Search and store node leading comments. Comments are searched in position range
	 * from previous extended position to node start position. If one or several comment are found,
	 * returns first comment start position, otherwise returns node start position.
	 * <p>
	 * Starts to search for first comment before node start position and return if none was found...
	 *</p><p>
	 * When first comment is found before node, goes up in comment list until one of
	 * following conditions becomes true:
	 * <ol>
	 * 	<li>comment end is before previous end</li>
	 * 	<li>comment start and previous end is on the same line but not on same line of node start</li>
	 * 	<li>there's other than white characters between current node and comment</li>
	 * 	<li>there's more than 1 line between current node and comment</li>
	 * </ol>
	 * If some comment have been found, then no token should be on
	 * on the same line before, so remove all comments which do not verify this assumption.
	 * </p><p>
	 * If finally there's leading still comments, then stores indexes of the first and last one
	 * in leading comments table.
	 */
    int storeLeadingComments(ASTNode node, int previousEnd, int[] parentLineRange) {
        int nodeStart = node.getStartPosition();
        int extended = nodeStart;
        int previousEndLine = getLineNumber(previousEnd, parentLineRange);
        int nodeStartLine = getLineNumber(nodeStart, parentLineRange);
        int idx = getCommentIndex(0, nodeStart, -1);
        if (idx == -1) {
            return nodeStart;
        }
        int startIdx = -1;
        int endIdx = idx;
        int previousStart = nodeStart;
        while (idx >= 0 && previousStart >= previousEnd) {
            Comment comment = this.comments[idx];
            int commentStart = comment.getStartPosition();
            int end = commentStart + comment.getLength() - 1;
            int commentLine = getLineNumber(commentStart, parentLineRange);
            if (end <= previousEnd || (commentLine == previousEndLine && commentLine != nodeStartLine)) {
                break;
            } else if ((end + 1) < previousStart) {
                this.scanner.resetTo(end + 1, previousStart);
                try {
                    int token = this.scanner.getNextToken();
                    if (token != TerminalTokens.TokenNameWHITESPACE || this.scanner.currentPosition != previousStart) {
                        if (idx == endIdx) {
                            return nodeStart;
                        }
                        break;
                    }
                } catch (InvalidInputException e) {
                    return nodeStart;
                }
                char[] gap = this.scanner.getCurrentIdentifierSource();
                int nbrLine = 0;
                int pos = -1;
                while ((pos = CharOperation.indexOf('\n', gap, pos + 1)) >= 0) {
                    nbrLine++;
                }
                if (nbrLine > 1) {
                    break;
                }
            }
            previousStart = commentStart;
            startIdx = idx--;
        }
        if (startIdx != -1) {
            int commentStart = this.comments[startIdx].getStartPosition();
            if (previousEnd < commentStart && previousEndLine != nodeStartLine) {
                int lastTokenEnd = previousEnd;
                this.scanner.resetTo(previousEnd, commentStart);
                try {
                    while (this.scanner.currentPosition < commentStart) {
                        if (this.scanner.getNextToken() != TerminalTokens.TokenNameWHITESPACE) {
                            lastTokenEnd = this.scanner.getCurrentTokenEndPosition();
                        }
                    }
                } catch (InvalidInputException e) {
                }
                int lastTokenLine = getLineNumber(lastTokenEnd, parentLineRange);
                int length = this.comments.length;
                while (startIdx < length && lastTokenLine == getLineNumber(this.comments[startIdx].getStartPosition(), parentLineRange) && nodeStartLine != lastTokenLine) {
                    startIdx++;
                }
            }
            if (startIdx <= endIdx) {
                if (++this.leadingPtr == 0) {
                    this.leadingNodes = new ASTNode[STORAGE_INCREMENT];
                    this.leadingIndexes = new long[STORAGE_INCREMENT];
                } else if (this.leadingPtr == this.leadingNodes.length) {
                    int newLength = (this.leadingPtr * 3 / 2) + STORAGE_INCREMENT;
                    System.arraycopy(this.leadingNodes, 0, this.leadingNodes = new ASTNode[newLength], 0, this.leadingPtr);
                    System.arraycopy(this.leadingIndexes, 0, this.leadingIndexes = new long[newLength], 0, this.leadingPtr);
                }
                this.leadingNodes[this.leadingPtr] = node;
                this.leadingIndexes[this.leadingPtr] = (((long) startIdx) << 32) + endIdx;
                extended = this.comments[endIdx].getStartPosition();
            }
        }
        return extended;
    }

    /**
	 * Search and store node trailing comments. Comments are searched in position range
	 * from node end position to specified next start. If one or several comment are found,
	 * returns last comment end position, otherwise returns node end position.
	 * <p>
	 * Starts to search for first comment after node end position and return if none was found...
	 *</p><p>
	 * When first comment is found after node, goes down in comment list until one of
	 * following conditions becomes true:
	 * <ol>
	 * 	<li>comment start is after next start</li>
	 * 	<li>there's other than white characters between current node and comment</li>
	 * 	<li>there's more than 1 line between current node and comment</li>
	 *</ol>
	 * If at least potential comments have been found, then all of them has to be separated
	 * from following node. So, remove all comments which do not verify this assumption.
	 * Note that this verification is not applicable on last node.
	 * </p><p>
	 * If finally there's still trailing comments, then stores indexes of the first and last one
	 * in trailing comments table.
	 */
    int storeTrailingComments(ASTNode node, int nextStart, boolean lastChild, int[] parentLineRange) {
        int nodeEnd = node.getStartPosition() + node.getLength() - 1;
        if (nodeEnd == nextStart) {
            if (++this.trailingPtr == 0) {
                this.trailingNodes = new ASTNode[STORAGE_INCREMENT];
                this.trailingIndexes = new long[STORAGE_INCREMENT];
                this.lastTrailingPtr = -1;
            } else if (this.trailingPtr == this.trailingNodes.length) {
                int newLength = (this.trailingPtr * 3 / 2) + STORAGE_INCREMENT;
                System.arraycopy(this.trailingNodes, 0, this.trailingNodes = new ASTNode[newLength], 0, this.trailingPtr);
                System.arraycopy(this.trailingIndexes, 0, this.trailingIndexes = new long[newLength], 0, this.trailingPtr);
            }
            this.trailingNodes[this.trailingPtr] = node;
            this.trailingIndexes[this.trailingPtr] = -1;
            return nodeEnd;
        }
        int extended = nodeEnd;
        int nodeEndLine = getLineNumber(nodeEnd, parentLineRange);
        int idx = getCommentIndex(0, nodeEnd, 1);
        if (idx == -1) {
            return nodeEnd;
        }
        int startIdx = idx;
        int endIdx = -1;
        int length = this.comments.length;
        int commentStart = extended + 1;
        int previousEnd = nodeEnd + 1;
        int sameLineIdx = -1;
        while (idx < length && commentStart < nextStart) {
            Comment comment = this.comments[idx];
            commentStart = comment.getStartPosition();
            if (commentStart >= nextStart) {
                break;
            } else if (previousEnd < commentStart) {
                this.scanner.resetTo(previousEnd, commentStart);
                try {
                    int token = this.scanner.getNextToken();
                    if (token != TerminalTokens.TokenNameWHITESPACE || this.scanner.currentPosition != commentStart) {
                        if (idx == startIdx) {
                            return nodeEnd;
                        }
                        break;
                    }
                } catch (InvalidInputException e) {
                    return nodeEnd;
                }
                char[] gap = this.scanner.getCurrentIdentifierSource();
                int nbrLine = 0;
                int pos = -1;
                while ((pos = CharOperation.indexOf('\n', gap, pos + 1)) >= 0) {
                    nbrLine++;
                }
                if (nbrLine > 1) {
                    break;
                }
            }
            int commentLine = getLineNumber(commentStart, parentLineRange);
            if (commentLine == nodeEndLine) {
                sameLineIdx = idx;
            }
            previousEnd = commentStart + comment.getLength();
            endIdx = idx++;
        }
        if (endIdx != -1) {
            if (!lastChild) {
                int nextLine = getLineNumber(nextStart, parentLineRange);
                int previousLine = getLineNumber(previousEnd, parentLineRange);
                if ((nextLine - previousLine) <= 1) {
                    if (sameLineIdx == -1) return nodeEnd;
                    endIdx = sameLineIdx;
                }
            }
            if (++this.trailingPtr == 0) {
                this.trailingNodes = new ASTNode[STORAGE_INCREMENT];
                this.trailingIndexes = new long[STORAGE_INCREMENT];
                this.lastTrailingPtr = -1;
            } else if (this.trailingPtr == this.trailingNodes.length) {
                int newLength = (this.trailingPtr * 3 / 2) + STORAGE_INCREMENT;
                System.arraycopy(this.trailingNodes, 0, this.trailingNodes = new ASTNode[newLength], 0, this.trailingPtr);
                System.arraycopy(this.trailingIndexes, 0, this.trailingIndexes = new long[newLength], 0, this.trailingPtr);
            }
            this.trailingNodes[this.trailingPtr] = node;
            long nodeRange = (((long) startIdx) << 32) + endIdx;
            this.trailingIndexes[this.trailingPtr] = nodeRange;
            extended = this.comments[endIdx].getStartPosition() + this.comments[endIdx].getLength() - 1;
            ASTNode previousNode = node;
            int ptr = this.trailingPtr - 1;
            while (ptr >= 0) {
                long range = this.trailingIndexes[ptr];
                if (range != -1) break;
                ASTNode unresolved = this.trailingNodes[ptr];
                if (previousNode != unresolved.getParent()) break;
                this.trailingIndexes[ptr] = nodeRange;
                previousNode = unresolved;
                ptr--;
            }
            if (ptr > this.lastTrailingPtr) {
                int offset = ptr - this.lastTrailingPtr;
                for (int i = ptr + 1; i <= this.trailingPtr; i++) {
                    this.trailingNodes[i - offset] = this.trailingNodes[i];
                    this.trailingIndexes[i - offset] = this.trailingIndexes[i];
                }
                this.trailingPtr -= offset;
            }
            this.lastTrailingPtr = this.trailingPtr;
        }
        return extended;
    }

    class CommentMapperVisitor extends DefaultASTVisitor {

        ASTNode topSiblingParent = null;

        ASTNode[] siblings = new ASTNode[10];

        int[][] parentLineRange = new int[10][];

        int siblingPtr = -1;

        protected boolean visitNode(ASTNode node) {
            ASTNode parent = node.getParent();
            int previousEnd = parent.getStartPosition();
            ASTNode sibling = parent == this.topSiblingParent ? (ASTNode) this.siblings[this.siblingPtr] : null;
            if (sibling != null) {
                try {
                    previousEnd = storeTrailingComments(sibling, node.getStartPosition(), false, this.parentLineRange[this.siblingPtr]);
                } catch (Exception ex) {
                }
            }
            if ((node.typeAndFlags & ASTNode.MALFORMED) != 0) {
                return false;
            }
            int[] previousLineRange = this.siblingPtr > -1 ? this.parentLineRange[this.siblingPtr] : new int[] { 1, DefaultCommentMapper.this.scanner.linePtr + 1 };
            try {
                storeLeadingComments(node, previousEnd, previousLineRange);
            } catch (Exception ex) {
            }
            if (this.topSiblingParent != parent) {
                if (this.siblings.length == ++this.siblingPtr) {
                    System.arraycopy(this.siblings, 0, this.siblings = new ASTNode[this.siblingPtr * 2], 0, this.siblingPtr);
                    System.arraycopy(this.parentLineRange, 0, this.parentLineRange = new int[this.siblingPtr * 2][], 0, this.siblingPtr);
                }
                if (this.topSiblingParent == null) {
                    this.parentLineRange[this.siblingPtr] = previousLineRange;
                } else {
                    int parentStart = parent.getStartPosition();
                    int firstLine = getLineNumber(parentStart, previousLineRange);
                    int lastLine = getLineNumber(parentStart + parent.getLength() - 1, previousLineRange);
                    if (this.parentLineRange[this.siblingPtr] == null) {
                        this.parentLineRange[this.siblingPtr] = new int[] { firstLine, lastLine };
                    } else {
                        int[] lineRange = this.parentLineRange[this.siblingPtr];
                        lineRange[0] = firstLine;
                        lineRange[1] = lastLine;
                    }
                }
                this.topSiblingParent = parent;
            }
            this.siblings[this.siblingPtr] = node;
            return true;
        }

        protected void endVisitNode(ASTNode node) {
            ASTNode sibling = this.topSiblingParent == node ? (ASTNode) this.siblings[this.siblingPtr] : null;
            if (sibling != null) {
                try {
                    storeTrailingComments(sibling, node.getStartPosition() + node.getLength() - 1, true, this.parentLineRange[this.siblingPtr]);
                } catch (Exception ex) {
                }
            }
            if (this.topSiblingParent != null && this.topSiblingParent == node) {
                this.siblingPtr--;
                this.topSiblingParent = node.getParent();
            }
        }

        public boolean visit(CompilationUnit node) {
            return true;
        }
    }
}
