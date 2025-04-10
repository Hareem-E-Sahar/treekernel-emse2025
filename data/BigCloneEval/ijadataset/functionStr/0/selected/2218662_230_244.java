public class Test {    private void createLineupNode(int stationId, DefaultMutableTreeNode parent) {
        for (Iterator lineups = xtvd.getLineups().entrySet().iterator(); lineups.hasNext(); ) {
            Lineup lineup = (Lineup) ((java.util.Map.Entry) lineups.next()).getValue();
            for (Iterator maps = lineup.getMap().iterator(); maps.hasNext(); ) {
                Map map = (Map) maps.next();
                if (map.getStation() == stationId) {
                    StringBuffer buffer = new StringBuffer(32);
                    buffer.append("Lineup: ").append(lineup.getName());
                    buffer.append(", type: ").append(lineup.getType().toString());
                    buffer.append(", channel: ").append(map.getChannel());
                    parent.add(new DefaultMutableTreeNode(buffer));
                }
            }
        }
    }
}