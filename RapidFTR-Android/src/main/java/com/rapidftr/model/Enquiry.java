package com.rapidftr.model;

import android.database.Cursor;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.rapidftr.database.Database;
import com.rapidftr.repository.ChildRepository;
import com.rapidftr.utils.RapidFtrDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.rapidftr.database.Database.EnquiryTableColumn.content;
import static com.rapidftr.database.Database.EnquiryTableColumn.potential_matches;
import static com.rapidftr.utils.JSONArrays.asList;

public class Enquiry extends BaseModel {

    public static final String ENQUIRY_FORM_NAME = "Enquiries";

    public Enquiry() throws JSONException {
        super();
        this.setUniqueId(createUniqueId());
    }

    public Enquiry(String content, String createdBy) throws JSONException {
        super(content);
        this.setCreatedBy(createdBy);
        this.setUniqueId(createUniqueId());
        this.setLastUpdatedAt(RapidFtrDateTime.now().defaultFormat());
    }

    public Enquiry(Cursor cursor) throws JSONException {
        super(cursor.getString(cursor.getColumnIndex(content.getColumnName())));

        for (Database.EnquiryTableColumn column : Database.EnquiryTableColumn.values()) {
            final int columnIndex = cursor.getColumnIndex(column.getColumnName());

            if (columnIndex < 0 || column.equals(content)) {
                continue;
            } else if (column.getPrimitiveType().equals(Boolean.class)) {
                this.put(column.getColumnName(), cursor.getInt(columnIndex) == 1);
            } else {
                this.put(column.getColumnName(), cursor.getString(columnIndex));
            }
        }
    }

    public Enquiry(String enquiryJSON) throws JSONException {
        super(enquiryJSON);
        setHistories();
    }

    public List<Child> getPotentialMatches(ChildRepository childRepository) throws JSONException {
        try {

            JSONArray matchingChildId = new JSONArray(getPotentialMatchingIds());

            List<String> matchingChildList = getListOfMatchingChildrenFrom(matchingChildId);

            return childRepository.getAllWithInternalIds(new ArrayList<String>(matchingChildList));
        } catch (JSONException exception) {
            return new ArrayList<Child>();
        }
    }


    private List<String> getListOfMatchingChildrenFrom(JSONArray matchingChildId) throws JSONException {
        List<String> matchingChildList = new ArrayList<String>();

        for (int i = 0; i < matchingChildId.length(); i++) {
            matchingChildList.add((String) matchingChildId.get(i));
        }
        return matchingChildList;
    }

    public boolean isValid() {
        int numberOfInternalFields = names().length();

        for (Database.EnquiryTableColumn field : Database.EnquiryTableColumn.internalFields()) {
            if (has(field.getColumnName())) {
                numberOfInternalFields--;
            }
        }
        return numberOfInternalFields > 0;
    }

    public JSONObject values() throws JSONException {
        List<Object> names = asList(names());

        Iterable<Object> systemFields = Iterables.transform(Database.EnquiryTableColumn.systemFields(), new Function<Database.EnquiryTableColumn, Object>() {
            @Override
            public Object apply(Database.EnquiryTableColumn enquiryTableColumn) {
                return enquiryTableColumn.getColumnName();
            }
        });

        Iterables.removeAll(names, Lists.newArrayList(systemFields));
        return new JSONObject(this, names.toArray(new String[names.size()]));
    }

    public String getPotentialMatchingIds() {
        String ids = getString(potential_matches.getColumnName());
        String matchingChildIds = ids == null ? "" : ids;
        return matchingChildIds;
    }

}
