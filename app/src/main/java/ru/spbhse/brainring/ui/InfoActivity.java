package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Scanner;

import ru.spbhse.brainring.R;

public class InfoActivity extends AppCompatActivity {
    private View pressedCard = null;
    private String infoLicense;
    private String infoLocal;
    private String infoNet;
    private String infoTraining;
    private String infoAuthors;
    private String infoGeneral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        infoLicense = loadText(getResources().openRawResource(R.raw.info_license));
        infoLocal = loadText(getResources().openRawResource(R.raw.info_local));
        infoNet = loadText(getResources().openRawResource(R.raw.info_net));
        infoAuthors = loadText(getResources().openRawResource(R.raw.info_authors));
        infoTraining = loadText(getResources().openRawResource(R.raw.info_training));
        infoGeneral = loadText(getResources().openRawResource(R.raw.info_general));

        TextView infoText = findViewById(R.id.infoText1);
        infoText.setMovementMethod(new ScrollingMovementMethod());

        CardView cardGeneral = findViewById(R.id.cardViewGeneral);
        ImageView cardGeneralBackground = findViewById(R.id.cardGeneralBackground);
        cardGeneral.setOnClickListener(v -> {
            highlight(cardGeneralBackground);
            infoText.setText(infoGeneral);
            infoText.scrollTo(0, 0);
        });

        CardView cardLicense = findViewById(R.id.cardViewLicense);
        ImageView cardLicenseBackground = findViewById(R.id.cardLicenseBackground);
        cardLicense.setOnClickListener(v -> {
            highlight(cardLicenseBackground);
            infoText.setText(infoLicense);
            infoText.scrollTo(0, 0);
        });

        CardView cardLocal = findViewById(R.id.cardViewLocal);
        ImageView cardLocalBackground = findViewById(R.id.cardLocalBackground);
        cardLocal.setOnClickListener(v -> {
            highlight(cardLocalBackground);
            infoText.setText(infoLocal);
            infoText.scrollTo(0, 0);
        });

        CardView cardTraining = findViewById(R.id.cardViewTraining);
        ImageView cardTrainingBackground = findViewById(R.id.cardTrainingBackground);
        cardTraining.setOnClickListener(v -> {
            highlight(cardTrainingBackground);
            infoText.setText(infoTraining);
            infoText.scrollTo(0, 0);
        });

        CardView cardNet = findViewById(R.id.cardViewNet);
        ImageView cardNetBackground = findViewById(R.id.cardNetBackground);
        cardNet.setOnClickListener(v -> {
            highlight(cardNetBackground);
            infoText.setText(infoNet);
            infoText.scrollTo(0, 0);
        });

        CardView cardAuthors = findViewById(R.id.cardViewAuthors);
        ImageView cardAuthorsBackground = findViewById(R.id.cardAuthorsBackground);
        cardAuthors.setOnClickListener(v -> {
            highlight(cardAuthorsBackground);
            infoText.setText(infoAuthors);
            infoText.scrollTo(0, 0);
        });
    }

    private String loadText(InputStream in) {
        try (Scanner scanner = new Scanner(in)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private void setHeight(View view, double height) {
        if (view == null) {
            return;
        }
        view.getLayoutParams().height = (int) height;
        view.setLayoutParams(view.getLayoutParams());
    }

    private void highlight(ImageView cardBackground) {
        if (cardBackground == pressedCard) {
            return;
        }
        if (pressedCard != null) {
            pressedCard.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        }
        cardBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.colorVanilla));
        pressedCard = cardBackground;
    }

    private double pixelsFromDp(double dpValue) {
        return dpValue * this.getResources().getDisplayMetrics().density;
    }
}