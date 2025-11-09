package com.example.android.notepad;

// 创建新的 Category.java 文件
public class Category {
    public static final String TABLE_NAME = "categories";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COLOR = "color"; // 可以为分类设置颜色

    public static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME + " TEXT UNIQUE,"
                    + COLUMN_COLOR + " TEXT"
                    + ");";
}
