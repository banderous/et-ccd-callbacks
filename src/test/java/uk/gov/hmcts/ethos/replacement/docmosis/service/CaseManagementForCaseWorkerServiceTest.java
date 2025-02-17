package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ecm.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.ecm.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ecm.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ecm.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MID_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@RunWith(SpringJUnit4ClassRunner.class)
public class CaseManagementForCaseWorkerServiceTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    @InjectMocks
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private CCDRequest scotlandCcdRequest1;
    private CCDRequest scotlandCcdRequest2;
    private CCDRequest scotlandCcdRequest3;
    private CCDRequest ccdRequest10;
    private CCDRequest ccdRequest11;
    private CCDRequest ccdRequest12;
    private CCDRequest ccdRequest13;
    private CCDRequest ccdRequest14;
    private CCDRequest ccdRequest15;
    private CCDRequest manchesterCcdRequest;
    private SubmitEvent submitEvent;

    @MockBean
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private ClerkService clerkService;

    @Before
    public void setUp() throws Exception {
        scotlandCcdRequest1 = new CCDRequest();
        CaseDetails caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        scotlandCcdRequest1.setCaseDetails(caseDetailsScot1);

        scotlandCcdRequest2 = new CCDRequest();
        CaseDetails caseDetailsScot2 = generateCaseDetails("caseDetailsScotTest2.json");
        scotlandCcdRequest2.setCaseDetails(caseDetailsScot2);

        scotlandCcdRequest3 = new CCDRequest();
        CaseDetails caseDetailsScot3 = generateCaseDetails("caseDetailsScotTest3.json");
        scotlandCcdRequest3.setCaseDetails(caseDetailsScot3);

        ccdRequest10 = new CCDRequest();
        CaseDetails caseDetails10 = generateCaseDetails("caseDetailsTest10.json");
        ccdRequest10.setCaseDetails(caseDetails10);

        ccdRequest11 = new CCDRequest();
        CaseDetails caseDetails11 = generateCaseDetails("caseDetailsTest11.json");
        ccdRequest11.setCaseDetails(caseDetails11);

        ccdRequest12 = new CCDRequest();
        CaseDetails caseDetails12 = generateCaseDetails("caseDetailsTest12.json");
        ccdRequest12.setCaseDetails(caseDetails12);

        ccdRequest13 = new CCDRequest();
        CaseDetails caseDetails13 = generateCaseDetails("caseDetailsTest13.json");
        ccdRequest13.setCaseDetails(caseDetails13);

        ccdRequest14 = new CCDRequest();
        CaseDetails caseDetails14 = generateCaseDetails("caseDetailsTest14.json");
        ccdRequest14.setCaseDetails(caseDetails14);

        ccdRequest15 = new CCDRequest();
        CaseDetails caseDetails15 = generateCaseDetails("caseDetailsTest15.json");
        ccdRequest15.setCaseDetails(caseDetails15);

        manchesterCcdRequest = new CCDRequest();
        CaseDetails manchesterCaseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        caseData.setPreAcceptCase(casePreAcceptType);
        caseData.setCaseRefECC("11111");

        EccCounterClaimTypeItem eccCounterClaimTypeItem = new EccCounterClaimTypeItem();
        EccCounterClaimType counterClaimType = new EccCounterClaimType();
        counterClaimType.setCounterClaim("72632632");
        eccCounterClaimTypeItem.setId(UUID.randomUUID().toString());
        eccCounterClaimTypeItem.setValue(counterClaimType);


        caseData.setEccCases(Arrays.asList(eccCounterClaimTypeItem));
        caseData.setRespondentECC(createRespondentECC());
        manchesterCaseDetails.setCaseData(caseData);
        manchesterCaseDetails.setCaseId("123456");
        manchesterCaseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        manchesterCaseDetails.setJurisdiction("TRIBUNALS");
        manchesterCcdRequest.setCaseDetails(manchesterCaseDetails);

        submitEvent = new SubmitEvent();
        CaseData submitCaseData = new CaseData();
        submitCaseData.setRespondentCollection(createRespondentCollection(true));
        submitCaseData.setClaimantIndType(createClaimantIndType());
        submitCaseData.setRepresentativeClaimantType(createRepresentedTypeC());
        submitCaseData.setRepCollection(createRepCollection(false));
        submitCaseData.setClaimantRepresentedQuestion(YES);
        ClaimantType claimantType = new ClaimantType();
        Address address = new Address();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setPostTown("Manchester");
        address.setCountry("UK");
        address.setPostCode("L1 122");
        claimantType.setClaimantAddressUK(address);
        submitCaseData.setClaimantType(claimantType);
        submitEvent.setState("Accepted");
        submitEvent.setCaseId(123);
        submitEvent.setCaseData(submitCaseData);

        caseManagementForCaseWorkerService = new CaseManagementForCaseWorkerService(caseRetrievalForCaseWorkerService,
                ccdClient, clerkService);
    }

    @Test
    public void caseDataDefaultsClaimantIndividual() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Anton Juliet Rodriguez", caseData.getClaimant());
    }

    @Test
    public void caseDataDefaultsResponseReceived() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertEquals(NO, respondentSumTypeItem.getValue().getResponseReceived());
        }
    }

    @Test
    public void caseDataDefaultsResetResponseRespondentAddress() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            respondentSumTypeItem.getValue().setResponseReceived(null);
            respondentSumTypeItem.getValue().setResponseRespondentAddress(new Address());
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine1("Address1");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine2("Address2");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine3("Address3");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCounty("County");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostTown("PostTown");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCountry("Country");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostCode("PostCode");

        }
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine1());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine2());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine3());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getCountry());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getCounty());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostCode());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostTown());
        }
    }

    @Test
    public void caseDataDefaultsResponseReceivedDoesNotChange() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseReceived());
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (respondentSumTypeItem != caseData.getRespondentCollection().get(0))
                assertEquals(NO, respondentSumTypeItem.getValue().getResponseReceived());
        }
    }

    @Test
    public void caseDataDefaultsClaimantCompany() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Orlando LTD", caseData.getClaimant());
    }

    @Test
    public void caseDataDefaultsClaimantMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseData.setClaimantTypeOfClaimant(null);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Missing claimant", caseData.getClaimant());
    }

    @Test
    public void caseDataDefaultsRespondentAvailable() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Antonio Vazquez", caseData.getRespondent());
    }

    @Test
    public void caseDataDefaultsRespondentMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Missing respondent", caseData.getRespondent());
    }

    @Test
    public void caseDataDefaultsStruckOutYESandNulltoNO() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.caseDataDefaults(caseData);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut());
        assertEquals("Juan Garcia", caseData.getRespondentCollection().get(1).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut());
        assertEquals("Roberto Dondini", caseData.getRespondentCollection().get(2).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut());
    }

    @Test
    public void caseDataDefaultsStruckOutUnchanged() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.caseDataDefaults(caseData);

        assertEquals(1, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut());
    }

    @Test
    public void caseDataDefaultsFlagsImageFileNameNull() {
        CaseData caseData = manchesterCcdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertNull(caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    public void caseDataDefaultsFlagsImageFileNameEmpty() {
        CaseData caseData = ccdRequest10.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertNull(caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    public void dateToCurrentPositionChanged() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals(caseData.getDateToPosition(), LocalDate.now().toString());
    }

    @Test
    public void dateToCurrentPositionUnChanged() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals("2019-11-15", caseData.getDateToPosition());
    }

    @Test
    public void dateToCurrentPositionNullPositionType() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseData.setPositionType(null);
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertNull(caseData.getPositionType());
        assertNull(caseData.getDateToPosition());
    }

    @Test
    public void dateToCurrentPositionNullCurrentPosition() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals(caseData.getDateToPosition(), LocalDate.now().toString());
    }

    @Test
    public void struckOutRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut());
        assertEquals("Roberto Dondini", caseData.getRespondentCollection().get(1).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut());
        assertEquals("Juan Garcia", caseData.getRespondentCollection().get(2).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut());
    }

    @Test
    public void struckOutRespondentUnchanged() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest3);

        assertEquals(1, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
    }

    @Test
    public void continuingRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest1);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseContinue());
        assertEquals("Juan Garcia", caseData.getRespondentCollection().get(1).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(1).getValue().getResponseContinue());
        assertEquals("Roberto Dondini", caseData.getRespondentCollection().get(2).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(2).getValue().getResponseContinue());
    }

    @Test
    public void continuingRespondentNull() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest3);
        assertEquals(1, caseData.getRespondentCollection().size());
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseContinue());
    }

    @Test
    public void buildFlagsImageFileNameForNullFlagsTypes() {
        var caseDetails = ccdRequest11.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    public void buildFlagsImageFileNameForNullFlagsFields() {
        var caseDetails = ccdRequest12.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    public void buildFlagsImageFileNameForEmptyFlagsFields() {
        var caseDetails = ccdRequest13.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    public void buildFlagsImageFileNameForFalseFlagsFields() {
        var caseDetails = ccdRequest14.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    public void buildFlagsImageFileNameForTrueFlagsFields() {
        var caseDetails = ccdRequest15.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "" +
                "<font color='DarkRed' size='5'> DO NOT POSTPONE </font>" +
                "<font size='5'> - </font>" +
                "<font color='Green' size='5'> LIVE APPEAL </font>" +
                "<font size='5'> - </font>" +
                "<font color='Red' size='5'> RULE 50(3)b </font>" +
                "<font size='5'> - </font>" +
                "<font color='LightBlack' size='5'> REPORTING </font>" +
                "<font size='5'> - </font>" +
                "<font color='Orange' size='5'> SENSITIVE </font>" +
                "<font size='5'> - </font>" +
                "<font color='Purple' size='5'> RESERVED </font>" +
                "<font size='5'> - </font>" +
                "<font color='Olive' size='5'> ECC </font>" +
                "<font size='5'> - </font>" +
                "<font color='SlateGray' size='5'> DIGITAL FILE </font>";
        assertEquals(expected, caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-011111111.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    public void buildFlagsImageFileNameForTrueFlagsFieldsScotland() {
        var caseDetails = scotlandCcdRequest3.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "<font color='DeepPink' size='5'> WITH OUTSTATION </font>";
        assertEquals(expected, caseDetails.getCaseData().getFlagsImageAltText());
        assertEquals("EMP-TRIB-100000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    public void amendHearingNonScotland() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals("Manchester", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingVenueDay().getSelectedLabel());
        assertEquals("2019-11-01T12:11:00.000", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingTimingStart());
        assertEquals("2019-11-01T12:11:00.000", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingTimingFinish());
    }

    @Test
    public void amendHearingScotland() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, SCOTLAND_CASE_TYPE_ID);
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());

        assertEquals(TribunalOffice.ABERDEEN.getOfficeName(), caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingAberdeen().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow());

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingAberdeen());

        assertEquals(TribunalOffice.EDINBURGH.getOfficeName(), caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingEdinburgh().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow());

        final String dundee = TribunalOffice.DUNDEE.getOfficeName();
        assertEquals(dundee, caseData.getHearingCollection().get(3).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingDundee().getSelectedLabel());
        assertEquals(dundee, caseData.getHearingCollection().get(3).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingVenueDayScotland());
    }

    @Test
    public void midRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(1, caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    public void midRespondentECCWithStruckOut() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(createRespondentCollection(false));
        submitEvent.setCaseData(caseData);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(2, caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    public void midRespondentECCEmpty() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertEquals("[Case Reference Number not found.]", errors.toString());
    }

    @Test
    public void midRespondentECCWithNoRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        manchesterCcdRequest.getCaseDetails().getCaseData().setRespondentECC(null);
        assertEquals(1, caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    public void createECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        var casedata = caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), ABOUT_TO_SUBMIT_EVENT_CALLBACK);
        assertEquals("11111", casedata.getCaseRefECC());
        assertEquals(FLAG_ECC, casedata.getCaseSource());
        assertTrue(casedata.getJurCodesCollection().get(0).getId().matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    public void linkOriginalCaseECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals("11111", caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getCaseRefECC());
    }

    @Test
    public void linkOriginalCaseECCCounterClaims() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals("72632632", caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim());
        EccCounterClaimTypeItem c1 = new EccCounterClaimTypeItem();
        EccCounterClaimTypeItem c2 = new EccCounterClaimTypeItem();
        EccCounterClaimType counterClaimType1 = new EccCounterClaimType();
        EccCounterClaimType counterClaimType2 = new EccCounterClaimType();
        counterClaimType1.setCounterClaim("72632632");
        counterClaimType2.setCounterClaim("63467343");
        c1.setId(UUID.randomUUID().toString());
        c2.setId(UUID.randomUUID().toString());
        c1.setValue(counterClaimType1);
        c2.setValue(counterClaimType2);
        manchesterCcdRequest.getCaseDetails().getCaseData().setEccCases(Arrays.asList(c1, c2));
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(c1.getValue().getCounterClaim(), caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim());
        assertEquals(c2.getValue().getCounterClaim(), caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(1).getValue().getCounterClaim());
    }

    @Test(expected = Exception.class)
    public void linkOriginalCaseECCException() throws IOException {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenThrow(new InternalException(ERROR_MESSAGE));
        caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK);
    }

    @Test
    public void createECCFromClosedCaseWithoutET3() {
        submitEvent.setState("Closed");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(NO);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertNull(caseData.getRespondentECC().getListItems());
        assertEquals(2, errors.size());
        submitEvent.setState("Accepted");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(YES);
    }

    private List<RespondentSumTypeItem> createRespondentCollection(boolean single) {
        RespondentSumTypeItem respondentSumTypeItem1 = createRespondentSumType("RespondentName1", false);
        RespondentSumTypeItem respondentSumTypeItem2 = createRespondentSumType("RespondentName2", false);
        RespondentSumTypeItem respondentSumTypeItem3 = createRespondentSumType("RespondentName3", true);
        if (single) {
            return new ArrayList<>(Collections.singletonList(respondentSumTypeItem1));
        } else {
            return new ArrayList<>(Arrays.asList(respondentSumTypeItem1, respondentSumTypeItem2, respondentSumTypeItem3));
        }
    }

    private RespondentSumTypeItem createRespondentSumType(String respondentName, boolean struckOut) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondentName);
        if (struckOut) {
            respondentSumType.setResponseStruckOut(YES);
        }
        respondentSumType.setResponseReceived(YES);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("111");
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

    private ClaimantIndType createClaimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("ClaimantSurname");
        claimantIndType.setClaimantFirstNames("ClaimantName");
        claimantIndType.setClaimantTitle("Mr");
        return claimantIndType;
    }

    private RepresentedTypeC createRepresentedTypeC() {
        RepresentedTypeC representativeClaimantType = new RepresentedTypeC();
        representativeClaimantType.setNameOfRepresentative("Claimant Rep Name");
        representativeClaimantType.setNameOfOrganisation("Claimant Rep Org");
        representativeClaimantType.setRepresentativeReference("Claimant Rep Ref");
        return representativeClaimantType;
    }

    private List<RepresentedTypeRItem> createRepCollection(boolean single) {
        RepresentedTypeRItem representedTypeRItem1 = createRepresentedTypeR("", "RepresentativeNameAAA");
        RepresentedTypeRItem representedTypeRItem2 = createRepresentedTypeR("dummy", "RepresentativeNameBBB");
        RepresentedTypeRItem representedTypeRItem3 = createRepresentedTypeR("RespondentName1", "RepresentativeNameCCC");
        if (single) {
            return new ArrayList<>(Collections.singletonList(representedTypeRItem1));
        } else {
            return new ArrayList<>(Arrays.asList(representedTypeRItem1, representedTypeRItem2, representedTypeRItem3));
        }
    }

    private RepresentedTypeRItem createRepresentedTypeR(String respondentName, String representativeName) {
        RepresentedTypeR representedTypeR = new RepresentedTypeR();
        representedTypeR.setRespRepName(respondentName);
        representedTypeR.setNameOfRepresentative(representativeName);
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("111");
        representedTypeRItem.setValue(representedTypeR);
        return representedTypeRItem;
    }

    private DynamicFixedListType createRespondentECC() {
        DynamicFixedListType respondentECC = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("RespondentName1");
        dynamicValueType.setLabel("RespondentName1");
        respondentECC.setValue(dynamicValueType);
        return respondentECC;
    }

}