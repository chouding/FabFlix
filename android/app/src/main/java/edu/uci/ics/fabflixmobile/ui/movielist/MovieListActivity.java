package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.data.model.ServerInfo;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.browse.SearchActivity;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String movie_title = getIntent().getStringExtra("movie_title");
        String page = getIntent().getStringExtra("page");
        String baseURL = ServerInfo.getServerUrl();
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final Button prev = binding.buttonPrev;
        final Button next = binding.buttonNext;

        final StringRequest getMoviesInfoRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/search" + String.format("?title=%s&year=&director=&star=&genres=&prefix=null&limit=10&sort=null&"+
                        "page=%s&url=null", movie_title, page),
                response -> {
                    try {
                        JSONArray responseJson = new JSONArray(response);
                        final ArrayList<Movie> movies = new ArrayList<>();
                        int num_movies = 0;
                        for (int i = 0; i < responseJson.length()-1; i++){
                            JSONObject item = responseJson.getJSONObject(i);
                            String id = item.get("movie_id").toString();
                            String title = item.get("title").toString();
                            short year = Short.parseShort(item.get("year").toString());
                            String director = item.get("director").toString();
                            String genres = item.get("genres").toString();
                            String stars = item.get("stars").toString();
                            String rating = item.get("rating").toString();
                            num_movies = Integer.parseInt(item.get("num_movies").toString());
                            movies.add(new Movie(id, title, year, director, genres, stars, rating));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            Intent singlePageIntent = new Intent(MovieListActivity.this, SinglePageActivity.class);
                            singlePageIntent.putExtra("movie_id", movie.getMovieId());
                            // activate the list page.
                            startActivity(singlePageIntent);
                        });

                        final int n = num_movies;
                        prev.setOnClickListener(view -> setPrev(movie_title, Integer.parseInt(page)));
                        next.setOnClickListener(view -> setNext(movie_title, Integer.parseInt(page), n));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(getMoviesInfoRequest);
    }

    public void setPrev(String title, int page){
        System.out.println("prev");
        // initialize the activity(page)/destination
        if (page > 1){
            System.out.println("proceed");
            finish();
            Intent prevIntent = new Intent(MovieListActivity.this, MovieListActivity.class);
            prevIntent.putExtra("movie_title", title);
            prevIntent.putExtra("page", Integer.toString(page-1));
            startActivity(prevIntent);
        }
    }

    public void setNext(String title, int page, int num_movies){
        int max_page = (int) Math.ceil(num_movies / (double) 10);
        System.out.println("next"+page+ " " + max_page);

        if (page < max_page){
            System.out.println("proceed");
            finish();
            Intent nextIntent = new Intent(MovieListActivity.this, MovieListActivity.class);
            nextIntent.putExtra("movie_title", title);
            nextIntent.putExtra("page", Integer.toString(page+1));
            startActivity(nextIntent);
        }
    }
}