package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.data.model.ServerInfo;
import edu.uci.ics.fabflixmobile.databinding.ActivitySingleMovieBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class SinglePageActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);
        String movie_id = getIntent().getStringExtra("movie_id");
        TextView title = findViewById(R.id.single_title);
        TextView director = findViewById(R.id.single_director);
        TextView year = findViewById(R.id.single_year);
        TextView genres = findViewById(R.id.single_genres);
        TextView stars = findViewById(R.id.single_stars);
        TextView ratings = findViewById(R.id.single_rating);

        String baseURL = ServerInfo.getServerUrl();
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;


        @SuppressLint("SetTextI18n") final StringRequest getMoviesInfoRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie" + String.format("?id=%s", movie_id),
                response -> {
                    try {
                        JSONArray responseJson = new JSONArray(response);
                        JSONObject result = responseJson.getJSONObject(0);

                        String movie_title = result.getString("movie_title");
                        String movie_year = result.getString("movie_year");
                        String movie_director = result.getString("movie_director");
                        String movie_genres = result.getString("movie_genres");
                        String movie_stars = result.getString("movie_stars");
                        String movie_ratings = result.getString("movie_rating");


                        title.setText("Title: " + movie_title);
                        year.setText("Year: " + movie_year);
                        director.setText("Director: " + movie_director);
                        genres.setText("Genres: " + movie_genres);
                        stars.setText("Stars: " + movie_stars);
                        ratings.setText("Rating: " + movie_ratings);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    System.out.println("single page error");
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(getMoviesInfoRequest);

    }
}
