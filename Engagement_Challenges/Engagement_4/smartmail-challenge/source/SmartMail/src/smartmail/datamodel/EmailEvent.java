package smartmail.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EmailEvent implements Serializable {

    private Set<EmailAddress> othersource;

    private List<EmailAddress> otherdestination;

    private EmailAddress source;

    private EmailAddress destination;
    private String protocol;

    private String type;

    private String subj;
    private String body;

    public EmailEvent() {
        otherdestination = new ArrayList<EmailAddress>();
    }

    public Set<EmailAddress> getOthersource() {
        return othersource;
    }

    public void setOthersource(Set<EmailAddress> othersource) {
        this.othersource = othersource;
    }

    public List<EmailAddress> getOtherdestination() {
        return otherdestination;
    }

    public void setOtherdestination(List<EmailAddress> otherdestination) {
        this.otherdestination = otherdestination;
    }

    public void addOtherdestination(EmailAddress otherdestination) {

        if (otherdestination instanceof MailingList) {
            MailingList ml = (MailingList) otherdestination;
            Set<EmailAddress> members = ml.getMembers();
            this.otherdestination.addAll(members);
        } else {
            this.otherdestination.add(otherdestination);
        }

    }

    public EmailAddress getSource() {
        return source;
    }

    public void setSource(EmailAddress source) {
        this.source = source;
    }

    public EmailAddress getDestination() {
        return destination;
    }

    public void setDestination(EmailAddress destination) {
        this.destination = destination;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String bodystr) {
        this.body = bodystr;
    }

    public String getContent() {
        return this.body;
    }

    public void setSubject(String subj) {
        this.subj = subj;
    }

    public String getSubject() {
        return subj;
    }
}
