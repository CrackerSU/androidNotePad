# NotePad
This is an AndroidStudio rebuild of google SDK sample NotePad
<img width="1038" height="312" alt="image" src="https://github.com/user-attachments/assets/452a1688-1552-4efc-b32c-0863d02fca4b" />


一、项目概述:

NotePad记事本应用是一款基于Android平台的轻量级笔记管理工具，在原版开源代码基础上进行了功能扩展和界面优化，为用户提供更加完善的笔记记录和管理体验。
本项目基于开源NotePad应用进行功能扩展，在原有基础笔记功能之上，增加了时间戳显示、笔记查询等基本功能，并实现了切换主题、背景颜色和笔记分类两大扩展功能。

二、实验环境
<img width="948" height="273" alt="image" src="https://github.com/user-attachments/assets/25e2c157-cc1b-450a-89e0-b73816d3e535" />

三、新增功能展示

  1.基本功能扩展
  
  1.1 时间戳显示：在笔记列表界面中，每个笔记条目现在都会显示创建或修改的时间戳
  
<img width="347" height="495" alt="image" src="https://github.com/user-attachments/assets/4a7ff07a-97de-4829-acce-154040935306" />
    
  如图所示，每条笔记标题下方都有时间戳显示，记录了每条笔记创建或上次修改时的具体时间，且笔记列表会根据时间先后来排序。

  1.2 笔记查询功能：支持根据标题或内容关键词进行快速搜索定位
    
<img width="453" height="804" alt="940d147f5b26712ce8ef4da1f96b6e60" src="https://github.com/user-attachments/assets/7495b0e4-ea3c-48a3-8b1f-ea8088c1b8f6" />

  如图所示，笔记列表上方有一个搜索栏，点击“放大镜”即可在输入栏输入要搜索的关键字，输入完毕后将会自动展示含有关键字的笔记，可根据笔记标题或内容搜索。
  
<img width="474" height="834" alt="8c1d46e06bd0196c6ab7d64933e7d5d4" src="https://github.com/user-attachments/assets/0497417a-37da-4e34-962b-7d8707c85fad" />

  如图所示，输入“学习”后下方展示了标题含有该关键词的两条笔记。
  
<img width="447" height="765" alt="205daee4e47d102cb89d3dbd85c0790f" src="https://github.com/user-attachments/assets/7fc73d1c-0a6a-4865-9619-cb128fb467b0" />

  如图所示，输入关键词后下方展示了笔记内容含有该关键词的一条笔记。
  
  2.扩展功能实现
  
  2.1 UI美化：对应用界面进行了优化，包括主题设定、背景更换和编辑器优化
  
<img width="474" height="795" alt="6d8f226560c721c6e45160b7a3721ca6" src="https://github.com/user-attachments/assets/44e17c8d-e07d-4a28-b8af-d1e6b130f574" />
  
  如图所示，点击主页右下方设置按钮，可进入该页面，可切换主页主题和编辑页背景颜色。

<img width="396" height="762" alt="5c09b336ba0ece0a1589dfa1e7652111" src="https://github.com/user-attachments/assets/04824bf7-c039-4376-84f6-280e9203ba7f" />

  浅色主题与前面所展示主页面一致，切换至深色主题之后，背景颜色会变暗，与此同时笔记列表的文字颜色会变成白色以免融入背景色。

<img width="420" height="776" alt="f305dac70885cb26056d201784e3642d" src="https://github.com/user-attachments/assets/319d4612-5cb4-4366-9bcb-6bdc9f35ef67" />

  切换编辑页背景颜色之后，编辑页的颜色会切换至所选择的颜色，图中所示为浅蓝色。

  2.2 笔记分类：实现了笔记的分类管理功能，用户可以创建不同的分类文件夹来组织笔记
  
 <img width="456" height="819" alt="096c28dfee3768ed27a82109dc54a354" src="https://github.com/user-attachments/assets/815d1678-ac2e-4164-9ec9-d49340fbf90f" /><img width="336" height="399" alt="497a0ee4869b67e82e33e8ee917f8d8e" src="https://github.com/user-attachments/assets/8eea3921-2a19-43cb-8f83-519bd1c2acd0" />
 
  如图所示，主页搜索栏下方会有当前所展示笔记的类别，点击小三角会跳出来所有分类供选择，点击某一分类后即展示当前分类下的所有笔记。

<img width="462" height="810" alt="009a570bb2ad460afed7dd7653d36c21" src="https://github.com/user-attachments/assets/2d4e47b5-966b-4ae5-ae80-0aeec5eddba6" /><img width="339" height="464" alt="9062585a4b528d9e17374e8e1d2152d2" src="https://github.com/user-attachments/assets/94d0517a-06dd-4723-93f2-22d268efeabf" /> 

  如图所示，进入笔记编辑页面，点击分类框小三角可选择当前笔记的分类，也可以添加一个新的分类。
  

四、关键代码讲解

  1.基本功能扩展
  
  1.1 时间戳实现机制
  
  1.1.1 数据库列定义
  
  在 NotePad.java 文件中定义了数据库表的列名，包括创建时间和修改时间：
  
    public static final class Notes implements BaseColumns {
    // ...
    public static final String COLUMN_NAME_CREATE_DATE = "created";
    public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    // ...
    }

  1.1.2 在 ContentProvider 中处理时间戳
  
  在 NotePadProvider.java 中，当插入新笔记时会自动添加创建时间和修改时间：

    // 在插入数据时设置创建时间和修改时间
    Long now = Long.valueOf(System.currentTimeMillis());
    // 如果没有提供创建时间，则使用当前时间
    if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
        values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
    }
    // 如果没有提供修改时间，则使用当前时间
    if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
    }
    
  1.1.3 在界面显示时间戳

  在 NotesList.java 中，查询并显示笔记的创建时间：

    // 获取创建时间戳并格式化为日期时间字符串
    long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CREATE_DATE));
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
    String formattedDate = sdf.format(new Date(createDate));
    dateView.setText(formattedDate);

  
  1.2 笔记查询功能实现机制

  1.2.1 搜索入口与菜单项

  在笔记编辑页中定义了搜索栏，允许用户触发搜索功能：

      <!-- 搜索栏 -->
    <androidx.appcompat.widget.SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="8dp"
        android:textColor="?attr/colorOnSurface"/>

  1.2.2  搜索核心逻辑

  搜索功能主要在 NotesList 类中实现：

    private void performSearch(String query) {
    // 构建搜索条件
    String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                       NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
    String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
    
    // 执行查询并更新列表
    Cursor cursor = getContentResolver().query(
        NotePad.Notes.CONTENT_URI,
        PROJECTION,
        selection,
        selectionArgs,
        NotePad.Notes.DEFAULT_SORT_ORDER
    );
    mAdapter.changeCursor(cursor);
    }

  数据库查询投影定义：

    private static final String[] PROJECTION = new String[] {
    NotePad.Notes._ID,
    NotePad.Notes.COLUMN_NAME_TITLE,
    NotePad.Notes.COLUMN_NAME_NOTE,
    NotePad.Notes.COLUMN_NAME_CREATE_DATE,
    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
    NotePad.Notes.COLUMN_NAME_CATEGORY
    };

  1.2.3 搜索流程执行

  1、用户输入搜索关键词
  
  2、系统构建 SQL 查询条件，使用 LIKE 操作符匹配 COLUMN_NAME_TITLE 和 COLUMN_NAME_NOTE 字段
  
  3、通过 ContentResolver.query() 方法执行数据库查询
  
  4、使用查询结果更新 NoteListAdapter 显示搜索结果


  2.扩展功能实现
  
  2.1 主题设定、背景更换实现机制

  2.1.1 主题样式设定

  从 styles.xml 文件中可以看到两个主要的主题样式：

      <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="colorPrimary">#2E7D32</item>
        <item name="colorPrimaryDark">#1B5E20</item>
        <item name="colorAccent">#FF4081</item>
        <item name="android:windowBackground">@drawable/bg_material</item>
      </style>
      
      <style name="AppTheme.Dark" parent="Theme.AppCompat.DayNight">
        <item name="colorPrimary">#2E7D32</item>
        <item name="colorPrimaryDark">#1B5E20</item>
        <item name="colorAccent">#FF4081</item>
        <item name="android:windowBackground">@drawable/bg_dark</item>
      </style>
      
AppTheme 使用了 bg_material 背景
AppTheme.Dark 使用了 bg_dark 背景

  2.1.2 设置界面布局

  在 activity_settings.xml 中定义了主题选择界面：

    <RadioGroup
    android:id="@+id/theme_group"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <RadioButton
        android:id="@+id/light_theme"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="浅色主题" />

    <RadioButton
        android:id="@+id/dark_theme"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="深色主题" />
    </RadioGroup>

  2.1.3 主题切换核心逻辑

  初始化和监听设置：

    // 主题切换监听器
    themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
    String selectedTheme = "";
    if (checkedId == R.id.light_theme) {
        selectedTheme = "light";
    } else if (checkedId == R.id.dark_theme) {
        selectedTheme = "dark";
    }
    savePreferenceAndRestart("theme", selectedTheme);
    });

   默认选项设置：

     // 设置默认选中的 RadioButton（根据已保存的设置）
     setDefaultSelection(themeGroup, prefs.getString("theme", "light"));

     private void setDefaultSelection(RadioGroup group, String value) {
     if (group.getId() == R.id.theme_group) {
         if ("dark".equals(value)) {
             ((RadioButton) findViewById(R.id.dark_theme)).setChecked(true);
         } else {
             ((RadioButton) findViewById(R.id.light_theme)).setChecked(true);
         }
     }
     // ... 其他逻辑
     }

  保存和应用设置：

     private void savePreferenceAndRestart(String key, String value) {
     prefs.edit().putString(key, value).apply();

      Intent intent = new Intent(this, NotesList.class);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
     startActivity(intent);
     finish();
     }

   2.1.4 颜色选择对话框实现

   在 NoteEditor.java 中，通过 showColorPickerDialog() 方法显示颜色选择对话框：

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

  2.1.5 背景颜色设置方法

      private void setNoteBackgroundColor(String color) {
      mNoteEdit.setBackgroundColor(Color.parseColor(color));
      }
      
此方法将解析传入的颜色字符串，并将其设置为 mNoteEdit EditText 控件的背景颜色。

2.2 笔记分类实现机制

2.2.1 数据模型定义

从 NotePad.java 文件中可以看到分类相关的列定义：

    public static final class Notes implements BaseColumns {
    // 其他字段...
    public static final String COLUMN_NAME_CATEGORY = "category";
    
    // 在查询时获取分类信息
    String category = cursor.getString(
        cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_CATEGORY)
     );
    }


2.2.2 分类逻辑

在 Category.java 中定义了分类的基本结构：

    public class Category {
    private int id;
    private String name;
    private String color;

    // 构造函数、getter和setter方法
    public Category(int id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // getter和setter方法...
    }

在 CategoryManagerActivity.java 中实现了分类的增删改查逻辑：

    public class CategoryManagerActivity extends AppCompatActivity {
    private List<Category> categoryList;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        // 初始化分类列表
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(this, categoryList);
        
        // 加载已有分类
        loadCategories();
    }

    private void loadCategories() {
        // 从数据库或SharedPreferences加载分类数据
        // ...
    }

    private void addCategory(String name, String color) {
        // 添加新分类的逻辑
        Category category = new Category(0, name, color);
        categoryList.add(category);
        adapter.notifyDataSetChanged();
        // 保存到持久化存储
    }

    private void deleteCategory(Category category) {
        // 删除指定分类的逻辑
        categoryList.remove(category);
        adapter.notifyDataSetChanged();
        // 从持久化存储中移除
      }
    }

在 NoteEditor.java 中集成了分类选择功能：

    public class NoteEditor extends Activity {
    private Spinner categorySpinner;
    private List<String> categories;

    private void setupCategorySpinner() {
        categorySpinner = findViewById(R.id.category_spinner);
        categories = loadAllCategories(); // 加载所有分类
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private List<String> loadAllCategories() {
        // 从存储中获取所有分类名称
        List<String> categoryList = new ArrayList<>();
        categoryList.add(getString(R.string.all_categories)); // 添加默认选项
        // 添加其他自定义分类
        return categoryList;
     }
    }


2.2.3 关键布局文件

    <!-- Category selector -->
    <LinearLayout
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:orientation="horizontal"
       android:padding="8dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/category_label"
        android:layout_marginEnd="8dp"
        android:layout_marginRight="8dp"
        android:textSize="16sp"/>
    <Spinner
        android:id="@+id/category_spinner"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"/>

    </LinearLayout>


  
五、总结

本次实验中，项目增加了许多新功能，整体变得更加完善和实用。

通过本次实验，我深入掌握了Android应用开发中的数据存储技术、界面优化方法和功能模块设计。特别是在数据库操作和用户交互设计方面积累了宝贵经验。
