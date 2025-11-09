// CategoryManagerActivity.java
package com.example.android.notepad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class CategoryManagerActivity extends Activity {
    private ListView categoryListView;
    private CategoryAdapter adapter;
    private Cursor cursor;
    private static final int ALL_CATEGORIES_ID = -1;
    private static final int DEFAULT_CATEGORY_ID = 1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        categoryListView = (ListView)findViewById(R.id.category_list);
        loadCategories();

        // 添加分类按钮
        findViewById(R.id.add_category_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddCategoryDialog();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        if (cursor != null) {
            cursor.close();
        }

        cursor = getContentResolver().query(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                new String[]{Category.COLUMN_ID, Category.COLUMN_NAME, Category.COLUMN_COLOR},
                null, null, Category.COLUMN_NAME);

        if (adapter == null) {
            adapter = new CategoryAdapter(this, cursor);
            categoryListView.setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加新分类");

        // 创建自定义布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_category_edit, null);
        builder.setView(dialogView);

        EditText categoryNameInput = (EditText) dialogView.findViewById(R.id.category_name);
        Spinner colorSpinner = (Spinner) dialogView.findViewById(R.id.category_color);

        // 设置颜色选项
        setupColorSpinner(colorSpinner);

        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = categoryNameInput.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    addNewCategory(categoryName, getColorFromSpinner(colorSpinner));
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showEditCategoryDialog(long categoryId, String categoryName, String categoryColor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编辑分类");

        // 创建自定义布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_category_edit, null);
        builder.setView(dialogView);

        EditText categoryNameInput = (EditText) dialogView.findViewById(R.id.category_name);
        Spinner colorSpinner = (Spinner) dialogView.findViewById(R.id.category_color);

        // 设置初始值
        categoryNameInput.setText(categoryName);
        setupColorSpinner(colorSpinner);
        setSelectedColor(colorSpinner, categoryColor);

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = categoryNameInput.getText().toString().trim();
                if (!newName.isEmpty() && !newName.equals(categoryName)) {
                    updateCategory(categoryId, newName, getColorFromSpinner(colorSpinner));
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteCategoryDialog(long categoryId, String categoryName) {
        // 不能删除默认分类
        if (categoryId == 1) {
            Toast.makeText(this, "不能删除默认分类", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除分类");
        builder.setMessage("确定要删除分类 \"" + categoryName + "\" 吗？该分类下的所有笔记将被移动到默认分类。");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCategory(categoryId);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addNewCategory(String categoryName, String color) {
        // 检查分类是否已存在
        Cursor existing = getContentResolver().query(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                new String[]{Category.COLUMN_ID},
                Category.COLUMN_NAME + "=?",
                new String[]{categoryName},
                null);

        if (existing != null && existing.getCount() > 0) {
            Toast.makeText(this, "分类 \"" + categoryName + "\" 已存在", Toast.LENGTH_SHORT).show();
            existing.close();
            return;
        }

        if (existing != null) {
            existing.close();
        }

        ContentValues values = new ContentValues();
        values.put(Category.COLUMN_NAME, categoryName);
        values.put(Category.COLUMN_COLOR, color);

        Uri result = getContentResolver().insert(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                values);

        if (result != null) {
            loadCategories();
            Toast.makeText(this, "分类添加成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "分类添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCategory(long categoryId, String categoryName, String color) {
        // 检查分类是否已存在
        Cursor existing = getContentResolver().query(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories"),
                new String[]{Category.COLUMN_ID},
                Category.COLUMN_NAME + "=? AND " + Category.COLUMN_ID + "!=?",
                new String[]{categoryName, String.valueOf(categoryId)},
                null);

        if (existing != null && existing.getCount() > 0) {
            Toast.makeText(this, "分类 \"" + categoryName + "\" 已存在", Toast.LENGTH_SHORT).show();
            existing.close();
            return;
        }

        if (existing != null) {
            existing.close();
        }

        ContentValues values = new ContentValues();
        values.put(Category.COLUMN_NAME, categoryName);
        values.put(Category.COLUMN_COLOR, color);

        int result = getContentResolver().update(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories/" + categoryId),
                values,
                null, null);

        if (result > 0) {
            loadCategories();
            Toast.makeText(this, "分类更新成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "分类更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCategory(long categoryId) {
        // 将该分类下的笔记移动到默认分类
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY_ID, 1);
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, "默认");

        getContentResolver().update(
                NotePad.Notes.CONTENT_URI,
                values,
                NotePad.Notes.COLUMN_NAME_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)});

        // 删除分类
        int result = getContentResolver().delete(
                Uri.parse("content://" + NotePad.AUTHORITY + "/categories/" + categoryId),
                null, null);

        if (result > 0) {
            loadCategories();
            Toast.makeText(this, "分类删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "分类删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupColorSpinner(Spinner spinner) {
        String[] colors = {"#CCCCCC", "#FFCCCC", "#CCFFCC", "#CCCCFF", "#FFFFCC", "#FFCCFF"};
        String[] colorNames = {"灰色", "粉色", "绿色", "蓝色", "黄色", "紫色"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private String getColorFromSpinner(Spinner spinner) {
        String[] colors = {"#CCCCCC", "#FFCCCC", "#CCFFCC", "#CCCCFF", "#FFFFCC", "#FFCCFF"};
        int selectedPosition = spinner.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < colors.length) {
            return colors[selectedPosition];
        }
        return "#CCCCCC"; // 默认颜色
    }

    private void setSelectedColor(Spinner spinner, String color) {
        String[] colors = {"#CCCCCC", "#FFCCCC", "#CCFFCC", "#CCCCFF", "#FFFFCC", "#FFCCFF"};
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(color)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private class CategoryAdapter extends CursorAdapter {
        public CategoryAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameView = (TextView) view.findViewById(R.id.category_name);
            View colorView = view.findViewById(R.id.category_color_view);
            ImageButton editButton = (ImageButton) view.findViewById(R.id.edit_category);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_category);

            // 提升 id 作用域
            long id = -1;
            int columnIndex = cursor.getColumnIndex(Category.COLUMN_ID);
            if (columnIndex != -1) {
                id = cursor.getLong(columnIndex);
            } else {
                Log.e("CategoryManagerActivity", "Column not found: " + Category.COLUMN_ID);
            }

            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Category.COLUMN_NAME));
            @SuppressLint("Range") String color = cursor.getString(cursor.getColumnIndex(Category.COLUMN_COLOR));

            nameView.setText(name);
            colorView.setBackgroundColor(android.graphics.Color.parseColor(color));

            // 使用最终赋值的 id
            long finalId = id;
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showEditCategoryDialog(finalId, name, color);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showDeleteCategoryDialog(finalId, name);
                }
            });
            // 控制删除按钮的显示：默认分类(通常ID为1)不显示删除按钮
            if (finalId == 1) {
                deleteButton.setVisibility(View.GONE);
            } else {
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

    }


}
