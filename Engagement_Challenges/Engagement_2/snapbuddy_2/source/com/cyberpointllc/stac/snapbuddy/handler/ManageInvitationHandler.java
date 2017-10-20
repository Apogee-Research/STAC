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
        if (context == null) {
            throw new  IllegalArgumentException("Context may not be null");
        }
        Person activePerson = context.getActivePerson();
        Set<Person> allInvitations = getSnapService().getInvitations(activePerson);
        Set<Person> invitationsToThisPerson = getSnapService().getInvitationsTo(activePerson);
        Set<Person> invitationsFromThisPerson = getInvitationsSent(activePerson, allInvitations, invitationsToThisPerson);
        StringBuilder sb = new  StringBuilder();
        Map<String, String> map = new  HashMap();
        sb.append(INVITATIONS_SENT_TITLE);
        if (invitationsFromThisPerson.isEmpty()) {
            sb.append(NO_INVITATIONS_SENT);
        } else {
            getContentsHelper(sb, map, invitationsFromThisPerson);
        }
        sb.append("<hr>");
        sb.append(INVITATIONS_RECEIVED_TITLE);
        if (invitationsToThisPerson.isEmpty()) {
            getContentsHelper1(sb);
        } else {
            getContentsHelper2(invitationsToThisPerson, sb, map);
        }
        return sb.toString();
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person activePerson = getPerson(httpExchange);
        Map<String, List<String>> multipartValues = MultipartHelper.getMultipartValues(httpExchange, FIELD_NAMES);
        List<String> options = multipartValues.get(SUBMIT_NAME);
        if ((options == null) || (options.size() != 1)) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Either " + ACCEPT + " or " + REJECT + " must be submitted");
        }
        boolean doAccept;
        if (ACCEPT.equals(options.get(0))) {
            doAccept = true;
        } else if (REJECT.equals(options.get(0))) {
            doAccept = false;
        } else {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Either " + ACCEPT + " or " + REJECT + " must be submitted");
        }
        List<String> identities = multipartValues.get(FIELD_NAME);
        if ((identities != null) && !identities.isEmpty()) {
            for (String identity : identities) {
                Person otherPerson = getSnapService().getPerson(identity);
                if (otherPerson != null) {
                    handlePostHelper(activePerson, doAccept, otherPerson);
                }
            }
        }
        return getDefaultRedirectResponse();
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
        if (thisPerson == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (allInvitedPersons == null) {
            throw new  IllegalArgumentException("Set of all invited Persons may not be null");
        }
        if (invitationSenders == null) {
            throw new  IllegalArgumentException("Set of invitation senders may not be null");
        }
        Set<Person> invitedPeople = new  HashSet(allInvitedPersons);
        invitedPeople.removeAll(invitationSenders);
        invitedPeople.remove(thisPerson);
        return invitedPeople;
    }

    private void getContentsHelper(StringBuilder sb, Map<String, String> map, Set<Person> invitationsFromThisPerson) {
        sb.append("<table>");
        for (Person invited : invitationsFromThisPerson) {
            map.clear();
            map.put("photoURL", getProfilePhotoUrl(invited));
            map.put("name", invited.getName());
            sb.append(INVITATION_SENT_TEMPLATE.replaceTags(map));
        }
        sb.append("</table>");
    }

    private void getContentsHelper1(StringBuilder sb) {
        sb.append(NO_INVITATIONS_RECEIVED);
    }

    private void getContentsHelper2(Set<Person> invitationsToThisPerson, StringBuilder sb, Map<String, String> map) {
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

    private void handlePostHelper(Person activePerson, boolean doAccept, Person otherPerson) {
        if (doAccept) {
            getSnapService().acceptInvitation(activePerson, otherPerson);
        } else {
            getSnapService().rejectInvitation(activePerson, otherPerson);
        }
    }
}
