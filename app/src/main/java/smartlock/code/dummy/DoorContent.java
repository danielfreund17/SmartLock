package smartlock.code.dummy;

import android.util.Log;

import smartlock.code.Operations.IsMyDoorLockedAsyncOperation;
import smartlock.code.Classes.SmartLockServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the door class.
 */
public class DoorContent
{

    /**
     * An array of sample (door) items.
     */
    public static final List<DoorItem> ITEMS = new ArrayList<DoorItem>();
    /**
     * A map of sample (door) items, by ID.
     */
    public static final Map<String, DoorItem> ITEM_MAP = new HashMap<String, DoorItem>();

    private static final int COUNT = 1;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDoorItem(i));
        }
    }

    //This method is part of the future interface for having more than one door.
    private static void addItem(DoorItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DoorItem createDoorItem(int position) {
        return new DoorItem(String.valueOf(position), "My Door", makeDetails(position));
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
        String url= SmartLockServer.Ip + "/smartLock/servlets/isdoorlocked";
        try
        {
            IsMyDoorLockedAsyncOperation op = new IsMyDoorLockedAsyncOperation();
            Boolean ans = op.execute(url).get();
            if(ans == null)
            {//TODO- change to connection failure
                return "Connection Failure.";
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
            return "Locked.";
        }
    }

    /**
     * A door single door item.
     */
    public static class DoorItem
    {
        public String id;
        public String content;
        public String details;

        public DoorItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }

        public void RefreshDoorStatus() {
            this.details = DoorContent.makeDetails(Integer.parseInt(id));
        }
    }
}
