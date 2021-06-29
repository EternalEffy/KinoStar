package com.example.kinostar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "filmsDb";
    public static final String TABLE_MY_FILMS = "my_films";
    public static final String TABLE_ADVICE = "my_advice";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_RATING = "rating";
    public static final String KEY_GENRE = "genre";
    public static final String KEY_YEAR = "year";
    public static final String KEY_DIRECTOR = "director";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_LENGTH = "status";
    public static final String KEY_FAVORITE = "favorite";
    public static final String KEY_CHECKED = "checked";
    public static final String KEY_POPULAR = "popular";
    public static final String KEY_IMG_URL = "img_url";

    public DBHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_MY_FILMS+ "("+ KEY_ID + " integer primary key autoincrement,"+KEY_NAME + " text NOT NULL UNIQUE,"
                + KEY_DESCRIPTION+ " text ,"+KEY_RATING+" double,"+KEY_GENRE+" text ,"+ KEY_YEAR+ " text ,"+ KEY_DIRECTOR+ " text ,"
                + KEY_COUNTRY+ " text ,"+ KEY_LENGTH+ " integer ,"
                + KEY_IMG_URL+ " text ," + KEY_FAVORITE+ " integer ,"+ KEY_CHECKED+ " integer, "+ KEY_POPULAR+ " integer "+")");

        db.execSQL("create table "+TABLE_ADVICE+ "("+ KEY_ID + " integer primary key autoincrement,"+KEY_NAME + " text NOT NULL UNIQUE,"
                + KEY_DESCRIPTION+ " text ,"+KEY_RATING+" double,"+KEY_GENRE+" text ,"+ KEY_YEAR+ " text ,"+ KEY_DIRECTOR+ " text ,"
                + KEY_COUNTRY+ " text ,"+ KEY_LENGTH+ " integer ,"
                + KEY_IMG_URL+ " text "+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+TABLE_MY_FILMS);
        db.execSQL("drop table if exists "+TABLE_ADVICE);

        onCreate(db);
    }
}