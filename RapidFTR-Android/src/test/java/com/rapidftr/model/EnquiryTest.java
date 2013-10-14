package com.rapidftr.model;

import android.database.Cursor;
import com.rapidftr.CustomTestRunner;
import com.rapidftr.database.Database;
import com.rapidftr.database.DatabaseSession;
import com.rapidftr.database.ShadowSQLiteHelper;
import com.rapidftr.repository.ChildRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(CustomTestRunner.class)
public class EnquiryTest {


    private String createdBy;
    private String reporterName;
    private JSONObject criteria;
    private DatabaseSession session;
    private ChildRepository childRepository;
    @Mock private Cursor cursor;

    @Before
    public void setUp() throws JSONException {
        initMocks(this);
        createdBy = "Rajni";
        reporterName = "Batman";
        criteria = new JSONObject("{\"name\":\"NAME\"}");
        session = new ShadowSQLiteHelper("test_database").getSession();
        childRepository = new ChildRepository("user1", session);
    }

    @Test
    public void shouldAutoGenerateAUniqueID() throws JSONException {
        Enquiry enquiry = new Enquiry();
        assertNotNull(enquiry.getUniqueId());
    }


    @Test
    public void createEnquiryWithAllFields() throws JSONException{

      Enquiry enquiry = new Enquiry(createdBy, reporterName, criteria);

      assertEquals(reporterName, enquiry.getEnquirerName());
      assertEquals(enquiry.getCriteria().getClass(), JSONObject.class);
      assertEquals(criteria.toString(), enquiry.getCriteria().toString());
      assertEquals(createdBy, enquiry.getCreatedBy());
      assertNotNull(enquiry.getCreatedAt());
      assertNotNull(enquiry.getLastUpdatedAt());
    }


    @Test
    public void enquiryShouldGetPotentialMatches() throws JSONException {
        String enquiryJSON = "{\"createdBy\":\"user\"," +
                "\"enquirer_name\":\"faris\"," +
                "\"criteria\":{\"age\":14,\"name\":\"Subhas\"}, " +
                "\"potential_matches\":\"[\\\"id1\\\", \\\"id2\\\"]\"}";

        doReturn(2).when(cursor).getColumnIndex(Database.EnquiryTableColumn.criteria.getColumnName());
        doReturn(enquiryJSON).when(cursor).getString(2);

        Child child1 = new Child("id1", "owner1", "{ 'test1' : 'value1' }");
        Child child2 = new Child("id2", "owner1", "{ 'test1' : 'value1' }");

        childRepository.createOrUpdate(child1);
        childRepository.createOrUpdate(child2);

        Enquiry enquiry = new Enquiry(cursor);

        assertEquals(2, enquiry.getPotentialMatches(childRepository).size());
        assertTrue(enquiry.getPotentialMatches(childRepository).contains(child1));
        assertTrue(enquiry.getPotentialMatches(childRepository).contains(child2));
    }

    @Test
    public void enquiryShouldGetMatchingIds() throws JSONException {
        String enquiryJSON = "{\"createdBy\":\"user\"," +
                "\"enquirer_name\":\"faris\"," +
                "\"criteria\":{\"age\":14,\"name\":\"Subhas\"}, " +
                "\"potential_matches\":\"[\\\"id1\\\", \\\"id2\\\"]\"}";

        doReturn(2).when(cursor).getColumnIndex(Database.EnquiryTableColumn.criteria.getColumnName());
        doReturn(enquiryJSON).when(cursor).getString(2);

        Enquiry enquiry = new Enquiry(cursor);

        assertEquals("[\"id1\", \"id2\"]",enquiry.matchingChildIds());
    }

    @Test(expected=JSONException.class)
    public void newEnquiryShouldNotHaveMatchingIds() throws JSONException {
        Enquiry enquiry = new Enquiry(createdBy, reporterName, criteria);
        enquiry.matchingChildIds();
    }
}
