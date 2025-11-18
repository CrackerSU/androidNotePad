// SettingsActivity.java
package com.example.android.notepad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化 SharedPreferences
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // 获取界面上的控件
        RadioGroup themeGroup = findViewById(R.id.theme_group);
        RadioGroup bgColorGroup = findViewById(R.id.bg_color_group);

        // 设置默认选中的 RadioButton（根据已保存的设置）
        setDefaultSelection(themeGroup, prefs.getString("theme", "light"));
        setDefaultSelection(bgColorGroup, prefs.getString("bg_color", "white"));

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

        // 背景颜色切换监听器
        bgColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedBgColor = "";
            if (checkedId == R.id.bg_white) {
                selectedBgColor = "white";
            } else if (checkedId == R.id.bg_blue) {
                selectedBgColor = "blue";
            } else if (checkedId == R.id.bg_green) {
                selectedBgColor = "green";
            }
            savePreferenceAndRestart("bg_color", selectedBgColor);
        });
    }

    /**
     * 根据保存的值设置默认选中项
     */
    private void setDefaultSelection(RadioGroup group, String value) {
        if (group.getId() == R.id.theme_group) {
            if ("dark".equals(value)) {
                ((RadioButton) findViewById(R.id.dark_theme)).setChecked(true);
            } else {
                ((RadioButton) findViewById(R.id.light_theme)).setChecked(true);
            }
        } else if (group.getId() == R.id.bg_color_group) {
            switch (value) {
                case "blue":
                    ((RadioButton) findViewById(R.id.bg_blue)).setChecked(true);
                    break;
                case "green":
                    ((RadioButton) findViewById(R.id.bg_green)).setChecked(true);
                    break;

                default: // white or default
                    ((RadioButton) findViewById(R.id.bg_white)).setChecked(true);
                    break;
            }
        }
    }


    /**
     * 保存偏好设置并重启应用
     *
     * @param key   偏好键名
     * @param value 偏好值
     */
    private void savePreferenceAndRestart(String key, String value) {
        prefs.edit().putString(key, value).apply();

        Intent intent = new Intent(this, NotesList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




}
