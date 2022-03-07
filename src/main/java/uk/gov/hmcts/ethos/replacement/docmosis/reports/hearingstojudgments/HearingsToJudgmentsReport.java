package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ecm.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_ALLOCATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public class HearingsToJudgmentsReport {
    static final String PERCENTAGE_FORMAT = "%.2f";

    static final List<String> VALID_CASE_STATES = Arrays.asList(
            CLOSED_STATE,
            ACCEPTED_STATE);

    static final List<String> VALID_HEARING_TYPES = Arrays.asList(
            HEARING_TYPE_JUDICIAL_HEARING,
            HEARING_TYPE_PERLIMINARY_HEARING,
            HEARING_TYPE_PERLIMINARY_HEARING_CM,
            HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC);

    static class HearingWithJudgment {
        String hearingDate;
        String judgmentDateSent;
        Long total;
        String reservedHearing;
        Boolean judgmentWithin4Weeks;
        String judge;
    }

    private final HearingsToJudgmentsDataSource hearingsToJudgmentsReportDataSource;
    private final String listingDateFrom;
    private final String listingDateTo;

    public HearingsToJudgmentsReport(HearingsToJudgmentsDataSource hearingsToJudgmentsReportDataSource,
                                     String listingDateFrom, String listingDateTo) {
        this.hearingsToJudgmentsReportDataSource = hearingsToJudgmentsReportDataSource;
        this.listingDateFrom = listingDateFrom;
        this.listingDateTo = listingDateTo;
    }

    public HearingsToJudgmentsReportData runReport(String caseTypeId, String managingOffice) {
        var submitEvents = getCases(caseTypeId, managingOffice);
        var office = StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)
                        ? managingOffice
                        : TribunalOffice.SCOTLAND.getOfficeName();
        var reportData = initReport(office);

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            populateData(reportData, submitEvents, caseTypeId);
        }

        return reportData;
    }

    private HearingsToJudgmentsReportData initReport(String office) {
        var reportSummary = new HearingsToJudgmentsReportSummary(office);
        return new HearingsToJudgmentsReportData(reportSummary);
    }

    private List<HearingsToJudgmentsSubmitEvent> getCases(String caseTypeId, String managingOffice) {
        return hearingsToJudgmentsReportDataSource.getData(UtilHelper.getListingCaseTypeId(caseTypeId),
                managingOffice, listingDateFrom, listingDateTo);
    }

    private void populateData(HearingsToJudgmentsReportData reportData,
                              List<HearingsToJudgmentsSubmitEvent> submitEvents,
                              String listingCaseTypeId) {
        log.info(String.format("Hearings to judgments case type id %s search results: %d",
                listingCaseTypeId, submitEvents.size()));

        List<HearingWithJudgment> allHearingsWithJudgments = new ArrayList<>();
        for (var submitEvent : submitEvents) {
            if (!isValidCase(submitEvent)) {
                continue;
            }

            var caseData = submitEvent.getCaseData();
            for (HearingTypeItem hearingItem: caseData.getHearingCollection()) {
                var judgmentsCollection = caseData.getJudgementCollection();
                var hearingsWithJudgments =
                        getHearingsAndJudgmentsCollection(hearingItem, judgmentsCollection);
                allHearingsWithJudgments.addAll(hearingsWithJudgments);

                for (var hearingWithJudgment : hearingsWithJudgments) {
                    if (Boolean.TRUE.equals(hearingWithJudgment.judgmentWithin4Weeks)) {
                        continue;
                    }

                    var reportDetail = new HearingsToJudgmentsReportDetail();
                    reportDetail.setReportOffice(caseData.getManagingOffice());
                    reportDetail.setCaseReference(caseData.getEthosCaseReference());
                    reportDetail.setHearingDate(hearingWithJudgment.hearingDate);
                    reportDetail.setJudgementDateSent(hearingWithJudgment.judgmentDateSent);
                    reportDetail.setTotalDays(hearingWithJudgment.total.toString());
                    reportDetail.setReservedHearing(hearingWithJudgment.reservedHearing);
                    reportDetail.setHearingJudge(hearingWithJudgment.judge);

                    reportData.addReportDetail(reportDetail);
                }
            }
        }

        reportData.getReportDetails().sort(Comparator.comparingInt(o -> Integer.parseInt(o.getTotalDays())));
        addReportSummary(reportData.getReportSummary(), allHearingsWithJudgments);
    }

    private void addReportSummary(HearingsToJudgmentsReportSummary reportSummary,
                                  List<HearingWithJudgment> hearings) {
        int totalCases = hearings.size();
        long totalCasesWithin4Weeks = hearings.stream().filter(h -> h.judgmentWithin4Weeks).count();
        long totalCasesNotWithin4Weeks = hearings.stream().filter(h -> !h.judgmentWithin4Weeks).count();

        float totalCasesWithin4WeeksPercent = (totalCases != 0)
                ? ((float) totalCasesWithin4Weeks / totalCases) * 100 : 0;
        float totalCasesNotWithin4WeeksPercent = (totalCases != 0)
                ? ((float) totalCasesNotWithin4Weeks / totalCases) * 100 : 0;

        reportSummary.setTotalCases(String.valueOf(totalCases));
        reportSummary.setTotal4Wk(String.valueOf(totalCasesWithin4Weeks));
        reportSummary.setTotalX4Wk(String.valueOf(totalCasesNotWithin4Weeks));
        reportSummary.setTotal4WkPercent(String.format(PERCENTAGE_FORMAT,
                totalCasesWithin4WeeksPercent));
        reportSummary.setTotalX4WkPercent(String.format(PERCENTAGE_FORMAT,
                totalCasesNotWithin4WeeksPercent));
    }

    private List<HearingWithJudgment> getHearingsAndJudgmentsCollection(HearingTypeItem hearingTypeItem,
                                                                        List<JudgementTypeItem> judgmentsCollection) {
        var hearingType = hearingTypeItem.getValue();
        List<HearingWithJudgment> hearingJudgmentsList = new ArrayList<>();
        if (!isValidHearing(hearingTypeItem)) {
            return hearingJudgmentsList;
        }

        for (var dateListedTypeItem : hearingType.getHearingDateCollection()) {
            var dateListedType = dateListedTypeItem.getValue();
            var hearingListedDate = LocalDate.parse(dateListedType.getListedDate(), OLD_DATE_TIME_PATTERN);
            var judgements = judgmentsCollection.stream()
                                .filter(j -> judgmentHearingDateMatchHearingListedDate(j.getValue(), hearingListedDate)
                                        && judgmentHearingMatchHearingNumber(j.getValue(), hearingType))
                                .collect(Collectors.toList());

            if (judgements.isEmpty()
                    || !isWithinDateRange(hearingListedDate)
                    || !isValidHearingDate(dateListedTypeItem)) {
                continue;
            }

            for (var judgmentItem : judgements) {
                var judgment = judgmentItem.getValue();
                var dateJudgmentMade = LocalDate.parse(judgment.getDateJudgmentMade(), OLD_DATE_TIME_PATTERN2);
                var dateJudgmentSent = LocalDate.parse(judgment.getDateJudgmentSent(), OLD_DATE_TIME_PATTERN2);
                var hearingDatePlus4Wks = hearingListedDate.plusWeeks(4).plusDays(1);

                var hearingJudgmentItem = new HearingWithJudgment();
                hearingJudgmentItem.judgmentWithin4Weeks = dateJudgmentMade.isBefore(hearingDatePlus4Wks);
                hearingJudgmentItem.hearingDate = hearingListedDate.format(OLD_DATE_TIME_PATTERN2);
                hearingJudgmentItem.judgmentDateSent = dateJudgmentSent.format(OLD_DATE_TIME_PATTERN2);
                hearingJudgmentItem.total = hearingListedDate.datesUntil(dateJudgmentSent.plusDays(1)).count();
                hearingJudgmentItem.reservedHearing = dateListedType.getHearingReservedJudgement();
                if (hearingType.hasHearingJudge()) {
                    hearingJudgmentItem.judge = hearingType.getJudge().getSelectedLabel();
                } else {
                    hearingJudgmentItem.judge = NOT_ALLOCATED;
                }

                hearingJudgmentsList.add(hearingJudgmentItem);
            }
        }

        return hearingJudgmentsList;
    }

    private boolean judgmentHearingMatchHearingNumber(JudgementType judgment, HearingType hearing) {
        return judgment != null && hearing != null
                && judgment.getDynamicJudgementHearing() != null
                && judgment.getDynamicJudgementHearing().getValue() != null
                && judgment.getDynamicJudgementHearing().getValue().getLabel() != null
                && judgment.getDynamicJudgementHearing().getValue().getLabel().split(" : ")[0]
                    .equals(hearing.getHearingNumber());
    }

    private boolean judgmentHearingDateMatchHearingListedDate(JudgementType judgment, LocalDate hearingListedDate) {
        return judgment != null && judgment.getJudgmentHearingDate() != null
                && hearingListedDate.isEqual(
                        LocalDate.parse(judgment.getJudgmentHearingDate(), OLD_DATE_TIME_PATTERN2));
    }

    private boolean isValidCase(HearingsToJudgmentsSubmitEvent submitEvent) {
        if (!VALID_CASE_STATES.contains(submitEvent.getState())) {
            return false;
        }

        var caseData = submitEvent.getCaseData();
        return caseHasJudgments(caseData) && isCaseWithValidHearing(caseData);
    }

    private boolean isCaseWithValidHearing(HearingsToJudgmentsCaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return false;
        }

        for (var hearingTypeItem : caseData.getHearingCollection()) {
            if (isValidHearing(hearingTypeItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidHearing(HearingTypeItem hearingTypeItem) {
        var hearingType = hearingTypeItem.getValue();
        if (hearingType == null
            || CollectionUtils.isEmpty(hearingType.getHearingDateCollection())
            || !VALID_HEARING_TYPES.contains(hearingType.getHearingType())) {
            return false;
        }

        for (var dateListedItemType : hearingType.getHearingDateCollection()) {
            if (isValidHearingDate(dateListedItemType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidHearingDate(DateListedTypeItem dateListedTypeItem) {
        var dateListedType = dateListedTypeItem.getValue();
        return HEARING_STATUS_HEARD.equals(dateListedType.getHearingStatus())
                && YES.equals(dateListedType.getHearingCaseDisposed());
    }

    private boolean isWithinDateRange(LocalDate hearingListedDate) {
        var from = LocalDate.parse(listingDateFrom, OLD_DATE_TIME_PATTERN);
        var to = LocalDate.parse(listingDateTo, OLD_DATE_TIME_PATTERN);
        return (!hearingListedDate.isBefore(from)) && (!hearingListedDate.isAfter(to));
    }

    private boolean caseHasJudgments(HearingsToJudgmentsCaseData caseData) {
        return CollectionUtils.isNotEmpty(caseData.getJudgementCollection());
    }
}