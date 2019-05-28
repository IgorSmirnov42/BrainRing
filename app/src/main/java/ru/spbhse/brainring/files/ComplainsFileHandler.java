package ru.spbhse.brainring.files;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComplainsFileHandler {
    private static final String filename = "complains.json";

    @NonNull
    public static List<ComplainedQuestion> getAllQuestionsFromFile() throws IOException, JSONException {
        List<ComplainedQuestion> questions = new ArrayList<>();
        File file = createFileIfNotExists();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    builder.append(line).append("\n");
                } else {
                    break;
                }
            }
        }
        JSONArray array = new JSONArray(builder.toString());
        for (int i = 0; i < array.length(); ++i) {
            JSONObject jsonQuestion = array.getJSONObject(i);
            questions.add(new ComplainedQuestion(
                    jsonQuestion.getString("questionText"),
                    jsonQuestion.getString("questionAnswer"),
                    jsonQuestion.getInt("questionId"),
                    jsonQuestion.getString("complainText")));
        }
        return questions;
    }

    public static void appendComplain(@NonNull ComplainedQuestion question) throws IOException, JSONException {
        List<ComplainedQuestion> questions = getAllQuestionsFromFile();
        questions.add(question);
        saveComplainsToFile(questions);
    }

    private static String allReadable(@NonNull List<ComplainedQuestion> questions) {
        StringBuilder result = new StringBuilder();
        for (ComplainedQuestion question : questions) {
            result.append(question.humanReadable());
        }
        return result.toString();
    }

    private static File createFileIfNotExists() throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
           if (!file.createNewFile()) {
               throw new IllegalStateException("Cannot create file");
           }
        }
        return file;
    }

    public static void saveComplainsToFile(@NonNull List<ComplainedQuestion> questions) throws IOException, JSONException {
        File file = createFileIfNotExists();

        List<JSONObject> complains = new ArrayList<>();
        for (ComplainedQuestion question : questions) {
            JSONObject jsonQuestion = new JSONObject();
            jsonQuestion.put("questionText", question.getQuestionText());
            jsonQuestion.put("questionAnswer", question.getQuestionAnswer());
            jsonQuestion.put("questionId", question.getQuestionId());
            jsonQuestion.put("complainText", question.getComplainText());
            complains.add(jsonQuestion);
        }

        JSONArray array = new JSONArray(complains);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(array.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendComplainWithEmail() throws IOException, JSONException {
        String readableComplains = allReadable(getAllQuestionsFromFile());
        String receiverMail = "ismirnov.testing@gmail.com";
        // TODO
    }
}
