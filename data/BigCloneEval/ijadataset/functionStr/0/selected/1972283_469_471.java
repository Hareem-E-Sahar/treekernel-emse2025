public class Test {    public MPSChannel[] getChannelsChanged() {
        return (MPSChannel[]) channelsChanged.toArray(new MPSChannel[(channelsChanged.size())]);
    }
}