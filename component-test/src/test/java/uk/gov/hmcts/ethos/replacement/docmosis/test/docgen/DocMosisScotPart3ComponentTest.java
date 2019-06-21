package uk.gov.hmcts.ethos.replacement.docmosis.test.docgen;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import uk.gov.hmcts.ethos.replacement.docmosis.test.ComponentTest;
import uk.gov.hmcts.ethos.replacement.docmosis.test.util.TestUtil;

@Category(ComponentTest.class)
@RunWith(SerenityRunner.class)
@WithTags({
        @WithTag("ComponentTest"),
        @WithTag("FunctionalTest")
})
public class DocMosisScotPart3ComponentTest {


    private TestUtil testUtil;

    @Before
    public void setUp() {
        testUtil = new TestUtil();
    }

    @Test
    @WithTag("SmokeTest")
    public void generateDocument_Part_Scot_15() throws Exception {
        testUtil.executeGenerateDocumentTest("15", "", "your claim - FURTHER INFORMATION REQUIRED", true);
    }

    @Test
    public void generateDocument_Part_Scot_16() throws Exception {
        testUtil.executeGenerateDocumentTest("16", "", "Rule 21 of the Employment Tribunal Rules of Procedure 2013", true);
    }

    @Test
    public void generateDocument_Part_Scot_16_1() throws Exception {
        testUtil.executeGenerateDocumentTest("16", "1", "EMPLOYMENT TRIBUNALS", true);
    }

    @After
    public void tearDown() {

    }
}