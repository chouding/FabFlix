package edu.uci.ics.fabflixmobile.ui.browse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.ServerInfo;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SearchActivity extends AppCompatActivity {
    private EditText movie_name;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        movie_name = binding.movieName;
        final Button searchSubmit = binding.searchSubmit;

        //assign a listener to call a function to handle the user request when clicking a button
        searchSubmit.setOnClickListener(view -> handleSearch());
    }

    @SuppressLint("SetTextI18n")
    public void handleSearch() {
        Intent browseIntent = new Intent(SearchActivity.this, MovieListActivity.class);
        browseIntent.putExtra("movie_title", movie_name.getText().toString());
        browseIntent.putExtra("page", "1");
        startActivity(browseIntent);
    }


}
