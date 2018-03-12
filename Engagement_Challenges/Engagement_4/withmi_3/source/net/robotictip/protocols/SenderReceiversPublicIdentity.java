package net.robotictip.protocols;

import net.robotictip.dropbys.Chat;
import net.robotictip.dropbys.Chatee;
import net.robotictip.dropbys.Conversation;
import net.robotictip.numerical.RsaPublicKey;
import net.robotictip.parser.simple.JACKObject;
import net.robotictip.parser.simple.parser.JACKReader;
import net.robotictip.parser.simple.parser.ParseTrouble;

import java.util.List;

public final class SenderReceiversPublicIdentity implements Comparable<SenderReceiversPublicIdentity>{

    /** arbitrary string to associate with this identity */
    private final String id;
    private final RsaPublicKey publicKey;
    private final SenderReceiversNetworkAddress callbackAddress;

    public SenderReceiversPublicIdentity(String id, RsaPublicKey publicKey){
        this(id, publicKey, null);
    }

    public SenderReceiversPublicIdentity(String id, RsaPublicKey publicKey, SenderReceiversNetworkAddress callbackAddress) {
        this.id = id;
        this.publicKey = publicKey;
        this.callbackAddress = callbackAddress;
    }

    public static SenderReceiversPublicIdentity fromJack(String jackString) throws SenderReceiversTrouble {
        JACKReader reader = new JACKReader();
        try {
            return fromJack((JACKObject) reader.parse(jackString));
        } catch (ParseTrouble e) {
            throw new SenderReceiversTrouble(e);
        }
    }

    public static SenderReceiversPublicIdentity fromJack(JACKObject jack) {
        String id = (String) jack.get("id");
        String callbackHome = (String) jack.get("callbackHost");
        long callbackPort = (long) jack.get("callbackPort");
        RsaPublicKey publicKey = RsaPublicKey.fromJson((JACKObject) jack.get("publicKey"));

        return new SenderReceiversPublicIdentity(id, publicKey, new SenderReceiversNetworkAddressBuilder().assignHome(callbackHome).definePort((int) callbackPort).generateSenderReceiversNetworkAddress());
    }

    public Chat.WithMiMsg.Builder generateDiscussionStateMsgBuilder(Conversation discussion) {
        Chat.ChatStateMsg.Builder discussionStateBuilder = Chat.ChatStateMsg.newBuilder();
        List<Chatee> users = discussion.obtainUsers();
        for (int j = 0; j < users.size(); j++) {
            generateDiscussionStateMsgBuilderGateKeeper(discussionStateBuilder, users, j);
        }

        // add our identity to the end of this list
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(this);
        discussionStateBuilder.addPublicId(identityMsg);

        Chat.ChatMsg discussionMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(discussion.takeName())
                .build();

        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT_STATE)
                .setChatStateMsg(discussionStateBuilder)
                .setTextMsg(discussionMsg);
        return withMiMsgBuilder;
    }

    private void generateDiscussionStateMsgBuilderGateKeeper(Chat.ChatStateMsg.Builder discussionStateBuilder, List<Chatee> users, int j) {
        Chatee user = users.get(j);
        SenderReceiversPublicIdentity identity = user.pullIdentity();
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(identity);
        discussionStateBuilder.addPublicId(identityMsg);
    }

    public String getId() { return id; }

    public String grabTruncatedId() {
        String tid = id;
        if (id.length() > 25){
            tid = tid.substring(0, 25) + "...";
        }
        return tid;
    }

    public RsaPublicKey pullPublicKey() { return publicKey; }

    public SenderReceiversNetworkAddress getCallbackAddress() {
        return callbackAddress;
    }

    public boolean hasCallbackAddress() { return callbackAddress != null; }

    @Override
    public String toString() {
        return "id: " + id + "\n" + publicKey;
    }

    public String toJack() {
        return toJACKObject().toJACKString();
    }

    public JACKObject toJACKObject() {
        JACKObject jack = new JACKObject();
        jack.put("id", id);
        jack.put("callbackHost", callbackAddress.getHome());
        jack.put("callbackPort", callbackAddress.pullPort());
        jack.put("publicKey", publicKey.toJSONObject());
        return jack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SenderReceiversPublicIdentity that = (SenderReceiversPublicIdentity) o;

        if (!id.equals(that.id)) return false;
        if (!publicKey.equals(that.publicKey)) return false;
        return callbackAddress != null ? callbackAddress.equals(that.callbackAddress) : that.callbackAddress == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + publicKey.hashCode();
        result = 31 * result + (callbackAddress != null ? callbackAddress.hashCode() : 0);
        return result;
    }   
    
    public String toVerboseString(){
    	String str = id + ":" + publicKey.toString() + ": ";
    	if (callbackAddress!=null){
    		str += callbackAddress;
    	} else{
    		str += "NO_CALLBACK";
    	}
    	return str;
    }
    
    @Override
    public int compareTo(SenderReceiversPublicIdentity other){
    	return this.toVerboseString().compareTo(other.toVerboseString());
    }
}
