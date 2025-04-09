public class Test {    public void addMember(Account member) {
        members.add(member);
        member.getChannels().add(this);
    }
}