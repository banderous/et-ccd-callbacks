package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.StaffDataRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffImportServiceTest {

    @Test
    void testImportStaff() throws IOException {
        var documentBinaryUrl = "http://dm-store/document/23232323";
        var userToken = "test-token";
        var userName = "Donald Duck";
        var user = new UserDetails();
        user.setName(userName);
        var userService = mock(UserService.class);
        when(userService.getUserDetails(userToken)).thenReturn(user);
        var excelReadingService = mockExcelReadingService(userToken, documentBinaryUrl);
        var judgeRepository = mock(JudgeRepository.class);
        var rowHandler = mock(StaffDataRowHandler.class);

        var adminData = createAdminData(documentBinaryUrl);

        var staffImportService = new StaffImportService(userService, excelReadingService, judgeRepository, rowHandler);
        staffImportService.importStaff(adminData, userToken);

        verify(judgeRepository, times(1)).deleteAll();
        verify(excelReadingService, times(1)).readWorkbook(userToken, documentBinaryUrl);
        assertEquals(userName, adminData.getStaffImportFile().getUser());
        assertNotNull(adminData.getStaffImportFile().getLastImported());
    }

    private ExcelReadingService mockExcelReadingService(String userToken, String documentBinaryUrl) throws IOException {
        var excelReadingService = mock(ExcelReadingService.class);
        var workbook = new XSSFWorkbook();
        when(excelReadingService.readWorkbook(userToken, documentBinaryUrl)).thenReturn(workbook);
        return excelReadingService;
    }

    private AdminData createAdminData(String documentBinaryUrl) {
        var adminData = new AdminData();
        var importFile = new ImportFile();
        var document = new Document();
        document.setBinaryUrl(documentBinaryUrl);
        importFile.setFile(document);
        adminData.setStaffImportFile(importFile);

        return adminData;
    }
}