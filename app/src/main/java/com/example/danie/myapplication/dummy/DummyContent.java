package com.example.danie.myapplication.dummy;

import android.util.Log;

import com.example.danie.myapplication.Operations.IsMyDoorLockedAsyncOperation;
import com.example.danie.myapplication.Classes.SmartLockServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 1;

    static {
        // Add some sample items.
        Log.d("in dummy content","in dummy content");
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "My Door", makeDetails(position));
    }

    private static String makeDetails(int position) {
        String doorStatus;
        doorStatus = getDoorStatus();
        StringBuilder builder = new StringBuilder();
        builder.append("Details about My Door");
        builder.append("\nThe door is: " + doorStatus);
        return builder.toString();
    }

    private static String getDoorStatus() {
        //TODO- web service if door is locked
       // String url= "http://10.0.2.2:8080/servlets/isdoorlocked";
        String url= SmartLockServer.Ip + "/smartLock/servlets/isdoorlocked";
        try
        {
            IsMyDoorLockedAsyncOperation op = new IsMyDoorLockedAsyncOperation();
            Boolean ans = op.execute(url).get();
            if(ans == null)
            {
                return "Connection Failure";
            }
            else if(ans == true)
            {
                return "Locked.";
            }
            else
            {
                return "Open.";
            }
        }
        catch(Exception ex)
        {
            Log.d("in dummycontect ex","in dummycontect ex");
            Log.d(ex.getMessage(),ex.getMessage());
            return "Locked.";
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }

        public void RefreshDoorStatus() {
            this.details = DummyContent.makeDetails(Integer.parseInt(id));
        }
    }
}
