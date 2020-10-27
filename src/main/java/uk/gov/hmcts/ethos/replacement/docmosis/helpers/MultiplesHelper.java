package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.ecm.common.model.bulk.types.CaseType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ecm.common.model.multiples.items.SubMultipleTypeItem;
import uk.gov.hmcts.ecm.common.model.multiples.types.SubMultipleType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.multiples.MultipleConstants.*;

@Slf4j
public class MultiplesHelper {

    public static List<String> HEADERS = new ArrayList<>(Arrays.asList(HEADER_1, HEADER_2, HEADER_3, HEADER_4, HEADER_5, HEADER_6));
    public static String SELECT_ALL = "All";

    public static List<String> getCaseIds(MultipleData multipleData) {

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            return multipleData.getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .map(caseId -> caseId.getValue().getEthosCaseReference())
                    .distinct()
                    .collect(Collectors.toList());

        } else {

            return new ArrayList<>();

        }
    }

    // MID EVENTS COLLECTIONS HAVE KEY AS NULL BUT WITH VALUES!
    public static List<String> getCaseIdsForMidEvent(MultipleData multipleData) {

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            return multipleData.getCaseIdCollection().stream()
                    .filter(caseId -> caseId.getValue().getEthosCaseReference() != null)
                    .map(caseId -> caseId.getValue().getEthosCaseReference())
                    .distinct()
                    .collect(Collectors.toList());

        } else {

            return new ArrayList<>();

        }
    }

    public static List<CaseIdTypeItem> filterDuplicatedAndEmptyCaseIds(MultipleData multipleData) {

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            return multipleData.getCaseIdCollection().stream()
                    .filter(caseId ->
                            caseId.getValue().getEthosCaseReference() != null
                                    && !caseId.getValue().getEthosCaseReference().trim().equals(""))
                    .filter(distinctByValue(CaseIdTypeItem::getValue))
                    .distinct()
                    .collect(Collectors.toList());

        } else {

            return new ArrayList<>();

        }
    }

    private static <T> Predicate<T> distinctByValue(Function<? super T, Object> keyExtractor) {

        Map<Object, Boolean> map = new ConcurrentHashMap<>();

        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;

    }

    public static String getLeadFromCaseIds(MultipleData multipleData) {

        List<String> caseIds = getCaseIds(multipleData);

        if (caseIds.isEmpty()) {

            return "";

        } else {

            return caseIds.get(0);

        }

    }

    public static void removeCaseIds(MultipleData multipleData, List<String> multipleObjectsFiltered) {

        List<CaseIdTypeItem> newCaseIdCollection = new ArrayList<>();

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            newCaseIdCollection = multipleData.getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .filter(caseId -> !multipleObjectsFiltered.contains(caseId.getValue().getEthosCaseReference()))
                    .distinct()
                    .collect(Collectors.toList());

        }

        multipleData.setCaseIdCollection(newCaseIdCollection);

    }

    public static void addCaseIds(MultipleData multipleData, List<String> multipleObjectsFiltered) {

        List<CaseIdTypeItem> caseIdCollectionToAdd = new ArrayList<>();

        for (String ethosCaseReference : multipleObjectsFiltered) {

            caseIdCollectionToAdd.add(createCaseIdTypeItem(ethosCaseReference));

        }

        if (multipleData.getCaseIdCollection() == null) {

            multipleData.setCaseIdCollection(new ArrayList<>(caseIdCollectionToAdd));

        } else {

            multipleData.getCaseIdCollection().addAll(caseIdCollectionToAdd);

        }

    }

    public static void addLeadToCaseIds(MultipleData multipleData, String leadCase) {

        CaseIdTypeItem caseIdTypeItem = createCaseIdTypeItem(leadCase);

        if (multipleData.getCaseIdCollection() == null) {

            multipleData.setCaseIdCollection(new ArrayList<>(Collections.singletonList(caseIdTypeItem)));

        } else {

            multipleData.getCaseIdCollection().add(0, caseIdTypeItem);

        }

    }

    public static CaseIdTypeItem createCaseIdTypeItem(String ethosCaseReference) {

        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference(ethosCaseReference);
        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId(UUID.randomUUID().toString());
        caseIdTypeItem.setValue(caseType);

        return caseIdTypeItem;
    }

    public static MultipleObject createMultipleObject(String ethosCaseReference, String subMultiple) {

        return MultipleObject.builder()
                .ethosCaseRef(ethosCaseReference)
                .subMultiple(subMultiple)
                .flag1("")
                .flag2("")
                .flag3("")
                .flag4("")
                .build();
    }

    public static String getExcelBinaryUrl(MultipleData multipleData) {
        return multipleData.getCaseImporterFile().getUploadedDocument().getDocumentBinaryUrl();
    }

    public static void resetMidFields(MultipleData multipleData) {

        multipleData.setFlag1(null);
        multipleData.setFlag2(null);
        multipleData.setFlag3(null);
        multipleData.setFlag4(null);

        multipleData.setManagingOffice(null);
        multipleData.setFileLocation(null);
        multipleData.setFileLocationGlasgow(null);
        multipleData.setFileLocationAberdeen(null);
        multipleData.setFileLocationDundee(null);
        multipleData.setFileLocationEdinburgh(null);
        multipleData.setClerkResponsible(null);
        multipleData.setPositionType(null);
        multipleData.setReceiptDate(null);
        multipleData.setHearingStage(null);

        multipleData.setBatchUpdateCase(null);
        multipleData.setBatchUpdateType(null);

        multipleData.setMoveCases(null);
        multipleData.setSubMultipleAction(null);
        multipleData.setScheduleDocName(null);

        multipleData.setBatchUpdateRespondent(null);
        multipleData.setBatchUpdateJurisdiction(null);
        multipleData.setBatchUpdateClaimantRep(null);

    }

    public static DynamicValueType getDynamicValue(String value) {

        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(value);
        dynamicValueType.setLabel(value);

        return dynamicValueType;

    }

    public static SubMultipleTypeItem createSubMultipleTypeItem(String subMultipleReference, String subMultipleName) {

        SubMultipleType subMultipleType = new SubMultipleType();
        subMultipleType.setSubMultipleName(subMultipleName);
        subMultipleType.setSubMultipleRef(subMultipleReference);

        SubMultipleTypeItem subMultipleTypeItem = new SubMultipleTypeItem();
        subMultipleTypeItem.setId(subMultipleReference);
        subMultipleTypeItem.setValue(subMultipleType);

        return subMultipleTypeItem;

    }

    public static void addSubMultipleTypeToCase(MultipleData multipleData, SubMultipleTypeItem subMultipleTypeItem) {

        List<SubMultipleTypeItem> subMultipleTypeItems = multipleData.getSubMultipleCollection();

        if (subMultipleTypeItems != null) {

            subMultipleTypeItems.add(subMultipleTypeItem);

        } else {

            subMultipleTypeItems = new ArrayList<>(Collections.singletonList(subMultipleTypeItem));

        }

        multipleData.setSubMultipleCollection(subMultipleTypeItems);

    }

    public static List<String> generateSubMultipleStringCollection(MultipleData multipleData) {

        if (multipleData.getSubMultipleCollection() != null && !multipleData.getSubMultipleCollection().isEmpty()) {

            return multipleData.getSubMultipleCollection().stream()
                    .map(subMultipleTypeItem -> subMultipleTypeItem.getValue().getSubMultipleName())
                    .distinct()
                    .collect(Collectors.toList());

        } else {

            return new ArrayList<>();

        }

    }

    public static String generateLeadMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl.replace("gateway","www").concat("/v2/case/" + caseId);

        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef +"</a>";

    }

    public static String generateExcelDocumentName(MultipleData multipleData) {

        return multipleData.getMultipleName() + "-" + multipleData.getMultipleReference() + ".xlsx";

    }

}
