package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.BF_ACTIONS_ERROR_MSG;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.HEARINGS_ERROR_MSG;

@ExtendWith(SpringExtension.class)
class CaseTransferDifferentCountryServiceTest {

    @InjectMocks
    CaseTransferDifferentCountryService caseTransferDifferentCountryService;

    @Mock
    CaseTransferUtils caseTransferUtils;

    @Mock
    CaseTransferEventService caseTransferEventService;

    @Captor
    private ArgumentCaptor<CaseTransferEventParams> caseTransferEventParamsArgumentCaptor;

    private final String claimantEthosCaseReference = "120001/2021";
    private final String caseTypeId = ENGLANDWALES_CASE_TYPE_ID;
    private final String jurisdiction = "EMPLOYMENT";
    private final String userToken = "my-test-token";
    private final String reasonCT = "Just a test";
    private final String expectedPositionType = CaseTransferDifferentCountryService.CASE_TRANSFERRED_POSITION_TYPE;

    @Test
    void testCaseTransfer() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(managingOffice, officeCT, null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());

        verifyCaseTransferEventParams(claimantEthosCaseReference, claimantEthosCaseReference, officeCT,
                caseTransferEventParamsArgumentCaptor.getValue());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferSuccessWithBfActionClearedAndHearingHeard() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_HEARD);
        addBfAction(caseDetails.getCaseData(), YES);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        verifyCaseTransferEventParams(claimantEthosCaseReference, claimantEthosCaseReference, officeCT,
                caseTransferEventParamsArgumentCaptor.getValue());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithBfAction() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(managingOffice, officeCT, null);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedError = String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithHearingListed() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_LISTED);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedError = String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithBfActionAndHearingListed() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_LISTED);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedErrors = List.of(String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference),
                String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference));
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(expectedErrors);

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertEquals(2, errors.size());
        assertEquals(expectedErrors.get(0), errors.get(0));
        assertEquals(expectedErrors.get(1), errors.get(1));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferWithEccCase() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120002/2021";
        var eccCases = List.of(eccCaseReference);
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData = createEccCaseSearchResult(eccCaseReference, managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(claimantEthosCaseReference,claimantEthosCaseReference, officeCT, params.get(0));
        verifyCaseTransferEventParams(eccCaseReference,claimantEthosCaseReference, officeCT, params.get(1));
    }

    @Test
    void caseTransferWithEccCases() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021", "120003/2021");
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData1 = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        var eccCaseData2 = createEccCaseSearchResult(eccCases.get(1), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData1, eccCaseData2));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(3)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(claimantEthosCaseReference,claimantEthosCaseReference, officeCT, params.get(0));
        verifyCaseTransferEventParams(eccCases.get(0),claimantEthosCaseReference, officeCT, params.get(1));
        verifyCaseTransferEventParams(eccCases.get(1),claimantEthosCaseReference, officeCT, params.get(2));
    }

    @Test
    void caseTransferWithEccCaseAsSource() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120009/2021";
        var eccCaseDetails = createEccCaseDetails(eccCaseReference, managingOffice, officeCT);
        var claimantCaseDeta = createCaseDetails(managingOffice, List.of(eccCaseReference), null, null).getCaseData();
        when(caseTransferUtils.getAllCasesToBeTransferred(eccCaseDetails, userToken))
                .thenReturn(List.of(claimantCaseDeta, eccCaseDetails.getCaseData()));

        var errors = caseTransferDifferentCountryService.transferCase(eccCaseDetails, userToken);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(eccCaseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(claimantEthosCaseReference,eccCaseReference, officeCT, params.get(0));
        verifyCaseTransferEventParams(eccCaseReference, eccCaseReference, officeCT, params.get(1));
    }

    @Test
    void caseTransferWithEccCaseReturnsTransferError() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021");
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        var caseTransferError = "A transfer error";
        when(caseTransferEventService.transfer(isA(CaseTransferEventParams.class))).thenReturn(
                List.of(caseTransferError));

        var errors = caseTransferDifferentCountryService.transferCase(caseDetails, userToken);

        assertEquals(2, errors.size());
        assertEquals(caseTransferError, errors.get(0));
        assertEquals(caseTransferError, errors.get(1));

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2)).transfer(isA(CaseTransferEventParams.class));
    }

    @Test
    void transferCaseNoCasesFoundThrowsException() {
        var caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(claimantEthosCaseReference)
                .buildAsCaseDetails(caseTypeId, jurisdiction);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken)).thenReturn(Collections.emptyList());

        Assertions.assertThrows(IllegalStateException.class,
                () -> caseTransferDifferentCountryService.transferCase(caseDetails, userToken));

        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));
    }

    private void verifyCaseTransferEventParams(String expectedEthosCaseReference,
                                               String expectedSourceEthosCaseReference, String expectedManagingOffice,
                                               CaseTransferEventParams params) {
        assertEquals(userToken, params.getUserToken());
        assertEquals(caseTypeId, params.getCaseTypeId());
        assertEquals(jurisdiction, params.getJurisdiction());
        assertEquals(List.of(expectedEthosCaseReference), params.getEthosCaseReferences());
        assertEquals(expectedSourceEthosCaseReference, params.getSourceEthosCaseReference());
        assertEquals(expectedManagingOffice, params.getNewManagingOffice());
        assertEquals(expectedPositionType, params.getPositionType());
        assertEquals(reasonCT, params.getReason());
        assertEquals(SINGLE_CASE_TYPE, params.getMultipleReference());
        assertFalse(params.isConfirmationRequired());
        assertFalse(params.isTransferSameCountry());
    }

    private CaseDetails createCaseDetails(String managingOffice, String officeCT, String hearingStatus) {
        return createCaseDetails(managingOffice, Collections.emptyList(), officeCT, hearingStatus);
    }

    private CaseDetails createCaseDetails(String managingOffice, List<String> eccCases, String officeCT,
                                          String hearingStatus) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
                .withEthosCaseReference(claimantEthosCaseReference)
                .withManagingOffice(managingOffice)
                .withCaseTransfer(officeCT, reasonCT);
        for (String eccCase : eccCases) {
            builder.withEccCase(eccCase);
        }

        if (hearingStatus != null) {
            builder.withHearing("1", null, null)
                    .withHearingSession(0, "1", "2021-12-25", hearingStatus, false);
        }

        return builder.buildAsCaseDetails(caseTypeId, jurisdiction);
    }

    private CaseData createEccCaseSearchResult(String ethosCaseReference, String managingOffice) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(claimantEthosCaseReference)
                .build();
    }

    private void addBfAction(CaseData caseData, String cleared) {
        var bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        var bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
    }

    private CaseDetails createEccCaseDetails(String ethosCaseReference, String managingOffice, String officeCT) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(claimantEthosCaseReference)
                .withCaseTransfer(officeCT, reasonCT)
                .buildAsCaseDetails(caseTypeId, jurisdiction);
    }

    private void verifyCaseDataAfterTransfer(CaseDetails caseDetails, String managingOffice, String officeCT) {
        assertEquals(managingOffice, caseDetails.getCaseData().getManagingOffice());
        assertEquals("Transferred to " + officeCT, caseDetails.getCaseData().getLinkedCaseCT());
        assertEquals(expectedPositionType, caseDetails.getCaseData().getPositionType());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    private void verifyCaseDataAfterTransferFails(CaseDetails caseDetails, String managingOffice, String officeCT) {
        assertEquals(managingOffice, caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
        assertNull(caseDetails.getCaseData().getPositionType());
    }
}
