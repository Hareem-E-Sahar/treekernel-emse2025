public class Test {    private String getChannelName(IRelationship relationship) {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append("/").append(Channels.CONTRIBUTIONS).append("/").append(Channels.RELATIONSHIP).append("/").append(relationship.getSuperior().getId()).append("/").append(relationship.getRelationship()).append("/").append(relationship.getSubordinate().getType()).append("/").append(Channels.DELETE);
        return returnValue.toString();
    }
}