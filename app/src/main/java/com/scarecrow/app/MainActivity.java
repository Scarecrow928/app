package com.scarecrow.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.MenuItemCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.text.TextUtils;

public class MainActivity extends AppCompatActivity {
    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";
    private TextView textView;
    private Button mainButton;
    private EditText mainEditText;
    private ListView mainListView;
    private ShareActionProvider mShareActionProvider;
    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    private SharedPreferences mSharedPreferences;
    private JSONAdaper mJSONAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.main_textview);
        mainButton = (Button) findViewById(R.id.main_button);
        mainEditText = (EditText) findViewById(R.id.main_edittext);
        mainListView = (ListView)  findViewById(R.id.main_listview);
        ArrayList mNameList = new ArrayList();

        mJSONAdapter = new JSONAdaper(this, getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mArrayAdapter.add(mainEditText.getText().toString());
//                mArrayAdapter.notifyDataSetChanged();
                Log.d("my app", "press");

                queryBooks(mainEditText.getText().toString());
            }
        });


        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
                String coverID = jsonObject.optString("cover_i");

                String title = "";
                if(jsonObject.has("title")) {
                    title = jsonObject.optString("title");
                }

                String author = "";
                if(jsonObject.has("author_name")) {
                    author = jsonObject.optJSONArray("author_name").optString(0);
                }

                String isbn = "";
                if(jsonObject.has("isbn")) {
                    isbn = jsonObject.optJSONArray("isbn").optString(0);
                }


                String publisher = "";
                if(jsonObject.has("publisher")) {
                    publisher = jsonObject.optJSONArray("publisher").optString(0);
                }

                String language = "";
                if(jsonObject.has("language")) {
                    language = jsonObject.optJSONArray("language").optString(0);
                }

                StringBuilder subject = new StringBuilder();
                if(jsonObject.has("subject")) {
                    JSONArray subjects = jsonObject.optJSONArray("subject");
                    Log.d("subject", subjects.toString());
                    Log.d("subjectsLength", String.valueOf(subjects.length()));

                    subject.append(subjects.optString(0));
                    for(int i = 1; i < subjects.length() && i < 5; i++) {
                        subject.append(", ").append(subjects.optString(i));
                    }
                    Log.d("subject", subject.toString());
                }

                Log.d("Intenttodetail", jsonObject.toString());
                Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
                detailIntent.putExtra("title", title);
                detailIntent.putExtra("author", author);
                detailIntent.putExtra("coverID", coverID);
                detailIntent.putExtra("isbn", isbn);
                detailIntent.putExtra("publisher", publisher);
                detailIntent.putExtra("language", language);
                detailIntent.putExtra("subject", subject.toString());
                startActivity(detailIntent);
            }
        });


        // 6. The text you'd like to share has changed,
        // and you need to update
        setShareIntent();
        displayWelcome();
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

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textView.getText());

            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void queryBooks(String searchString) {
        progressBar.setVisibility(View.VISIBLE);
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(QUERY_URL + urlString, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                mJSONAdapter.updateData(response.optJSONArray("docs"));
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Error" + statusCode + " "  + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayWelcome() {
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = mSharedPreferences.getString(PREF_NAME, "");

        if (name.length() > 0) {
            Toast.makeText(getApplicationContext(), "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
        } else {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello");
            alert.setMessage("What is your name?");

            final EditText input = new EditText(this);
            alert.setView(input);

            // "OK" Button.
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputName = input.getText().toString();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(PREF_NAME, inputName);
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_SHORT).show();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.show();
        }
    }
}
