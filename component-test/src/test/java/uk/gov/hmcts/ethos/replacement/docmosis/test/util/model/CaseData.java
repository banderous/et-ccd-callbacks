package uk.gov.hmcts.ethos.replacement.docmosis.test.util.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ethos.replacement.docmosis.test.util.model.items.*;
import uk.gov.hmcts.ethos.replacement.docmosis.test.util.model.types.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseData {

    @JsonProperty("ethosCaseReference")
    private String ethosCaseReference;
    @JsonProperty("caseType")
    private String caseType;
    @JsonProperty("multipleType")
    private String multipleType;
    @JsonProperty("multipleOthers")
    private String multipleOthers;
    @JsonProperty("multipleReference")
    private String multipleReference;
    @JsonProperty("claimant_TypeOfClaimant")
    private String claimantTypeOfClaimant;
    @JsonProperty("claimant_Company")
    private String claimantCompany;
    @JsonProperty("claimantIndType")
    private ClaimantIndType claimantIndType;
    @JsonProperty("claimantType")
    private ClaimantType claimantType;
    @JsonProperty("claimantOtherType")
    private ClaimantOtherType claimantOtherType;
    @JsonProperty("receiptDate")
    private String receiptDate;
    @JsonProperty("feeGroupReference")
    private String feeGroupReference;
    @JsonProperty("respondentSumType")
    private RespondentSumType respondentSumType;
    @JsonProperty("representativeClaimantType")
    private RepresentedTypeC representativeClaimantType;
    @JsonProperty("representativeRespondentType")
    private RepresentedTypeR representativeRespondentType;
    @JsonProperty("respondentCollection")
    private List<RespondentSumTypeItem> respondentCollection;
    @JsonProperty("repCollection")
    private List<RepresentedTypeRItem> repCollection;
    @JsonProperty("positionType")
    private String positionType;
    @JsonProperty("fileLocation")
    private String fileLocation;
    @JsonProperty("hearingType")
    private HearingType hearingType;
    @JsonProperty("hearingCollection")
    private List<HearingTypeItem> hearingCollection;
    @JsonProperty("depositCollection")
    private List<DepositTypeItem> depositCollection;
    @JsonProperty("judgementCollection")
    private List<JudgementTypeItem> judgementCollection;
    @JsonProperty("judgementDetailsCollection")
    private List<JudgementDetailsTypeItem> judgementDetailsCollection;
    @JsonProperty("costsCollection")
    private List<CostsTypeItem> costsCollection;
    @JsonProperty("disposeType")
    private DisposeType disposeType;
    @JsonProperty("NH_JudgementType")
    private NhJudgementType NH_JudgementType;
    @JsonProperty("jurCodesCollection")
    private List<JurCodesTypeItem> jurCodesCollection;
    @JsonProperty("acasOffice")
    private String acasOffice;
    @JsonProperty("clerkResponsible")
    private String clerkResponsible;
    @JsonProperty("userLocation")
    private String userLocation;
    @JsonProperty("subMultipleReference")
    private String subMultipleReference;
    @JsonProperty("addSubMultipleComment")
    private String addSubMultipleComment;
    @JsonProperty("panelCollection")
    private List<PanelTypeItem> panelCollection;
    @JsonProperty("documentCollection")
    private List<DocumentTypeItem> documentCollection;
    @JsonProperty("referToETJ")
    private List<ReferralTypeItem> referToETJ;
    @JsonProperty("responseType")
    private ResponseType responseType;
    @JsonProperty("responseTypeCollection")
    private List<ResponseTypeItem> responseTypeCollection;
    @JsonProperty("withdrawType")
    private WithdrawType withdrawType;
    @JsonProperty("archiveType")
    private ArchiveType archiveType;
    @JsonProperty("referredToJudge")
    private String referredToJudge;
    @JsonProperty("backFromJudge")
    private String backFromJudge;
    @JsonProperty("additionalType")
    private AdditionalType additionalType;
    @JsonProperty("reconsiderationType")
    private ReconsiderationType reconsiderationType;
    @JsonProperty("reconsiderationCollection")
    private List<ReconsiderationTypeItem> reconsiderationCollection;
    @JsonProperty("correspondenceType")
    private CorrespondenceType correspondenceType;
    @JsonProperty("correspondenceScotType")
    private CorrespondenceScotType correspondenceScotType;
    @JsonProperty("caseNotes")
    private String caseNotes;
    @JsonProperty("claimantWorkAddress")
    private ClaimantWorkAddressType claimantWorkAddress;
    @JsonProperty("claimantRepresentedQuestion")
    private String claimantRepresentedQuestion;

}