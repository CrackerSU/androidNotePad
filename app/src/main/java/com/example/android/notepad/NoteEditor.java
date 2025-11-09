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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * This Activity handles "editing" a note, where editing is responding to
 * {@link Intent#ACTION_VIEW} (request to view data), edit a note
 * {@link Intent#ACTION_EDIT}, create a note {@link Intent#ACTION_INSERT}, or
 * create a new note from the current contents of the clipboard {@link Intent#ACTION_PASTE}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler}
 * or {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class NoteEditor extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "NoteEditor";

    /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION =
            new String[] {
                    NotePad.Notes._ID,
                    NotePad.Notes.COLUMN_NAME_TITLE,
                    NotePad.Notes.COLUMN_NAME_NOTE,
                    NotePad.Notes.COLUMN_NAME_CATEGORY,
                    NotePad.Notes.COLUMN_NAME_CATEGORY_ID
            };

    // A label for the saved state of the activity
    private static final String ORIGINAL_CONTENT = "origContent";

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mTitleEdit; // 标题输入框
    private EditText mNoteEdit;  // 正文输入框
    private String mOriginalContent;

    // Category variables
    private Spinner mCategorySpinner;
    private long mSelectedCategoryId = 1; // Default category ID
    private String mSelectedCategoryName = "默认"; // Default category name

    /**
     * Defines a custom EditText View that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends androidx.appcompat.widget.AppCompatEditText {
        private Rect mRect;
        private Paint mPaint;

        public LinedEditText(Context context) {
            super(context);
            init();
        }

        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public LinedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "light");

        if ("dark".equals(theme)) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        //SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String bgColor = prefs.getString("bg_color", "white");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        View rootView = findViewById(android.R.id.content);
        if ("blue".equals(bgColor)) {
            rootView.setBackgroundResource(R.color.bg_blue);
        } else if ("green".equals(bgColor)) {
            rootView.setBackgroundResource(R.color.bg_green);
        } else {
            rootView.setBackgroundResource(R.color.bg_white);
        }

//        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action) || Intent.ACTION_PASTE.equals(action)) {
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }

            setResult(RESULT_OK, new Intent().setAction(mUri.toString()));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        mCursor = managedQuery(
                mUri,
                PROJECTION,
                null,
                null,
                null
        );

        if (Intent.ACTION_PASTE.equals(action)) {
            performPaste();
            mState = STATE_EDIT;
        }

        setContentView(R.layout.note_editor);

        mTitleEdit = findViewById(R.id.title_edit_text);
        mNoteEdit = findViewById(R.id.note_edit_text);
        mCategorySpinner = findViewById(R.id.category_spinner);

        initCategorySelector();

// 添加保存和删除按钮的点击事件
        Button btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete);

        btnSave.setOnClickListener(v -> {
            String title = mTitleEdit.getText().toString();
            String text = mNoteEdit.getText().toString();
            updateNote(text, title);
            setResult(RESULT_OK);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            deleteNote();
            setResult(RESULT_OK);
            finish();
        });

        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    private void initCategorySelector() {
        // 查询现有分类
        Cursor categoriesCursor = getContentResolver().query(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                new String[]{Category.COLUMN_ID, Category.COLUMN_NAME},
                null, null, Category.COLUMN_NAME);

        List<String> categoryNames = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();

        if (categoriesCursor != null) {
            while (categoriesCursor.moveToNext()) {
                @SuppressLint("Range") long id = categoriesCursor.getLong(categoriesCursor.getColumnIndex(Category.COLUMN_ID));
                @SuppressLint("Range") String name = categoriesCursor.getString(categoriesCursor.getColumnIndex(Category.COLUMN_NAME));
                categoryNames.add(name);
                categoryIds.add(id);
            }
            categoriesCursor.close();
        }

        // 添加“添加分类”项
        categoryNames.add("添加分类");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == categoryNames.size() - 1) {
                    // 用户选择了“添加分类”
                    showAddCategoryDialog();
                } else {
                    mSelectedCategoryId = categoryIds.get(position);
                    mSelectedCategoryName = categoryNames.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedCategoryId = 1;
                mSelectedCategoryName = "默认";
            }
        });
    }

    private void showAddCategoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_category);

        EditText editText = dialog.findViewById(R.id.category_name_edit_text);
        Button saveButton = dialog.findViewById(R.id.save_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            String categoryName = editText.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                insertNewCategory(categoryName);
                refreshCategoryList(); // 刷新分类列表
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void insertNewCategory(String name) {
        ContentValues values = new ContentValues();
        values.put(Category.COLUMN_NAME, name);
        Uri uri = getContentResolver().insert(Uri.parse("content://" + NotePad.AUTHORITY + "/categories"), values);
        if (uri != null) {
            Log.d(TAG, "New category inserted: " + name);
            refreshCategoryList();//
        }
    }

    private void refreshCategoryList() {
        // 重新初始化分类选择器
        mCategorySpinner.setAdapter(null);
        initCategorySelector();
    }

    @SuppressLint("Range")
    @Override
    protected void onResume() {
        super.onResume();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor = getContentResolver().query(mUri, PROJECTION, null, null, null);
            mCursor.moveToFirst();

            if (mState == STATE_EDIT) {
                int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                String title = mCursor.getString(colTitleIndex);
                mTitleEdit.setText(title);

                int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                String note = mCursor.getString(colNoteIndex);
                mNoteEdit.setText(note);

                setSelectedCategory(mCursor.getLong(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY_ID)));
            } else if (mState == STATE_INSERT) {
                mTitleEdit.setText("");
                mNoteEdit.setText("");
            }

            if (mOriginalContent == null) {
                mOriginalContent = mNoteEdit.getText().toString();
            }
        } else {
            setTitle(getText(R.string.error_title));
            mNoteEdit.setText(getText(R.string.error_message));
        }
    }

    private void setSelectedCategory(long categoryId) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) mCategorySpinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (i < adapter.getCount() - 1) { // 排除“添加分类”项
                    // 这里需要额外存储 ID 和 Name 映射，建议用 Map 或自定义类
                    // 简化处理：假设第一个是默认，其余为其他
                    if (i == 0) {
                        if (categoryId == 1) {
                            mCategorySpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCursor != null && !mCursor.isClosed()) {
            String title = mTitleEdit.getText().toString();
            String text = mNoteEdit.getText().toString();

            if (isFinishing() && text.isEmpty()) {
                setResult(RESULT_CANCELED);
                deleteNote();
            } else if (mState == STATE_EDIT) {
                updateNote(text, title);
            } else if (mState == STATE_INSERT) {
                updateNote(text, title);
                mState = STATE_EDIT;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);

        if (mState == STATE_EDIT) {
            Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, NoteEditor.class), null, intent, 0, null);
        }
        getMenuInflater().inflate(R.menu.editor_options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
        String savedNote = mCursor.getString(colNoteIndex);
        String currentNote = mNoteEdit.getText().toString();
        if (savedNote.equals(currentNote)) {
            menu.findItem(R.id.menu_revert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_revert).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_save) {
            String title = mTitleEdit.getText().toString();
            String text = mNoteEdit.getText().toString();
            updateNote(text, title);
            setResult(RESULT_OK);//
            finish();
            return true;
        } else if (itemId == R.id.menu_delete) {
            deleteNote();
            setResult(RESULT_OK);//
            finish();
            return true;
        } else if (itemId == R.id.menu_revert) {
            cancelNote();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("Range")
    private final void performPaste() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ContentResolver cr = getContentResolver();
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {
            String text = null;
            String title = null;
            ClipData.Item item = clip.getItemAt(0);
            Uri uri = item.getUri();

            if (uri != null && NotePad.Notes.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {
                Cursor orig = cr.query(uri, PROJECTION, null, null, null);
                if (orig != null && orig.moveToFirst()) {
                    text = orig.getString(orig.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE));
                    title = orig.getString(orig.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE));
                    orig.close();
                }
            }

            if (text == null) {
                text = item.coerceToText(this).toString();
            }

            updateNote(text, title);
        }
    }

    private final void updateNote(String text, String title) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        if (mState == STATE_INSERT && title == null) {
            title = text.substring(0, Math.min(30, text.length()));
            int lastSpace = title.lastIndexOf(' ');
            if (lastSpace > 0) {
                title = title.substring(0, lastSpace);
            }
        }

        if (title != null) {
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        }

        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY_ID, mSelectedCategoryId);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, mSelectedCategoryName);

        getContentResolver().update(mUri, values, null, null);
    }

    private final void cancelNote() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mState == STATE_EDIT) {
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                deleteNote();
            }
            mCursor.close();
            mCursor = null;
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mNoteEdit.setText("");
        }
    }

    private void showColorPickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_color_picker);

        dialog.findViewById(R.id.color_white).setOnClickListener(v -> setNoteBackgroundColor("#FFFFFF"));
        dialog.findViewById(R.id.color_yellow).setOnClickListener(v -> setNoteBackgroundColor("#FFFF00"));
        dialog.findViewById(R.id.color_pink).setOnClickListener(v -> setNoteBackgroundColor("#FFC0CB"));
        dialog.findViewById(R.id.color_blue).setOnClickListener(v -> setNoteBackgroundColor("#87CEEB"));
        dialog.findViewById(R.id.color_green).setOnClickListener(v -> setNoteBackgroundColor("#90EE90"));

        dialog.show();
    }

    private void setNoteBackgroundColor(String color) {
        mNoteEdit.setBackgroundColor(Color.parseColor(color));
    }
}
