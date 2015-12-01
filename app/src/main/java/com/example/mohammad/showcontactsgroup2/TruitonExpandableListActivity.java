package com.example.mohammad.showcontactsgroup2;

import java.util.HashMap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.content.CursorLoader;
import android.app.Activity;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.ExpandableListView;
import android.app.LoaderManager;

public class TruitonExpandableListActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //    private final String LOG_TAG = getClass().getSimpleName().toString();
    Context ctx;

    private final String DEBUG_TAG = getClass().getSimpleName().toString();

    private static final String[] CONTACTS_PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME};

    private static final String[] GROUPS_PROJECTION = new String[]{
            ContactsContract.Groups.TITLE, ContactsContract.Groups._ID};

//    private static final String[] PHONE_PROJECTION = new String[] {
//            ContactsContract.CommonDataKinds.Phone._ID,
//            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
//            ContactsContract.CommonDataKinds.Phone.NUMBER,
//            ContactsContract.CommonDataKinds.Phone.TYPE };
//
//    private static final String[] CONTACT_PROJECTION = new String[] {
//            ContactsContract.Contacts._ID,
//            ContactsContract.Contacts.DISPLAY_NAME };

    TruitonSimpleCursorTreeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();

        populateContactList();
//        mAdapter = new TruitonSimpleCursorTreeAdapter(this,
//                android.R.layout.simple_expandable_list_item_1,
//                android.R.layout.simple_expandable_list_item_1,
//                new String[] { ContactsContract.Contacts.DISPLAY_NAME },
//                new int[] { android.R.id.text1 },
//                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
//                new int[] { android.R.id.text1 });

//        expandableContactListView.setAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        Loader loader = getLoaderManager().getLoader(-1);
        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(-1, null, this);
        } else {
            getLoaderManager().initLoader(-1, null, this);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // This is called when a new Loader needs to be created.
        Log.d(DEBUG_TAG, "onCreateLoader for loader_id " + id);
        CursorLoader cl;
        if (id != -1) {
            // child cursor
            Uri contactsUri = ContactsContract.Data.CONTENT_URI;
            String selection = "((" + ContactsContract.Contacts.DISPLAY_NAME
                    + " NOTNULL) AND ("
                    + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + ContactsContract.Contacts.DISPLAY_NAME + " != '') AND ("
                    + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                    + " = ? ))";
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";
            String[] selectionArgs = new String[]{String.valueOf(id)};

            cl = new CursorLoader(ctx, contactsUri,
                    CONTACTS_PROJECTION, selection, selectionArgs, sortOrder);
        } else {
            // group cursor
            Uri groupsUri = ContactsContract.Groups.CONTENT_URI;
            String selection = "((" + ContactsContract.Groups.TITLE
                    + " NOTNULL) AND (" + ContactsContract.Groups.TITLE
                    + " != '' ))";
            String sortOrder = ContactsContract.Groups.TITLE
                    + " COLLATE LOCALIZED ASC";
            cl = new CursorLoader(ctx, groupsUri,
                    GROUPS_PROJECTION, selection, null, sortOrder);
        }

        return cl;
//        Log.d(LOG_TAG, "onCreateLoader for loader_id " + id);
//        CursorLoader cl;
//        if (id != -1) {
//            // child cursor
//            Uri contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//            String selection = "("
//                    + ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                    + " = ? )";
//            String sortOrder = ContactsContract.CommonDataKinds.Phone.TYPE
//                    + " COLLATE LOCALIZED ASC";
//            String[] selectionArgs = new String[] { String.valueOf(id) };
//
//            cl = new CursorLoader(this, contactsUri, PHONE_PROJECTION,
//                    selection, selectionArgs, sortOrder);
//        } else {
//            // group cursor
////            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
////            String selection = "((" + ContactsContract.Contacts.DISPLAY_NAME
////                    + " NOTNULL) AND ("
////                    + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
////                    + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
////            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
////                    + " COLLATE LOCALIZED ASC";
////            cl = new CursorLoader(this, contactsUri, CONTACT_PROJECTION,
////                    selection, null, sortOrder);
//            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
//            String selection = "((" + ContactsContract.Groups.TITLE
//                    + " NOTNULL) AND ("
//                    + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
//                    + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
//            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
//                    + " COLLATE LOCALIZED ASC";
//            cl = new CursorLoader(this, contactsUri, CONTACT_PROJECTION,
//                    selection, null, sortOrder);
//
//
//        }
//
//        return cl;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.
        int id = loader.getId();
        Log.d(DEBUG_TAG, "onLoadFinished() for loader_id " + id);
        if (id != -1) {
            // child cursor
            if (!data.isClosed()) {
                Log.d(DEBUG_TAG, "data.getCount() " + data.getCount());

                HashMap<Integer, Integer> groupMap = mAdapter.getGroupMap();
                try {
                    int groupPos = groupMap.get(id);
                    Log.d(DEBUG_TAG, "onLoadFinished() for groupPos " + groupPos);
                    mAdapter.setChildrenCursor(groupPos, data);
                } catch (NullPointerException e) {
                    Log.w("DEBUG", "Adapter expired, try again on the next query: "
                            + e.getMessage());
                }
            }
        } else {
            mAdapter.setGroupCursor(data);
        }

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // is about to be closed.
        int id = loader.getId();
        Log.d(DEBUG_TAG, "onLoaderReset() for loader_id " + id);
        if (id != -1) {
            // child cursor
            try {
                mAdapter.setChildrenCursor(id, null);
            } catch (NullPointerException e) {
                Log.w("TAG", "Adapter expired, try again on the next query: "
                        + e.getMessage());
            }
        } else {
            mAdapter.setGroupCursor(null);
        }
    }

    private void populateContactList() {
        // Set up our adapter
//        mAdapter = new TruitonSimpleCursorTreeAdapter(ctx,this,
//                android.R.layout.simple_expandable_list_item_1,
//                android.R.layout.simple_expandable_list_item_1,
//                new String[] { ContactsContract.Groups.TITLE }, // Name for group layouts
//                new int[] { android.R.id.text1 },
//                new String[] { ContactsContract.Contacts.DISPLAY_NAME }, // Name for child layouts
//                new int[] { android.R.id.text1 });
//
//        setListAdapter(mAdapter);

        ExpandableListView expandableContactListView = (ExpandableListView) findViewById(R.id.expandableListView1);

        mAdapter = new TruitonSimpleCursorTreeAdapter(this,
                android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{ContactsContract.Groups.TITLE},
                new int[]{android.R.id.text1},
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                new int[]{android.R.id.text1});
        expandableContactListView.setAdapter(mAdapter);
    }
}
