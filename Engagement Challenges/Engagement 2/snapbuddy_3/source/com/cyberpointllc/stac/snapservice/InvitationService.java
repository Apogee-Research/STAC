package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Invitation;
import java.util.Set;

public interface InvitationService {

    /**
     * Returns an unmodifiable Set of
     * the currently active invitations.
     *
     * @return Set of invitations;
     * may be empty but guaranteed to not be <code>null</code>
     */
    Set<Invitation> getInvitations();

    /**
     * Adds the specified Invitation to the collection.
     *
     * @param invitation to be added
     * @return boolean true if the invitation was added
     */
    boolean addInvitation(Invitation invitation);

    /**
     * Removes the specified Invitation from the collection.
     *
     * @param invitation to be removed
     * @return boolean true if the invitation was removed
     */
    boolean removeInvitation(Invitation invitation);
}
