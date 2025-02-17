package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Service("caseTransferService")
@RequiredArgsConstructor
@Slf4j
public class CaseTransferSameCountryService {

    private final CaseTransferUtils caseTransferUtils;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> transferCase(CaseDetails caseDetails, String userToken) {
        var caseDataList = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken);
        if (caseDataList.isEmpty()) {
            throw new IllegalStateException("No cases found for Case Transfer for "
                    + caseDetails.getCaseData().getEthosCaseReference());
        }
        var errors = validate(caseDataList);

        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCases(caseDetails, caseDataList, userToken);
    }

    public List<String> updateEccLinkedCase(CaseDetails caseDetails, String userToken) {
        return transferCases(caseDetails, Collections.emptyList(), userToken);
    }

    private List<String> validate(List<CaseData> cases) {
        var errors = new ArrayList<String>();

        for (var caseData : cases) {
            var validationErrors = caseTransferUtils.validateCase(caseData);
            if (!validationErrors.isEmpty()) {
                errors.addAll(validationErrors);
            }
        }

        return errors;
    }

    private List<String> transferCases(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        var errors = new ArrayList<String>();

        // Remove source case as we will be updating this directly rather than sending it to the event queue
        var sourceCaseData = caseDetails.getCaseData();
        var casesToSubmitTransferEvent = caseDataList.stream()
                .filter(caseData -> !caseData.getEthosCaseReference().equals(sourceCaseData.getEthosCaseReference()))
                .collect(Collectors.toList());

        var newManagingOffice = caseDetails.getCaseData().getOfficeCT().getSelectedCode();

        for (CaseData caseData : casesToSubmitTransferEvent) {
            var params = CaseTransferEventParams.builder()
                    .userToken(userToken)
                    .caseTypeId(caseDetails.getCaseTypeId())
                    .jurisdiction(caseDetails.getJurisdiction())
                    .ethosCaseReferences(List.of(caseData.getEthosCaseReference()))
                    .sourceEthosCaseReference(sourceCaseData.getEthosCaseReference())
                    .newManagingOffice(newManagingOffice)
                    .reason(caseDetails.getCaseData().getReasonForCT())
                    .multipleReference(SINGLE_CASE_TYPE)
                    .confirmationRequired(false)
                    .transferSameCountry(true)
                    .build();

            log.info("Creating Case Transfer event for {}", caseData.getEthosCaseReference());
            var transferErrors = caseTransferEventService.transfer(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        sourceCaseData.setManagingOffice(newManagingOffice);
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }
}
