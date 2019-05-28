package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.files.ComplainsFileHandler;

public class ComplainActivity extends AppCompatActivity {

    private List<ComplainedQuestion> questions = new ArrayList<>(); //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        showListOfQuestions();
        ListView view = findViewById(R.id.questionsList);
        view.setOnItemClickListener((parent, view1, position, id) -> {
            // TODO run questions.get(position) edit activity
        });

        Button sendAllButton = findViewById(R.id.sendAllButton);
        sendAllButton.setOnClickListener(v -> {
            // TODO send all
        });

        Button clearListButton = findViewById(R.id.clearListButton);
        clearListButton.setOnClickListener(v -> {
            questions = new ArrayList<>();
            try {
                ComplainsFileHandler.saveComplainsToFile(questions);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
            showListOfQuestions();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showListOfQuestions();
    }

    private void showListOfQuestions() {
        try {
            questions = ComplainsFileHandler.getAllQuestionsFromFile();
        } catch (Exception e) {
            Log.wtf("BrainRing", "Error while opening file");
            e.printStackTrace();
            finish();
            return;
        }
        ListView view = findViewById(R.id.questionsList);
        ArrayAdapter<ComplainedQuestion> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questions);
        view.setAdapter(adapter);
    }
}
