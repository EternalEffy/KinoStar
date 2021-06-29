package com.example.kinostar;

public class Profile {
    private String fullName;
    private String age;
    private String genre;
    private int score;
    private int rank;

    public Profile (String fullName,String age,String genre,int rank,int score){
        this.fullName = fullName;
        this.age = age;
        this.genre = genre;
        this.score = score;
        this.rank = rank;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
