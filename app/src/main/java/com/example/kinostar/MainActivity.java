package com.example.kinostar;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaParser;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    private Profile profile;
    private boolean firstTime;
    private int page_number = 0;
    private int page_back;
    private boolean ad, dark_theme;
    private List<Film> films = new ArrayList<>();
    private List<Film> search_result = new ArrayList<>();
    private List<Film> my_films = new ArrayList<>();
    private List<Film> favorite_films = new ArrayList<>();
    private List<Film> advice_films = new ArrayList<>();
    private List<Film> filmsDb_list = new ArrayList<>();
    private SharedPreferences app_data;
    private final static  String  popular_url = "https://www.kinopoisk.ru/popular/films/";
    private Elements content;
    private final DBHelper dbHelper = new DBHelper(this);
    private final ContentValues contentValues = new ContentValues();
    private SQLiteDatabase filmsDB;
    private Cursor cursor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
        getActionBar().hide();
        app_data = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
        loadFilmDB();
        loadAppData();
        if(films.size()==0) {
            new loadPopularFilms().execute();
        }else {
            setContentView(R.layout.activity_main);
            helloMember(firstTime);
        }
    }

    private void loadAdvice() {
        filmsDB = dbHelper.getWritableDatabase();
        cursor = filmsDB.query(DBHelper.TABLE_ADVICE,null,null,null,null,null,null);

        if(cursor.moveToFirst()) {
            do {
                Film film = new Film(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME)), cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION)),
                        cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_RATING)), false, false, cursor.getString(cursor.getColumnIndex(DBHelper.KEY_GENRE)),
                        cursor.getString(cursor.getColumnIndex(DBHelper.KEY_YEAR)), cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DIRECTOR)),
                        cursor.getString(cursor.getColumnIndex(DBHelper.KEY_COUNTRY)), cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_LENGTH)));
                film.setPopular(false);
                film.setIcon_url(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_IMG_URL)));
                filmsDb_list.add(film);
                advice_films.add(film);
            } while (cursor.moveToNext());
        }
    }

    private void loadFilmDB() {
        filmsDB = dbHelper.getWritableDatabase();
        cursor = filmsDB.query(DBHelper.TABLE_MY_FILMS,null,null,null,null,null,null);
        if(filmsDb_list.size()>0){
            filmsDb_list.clear();
            my_films.clear();
            films.clear();
            favorite_films.clear();
        }
        if(cursor.moveToFirst()){
            do {
                boolean checked = false;
                boolean favorite = false;
                boolean popular = false;
                if(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_FAVORITE))==1){
                    favorite = true;
                }
                if(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_CHECKED))==1){
                    checked = true;
                }
                if(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_POPULAR))==1){
                    popular = true;
                }
                Film film = new Film(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME)),cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION)),
                        cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_RATING)),favorite,checked,cursor.getString(cursor.getColumnIndex(DBHelper.KEY_GENRE)),
                        cursor.getString(cursor.getColumnIndex(DBHelper.KEY_YEAR)),cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DIRECTOR)),
                        cursor.getString(cursor.getColumnIndex(DBHelper.KEY_COUNTRY)), cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_LENGTH)));
                film.setPopular(popular);
                film.setIcon_url(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_IMG_URL)));
                filmsDb_list.add(film);

                if(film.isChecked()){
                    my_films.add(film);
                }

                if(film.isFavorite()){
                    favorite_films.add(film);
                }

                if(film.isPopular()) {
                    films.add(film);
                }

            }while (cursor.moveToNext());
        } Log.d("size@@@@@@@@",filmsDb_list.size()+" "+films.size()+" "+my_films.size()+" "+favorite_films.size()+advice_films.size());
    }

    private void makeContent(Film film){
        int favorite = 0;
        int checked = 0;
        int popular = 0;
        if(film.isFavorite()){
            favorite = 1;
        }
        if(film.isChecked()){
            checked = 1;
        }
        if(film.isPopular()){
            popular = 1;
        }
            contentValues.put(DBHelper.KEY_NAME, film.getTitle());
            contentValues.put(DBHelper.KEY_DESCRIPTION, film.getShort_description());
            contentValues.put(DBHelper.KEY_RATING, film.getRating());
            contentValues.put(DBHelper.KEY_FAVORITE, favorite);
            contentValues.put(DBHelper.KEY_CHECKED, checked);
            contentValues.put(DBHelper.KEY_POPULAR, popular);
            contentValues.put(DBHelper.KEY_GENRE, film.getGenre());
            contentValues.put(DBHelper.KEY_YEAR, film.getYear());
            contentValues.put(DBHelper.KEY_DIRECTOR, film.getDirector());
            contentValues.put(DBHelper.KEY_COUNTRY, film.getCountry());
            contentValues.put(DBHelper.KEY_LENGTH, film.getLength());
            contentValues.put(DBHelper.KEY_IMG_URL, film.getIcon_url());

    }


    private void saveAppData(Profile profile){
        Log.d("@@@@@",profile.getScore()+"");
        app_data.edit().putString("name",profile.getFullName()).apply();
        app_data.edit().putString("age",profile.getAge()).apply();
        app_data.edit().putString("genre",profile.getGenre()).apply();
        app_data.edit().putInt("score",profile.getScore()).apply();
        app_data.edit().putInt("rank",profile.getRank()).apply();
        app_data.edit().putBoolean("commercial",ad).apply();
        app_data.edit().putBoolean("theme",dark_theme).apply();
        app_data.edit().putBoolean("firstTime",firstTime).apply();
    }

    private void loadAppData(){
        firstTime = app_data.getBoolean("firstTime",true);
        ad = app_data.getBoolean("commercial",false);
        dark_theme = app_data.getBoolean("theme",false);
        profile = new Profile(app_data.getString("name",null),
                app_data.getString("age",null),app_data.getString("genre",null),
                app_data.getInt("rank",0),app_data.getInt("score",0));
    }


    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void helloMember(boolean firstTime){
        if(firstTime){
            setContentView(R.layout.hello_member);
            Button start = findViewById(R.id.start);
            start.setOnClickListener(v -> {
              setContentView(R.layout.create_profile);
              TextView title = findViewById(R.id.create_profile);
              title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
              EditText name = findViewById(R.id.name);
              name.setFilters(new InputFilter[] {
                      (source, start1, end, dest, dstart, dend) -> {
                          if(source.equals("")){
                              return source;
                          }
                          if(source.toString().matches("[a-zA-Zа-яА-Я]+")){
                              return source;
                          }
                          return "";
                      }
              });
              name.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
              EditText age = findViewById(R.id.age);
              age.setFilters(new InputFilter[]{new InputFilterMinMax("1","100")});
              age.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
              Spinner genre = findViewById(R.id.genre_selector);
              genre.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
              Button save = findViewById(R.id.save);
              save.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
              save.setOnClickListener(v1 -> {
                  createProfile(name.getText().toString(),age.getText().toString(), (String) genre.getSelectedItem());
                  loadPage(0);
              });
            });
        }else loadPage(0);
    }

    @SuppressLint("ResourceType")
    private void createProfile(String name, String age, String genre) {
        profile = new Profile(name, age, genre,0,0);
        firstTime = false;
        saveAppData(profile);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadPage(int page_number) {
        setContentView(R.layout.activity_main);
        LinearLayout linear = (LinearLayout) findViewById(R.id.page);
        View view;
        loadBottomBar();
        switch (page_number){
            case 0:
                loadTopBar(page_number);
                if(films.size()==0){
                    view = getLayoutInflater().inflate(R.layout.reload,null);
                    Button reload = view.findViewById(R.id.reload);
                    reload.setOnClickListener(v -> {
                        loadPage(0);
                    });
                }else {
                    view = getLayoutInflater().inflate(R.layout.list, null);
                    ListView listView = view.findViewById(R.id.list_popular);
                    listView.setAdapter(new FilmsAdapter(this, R.layout.popular_list, films));
                    listView.setOnItemClickListener((parent, view1, position, id) -> {
                        page_back = page_number;
                        setContentView(R.layout.film_page);
                        loadFilmPage(films.get(position));
                    });
                }
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                break;
            case 1:
                loadTopBar(page_number);
                view = getLayoutInflater().inflate(R.layout.my_list, null);
                ListView myListView = view.findViewById(R.id.my_list_view);
                myListView.setAdapter(new FilmsAdapter(this,R.layout.popular_list,my_films));
                if(my_films.size()>0){
                    view.findViewById(R.id.nothing).setVisibility(View.GONE);
                }else view.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                myListView.setOnItemClickListener((parent, view12, position, id) -> {
                    page_back = page_number;
                    setContentView(R.layout.film_page);
                    Button favorite = findViewById(R.id.favorite);
                    if(my_films.get(position).isFavorite()){
                        favorite.setBackground(getDrawable(R.drawable.love));
                    }else favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                    loadFilmPage(my_films.get(position));
                });
                break;
            case 2:
                loadTopBar(page_number);
                view = getLayoutInflater().inflate(R.layout.profile_page, null);
                ImageView icon = view.findViewById(R.id.profile_icon);
                icon.setImageDrawable(getIcon(profile.getRank()));
                ProgressBar progress = view.findViewById(R.id.progress);
                if(profile.getScore()<100){
                    progress.setProgress(profile.getScore());
                }else if(profile.getScore()<200 & profile.getScore()>100){
                    progress.setProgress(profile.getScore()-100);
                }else if(profile.getScore()<300 & profile.getScore()>200){
                    progress.setProgress(profile.getScore()-200);
                } else if(profile.getScore()<400 & profile.getScore()>500){
                    progress.setProgress(profile.getScore()-300);
                }else if(profile.getScore()<500 & profile.getScore()>600){
                    progress.setProgress(profile.getScore()-400);
                }else if(profile.getScore()<600){
                    progress.setProgress(profile.getScore()-500);
                }
                TextView name = view.findViewById(R.id.name);
                name.setText(profile.getFullName());
                TextView age = view.findViewById(R.id.age);
                age.setText(profile.getAge()+" "+getYearAddition(Integer.parseInt(profile.getAge())));
                TextView genre = view.findViewById(R.id.genre);
                genre.setText("Любимый жанр: "+profile.getGenre());
                TextView film_count = view.findViewById(R.id.film_count);
                film_count.setText(my_films.size()+" "+getWordAddition(my_films.size(),"фильм"));
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                Button favorite_list = findViewById(R.id.favorite_list);
                Button info = findViewById(R.id.info);
                Button advice = findViewById(R.id.advice_list);
                advice.setOnClickListener(v -> {
                    this.page_number = 5;
                    loadPage(5);
                });
                favorite_list.setOnClickListener(v -> {
                    this.page_number = 4;
                    loadPage(this.page_number);
                });
                info.setOnClickListener(v -> {
                    loadInfoPage();
                });

                break;
            case 3:
                loadTopBar(page_number);
                break;
            case 4:
                loadTopBar(page_number);
                view = getLayoutInflater().inflate(R.layout.my_list, null);
                ListView myFavoriteView = view.findViewById(R.id.my_list_view);
                myFavoriteView.setAdapter(new FilmsAdapter(this,R.layout.popular_list,favorite_films));
                if(favorite_films.size()>0){
                    view.findViewById(R.id.nothing).setVisibility(View.GONE);
                }else view.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                myFavoriteView.setOnItemClickListener((parent, view1, position, id) -> {
                    page_back = page_number;
                    setContentView(R.layout.film_page);
                    Button favorite = findViewById(R.id.favorite);
                    if(favorite_films.get(position).isFavorite()){
                        favorite.setBackground(getDrawable(R.drawable.love));
                    }else favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                    loadFilmPage(favorite_films.get(position));
                });
                break;
            case 5:
                loadTopBar(page_number);
                view = getLayoutInflater().inflate(R.layout.my_list, null);
                ListView myAdviceView = view.findViewById(R.id.my_list_view);
                myAdviceView.setAdapter(new FilmsAdapter(this,R.layout.popular_list,advice_films));
                if(advice_films.size()>0){
                    view.findViewById(R.id.nothing).setVisibility(View.GONE);
                }else view.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                myAdviceView.setOnItemClickListener((parent, view1, position, id) -> {
                    page_back = page_number;
                    setContentView(R.layout.film_page);
                    Button favorite = findViewById(R.id.favorite);
                    if(favorite_films.get(position).isFavorite()){
                        favorite.setBackground(getDrawable(R.drawable.love));
                    }else favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                    loadFilmPage(favorite_films.get(position));
                });
                break;
            case 6:
                loadTopBar(page_number);
                if(search_result.size()==0){
                    view = getLayoutInflater().inflate(R.layout.reload,null);
                    Button reload = view.findViewById(R.id.reload);
                    reload.setOnClickListener(v -> {
                        loadPage(0);
                    });
                }else {
                    view = getLayoutInflater().inflate(R.layout.list, null);
                    ListView listView = view.findViewById(R.id.list_popular);
                    listView.setAdapter(new FilmsAdapter(this, R.layout.popular_list, search_result));
                    listView.setOnItemClickListener((parent, view1, position, id) -> {
                        page_back = page_number;
                        setContentView(R.layout.film_page);
                        loadFilmPage(search_result.get(position));
                    });
                }
                linear.addView(view);
                linear.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                break;
        }

    }

    private class loadPopularFilms extends AsyncTask<String, Void, String>{

        CustomLoadBar pd;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("ResourceType")
        @Override
        protected void onPreExecute() {
            pd = new CustomLoadBar(MainActivity.this);
            pd.getWindow().setBackgroundDrawable(getDrawable(R.drawable.transp));
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            loadFilms();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            setContentView(R.layout.activity_main);
            helloMember(firstTime);
            super.onPostExecute(s);
        }
    }

    private class loadAdviceFilms extends AsyncTask<String, Void, String>{

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("ResourceType")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            findAdvice();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Ошибка", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadFilmPage(Film film) {
        loadBottomBar();
        ImageView imageView = findViewById(R.id.preview);
        new DownloadImageTask(imageView).execute(film.getIcon_url());
        TextView title = findViewById(R.id.film_title);
        title.setText(film.getTitle()+"("+film.getYear()+")");
        title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
        TextView description = findViewById(R.id.film_description);
        description.setText(film.getShort_description());
        description.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
        Button knowlage = findViewById(R.id.knowla1ge);
        Button favorite = findViewById(R.id.favorite);
        Button add = findViewById(R.id.add_film);
        if(film.isFavorite()){
            favorite.setBackground(getDrawable(R.drawable.love));
        }else {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_NO:
                    favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    favorite.setBackground(getDrawable(R.drawable.white_favorite));
                    break;
            }
        }
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                if(film.isChecked()){
                    add.setBackground(getDrawable(R.drawable.check));
                }else add.setBackground(getDrawable(R.drawable.uncheck));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                if(film.isChecked()){
                    add.setBackground(getDrawable(R.drawable.white_check));
                }else add.setBackground(getDrawable(R.drawable.white_uncheck));
                add.setBackgroundResource(R.drawable.white_add);
                findViewById(R.id.back).setBackgroundResource(R.drawable.white_back);
                break;
        }
        add.setOnClickListener(v -> {
            film.setChecked(!film.isChecked());
            if(film.isChecked()){
                profile.setScore(profile.getScore()+2);
                rating(profile.getScore());
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        add.setBackgroundResource(R.drawable.check);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        add.setBackgroundResource(R.drawable.white_check);
                        break;
                }
                my_films.add(film);
            }else {
                profile.setScore(profile.getScore()-2);
                rating(profile.getScore());
                my_films.remove(film);
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        add.setBackgroundResource(R.drawable.uncheck);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        add.setBackgroundResource(R.drawable.white_uncheck);
                        break;
                }
            }
            boolean unic = true;
            for(int i=0;i<filmsDb_list.size();i++){
                Log.d("unic!!!!!",filmsDb_list.get(i).getTitle()+ " "+ film.getTitle());
                if(filmsDb_list.get(i).getTitle().equals(film.getTitle())) {
                    unic = false;
                }
            }
            if(unic){
                saveFilm(film);
            }else {
                updateFilm(film);
            }
            saveAppData(profile);

        });
        ImageView rating = findViewById(R.id.film_rating);
        TextView ratingText = findViewById(R.id.film_rating_text);
        if(film.getRating()>6.5){
            rating.setImageResource(R.drawable.circle);
        }else rating.setImageResource(R.drawable.gray_circle);

        ratingText.setText(String.valueOf(film.getRating()));
        favorite.setOnClickListener(v -> {
            film.setFavorite(!film.isFavorite());
            if(film.isFavorite()) {
                favorite.setBackground(getDrawable(R.drawable.love));
                favorite_films.add(film);
            }else{
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        favorite.setBackground(getDrawable(R.drawable.white_favorite));
                        break;
                }
                favorite_films.remove(film);
            }
            boolean unic = true;
            for(int i=0;i<filmsDb_list.size();i++){
                Log.d("unic!!!!!",filmsDb_list.get(i).getTitle()+ " "+ film.getTitle());
                if(filmsDb_list.get(i).getTitle().equals(film.getTitle())) {
                    unic = false;
                }
            }
            if(unic){
                saveFilm(film);
            }else {
                updateFilm(film);
            }
            saveAppData(profile);
        });
        knowlage.setOnClickListener(v -> {
            filmScroll(film);
        });
        findViewById(R.id.back).setOnClickListener(v -> {
            loadPage(page_number);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadInfoPage(){
        setContentView(R.layout.info_page);
        loadBottomBar();
        Button back = findViewById(R.id.back);
        Button settings = findViewById(R.id.settings_button);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                back.setBackgroundResource(R.drawable.white_back);
                settings.setBackgroundResource(R.drawable.white_settings);
                break;
        }
        settings.setOnClickListener(v -> {
            page_number = 3;
            loadPage(page_number);
        });
        back.setOnClickListener(v1 -> {
            loadPage(page_number);
        });
    }



    private void rating(int score){
        if(score>=0 & score<100) {
            profile.setRank(0);
        }else if(score>=100 & score<200){
            profile.setRank(1);
        }else if(score>=200 & score<300){
            profile.setRank(2);
        }else if(score>=300 & score<400){
            profile.setRank(3);
        }else if(score>=400 & score<500){
            profile.setRank(4);
        }else if(score>=500){
            profile.setRank(5);
        }
    }

    public String getWordAddition(int num,String word) {

        int preLastDigit = num % 100 / 10;

        if (preLastDigit == 1) {
            return word+"ов";
        }

        switch (num % 10) {
            case 1:
                return word;
            case 2:
            case 3:
            case 4:
                return word+"а";
            default:
                return word+"ов";
        }

    }

    public String getTimeWordAddition(int num,String word) {

        int preLastDigit = num % 100 / 10;

        if (preLastDigit == 1) {
            return word;
        }

        switch (num % 10) {
            case 1:
                return word+"a";
            case 2:
            case 3:
            case 4:
                return word+"ы";
            default:
                return word;
        }

    }

    public String getYearAddition(int num) {

        int preLastDigit = num % 100 / 10;

        if (preLastDigit == 1) {
            return "лет";
        }

        switch (num % 10) {
            case 1:
                return "год";
            case 2:
            case 3:
            case 4:
                return "года";
            default:
                return "лет";
        }

    }


    @SuppressLint("NewApi")
    private Drawable getIcon(int rank) {
        switch (rank){
            case 0:
                return getDrawable(R.drawable.rank_six);
            case 1:
                return getDrawable(R.drawable.rank_five);
            case 2:
                return getDrawable(R.drawable.rank_fourth);
            case 3:
                return getDrawable(R.drawable.rank_three);
            case 4:
                return getDrawable(R.drawable.rank_two);
            case 5:
                return getDrawable(R.drawable.rank_one);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadBottomBar(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                findViewById(R.id.main_button).setBackgroundResource(R.drawable.white_main);
                findViewById(R.id.list_button).setBackgroundResource(R.drawable.white_list);
                findViewById(R.id.profile_button).setBackgroundResource(R.drawable.white_profile);
                break;
        }
        findViewById(R.id.main_button).setOnClickListener(v -> {
            if(films.size()==0){
                loadFilmDB();
            }
            if(page_number == 3){
                page_number = 0;
                page_back = page_number;
                hideSettingsAnim();
            }else {
                page_back = page_number;
                page_number = 0;
                loadPage(page_number);
            }
            });
        findViewById(R.id.list_button).setOnClickListener(v -> {
            if(page_number == 3){
                page_number = 1;
                page_back = page_number;
                hideSettingsAnim();
            }
            else {
                page_back = page_number;
                page_number = 1;
                loadPage(page_number);
            }
            });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            if(page_number == 3){
                page_number = 2;
                page_back = page_number;
                hideSettingsAnim();
            }
            else {
                page_back = page_number;
                page_number = 2;
                loadPage(page_number);
            }
            });
    }

    private void hideSettingsAnim(){
        TextView top_bar_title = findViewById(R.id.top_bar_title);
        top_bar_title.setVisibility(View.GONE);
        View v = findViewById(R.id.top_bar);
        LinearLayout top_bar = findViewById(R.id.settings_top);
        top_bar.removeAllViews();
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.drawer_reverse);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ViewGroup.LayoutParams par = v.getLayoutParams();
                par.height = 1100;
                v.setLayoutParams(par);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onAnimationEnd(Animation animation) {
                loadPage(page_number);
                findViewById(R.id.search_button).startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.show));
                findViewById(R.id.settings_button).startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.show));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void filmScroll(Film film){
        setContentView(R.layout.film_info);
        Button favorite = findViewById(R.id.favorite);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                if(film.isFavorite()){
                    favorite.setBackground(getDrawable(R.drawable.love));
                }else favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                findViewById(R.id.back).setBackgroundResource(R.drawable.white_back);
                if(film.isFavorite()){
                    favorite.setBackground(getDrawable(R.drawable.love));
                }else favorite.setBackground(getDrawable(R.drawable.white_favorite));
                break;
        }
        findViewById(R.id.back).setOnClickListener(v -> {
            setContentView(R.layout.film_page);
            loadFilmPage(film);
        });
        favorite.setOnClickListener(v -> {
            film.setFavorite(!film.isFavorite());
            if(film.isFavorite()){
                favorite.setBackground(getDrawable(R.drawable.love));
                favorite_films.add(film);
            }else {
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        favorite.setBackground(getDrawable(R.drawable.favorite_vector));
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        favorite.setBackground(getDrawable(R.drawable.white_favorite));
                        break;
                }
                favorite_films.remove(film);
            }
        });
        makeStars(film);
        TextView top_bar_title = findViewById(R.id.top_bar_title);
        top_bar_title.setText(film.getTitle());
        top_bar_title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
        TextView year = findViewById(R.id.put_year);
        year.setText(film.getYear());
        TextView country = findViewById(R.id.put_country);
        country.setText(film.getCountry());
        TextView genre = findViewById(R.id.put_film_genre);
        genre.setText(film.getGenre());
        TextView length = findViewById(R.id.put_status);
        length.setText(film.getLength()+getTimeWordAddition(film.getLength()," минут"));
        TextView director = findViewById(R.id.put_director);
        director.setText(film.getDirector());
        TextView description = findViewById(R.id.description_info);
        description.setText(film.getShort_description());
        loadBottomBar();
    }

    private void makeStars(Film film) {
        double rating = film.getRating();
        ImageView[] stars = new ImageView[]{findViewById(R.id.star1),findViewById(R.id.star2),findViewById(R.id.star3),findViewById(R.id.star4),findViewById(R.id.star5),
                findViewById(R.id.star6),findViewById(R.id.star7),findViewById(R.id.star8),findViewById(R.id.star9),findViewById(R.id.star10)};
        for(int i = 0;i<Math.floor(rating);i++){
            stars[i].setImageResource(R.drawable.star_solid);
        }
        if(rating%1!=0){
            stars[(int) Math.floor(rating)].setImageResource(R.drawable.half_star);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showSettingsAnim(){
        TextView top_bar_title = findViewById(R.id.top_bar_title);
        View v = findViewById(R.id.top_bar);
        LinearLayout top_bar = findViewById(R.id.settings_top);
        View view = getLayoutInflater().inflate(R.layout.settings, null);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                view.findViewById(R.id.ad).setBackgroundResource(R.drawable.white_ad);
                view.findViewById(R.id.like).setBackgroundResource(R.drawable.white_like);
                view.findViewById(R.id.theme).setBackgroundResource(R.drawable.theme_on);
                break;
        }
        view.findViewById(R.id.ad).setOnClickListener(v1 -> {
            ad = !ad;
            Toast.makeText(MainActivity.this,ad+"",Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.theme).setOnClickListener(v12 -> {
            dark_theme = !dark_theme;
            if (dark_theme) {
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                view.findViewById(R.id.theme).setBackgroundResource(R.drawable.theme_on);
            } else {
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                view.findViewById(R.id.theme).setBackgroundResource(R.drawable.theme_off);
            }
        });
        top_bar.addView(view);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.drawer);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ViewGroup.LayoutParams par = v.getLayoutParams();
                par.height = 1300;
                v.setLayoutParams(par);
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
                top_bar_title.setVisibility(View.VISIBLE);
                top_bar_title.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.show));
                view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.show));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadTopBar(int page){
        TextView top_bar_title = findViewById(R.id.top_bar_title);
        top_bar_title.setVisibility(View.INVISIBLE);
        EditText search_field = findViewById(R.id.search_field);
        search_field.setVisibility(View.GONE);
        TextView count = findViewById(R.id.count);
        top_bar_title.setText(Dictionary.titles[page]);
        Button srch = findViewById(R.id.search_button);
        Button filter = findViewById(R.id.params);
        Button sttng = findViewById(R.id.settings_button);
        filter.setVisibility(View.GONE);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                srch.setBackgroundResource(R.drawable.white_search);
                sttng.setBackgroundResource(R.drawable.white_settings);
                filter.setBackgroundResource(R.drawable.white_filter);
                break;
        }
        sttng.setOnClickListener(v -> {
            page_back = page_number;
            page_number = 3;
            loadPage(page_number);
        });
        srch.setOnClickListener(v -> {
            top_bar_title.setVisibility(View.GONE);
            count.setVisibility(View.GONE);
            sttng.setVisibility(View.GONE);
            search_field.setVisibility(View.VISIBLE);
            search_field.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
            filter.setVisibility(View.VISIBLE);
            filter.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
            srch.setOnClickListener(v1 -> {
                search(search_field.getText().toString(),0);
            });
            filter.setOnClickListener(v13 -> {
                loadSearchPage();
            });
            LinearLayout pageLinear = findViewById(R.id.page);
            pageLinear.setOnClickListener(v14 -> {
                if(search_field.getVisibility()==View.VISIBLE) {
                    loadTopBar(page_number);
                }
            });
        });
        switch (page){
            case 0:
            case 4:
            case 5:
            case 6:
                top_bar_title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                top_bar_title.setVisibility(View.VISIBLE);
                srch.setVisibility(View.VISIBLE);
                sttng.setVisibility(View.VISIBLE);
                break;
            case 1:
                top_bar_title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                top_bar_title.setVisibility(View.VISIBLE);
                srch.setVisibility(View.VISIBLE);
                sttng.setVisibility(View.VISIBLE);
                count.setVisibility(View.VISIBLE);
                count.setText("Всего просмотренно: "+my_films.size());
                count.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                break;
            case 2:
                top_bar_title.startAnimation(AnimationUtils.loadAnimation(this,R.anim.show));
                top_bar_title.setVisibility(View.VISIBLE);
                srch.setVisibility(View.GONE);
                sttng.setVisibility(View.VISIBLE);
                break;
            case 3:
                srch.setVisibility(View.GONE);
                sttng.setVisibility(View.GONE);
                count.setVisibility(View.GONE);
                showSettingsAnim();
                findViewById(R.id.page).setOnClickListener(v12 -> {
                    page_number = page_back;
                    hideSettingsAnim();
                });
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadSearchPage() {
        setContentView(R.layout.search);
        Button reset = findViewById(R.id.reset);
        Button show = findViewById(R.id.show);
        TextView titleAll = findViewById(R.id.all);
        TextView genreAll = findViewById(R.id.genre_text);
        TextView periodAll = findViewById(R.id.period_years);
            reset.setAlpha((float) 0.5);
            reset.setEnabled(false);
        loadBottomBar();
        findViewById(R.id.back).setOnClickListener(v -> {
            setContentView(R.layout.film_page);
            loadPage(page_number);
        });
        LinearLayout all = findViewById(R.id.btn_all);
        LinearLayout genre = findViewById(R.id.btn_genre);
        LinearLayout year = findViewById(R.id.btn_year);
        all.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.spinner,null);
            Spinner spinner = view.findViewById(R.id.view_spinner_params);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Показывать:")
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        titleAll.setText(spinner.getSelectedItem().toString());
                        reset.setAlpha((float) 1.0);
                        reset.setEnabled(true);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });
        genre.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.spiner2,null);
            Spinner spinner = view.findViewById(R.id.genre_spinner_params);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Жанр:")
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        genreAll.setText(spinner.getSelectedItem().toString());
                        reset.setAlpha((float) 1.0);
                        reset.setEnabled(true);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });
        year.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.period,null);
            Spinner start = view.findViewById(R.id.period1);
            Spinner end = view.findViewById(R.id.period2);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Период:")
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        periodAll.setText(start.getSelectedItem().toString()+"-"+end.getSelectedItem().toString());
                        reset.setAlpha((float) 1.0);
                        reset.setEnabled(true);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });
        reset.setOnClickListener(v -> loadSearchPage());
        TextView rating = findViewById(R.id.rating_seek);
        SeekBar rating_bar = findViewById(R.id.rating_bar);
        rating.setText((rating_bar.getProgress())+"-10");
        Switch hide = findViewById(R.id.hide);
        rating_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rating.setText((seekBar.getProgress())+"-10");
                reset.setAlpha((float) 1.0);
                reset.setEnabled(true);
            }
        });
        show.setOnClickListener(v -> {
            search(titleAll.getText().toString()+"/"+genreAll.getText().toString()+"/"+periodAll.getText().toString()+"/"+rating.getText().toString()+"/"+hide.isChecked(),1);
        });
    }

    private String makeRightUrl(String rus){
        rus = rus.replaceAll("ё","е");
        try {
            rus = URLEncoder.encode(rus,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return rus;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void search(String s, int i) {
        switch (i){
            case 0:
                String url = "https://api.kinopoisk.dev/movie?search="+makeRightUrl(s)+"&field=name&isStrict=false&sortField=votes.kp&sortType=-1&token=4V4F6E2-AG0MJMB-PVPG4P7-0T5XD69";
                Log.d("@@@@@@@@",url);
                new getFilms().execute(url,"search");
                break;
            case 1:
                String[] requests = s.split("/");
                //new getFilms().execute(url,"search");
                break;
            case 2:
                //new getFilms().execute(url,"search");
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        if(page_number==4 | findViewById(R.id.info_title)!=null | page_number==5){
            page_number=2;
            loadPage(page_number);
        }else if(page_number==3){
            page_number=page_back;
            hideSettingsAnim();
        }else if(page_number==2 & page_back==4){
            page_back = 1;
            page_number=page_back;
            loadPage(page_number);
        }else if(page_number==0 & findViewById(R.id.film_title)==null) super.onBackPressed();
        else if(page_back==page_number & findViewById(R.id.film_title)==null){
            page_back--;
            page_number = page_back;
            loadPage(page_number);
        }else if(page_number>0) {
            page_number = page_back;
            loadPage(page_number);
        }else if(findViewById(R.id.film_title)!=null){
            page_number = page_back;
            loadPage(page_number);
        }
    }


    public class getFilms extends AsyncTask<String , Void ,String> {
        String server_response;
        String request;
        CustomLoadBar pd;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                Log.d("@@@@", url + "");
                if (strings.length > 1) {
                    request = strings[1];
                }
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (request !=null) {
                search_result.clear();
                makeFilmSearch(server_response);
                page_number = 6;
                loadPage(page_number);
            } else {
                makeFilm(server_response);
            }
        }
    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


    private void  makeFilm(String server_response_json){
        JSONObject jsonRoot = null;
        String title, short_description, genre = null, year = null,country = null, director = null;
        int movieLength;
        Double rating;
        String icon_url;
        try {
            jsonRoot = new JSONObject(server_response_json);
                title = jsonRoot.getString("name");
                short_description = jsonRoot.getString("description");
                genre = jsonRoot.getJSONArray("genres").getJSONObject(0).getString("name");
                year = jsonRoot.getString("year");
                rating = Double.valueOf(jsonRoot.getJSONObject("rating").getString("kp"));
                icon_url = jsonRoot.getJSONObject("poster").getString("previewUrl");
                director = jsonRoot.getJSONArray("persons").getJSONObject(0).getString("name");
                country = jsonRoot.getJSONArray("countries").getJSONObject(0).getString("name");
                if(short_description.equals("null")){
                    short_description = "Описание отсутствует";
                }
                try {
                    movieLength = Integer.parseInt(jsonRoot.getString("movieLength"));
                }catch (org.json.JSONException e){
                    movieLength = 0;
                }
                Film film = new Film(title,short_description,rating,false,false,genre,year,director,country,movieLength);
                film.setIcon_url(icon_url);
                film.setPopular(true);
                filmsDb_list.add(film);
                films.add(film);
                makeContent(film);
                filmsDB.insert(DBHelper.TABLE_MY_FILMS,null,contentValues);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
        }
    }


    private void makeFilmSearch(String server_response_json){
        JSONObject jsonRoot = null;
        JSONArray jsonArray = null;
        String title = null, short_description = "Описание отсутствует", genre = "", year = null,country = "",director;
        Double rating;
        int movieLength = 0;
        String icon_url = null;
        try {
            jsonRoot = new JSONObject(server_response_json);
            jsonArray = jsonRoot.getJSONArray("docs");
            for(int i=0;i<jsonArray.length();i++) {
                title = jsonArray.getJSONObject(i).getString("name");
                short_description = jsonArray.getJSONObject(i).getString("description");
                year = jsonArray.getJSONObject(i).getString("year");
                rating = Double.valueOf(jsonArray.getJSONObject(i).getJSONObject("rating").getString("kp"));
                icon_url = jsonArray.getJSONObject(i).getJSONObject("poster").getString("previewUrl");
                try {
                    movieLength = Integer.parseInt(jsonArray.getJSONObject(i).getString("movieLength"));
                }catch (org.json.JSONException e){
                    movieLength = 0;
                }
                try{
                    director = jsonArray.getJSONObject(i).getJSONArray("persons").getJSONObject(0).getString("name");
                }catch (org.json.JSONException r){
                    director = "";
                }
                if(short_description.equals("null")){
                    short_description = "Описание отсутствует";
                }
                Film film = new Film(title, short_description, rating, false, false, genre, year, director, country,movieLength);
                film.setIcon_url(icon_url);
                search_result.add(film);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
        }
    }

    private void updateFilm(Film film){
            Log.d("update!!!!!!!!!",film.getTitle());
            makeContent(film);
            filmsDB.update(DBHelper.TABLE_MY_FILMS, contentValues,DBHelper.KEY_NAME + "=" + "'"+film.getTitle()+"'",null);
    }

    private void saveFilm(Film film){
        Log.d("save!!!!!!!!!",film.getTitle());
        filmsDb_list.add(film);
        makeContent(film);
        filmsDB.insert(DBHelper.TABLE_MY_FILMS, null , contentValues);
    }

    private void findAdvice(){

    }

    private void loadFilms(){
        try {
            Document document = Jsoup.connect(popular_url).get();
            content = document.select("a[href].selection-film-item-meta__link");
            for(int i=0;i<3;i++) {
                String id = content.get(i).attr("href").substring(6).replaceAll("/","");
                new getFilms().execute("https://api.kinopoisk.dev/movie?search="+id+"&field=id&token=4V4F6E2-AG0MJMB-PVPG4P7-0T5XD69");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveAppData(profile);
    }
}