package com.cyberpointllc.stac.snapservice.model;

import org.apache.commons.lang3.StringUtils;

public class Invitation {

    private final String inviteFromIdentity;

    private final String inviteToIdentity;

    public Invitation(String inviteFromIdentity, String inviteToIdentity) {
        if (StringUtils.isBlank(inviteFromIdentity)) {
            throw new  IllegalArgumentException("Invitation sender identity may not be null or empty");
        }
        if (StringUtils.isBlank(inviteToIdentity)) {
            throw new  IllegalArgumentException("Invitation receiver identity may not be null or empty");
        }
        this.inviteFromIdentity = inviteFromIdentity;
        this.inviteToIdentity = inviteToIdentity;
    }

    /**
     * Returns of the identity of the
     * person sending the invitation.
     *
     * @return String of invitation sender identity;
     * guaranteed to not be <code>null</code>
     */
    public String getInviteFromIdentity() {
        return inviteFromIdentity;
    }

    /**
     * Returns of the identity of the
     * person receiving the invitation.
     *
     * @return String of invitation receiver identity;
     * guaranteed to not be <code>null</code>
     */
    public String getInviteToIdentity() {
        return inviteToIdentity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        Invitation that = (Invitation) obj;
        return inviteFromIdentity.equals(that.inviteFromIdentity) && inviteToIdentity.equals(that.inviteToIdentity);
    }

    @Override
    public int hashCode() {
        return (31 * inviteFromIdentity.hashCode()) + inviteToIdentity.hashCode();
    }

    @Override
    public String toString() {
        return "Invitation{" + "inviteFromIdentity=" + inviteFromIdentity + ", inviteToIdentity=" + inviteToIdentity + '}';
    }
}
