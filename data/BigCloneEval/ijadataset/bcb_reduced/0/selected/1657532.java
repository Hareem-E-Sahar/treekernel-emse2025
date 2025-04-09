package com.enerjy.index.java;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import com.enerjy.common.jdt.SourceTokens;

/**
 * A source token scanner to produce source metrics.
 * 
 * @author michael
 */
@SuppressWarnings("restriction")
class SourceMetricsScanner {

    private final SourceTokens sourceTokens;

    private final int size;

    /**
     * Construct a source scanner on a body of source code.
     * 
     * @param source Source to scan.
     * @param sourceCompat Source compatibility level of the given source code
     */
    SourceMetricsScanner(char[] source, long sourceCompat) {
        sourceTokens = new SourceTokens(source, sourceCompat);
        size = source.length;
    }

    /**
     * Process the source into the given data builder.
     * 
     * @param data Destination for identified metrics.
     */
    void process(SourceMetricsDataBuilder data) {
        data.set(SourceMetricsData.SIZE, size);
        Set<String> operators = new HashSet<String>();
        int standAlone = 0;
        for (SourceTokens.Token token : sourceTokens) {
            if (token.isEOF()) {
                SourceTokens.Token prev = token.previousToken();
                if (null == prev) {
                    data.set(SourceMetricsData.LINES, 0);
                } else if (prev.getTokenText().endsWith("\r") || prev.getTokenText().endsWith("\n")) {
                    data.set(SourceMetricsData.LINES, token.getLineNumber() - 1L);
                } else {
                    data.set(SourceMetricsData.LINES, token.getLineNumber());
                }
                break;
            }
            if (token.isLineComment()) {
                if (isFirstOnLine(token)) {
                    data.bump(SourceMetricsData.LINE_COMMENT);
                    if (endOfMultipleLineComments(token)) {
                        bumpCommentLocationMetrics(data, token);
                    }
                }
            } else if (token.isBlockComment()) {
                processMultilineComment(data, token, SourceMetricsData.BLOCK_COMMENT);
            } else if (token.isJavadocComment()) {
                processMultilineComment(data, token, SourceMetricsData.DOC_COMMENT);
            } else if (token.isWhitespace()) {
                int lineCount = token.getNewlineCount();
                SourceTokens.Token prev = token.previousToken();
                if ((null != prev) && prev.isLineComment()) {
                    lineCount++;
                }
                if (1 < lineCount) {
                    data.bump(SourceMetricsData.WHITESPACE, lineCount - 1L);
                }
            } else if (";".equals(token.getTokenText())) {
                data.bump(SourceMetricsData.LOGICAL_LINES);
            } else if (-1 != "(){}".indexOf(token.getTokenText())) {
                if (isAloneOnLine(token)) {
                    standAlone++;
                }
            }
            if (token.isLiteral()) {
                data.bump(SourceMetricsData.OPERANDS);
            } else if (!token.isComment() && !token.isWhitespace()) {
                data.bump(SourceMetricsData.OPERATORS);
                operators.add(token.getTokenText());
            }
        }
        data.set(SourceMetricsData.UNIQUE_OPERATORS, operators.size());
        data.set(SourceMetricsData.LOC, data.get(SourceMetricsData.LINES) - data.get(SourceMetricsData.WHITESPACE) - data.get(SourceMetricsData.LINE_COMMENT) - data.get(SourceMetricsData.BLOCK_COMMENT) - data.get(SourceMetricsData.DOC_COMMENT));
        data.set(SourceMetricsData.ELOC, data.get(SourceMetricsData.LOC) - standAlone);
        data.set(SourceMetricsData.COMMENTS, data.get(SourceMetricsData.LINE_COMMENT) + data.get(SourceMetricsData.BLOCK_COMMENT) + data.get(SourceMetricsData.DOC_COMMENT));
        String chunk = new String(sourceTokens.getChunk());
        byte[] bytes = chunk.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes);
        data.set(SourceMetricsData.CRC, Double.longBitsToDouble(crc.getValue()));
    }

    private static void processMultilineComment(SourceMetricsDataBuilder data, SourceTokens.Token token, int metric) {
        boolean bumped = false;
        int newlineCount = token.getNewlineCount();
        if (0 == newlineCount) {
            if (isAloneOnLine(token) || isLastMultipleOnLine(token)) {
                data.bump(metric);
                bumped = true;
            }
        } else {
            boolean start = isFirstOnLine(token);
            boolean end = isLastOnLine(token);
            if (start && end) {
                data.bump(metric, newlineCount + 1L);
                bumped = true;
            } else if (start || end) {
                data.bump(metric, newlineCount);
                bumped = true;
            }
        }
        if (bumped) {
            bumpCommentLocationMetrics(data, token);
        }
    }

    private static void bumpCommentLocationMetrics(SourceMetricsDataBuilder data, SourceTokens.Token token) {
        if (data.inDeclarationBlock(token.getLineNumber())) {
            data.bump(SourceMetricsData.DECL_COMMENTS);
        } else {
            data.bump(SourceMetricsData.EXEC_COMMENTS);
        }
    }

    private static boolean isAloneOnLine(SourceTokens.Token token) {
        return isFirstOnLine(token) && isLastOnLine(token);
    }

    private static boolean isFirstOnLine(SourceTokens.Token token) {
        SourceTokens.Token prev = token.previousToken();
        if (null == prev) {
            return true;
        }
        if (prev.isLineComment()) {
            return true;
        }
        if (!prev.isWhitespace()) {
            return false;
        }
        if (prev.getNewlineCount() > 0) {
            return true;
        }
        SourceTokens.Token prevPrev = prev.previousToken();
        if (null == prevPrev) {
            return true;
        }
        return prevPrev.isLineComment();
    }

    private static boolean isLastOnLine(SourceTokens.Token token) {
        SourceTokens.Token next = token.nextToken();
        if (null == next) {
            return true;
        }
        if (!next.isWhitespace()) {
            return false;
        }
        return next.getNewlineCount() > 0;
    }

    private static boolean isLastMultipleOnLine(SourceTokens.Token token) {
        if (!isLastOnLine(token)) {
            return false;
        }
        for (SourceTokens.Token prev = token.previousToken(); null != prev; prev = prev.previousToken()) {
            if (!prev.isWhitespace() && !prev.isSameType(token)) {
                break;
            } else if (isFirstOnLine(prev)) {
                return true;
            }
        }
        return false;
    }

    private static boolean endOfMultipleLineComments(SourceTokens.Token token) {
        SourceTokens.Token next = token.nextToken();
        if ((null == next) || !next.isWhitespace() || (0 != next.getNewlineCount())) {
            return true;
        }
        next = next.nextToken();
        return (null == next) || !next.isLineComment();
    }
}
