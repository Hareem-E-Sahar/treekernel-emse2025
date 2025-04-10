public class Test {    public String toNotifyString() {
        TokenStream input = parser.getTokenStream();
        for (int i = 0; i < input.size() && lastTokenConsumed != null && i <= lastTokenConsumed.getTokenIndex(); i++) {
            Token t = input.get(i);
            if (t.getChannel() != Token.DEFAULT_CHANNEL) {
                numHiddenTokens++;
                numHiddenCharsMatched += t.getText().length();
            }
        }
        numCharsMatched = lastTokenConsumed.getStopIndex() + 1;
        decisionMaxFixedLookaheads = trim(decisionMaxFixedLookaheads, numFixedDecisions);
        decisionMaxCyclicLookaheads = trim(decisionMaxCyclicLookaheads, numCyclicDecisions);
        StringBuffer buf = new StringBuffer();
        buf.append(Version);
        buf.append('\t');
        buf.append(parser.getClass().getName());
        buf.append('\t');
        buf.append(numRuleInvocations);
        buf.append('\t');
        buf.append(maxRuleInvocationDepth);
        buf.append('\t');
        buf.append(numFixedDecisions);
        buf.append('\t');
        buf.append(Stats.min(decisionMaxFixedLookaheads));
        buf.append('\t');
        buf.append(Stats.max(decisionMaxFixedLookaheads));
        buf.append('\t');
        buf.append(Stats.avg(decisionMaxFixedLookaheads));
        buf.append('\t');
        buf.append(Stats.stddev(decisionMaxFixedLookaheads));
        buf.append('\t');
        buf.append(numCyclicDecisions);
        buf.append('\t');
        buf.append(Stats.min(decisionMaxCyclicLookaheads));
        buf.append('\t');
        buf.append(Stats.max(decisionMaxCyclicLookaheads));
        buf.append('\t');
        buf.append(Stats.avg(decisionMaxCyclicLookaheads));
        buf.append('\t');
        buf.append(Stats.stddev(decisionMaxCyclicLookaheads));
        buf.append('\t');
        buf.append(numBacktrackDecisions);
        buf.append('\t');
        buf.append(Stats.min(toArray(decisionMaxSynPredLookaheads)));
        buf.append('\t');
        buf.append(Stats.max(toArray(decisionMaxSynPredLookaheads)));
        buf.append('\t');
        buf.append(Stats.avg(toArray(decisionMaxSynPredLookaheads)));
        buf.append('\t');
        buf.append(Stats.stddev(toArray(decisionMaxSynPredLookaheads)));
        buf.append('\t');
        buf.append(numSemanticPredicates);
        buf.append('\t');
        buf.append(parser.getTokenStream().size());
        buf.append('\t');
        buf.append(numHiddenTokens);
        buf.append('\t');
        buf.append(numCharsMatched);
        buf.append('\t');
        buf.append(numHiddenCharsMatched);
        buf.append('\t');
        buf.append(numberReportedErrors);
        buf.append('\t');
        buf.append(numMemoizationCacheHits);
        buf.append('\t');
        buf.append(numMemoizationCacheMisses);
        buf.append('\t');
        buf.append(numGuessingRuleInvocations);
        buf.append('\t');
        buf.append(numMemoizationCacheEntries);
        return buf.toString();
    }
}