package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_CLOSED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class DefaultValuesReaderServiceTest {

    private CaseDefaultValuesConfiguration config;

    private TribunalOfficesService tribunalOfficesService;

    private DefaultValuesReaderService defaultValuesReaderService;

    @Before
    public void setup() {
        config = mock(CaseDefaultValuesConfiguration.class);
        tribunalOfficesService = mock(TribunalOfficesService.class);
        defaultValuesReaderService = new DefaultValuesReaderService(config, tribunalOfficesService);
    }

    @Test
    public void testGetDefaultValues() {
        // Arrange
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setAddress1("TestAddress1");
        contactDetails.setAddress2("TestAddress2");
        contactDetails.setAddress3("TestAddress3");
        contactDetails.setTown("TestTown");
        contactDetails.setPostcode("TestPostcode");
        contactDetails.setTelephone("TestTelephone");
        contactDetails.setFax("TestFax");
        contactDetails.setDx("TestDx");
        contactDetails.setEmail("TestEmail");
        contactDetails.setManagingOffice("TestManagingOffice");

        var officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(tribunalOfficesService.getTribunalContactDetails(officeName)).thenReturn(contactDetails);
        var caseType = MULTIPLE_CASE_TYPE;
        when(config.getCaseType()).thenReturn(caseType);
        var positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);

        // Act
        var defaultValues = defaultValuesReaderService.getDefaultValues(officeName);

        // Assert
        assertEquals(positionType, defaultValues.getPositionType());
        assertEquals(caseType, defaultValues.getCaseType());
        assertEquals("TestAddress1", defaultValues.getTribunalCorrespondenceAddressLine1());
        assertEquals("TestAddress2", defaultValues.getTribunalCorrespondenceAddressLine2());
        assertEquals("TestAddress3", defaultValues.getTribunalCorrespondenceAddressLine3());
        assertEquals("TestTown", defaultValues.getTribunalCorrespondenceTown());
        assertEquals("TestPostcode", defaultValues.getTribunalCorrespondencePostCode());
        assertEquals("TestTelephone", defaultValues.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", defaultValues.getTribunalCorrespondenceFax());
        assertEquals("TestDx", defaultValues.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", defaultValues.getTribunalCorrespondenceEmail());
        assertEquals("TestManagingOffice", defaultValues.getManagingOffice());
    }

    @Test
    public void testGetClaimantTypeOfClaimant() {
        var claimantTypeOfClaimant = Constants.INDIVIDUAL_TYPE_CLAIMANT;
        when(config.getClaimantTypeOfClaimant()).thenReturn(claimantTypeOfClaimant);

        assertEquals(claimantTypeOfClaimant, defaultValuesReaderService.getClaimantTypeOfClaimant());
    }

    @Test
    public void testGetPositionType() {
        var positionType = Constants.POSITION_TYPE_CASE_CLOSED;
        when(config.getPositionType()).thenReturn(positionType);

        assertEquals(positionType, defaultValuesReaderService.getPositionType());
    }

    @Test
    public void testGetCaseDataWithNoValues() {
        var defaultValues = createDefaultValues();
        var caseData = new CaseData();

        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals(POSITION_TYPE_CASE_CLOSED, caseData.getPositionType());
        assertEquals(POSITION_TYPE_CASE_CLOSED, caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals(MULTIPLE_CASE_TYPE, caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());
        assertNull(caseData.getClaimantWorkAddress());
    }

    @Test
    public void testGetCaseDataWithExistingValues() {
        var defaultValues = createDefaultValues();
        var caseData = createCaseWithValues();

        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals("ExistingPositionType", caseData.getPositionType());
        assertEquals("ExistingCaseSource", caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals("ExistingCaseType", caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());
        assertNull(caseData.getClaimantWorkAddress());
    }

    @Test
    public void testGetCaseDataWithClaimantWorkAddress() {
        var defaultValues = createDefaultValues();
        var caseData = new CaseData();
        caseData.setClaimantWorkAddressQuestion(YES);
        caseData.setClaimantWorkAddressQRespondent(new DynamicFixedListType("Respondent 2"));
        caseData.setRespondentCollection(createRespondents());

        defaultValuesReaderService.getCaseData(caseData, defaultValues);

        assertEquals(POSITION_TYPE_CASE_CLOSED, caseData.getPositionType());
        assertEquals(POSITION_TYPE_CASE_CLOSED, caseData.getCaseSource());
        assertEquals("TestManagingOffice", caseData.getManagingOffice());
        assertEquals(MULTIPLE_CASE_TYPE, caseData.getEcmCaseType());
        verifyAddress(caseData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", caseData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", caseData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", caseData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", caseData.getTribunalCorrespondenceEmail());

        var address = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
        assertEquals("Respondent 2 AddressLine1", address.getAddressLine1());
    }

    @Test
    public void testGetListingData() {
        var defaultValues = createDefaultValues();
        var listingData = new ListingData();

        defaultValuesReaderService.getListingData(listingData, defaultValues);

        verifyAddress(listingData.getTribunalCorrespondenceAddress());
        assertEquals("TestTelephone", listingData.getTribunalCorrespondenceTelephone());
        assertEquals("TestFax", listingData.getTribunalCorrespondenceFax());
        assertEquals("TestDX", listingData.getTribunalCorrespondenceDX());
        assertEquals("TestEmail", listingData.getTribunalCorrespondenceEmail());
    }

    private DefaultValues createDefaultValues() {
        return DefaultValues.builder()
                .positionType(POSITION_TYPE_CASE_CLOSED)
                .caseType(MULTIPLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("TestAddress1")
                .tribunalCorrespondenceAddressLine2("TestAddress2")
                .tribunalCorrespondenceAddressLine3("TestAddress3")
                .tribunalCorrespondenceTown("TestTown")
                .tribunalCorrespondencePostCode("TestPostcode")
                .tribunalCorrespondenceTelephone("TestTelephone")
                .tribunalCorrespondenceFax("TestFax")
                .tribunalCorrespondenceDX("TestDX")
                .tribunalCorrespondenceEmail("TestEmail")
                .managingOffice("TestManagingOffice")
                .build();
    }

    private CaseData createCaseWithValues() {
        var caseData = new CaseData();
        caseData.setPositionType("ExistingPositionType");
        caseData.setCaseSource("ExistingCaseSource");
        caseData.setManagingOffice("ExistingManagingOffice");
        caseData.setEcmCaseType("ExistingCaseType");

        return caseData;
    }

    private List<RespondentSumTypeItem> createRespondents() {
        var respondents = new ArrayList<RespondentSumTypeItem>();

        for (var i = 1; i <= 3; i++) {
            var respondentSumType = new RespondentSumType();
            respondentSumType.setRespondentName("Respondent " + i);
            var address = new Address();
            address.setAddressLine1(respondentSumType.getRespondentName() + " AddressLine1");
            respondentSumType.setRespondentAddress(address);
            var item = new RespondentSumTypeItem();
            item.setValue(respondentSumType);
            respondents.add(item);
        }

        return respondents;
    }

    private void verifyAddress(Address address) {
        assertEquals("TestAddress1", address.getAddressLine1());
        assertEquals("TestAddress2", address.getAddressLine2());
        assertEquals("TestAddress3", address.getAddressLine3());
        assertEquals("TestTown", address.getPostTown());
        assertEquals("TestPostcode", address.getPostCode());
    }
}