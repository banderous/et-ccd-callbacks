const ccdUserType = {
    CASEWORKER: 'Caseworker',
    JUDGE: 'Judge'
};

const eventNames = {
    ACCEPT_CASE: 'Accept/Reject Case',
    REJECT_CASE: 'Accept/Reject Case',
    CASE_DETAILS: 'Case Details',
    PRE_ACCEPTANCE_CASE: 'preAcceptanceCase',
    ACCEPT_REJECTED_CASE: 'acceptRejectedCase',
    CLAIMANT_DETAILS: 'Claimant Details',
    CLAIMANT_REPRESENTATIVE: 'Claimant Representative',
    CLAIMANT_RESPONDENT_DETAILS: 'Respondent Details',
    RESPONDENT_REPRESENTATIVE: 'Respondent Representative',
    JURISDICTION: 'Jurisdiction'
};

const states = {
    SUBMITTED: 'Submitted',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    CLOSED: 'Closed',
    TRANSFERRED: 'Transferred'
};

module.exports = {
    ccdUserType,
    eventNames,
    states
};
