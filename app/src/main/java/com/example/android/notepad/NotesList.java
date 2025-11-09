/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import com.example.android.notepad.NotePad;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.SearchView;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LifecycleOwner;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the incoming Intent if there is one, otherwise it defaults to displaying the
 * contents of the {@link NotePadProvider}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler} or
 * {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class NotesList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // For logging and debugging
    private static final String TAG = "NotesList";

    // Loader ID
    private static final int NOTES_LOADER = 0;

    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_NOTE, // 2
            NotePad.Notes.COLUMN_NAME_CATEGORY, // 3
            NotePad.Notes.COLUMN_NAME_CREATE_DATE // 4
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_NOTE = 2;
    private static final int COLUMN_INDEX_CATEGORY = 3;
    private static final int COLUMN_INDEX_CREATE_DATE = 4;

    private NotesListAdapter mAdapter;
    private androidx.appcompat.widget.SearchView mSearchView;
    private String mSearchQuery = "";

    // Category filter
    private Spinner mCategorySpinner;
    private long mSelectedCategoryId = -1; // -1 means show all categories
    // 在类成员变量区域添加以下声明
    private View fabAddNote;
    private ListView mTodosListView;
    private static final int ALL_CATEGORIES_ID = -1;
    private static final int DEFAULT_CATEGORY_ID = 1;
    /**
     * onCreate is called when Android starts this Activity from scratch.
     */
    @SuppressLint("MissingInflatedId")
    @Override protected void onCreate(Bundle savedInstanceState) {
        // 从 SharedPreferences 获取主题
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");

        if ("dark".equals(theme)) {
            setTheme(R.style.AppTheme_Dark); // 使用 Dark 主题
        } else {
            setTheme(R.style.AppTheme_Light); // 使用 Light 主题
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list);


        if (getActionBar() != null) {
            getActionBar().setTitle("企业笔记");
        }

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

// 初始化搜索框
        mSearchView = (SearchView) findViewById(R.id.search_view);
        setupSearchListener();

// 初始化分类下拉框
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        initCategorySpinner();

        getListView().setOnCreateContextMenuListener(this);

// 创建适配器
        Cursor cursor = getContentResolver().query(
                getIntent().getData(),
                PROJECTION,
                null,
                null,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
        mAdapter = new NotesListAdapter(this, cursor);
        setListAdapter(mAdapter);

// 初始化 FloatingActionButton
        fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesList.this, NoteEditor.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.setData(getIntent().getData());
                startActivity(intent);
            }
        });
// 初始化设置按钮
        FloatingActionButton fabSettings = findViewById(R.id.fab_settings);
        fabSettings.setOnClickListener(v -> {
            Intent settingsTntent = new Intent(NotesList.this, SettingsActivity.class);
            startActivity(settingsTntent);
        });

    }
    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        super.onDestroy();
    }

    /**
     * Initialize category spinner for filtering notes by category
     */
    private void initCategorySpinner() {
        // Query all categories
        Cursor categoriesCursor = getContentResolver().query(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                new String[]{Category.COLUMN_ID, Category.COLUMN_NAME},
                null, null, Category.COLUMN_NAME);

        // 创建一个新的 MatrixCursor 来添加"全部分类"选项
        MatrixCursor allCategoriesCursor = new MatrixCursor(new String[]{Category.COLUMN_ID, Category.COLUMN_NAME});
        allCategoriesCursor.addRow(new Object[]{-1, "全部分类"}); // 添加"全部分类"选项
// 将查询到的分类数据添加到 MatrixCursor 中
        if (categoriesCursor != null) {
            while (categoriesCursor.moveToNext()) {
                @SuppressLint("Range") long id = categoriesCursor.getLong(categoriesCursor.getColumnIndex(Category.COLUMN_ID));
                @SuppressLint("Range") String name = categoriesCursor.getString(categoriesCursor.getColumnIndex(Category.COLUMN_NAME));
                allCategoriesCursor.addRow(new Object[]{id, name});
            }
            categoriesCursor.close();
        }
        // Create adapter for categories
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                allCategoriesCursor,
                new String[]{Category.COLUMN_NAME},
                new int[]{android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);
        // 设置默认选择为"全部分类"
        mCategorySpinner.setSelection(0);
        mSelectedCategoryId = -1; // 设置默认分类ID为-1表示全部分类

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCategoryId = id;
                refreshNotesList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedCategoryId = -1;
                refreshNotesList();
            }
        });
    }

    /**
     * Refresh notes list based on current filters (category and search)
     */
//    private void refreshNotesList() {
//        CursorLoader loader = new CursorLoader(
//                this,
//                getIntent().getData(),
//                PROJECTION,
//                buildSelection(),
//                buildSelectionArgs(),
//                NotePad.Notes.DEFAULT_SORT_ORDER
//        );
//
//        Cursor cursor = loader.loadInBackground();
//        mAdapter.swapCursor(cursor);
//    }
    // 修改 refreshNotesList() 方法以处理"全部分类"的情况
    private void refreshNotesList() {
        StringBuilder where = new StringBuilder();
        List<String> whereArgsList = new ArrayList<>();

        // 处理搜索条件
        if (!TextUtils.isEmpty(mSearchQuery)) {
            where.append("(" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?)");
            whereArgsList.add("%" + mSearchQuery + "%");
            whereArgsList.add("%" + mSearchQuery + "%");
        }

        // 处理分类筛选条件
        if (mSelectedCategoryId != -1) {  // 如果不是"全部分类"
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(NotePad.Notes.COLUMN_NAME_CATEGORY_ID + " = ?");
            whereArgsList.add(String.valueOf(mSelectedCategoryId));
        }

        String[] whereArgs = whereArgsList.toArray(new String[0]);

        // 重新查询数据
        Cursor cursor = getContentResolver().query(
                getIntent().getData(),
                new String[]{
                        NotePad.Notes._ID,           // 0
                        NotePad.Notes.COLUMN_NAME_TITLE,    // 1
                        NotePad.Notes.COLUMN_NAME_NOTE,     // 2
                        NotePad.Notes.COLUMN_NAME_CATEGORY, // 3
                        NotePad.Notes.COLUMN_NAME_CREATE_DATE   // 4
                },
                where.length() > 0 ? where.toString() : null,
                whereArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );

        // 更新适配器中的数据
        mAdapter.changeCursor(cursor);
    }


    /**
     * 构建查询条件 selection 字符串
     */
    private String buildSelection() {
        StringBuilder selection = new StringBuilder();

        // 分类筛选
        if (mSelectedCategoryId != -1) {
            selection.append(NotePad.Notes.COLUMN_NAME_CATEGORY_ID).append("=?");
        }

        // 搜索关键词筛选
        if (!mSearchQuery.isEmpty()) {
            String searchSelection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";

            if (selection.length() > 0) {
                selection.insert(0, "(").append(") AND (").append(searchSelection).append(")");
            } else {
                selection.append(searchSelection);
            }
        }

        return selection.toString();
    }

    /**
     * 构建查询参数 selectionArgs 数组
     */
    private String[] buildSelectionArgs() {
        java.util.List<String> selectionArgs = new java.util.ArrayList<>();

        // 分类参数
        if (mSelectedCategoryId != -1) {
            selectionArgs.add(String.valueOf(mSelectedCategoryId));
        }

        // 搜索关键词参数
        if (!mSearchQuery.isEmpty()) {
            selectionArgs.add("%" + mSearchQuery + "%"); // title
            selectionArgs.add("%" + mSearchQuery + "%"); // note
        }

        return selectionArgs.toArray(new String[0]);
    }




    private void setupSearchListener() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchQuery = query;
                refreshNotesList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mSearchQuery = "";
                } else {
                    mSearchQuery = newText;
                }
                refreshNotesList();
                return true;
            }
        });
    }

    /**
     * Called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Sets up a menu that provides the Insert option plus a list of alternative actions for
     * this Activity. Other applications that want to handle notes can "register" themselves in
     * Android by providing an intent filter that includes the category ALTERNATIVE and the
     * mimeTYpe NotePad.Notes.CONTENT_TYPE. If they do this, the code in onCreateOptionsMenu()
     * will add the Activity that contains the intent filter to its list of options. In effect,
     * the menu will offer the user other applications that can handle notes.
     * @param menu A Menu object, to which menu items should be added.
     * @return True, always. The menu should be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);
        // 添加分类管理菜单项
        menu.add(0, R.id.menu_categories, 0, "分类管理")
                .setIcon(android.R.drawable.ic_menu_manage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);


// 在 onCreateOptionsMenu 中添加设置菜单项
        menu.add(0, R.id.menu_settings, 0, "设置")
                .setIcon(android.R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // The paste menu item is enabled if there is data on the clipboard.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);


        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // If the clipboard contains an item, enables the Paste option on the menu.
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // If the clipboard is empty, disables the menu's Paste option.
            mPasteItem.setEnabled(false);
        }

        // Gets the number of notes currently being displayed.
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {

            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Creates an array of Intents with one element. This will be used to send an Intent
            // based on the selected menu item.
            Intent[] specifics = new Intent[1];

            // Sets the Intent in the array to be an EDIT action on the URI of the selected note.
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // Creates an array of menu items with one element. This will contain the EDIT option.
            MenuItem[] items = new MenuItem[1];

            // Creates an Intent with no specific action, using the URI of the selected note.
            Intent intent = new Intent(null, uri);

            /* Adds the category ALTERNATIVE to the Intent, with the note ID URI as its
             * data. This prepares the Intent as a place to group alternative options in the
             * menu.
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * Add alternatives to the menu
             */
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
                    Menu.NONE,                  // A unique item ID is not required.
                    Menu.NONE,                  // The alternatives don't need to be in order.
                    null,                       // The caller's name is not excluded from the group.
                    specifics,                  // These specific options must appear first.
                    intent,                     // These Intent objects map to the options in specifics.
                    Menu.NONE,                  // No flags are required.
                    items                       // The menu items generated from the specifics-to-
                    // Intents mapping
            );
            // If the Edit menu item exists, adds shortcuts for it.
            if (items[0] != null) {

                // Sets the Edit menu item shortcut to numeric "1", letter "e"
                items[0].setShortcut('1', 'e');
            }
        } else {
            // If the list is empty, removes any existing alternative actions from the menu
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        // Displays the menu
        return true;
    }

    /**
     * This method is called when the user selects an option from the menu, but no item
     * in the list is selected. If the option was INSERT, then a new Intent is sent out with action
     * ACTION_INSERT. The data from the incoming Intent is put into the new Intent. In effect,
     * this triggers the NoteEditor activity in the NotePad application.
     *
     * If the item was not INSERT, then most likely it was an alternative option from another
     * application. The parent method is called to process the item.
     * @param item The menu item that was selected by the user
     * @return True, if the INSERT menu item was selected; otherwise, the result of calling
     * the parent method.
     */
// 在 NotesList.java 的 onOptionsItemSelected 方法中添加
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_add) {
            Intent intent = new Intent(this, NoteEditor.class);
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(getIntent().getData());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_paste) {
            Intent intent = new Intent(this, NoteEditor.class);
            intent.setAction(Intent.ACTION_PASTE);
            intent.setData(getIntent().getData());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_categories) {  // 添加分类管理入口处理
            Intent intent = new Intent(this, CategoryManagerActivity.class);
            //startActivity(intent);
            startActivityForResult(intent, 1);
            return true;
        }
        else if (itemId == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }



    /**
     * This method is called when the user context-clicks a note in the list. NotesList registers
     * itself as the handler for context menus in its ListView (this is done in onCreate()).
     *
     * The only available options are COPY and DELETE.
     *
     * Context-click is equivalent to long-press.
     *
     * @param menu A ContexMenu object to which items should be added.
     * @param view The View for which the context menu is being constructed.
     * @param menuInfo Data associated with view.
     * @throws ClassCastException
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        // Tries to get the position of the item in the ListView that was long-pressed.
        try {
            // Casts the incoming data object into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // If the menu object can't be cast, logs an error.
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * Gets the data associated with the item at the selected position. getItem() returns
         * whatever the backing adapter of the ListView has associated with the item. In NotesList,
         * the adapter associated all of the data for a note with its list item. As a result,
         * getItem() returns that data as a Cursor.
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // If the cursor is empty, then for some reason the adapter can't get the data from the
        // provider, so returns null to the caller.
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // Sets the menu header to be the title of the selected note.
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    /**
     * This method is called when the user selects an item from the context menu
     * (see onCreateContextMenu()). The only menu items that are actually handled are DELETE and
     * COPY. Anything else is an alternative option, for which default handling should be done.
     *
     * @param item The selected menu item
     * @return True if the menu item was DELETE, and no default processing is need, otherwise false,
     * which triggers the default handling of the item.
     * @throws ClassCastException
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
        int itemId = item.getItemId();

        if (itemId == R.id.context_open) {
            Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
            intent.setClass(NotesList.this, NoteEditor.class);
            //startActivity(intent);
            startActivityForResult(intent, 0);
            return true;
        } else if (itemId == R.id.context_copy) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newUri(
                    getContentResolver(),
                    "Note",
                    noteUri)
            );
            return true;
        } else if (itemId == R.id.context_delete) {
            getContentResolver().delete(noteUri, null, null);
            refreshNotesList(); // Refresh list after deletion
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
//生活工作  学习
    /**
     * This method is called when the user clicks a note in the displayed list.
     *
     * This method handles incoming actions of either PICK (get data from the provider) or
     * GET_CONTENT (get or create data). If the incoming action is EDIT, this method sends a
     * new Intent to start NoteEditor.
     * @param l The ListView that contains the clicked item
     * @param v The View of the individual item
     * @param position The position of v in the displayed list
     * @param id The row ID of the clicked item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            Intent intent = new Intent(NotesList.this, NoteEditor.class);
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(uri);
            //startActivity(intent);
            startActivityForResult(intent, 0);
        }
    }

    // NotesList.java
    private class NotesListAdapter extends CursorAdapter {
        public NotesListAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.noteslist_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView dateView = (TextView) view.findViewById(R.id.date);
            TextView categoryView = (TextView) view.findViewById(R.id.category_text);

            // 获取标题
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_TITLE));
            titleView.setText(title);

            // 获取创建时间戳并格式化为日期时间字符串
            long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CREATE_DATE));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(new Date(createDate));
            dateView.setText(formattedDate);

            // 获取分类信息
            String category = cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY));
            categoryView.setText("分类: " + category);
        }
    }

    // LoaderCallbacks implementation
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;

        // Build selection criteria based on category filter
        if (mSelectedCategoryId != -1) {
            selection = NotePad.Notes.COLUMN_NAME_CATEGORY_ID + "=?";
            selectionArgs = new String[]{String.valueOf(mSelectedCategoryId)};
        }

        // Apply search filter if exists
        if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            String searchSelection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
            String[] searchArgs = { "%" + mSearchQuery + "%", "%" + mSearchQuery + "%" };

            if (selection != null) {
                selection = "(" + selection + ") AND (" + searchSelection + ")";
                // Merge selection args
                String[] newArgs = new String[(selectionArgs != null ? selectionArgs.length : 0) + searchArgs.length];
                int i = 0;
                if (selectionArgs != null) {
                    for (String arg : selectionArgs) {
                        newArgs[i++] = arg;
                    }
                }
                for (String arg : searchArgs) {
                    newArgs[i++] = arg;
                }
                selectionArgs = newArgs;
            } else {
                selection = searchSelection;
                selectionArgs = searchArgs;
            }
        }

        // Return a new CursorLoader with the filtered query
        return new CursorLoader(
                this,
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // 无论保存还是删除操作，都刷新列表
//        refreshNotesList();
//    }
    // 修改 onActivityResult 方法来处理从不同Activity返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // requestCode 0: 从 NoteEditor 返回
        // requestCode 1: 从 CategoryManagerActivity 返回
        if (requestCode == 0 || requestCode == 1) {
            // 无论保存还是删除操作，都刷新列表
            refreshNotesList();

            // 如果是从分类管理返回，还需要重新初始化分类下拉框
            if (requestCode == 1) {
                initCategorySpinner();
            }
        }
    }
    // 在 NotesList.java 中添加以下方法
    private int getCategoryColor(String categoryName) {
        // 根据分类名称返回对应颜色
        int hash = categoryName.hashCode();
        int colorIndex = Math.abs(hash) % 5;

        switch (colorIndex) {
            case 0:
                return getResources().getColor(R.color.category_red);
            case 1:
                return getResources().getColor(R.color.category_blue);
            case 2:
                return getResources().getColor(R.color.category_green);
            case 3:
                return getResources().getColor(R.color.category_yellow);
            case 4:
                return getResources().getColor(R.color.category_purple);
            default:
                return getResources().getColor(R.color.colorPrimary);
        }
    }

    // NotesList.java



}
