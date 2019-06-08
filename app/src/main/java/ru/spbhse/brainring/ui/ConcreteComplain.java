package ru.spbhse.brainring.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.files.ComplainedQuestion;
import ru.spbhse.brainring.files.ComplainsFileHandler;
import ru.spbhse.brainring.utils.MailSender;

/**
 * This activity is shown when user presses on some question in {@code ComplainActivity}
 * Suggests user to submit concrete complain
 */
public class ConcreteComplain extends AppCompatActivity {
    private int indexInList;
    private boolean alreadyDeleted = false;
    private List<ComplainedQuestion> list;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concrete_complain);
        try {
            list = ComplainsFileHandler.getAllQuestionsFromFile(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf(Controller.APP_TAG, "Cannot load list of questions");
            finish();
            return;
        }
        indexInList = getIntent().getIntExtra("index", 0);

        TextView questionText = findViewById(R.id.questionText);
        questionText.setText(list.get(indexInList).getQuestionText());
        questionText.setMovementMethod(new ScrollingMovementMethod());

        TextView answer = findViewById(R.id.answer);
        answer.setText(list.get(indexInList).getQuestionAnswer());
        answer.setMovementMethod(new ScrollingMovementMethod());

        EditText complain = findViewById(R.id.complainText);
        complain.setText(list.get(indexInList).getComplainText());

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> {
            if (!alreadyDeleted) {
                saveWrittenText();
                MailSender.sendMail(this, "[Complain] Жалоба на вопрос",
                        list.get(indexInList).humanReadable());
            }
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            if (!alreadyDeleted) {
                alreadyDeleted = true;
                list.remove(indexInList);
                finish();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected void onPause() {
        super.onPause();
        saveWrittenText();
        try {
            ComplainsFileHandler.saveComplainsToFile(list, this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf(Controller.APP_TAG, "Error while writing to file");
        }
    }


    private void saveWrittenText() {
        if (!alreadyDeleted) {
            EditText complain = findViewById(R.id.complainText);
            list.get(indexInList).setComplainText(complain.getText().toString());
        }
    }
}
