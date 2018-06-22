package com.scarecrow.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String language;
    private String subject;
    private TextView titleText;
    private TextView authorText;
    private TextView publisherText;
    private TextView isbnText;
    private TextView languageText;
    private  TextView subjectText;

    String mImageURL;
    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_detail2);
        titleText = findViewById(R.id.detail_title);
        authorText = findViewById(R.id.detail_author);
        publisherText = findViewById(R.id.detail_publisher);
        isbnText = findViewById(R.id.detail_isbn);
        languageText = findViewById(R.id.detail_language);
        subjectText = findViewById(R.id.detail_subject);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
        }
        ImageView imageView = (ImageView) findViewById(R.id.img_cover);

        String coverID = this.getIntent().getExtras().getString("coverID");
        if(!TextUtils.isEmpty(coverID)) {
            mImageURL = IMAGE_URL_BASE + coverID + "-L.jpg";
            Picasso.with(this).load(mImageURL).placeholder(R.drawable.img_books_loading).into(imageView);
        }

        title = "Title: " + getIntent().getExtras().getString("title");
        author = "Author: " + getIntent().getExtras().getString("author");
        subject = "";
        publisher = "Publisher: " + getIntent().getExtras().getString("publisher");
        isbn = "ISBN: " + getIntent().getExtras().getString("isbn");
        language = "Language: " + getIntent().getExtras().getString("language");
        subject = "Subject: " + getIntent().getExtras().getString("subject");

        titleText.setText(title);
        authorText.setText(author);
        publisherText.setText(publisher);
        isbnText.setText(isbn);
        languageText.setText(language);
        subjectText.setText(subject);

        super.onCreate(savedInstanceState);
    }

    private void setShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String shareString = title + ", " + author + ", " + subject + ", " + publisher + ", " + isbn;
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Book Recommendation!" + shareString);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mImageURL);
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_item_share, menu);
        } catch (ClassCastException e) {
            Log.e("my app", e.getMessage());
            return false;
        }
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        // Create an Intent to share your content
        setShareIntent();
        return true;
    }
}
