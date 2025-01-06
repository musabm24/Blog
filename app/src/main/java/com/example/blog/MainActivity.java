package com.example.blog;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Button selectImageButton, saveBlogButton, searchButton, deleteSelectedButton;
    private EditText blogName, blogBody, searchEditText;
    private LinearLayout blogsContainer;

    private final List<Integer> selectedBlogIds = new ArrayList<>(); // Holds IDs of selected blogs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database
        db = openOrCreateDatabase("blogsdb", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS blogtable (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, body TEXT, image_uri TEXT)");

        // Bind UI elements
        blogName = findViewById(R.id.blog_name);
        blogBody = findViewById(R.id.blog_body);
        selectImageButton = findViewById(R.id.select_image_button);
        saveBlogButton = findViewById(R.id.save_blog_button);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchBtn);
        deleteSelectedButton = findViewById(R.id.delete_selected_button);
        blogsContainer = findViewById(R.id.blogsContainer);

        saveBlogButton.setOnClickListener(v -> addBlog());
        searchButton.setOnClickListener(v -> displayBlogs(searchEditText.getText().toString().trim()));
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedBlogs());

        // Load blogs on activity start
        displayBlogs("");
    }

    private void addBlog() {
        String name = blogName.getText().toString().trim();
        String body = blogBody.getText().toString().trim();

        if (name.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Please enter both name and body", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("body", body);

        try {
            long result = db.insert("blogtable", null, values);

            if (result != -1) {
                Toast.makeText(this, "Blog added successfully", Toast.LENGTH_SHORT).show();
                blogName.getText().clear();
                blogBody.getText().clear();
                displayBlogs(""); // Refresh the list of blogs
            } else {
                Toast.makeText(this, "Error adding blog", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to insert blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBlogs(String searchQuery) {
        blogsContainer.removeAllViews();
        selectedBlogIds.clear(); // Reset selected blogs

        String sql = "SELECT * FROM blogtable";
        if (!searchQuery.isEmpty()) {
            sql += " WHERE name LIKE ? OR body LIKE ?";
        }

        Cursor cursor = db.rawQuery(sql, searchQuery.isEmpty() ? null : new String[]{"%" + searchQuery + "%", "%" + searchQuery + "%"});
        int idColumnIndex = cursor.getColumnIndex("id");
        int nameColumnIndex = cursor.getColumnIndex("name");
        int bodyColumnIndex = cursor.getColumnIndex("body");

        while (cursor.moveToNext()) {
            if (idColumnIndex != -1 && nameColumnIndex != -1 && bodyColumnIndex != -1) {
                final int id = cursor.getInt(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String body = cursor.getString(bodyColumnIndex);

                LinearLayout blogEntryLayout = new LinearLayout(this);
                blogEntryLayout.setOrientation(LinearLayout.HORIZONTAL);
                blogEntryLayout.setPadding(10, 10, 10, 10);

                CheckBox blogCheckBox = new CheckBox(this);
                blogCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedBlogIds.add(id);
                    } else {
                        selectedBlogIds.remove(Integer.valueOf(id));
                    }
                });

                TextView blogTextView = new TextView(this);
                blogTextView.setText("Blog Name: " + name);
                blogTextView.setMaxLines(1); // Ensure only one line is displayed
                blogTextView.setEllipsize(android.text.TextUtils.TruncateAt.END); // Add "..." for long text
                blogTextView.setPadding(10, 10, 10, 10);

                // Set weight for the text view to use available space
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0, // Width is determined by weight
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f // Weight
                );
                blogTextView.setLayoutParams(textParams);

                Button showButton = new Button(this);
                showButton.setText("Show");
                showButton.setOnClickListener(view -> showBlogDetails(id, name, body));

                Button deleteButton = new Button(this);
                deleteButton.setText("Delete");
                deleteButton.setOnClickListener(view -> deleteBlog(id));

                // Add views to the layout
                blogEntryLayout.addView(blogCheckBox);
                blogEntryLayout.addView(blogTextView);
                blogEntryLayout.addView(showButton);
                blogEntryLayout.addView(deleteButton);

                blogsContainer.addView(blogEntryLayout);
            } else {
                Log.e("AddBlogActivity", "Invalid column indices");
            }
        }

        cursor.close();
    }


    private void deleteBlog(int id) {
        int rowsDeleted = db.delete("blogtable", "id = ?", new String[]{String.valueOf(id)});
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Blog deleted", Toast.LENGTH_SHORT).show();
            displayBlogs(""); // Refresh the list of blogs
        } else {
            Toast.makeText(this, "Error deleting blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSelectedBlogs() {
        if (selectedBlogIds.isEmpty()) {
            Toast.makeText(this, "No blogs selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int id : selectedBlogIds) {
            db.delete("blogtable", "id = ?", new String[]{String.valueOf(id)});
        }
        Toast.makeText(this, "Selected blogs deleted", Toast.LENGTH_SHORT).show();
        displayBlogs(""); // Refresh the list of blogs
    }

    private void showBlogDetails(int id, String name, String body) {
        Intent intent = new Intent(MainActivity.this, BlogActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("body", body);
        startActivity(intent);
    }
}
