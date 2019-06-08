package ru.spbhse.brainring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.logic.TrainingPlayerLogic;

/** This activity is to prepare data, such as package and reading time, for conducting a training game */
public class TrainingGamePreparationActivity extends AppCompatActivity {
    private static final int SEEK_BAR_STEP = 10;
    private static final int SEEK_BAR_MIN = 10;
    private int counter = TrainingPlayerLogic.DEFAULT_READING_TIME;
    private String packageAddress = null;
    private String basePackageAddress;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_game_prepare);

        TextView packagesInfo = findViewById(R.id.selectPackages);
        packagesInfo.setMovementMethod(LinkMovementMethod.getInstance());

        basePackageAddress = getResources().getString(R.string.base_package);

        TextView currentPackage = findViewById(R.id.currentPackageName);
        currentPackage.setText(basePackageAddress);
        currentPackage.setMovementMethod(LinkMovementMethod.getInstance());

        packageAddress = basePackageAddress;

        EditText urlEditor = findViewById(R.id.urlEditor);

        Button startTrainingGameButton = findViewById(R.id.startTrainingGameButton);
        startTrainingGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(TrainingGamePreparationActivity.this, TrainingGameActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, packageAddress);
            intent.putExtra(Intent.EXTRA_TEXT, counter);
            startActivity(intent);
        });

        Button urlWrittenButton = findViewById(R.id.urlWrittenButton);
        urlWrittenButton.setOnClickListener(v -> {
                    try {
                        URL hyperlink = new URL(urlEditor.getText().toString());
                        packageAddress = hyperlink.toString();
                        Spanned currentPackageName = Html.fromHtml(
                                "<a href=\"" + hyperlink + "\">" + hyperlink.getFile() + "</a> "
                        );
                        currentPackage.setText(currentPackageName);
                        urlEditor.getText().clear();
                    } catch (MalformedURLException e) {
                        urlEditor.getText().clear();
                        Toast.makeText(TrainingGamePreparationActivity.this,
                                getString(R.string.package_not_found),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        Button resetCurrentPackage = findViewById(R.id.resetCurrentPackage);
        resetCurrentPackage.setOnClickListener(v -> {
            packageAddress = basePackageAddress;
            currentPackage.setText(getResources().getString(R.string.base_package));
        });

        SeekBar timeCounterBar = findViewById(R.id.trainingCounterBar);
        TextView timeCounterValue = findViewById(R.id.trainingCounterValue);
        timeCounterValue.setText(String.valueOf(counter));
        timeCounterBar.setProgress(counter / SEEK_BAR_STEP - 1);
        timeCounterBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                counter = SEEK_BAR_MIN + progress * SEEK_BAR_STEP;
                timeCounterValue.setText(String.valueOf(counter));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
