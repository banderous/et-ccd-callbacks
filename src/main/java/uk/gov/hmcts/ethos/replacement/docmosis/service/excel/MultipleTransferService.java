package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.multiples.items.CaseMultipleTypeItem;
import uk.gov.hmcts.ecm.common.model.multiples.types.MultipleObjectType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PersistentQHelperService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATING_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service("multipleTransferService")
public class MultipleTransferService {

    private final ExcelReadingService excelReadingService;
    private final PersistentQHelperService persistentQHelperService;
    private final MultipleCasesReadingService multipleCasesReadingService;
    private final SingleCasesReadingService singleCasesReadingService;
    private final CaseTransferUtils caseTransferUtils;

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    public void multipleTransferLogic(String userToken, MultipleDetails multipleDetails, List<String> errors) {
        log.info("Multiple transfer logic");

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                        errors,
                        multipleDetails.getCaseData(),
                        FilterExcelType.ALL);

        if (multipleObjects.keySet().isEmpty()) {

            log.info("No cases in the multiple");

            errors.add("No cases in the multiple");

        } else {

            validateCaseBeforeTransfer(userToken, multipleDetails, errors, multipleObjects);

            if (errors.isEmpty()) {
                multipleDetails.getCaseData().setState(UPDATING_STATE);
                sendUpdatesToSinglesCT(userToken, multipleDetails, errors, multipleObjects);
            }

        }

        log.info("Resetting mid fields");

        MultiplesHelper.resetMidFields(multipleDetails.getCaseData());

    }

    private void validateCaseBeforeTransfer(String userToken, MultipleDetails multipleDetails, List<String> errors,
                                            SortedMap<String, Object> multipleObjects) {
        List<String> ethosCaseRefCollection = new ArrayList<>(multipleObjects.keySet());
        var submitEvents = singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(), ethosCaseRefCollection,
                multipleDetails.getCaseData().getMultipleSource());
        for (var submitEvent : submitEvents) {
            var validationErrors = caseTransferUtils.validateCase(submitEvent.getCaseData());
            if (!validationErrors.isEmpty()) {
                errors.addAll(validationErrors);
            }
        }
    }

    private void sendUpdatesToSinglesCT(String userToken, MultipleDetails multipleDetails,
                                        List<String> errors, SortedMap<String, Object> multipleObjects) {

        List<String> ethosCaseRefCollection = new ArrayList<>(multipleObjects.keySet());
        var multipleData = multipleDetails.getCaseData();
        boolean sameCountryCaseTransfer = isSameCountryCaseTransfer(multipleDetails.getCaseTypeId(),
                multipleData.getOfficeMultipleCT().getValue().getCode());
        persistentQHelperService.sendCreationEventToSingles(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                errors,
                ethosCaseRefCollection,
                multipleData.getOfficeMultipleCT().getValue().getCode(),
                multipleData.getPositionTypeCT(),
                ccdGatewayBaseUrl,
                multipleData.getReasonForCT(),
                multipleData.getMultipleReference(),
                YES,
                MultiplesHelper.generateMarkUp(ccdGatewayBaseUrl,
                        multipleDetails.getCaseId(),
                        multipleData.getMultipleReference()),
                sameCountryCaseTransfer,
                null
        );
    }

    public boolean isSameCountryCaseTransfer(String caseTypeId, String officeMultipleCT) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(officeMultipleCT);
        boolean isScottishDestinationOffice = TribunalOffice.SCOTLAND_OFFICES.contains(tribunalOffice);

        return ((isScottishDestinationOffice && SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId))
                || (!isScottishDestinationOffice && ENGLANDWALES_BULK_CASE_TYPE_ID.equals(caseTypeId)));
    }

    public void populateDataIfComingFromCT(String userToken, MultipleDetails multipleDetails, List<String> errors) {

        if (multipleDetails.getCaseData().getMultipleSource().equals(MIGRATION_CASE_SOURCE)
                && multipleDetails.getCaseData().getLinkedMultipleCT() != null) {

            String oldCaseTypeId = multipleDetails.getCaseData().getLinkedMultipleCT();
            String multipleReference = multipleDetails.getCaseData().getMultipleReference();

            log.info("Retrieve the old multiple data");

            var oldSubmitMultipleEvent = multipleCasesReadingService.retrieveMultipleCasesWithRetries(
                    userToken,
                    oldCaseTypeId,
                    multipleReference).get(0);

            log.info("Generate case multiple items");

            multipleDetails.getCaseData().setCaseMultipleCollection(generateCaseMultipleItems(
                    userToken,
                    oldSubmitMultipleEvent,
                    errors));

            log.info("Generate linked multiple CT markup");

            multipleDetails.getCaseData().setLinkedMultipleCT(MultiplesHelper.generateMarkUp(
                    ccdGatewayBaseUrl,
                    String.valueOf(oldSubmitMultipleEvent.getCaseId()),
                    multipleReference));

            multipleDetails.getCaseData().setReasonForCT(oldSubmitMultipleEvent.getCaseData().getReasonForCT());
            multipleDetails.getCaseData().setMultipleName(oldSubmitMultipleEvent.getCaseData().getMultipleName());
        }

    }

    private List<CaseMultipleTypeItem> generateCaseMultipleItems(String userToken,
                                                                 SubmitMultipleEvent oldSubmitMultipleEvent,
                                                                 List<String> errors) {

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(oldSubmitMultipleEvent.getCaseData()),
                        errors,
                        oldSubmitMultipleEvent.getCaseData(),
                        FilterExcelType.ALL);

        List<CaseMultipleTypeItem> newMultipleObjectsUpdated = new ArrayList<>();

        if (!multipleObjects.keySet().isEmpty()) {

            multipleObjects.forEach((key, value) -> {
                var multipleObject = (MultipleObject) value;
                var caseMultipleTypeItem = new CaseMultipleTypeItem();

                var multipleObjectType = new MultipleObjectType();
                multipleObjectType.setSubMultiple(multipleObject.getSubMultiple());
                multipleObjectType.setEthosCaseRef(multipleObject.getEthosCaseRef());

                caseMultipleTypeItem.setId(UUID.randomUUID().toString());
                caseMultipleTypeItem.setValue(multipleObjectType);

                newMultipleObjectsUpdated.add(caseMultipleTypeItem);
            });

        }

        return newMultipleObjectsUpdated;

    }

}
