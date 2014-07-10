package com.rapidftr.forms;

import com.google.common.io.CharStreams;
import com.rapidftr.R;
import com.rapidftr.RapidFtrApplication;
import lombok.Cleanup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FormSectionTest {

    private static final String NAME_FIELD_ID = "name";
    private static final String RC_ID_NO_FIELD_ID = "rc_id_no";
    private static final String PROTECTION_STATUS_FIELD_ID = "protection_status";
    private static final String FTR_STATUS_FIELD_ID = "ftr_status";

    private List<FormSection> formSections;

    @Before
    public void setUp() throws IOException {
        this.formSections = loadFormSectionsFromClassPathResource();
    }

    @Test
    public void shouldReturnSortedHighlightedFields() {
        assertEquals(10, formSections.size());
        assertEquals("Basic Identity", formSections.get(0).getName().get("en"));

        List<FormField> formFields = formSections.get(0).getOrderedHighLightedFields();
        assertEquals(4, formFields.size());

        assertEquals(NAME_FIELD_ID, formFields.get(0).getId());
        assertEquals(RC_ID_NO_FIELD_ID, formFields.get(1).getId());
        assertEquals(PROTECTION_STATUS_FIELD_ID, formFields.get(2).getId());
        assertEquals(FTR_STATUS_FIELD_ID, formFields.get(3).getId());
    }

    public static List<FormSection> loadFormSectionsFromClassPathResource() throws IOException {
        @Cleanup InputStream inputStream = FormSectionTest.class.getClassLoader().getResourceAsStream("form_sections.json");

        String json = CharStreams.toString(new InputStreamReader(inputStream));
        return Arrays.asList(RapidFtrApplication.JSON_MAPPER.readValue(json, FormSection[].class));
    }
}
