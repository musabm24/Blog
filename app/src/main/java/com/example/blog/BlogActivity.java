package com.example.blog;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BlogActivity extends AppCompatActivity {

    private EditText blogNameEditText, blogBodyEditText;
    private Button saveButton, shareButton, backButton;
    private SQLiteDatabase db;

    private int blogId; // Holds the blog ID for updates

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        // Initialize database
        db = openOrCreateDatabase("blogsdb", MODE_PRIVATE, null);

        // Bind UI elements
        blogNameEditText = findViewById(R.id.editTextText);
        blogBodyEditText = findViewById(R.id.editTextText2);
        saveButton = findViewById(R.id.Save_Button);
        shareButton = findViewById(R.id.Share_Button);
        backButton = findViewById(R.id.Back_Button);

        // Retrieve blog details passed via Intent
        Intent intent = getIntent();
        blogId = intent.getIntExtra("id", -1);
        String blogName = intent.getStringExtra("name");
        String blogBody = intent.getStringExtra("body");

        // Populate EditTexts with blog details
        blogNameEditText.setText(blogName);
        blogBodyEditText.setText(blogBody);

        // Save button to update the blog
        saveButton.setOnClickListener(v -> saveBlogChanges());

        // Share button to share the blog content
        shareButton.setOnClickListener(v -> shareBlog(blogNameEditText.getText().toString(), blogBodyEditText.getText().toString()));

        // Back button to return to AddBlogActivity
        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(BlogActivity.this, AddBlogActivity.class);
            startActivity(backIntent);
            finish();
        });
    }

    private void saveBlogChanges() {
        String updatedName = blogNameEditText.getText().toString().trim();
        String updatedBody = blogBodyEditText.getText().toString().trim();

        if (updatedName.isEmpty() || updatedBody.isEmpty()) {
            Toast.makeText(this, "Please enter both name and body", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name", updatedName);
        values.put("body", updatedBody);

        int rowsUpdated = db.update("blogtable", values, "id = ?", new String[]{String.valueOf(blogId)});
        if (rowsUpdated > 0) {
            Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error updating blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareBlog(String name, String body) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareContent = "Blog Name: " + name + "\n\n" + "Content:\n" + body;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        startActivity(Intent.createChooser(shareIntent, "Share Blog via"));
    }
}
