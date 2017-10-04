package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManageInvitationHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/invitations";

    private static final String TITLE = "Manage Invitations";

    private static final String FIELD_NAME = "invite";

    private static final String SUBMIT_NAME = "submit";

    private static final Set<String> FIELD_NAMES = new  HashSet(Arrays.asList(FIELD_NAME, SUBMIT_NAME));

    private static final String ACCEPT = "Accept";

    private static final String REJECT = "Reject";

    private static final String INVITATIONS_SENT_TITLE = "<h3>Invitations Sent by You</h3>";

    private static final String INVITATIONS_RECEIVED_TITLE = "<h3>Pending Invitation Requests</h3>";

    private static final String NO_INVITATIONS_SENT = "You have not sent any invitations.";

    private static final String NO_INVITATIONS_RECEIVED = "You have no pending invitations to manage.";

    private static final TemplateEngine INVITATION_SENT_TEMPLATE = new  TemplateEngine("<tr>" + "    <td><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>");

    private static final TemplateEngine INVITATION_RECEIVED_TEMPLATE = new  TemplateEngine("<tr>" + "    <td>" + "        <input type=\"checkbox\" name=\"" + FIELD_NAME + "\" value=\"{{identity}}\"> " + "    </td>" + "    <td><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>");

    public ManageInvitationHandler(SnapService snapService) {
        super(snapService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getContents(SnapContext context) {
        ClassgetContents replacementClass = new  ClassgetContents(context);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        return replacementClass.doIt4();
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        ClasshandlePost replacementClass = new  ClasshandlePost(httpExchange);
        ;
        return replacementClass.doIt0();
    }

    /**
     * Returns the set of Persons who the specified Person
     * has invited to be a friend.
     * This is computed by taking all invited people (includes
     * those who have sent an invitation to this Person and those
     * this Person has invited) and removing those who have sent
     * an invitation to this Person.
     *
     * @param thisPerson        involved in the invitation
     * @param allInvitedPersons either inviting or invited by this Person
     * @param invitationSenders who sent an invitation to this Person
     * @return Set of Persons invited by this Person to be a friend;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if any argument is <code>null</code>
     */
    private static Set<Person> getInvitationsSent(Person thisPerson, Set<Person> allInvitedPersons, Set<Person> invitationSenders) {
        ClassgetInvitationsSent replacementClass = new  ClassgetInvitationsSent(thisPerson, allInvitedPersons, invitationSenders);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    protected class ManageInvitationHandlerHelper0 {

        public ManageInvitationHandlerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class ClassgetContents {

        public ClassgetContents(SnapContext context) {
            this.context = context;
        }

        private SnapContext context;

        public void doIt0() {
            if (context == null) {
                throw new  IllegalArgumentException("Context may not be null");
            }
        }

        private Person activePerson;

        private Set<Person> allInvitations;

        public void doIt1() {
            activePerson = context.getActivePerson();
            allInvitations = getSnapService().getInvitations(activePerson);
        }

        private Set<Person> invitationsToThisPerson;

        public void doIt2() {
            invitationsToThisPerson = getSnapService().getInvitationsTo(activePerson);
        }

        private Set<Person> invitationsFromThisPerson;

        private StringBuilder sb;

        private Map<String, String> map;

        public void doIt3() {
            invitationsFromThisPerson = getInvitationsSent(activePerson, allInvitations, invitationsToThisPerson);
            sb = new  StringBuilder();
            map = new  HashMap();
            sb.append(INVITATIONS_SENT_TITLE);
            if (invitationsFromThisPerson.isEmpty()) {
                sb.append(NO_INVITATIONS_SENT);
            } else {
                sb.append("<table>");
                for (Person invited : invitationsFromThisPerson) {
                    map.clear();
                    map.put("photoURL", getProfilePhotoUrl(invited));
                    map.put("name", invited.getName());
                    sb.append(INVITATION_SENT_TEMPLATE.replaceTags(map));
                }
                sb.append("</table>");
            }
            sb.append("<hr>");
            sb.append(INVITATIONS_RECEIVED_TITLE);
        }

        public String doIt4() {
            if (invitationsToThisPerson.isEmpty()) {
                sb.append(NO_INVITATIONS_RECEIVED);
            } else {
                sb.append("<form action=\"");
                sb.append(PATH);
                sb.append("\" method=\"post\" enctype=\"multipart/form-data\">");
                sb.append("<table>");
                for (Person sender : invitationsToThisPerson) {
                    map.clear();
                    map.put("identity", sender.getIdentity());
                    map.put("photoURL", getProfilePhotoUrl(sender));
                    map.put("name", sender.getName());
                    sb.append(INVITATION_RECEIVED_TEMPLATE.replaceTags(map));
                }
                sb.append("</table>");
                sb.append("<input type=\"submit\" value=\"").append(ACCEPT).append("\" name=\"").append(SUBMIT_NAME).append("\" >");
                sb.append("<input type=\"submit\" value=\"").append(REJECT).append("\" name=\"").append(SUBMIT_NAME).append("\" >");
                sb.append("</form>");
            }
            return sb.toString();
        }
    }

    protected class ClasshandlePost {

        public ClasshandlePost(HttpExchange httpExchange) {
            this.httpExchange = httpExchange;
        }

        private HttpExchange httpExchange;

        private Person activePerson;

        private Map<String, List<String>> multipartValues;

        private List<String> options;

        private ManageInvitationHandlerHelper0 conditionObj0;

        private boolean doAccept;

        private List<String> identities;

        public HttpHandlerResponse doIt0() {
            activePerson = getPerson(httpExchange);
            multipartValues = MultipartHelper.getMultipartValues(httpExchange, FIELD_NAMES);
            options = multipartValues.get(SUBMIT_NAME);
            conditionObj0 = new  ManageInvitationHandlerHelper0(1);
            if ((options == null) || (options.size() != conditionObj0.getValue())) {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Either " + ACCEPT + " or " + REJECT + " must be submitted");
            }
            if (ACCEPT.equals(options.get(0))) {
                doAccept = true;
            } else if (REJECT.equals(options.get(0))) {
                doAccept = false;
            } else {
                return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Either " + ACCEPT + " or " + REJECT + " must be submitted");
            }
            identities = multipartValues.get(FIELD_NAME);
            if ((identities != null) && !identities.isEmpty()) {
                for (String identity : identities) {
                    Person otherPerson = getSnapService().getPerson(identity);
                    if (otherPerson != null) {
                        if (doAccept) {
                            getSnapService().acceptInvitation(activePerson, otherPerson);
                        } else {
                            getSnapService().rejectInvitation(activePerson, otherPerson);
                        }
                    }
                }
            }
            return getDefaultRedirectResponse();
        }
    }

    private static class ClassgetInvitationsSent {

        public ClassgetInvitationsSent(Person thisPerson, Set<Person> allInvitedPersons, Set<Person> invitationSenders) {
            this.thisPerson = thisPerson;
            this.allInvitedPersons = allInvitedPersons;
            this.invitationSenders = invitationSenders;
        }

        private Person thisPerson;

        private Set<Person> allInvitedPersons;

        private Set<Person> invitationSenders;

        private Set<Person> invitedPeople;

        public void doIt0() {
            if (thisPerson == null) {
                throw new  IllegalArgumentException("Person may not be null");
            }
            if (allInvitedPersons == null) {
                throw new  IllegalArgumentException("Set of all invited Persons may not be null");
            }
            if (invitationSenders == null) {
                throw new  IllegalArgumentException("Set of invitation senders may not be null");
            }
            invitedPeople = new  HashSet(allInvitedPersons);
        }

        public void doIt1() {
            invitedPeople.removeAll(invitationSenders);
        }

        public Set<Person> doIt2() {
            invitedPeople.remove(thisPerson);
            return invitedPeople;
        }
    }
}
