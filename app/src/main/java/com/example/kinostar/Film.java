package com.example.kinostar;

public class Film {
    private String title, short_description, genre, year, director, country;
    private double my_rating;
    private int length;
    private double rating;
    private boolean favorite, checked, popular;
    private String icon_url;

    public Film (String title, String short_description,double rating,boolean favorite,boolean checked, String genre,String year, String director, String country, int length){
        this.title = title;
        this.short_description = short_description;
        this.rating = rating;
        this.favorite = favorite;
        this.checked = checked;
        this.genre = genre;
        this.year = year;
        this.director = director;
        this.country = country;
        this.length = length;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public double getMyRating() {
        return my_rating;
    }

    public void setMyRating(int my_rating) {
        this.my_rating = my_rating;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
