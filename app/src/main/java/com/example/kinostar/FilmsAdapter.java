package com.example.kinostar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.InputStream;
import java.util.List;

public class FilmsAdapter extends ArrayAdapter {

    private Context context;
    private List<Film> films;

    public FilmsAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);

        this.context = context;
        this.films = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Film film = films.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.popular_list, null);

        TextView title = (TextView) view.findViewById(R.id.label);
        TextView desc = (TextView) view.findViewById(R.id.descrip);
        TextView score = (TextView) view.findViewById(R.id.film_score);
        ImageView image_score = view.findViewById(R.id.circle_score);
        ImageView icon = view.findViewById(R.id.icon);
        if(film.getRating()>6.5){
            image_score.setImageResource(R.drawable.circle);
        }else image_score.setImageResource(R.drawable.gray_circle);

        title.setText(film.getTitle());
        desc.setText(film.getShort_description());
        score.setText(String.valueOf(film.getRating()));
        new DownloadImageTask(icon).execute(film.getIcon_url());
        return view;
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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

}
