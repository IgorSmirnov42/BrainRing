package ru.spbhse.brainring.network.messages.messageTypes;

import android.support.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

import ru.spbhse.brainring.network.messages.MessageCodes;
import ru.spbhse.brainring.network.messages.MessageGenerator;
import ru.spbhse.brainring.network.messages.Message;

/**
 * Message from server to client with information about finished round in online game
 * Format:  [String : correctAnswer]
 *          [String : comment]
 *          [int : firstUserScore]
 *          [int : secondUserScore]
 *          [String : questionMessage]
 *      correctAnswer -- correct answer for a question
 *      comment -- comment for an answer
 *      firstUserScore -- number of points first user has
 *      secondUserScore -- number of points second user has
 *      questionMessage -- message with additional information about question tha should be printed
 *              (i.e. nick of player who answered right)
 */
public class CorrectAnswerAndScoreMessage extends Message {
    private String correctAnswer;
    private String comment;
    private int firstUserScore;
    private int secondUserScore;
    private String questionMessage;

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getComment() {
        return comment;
    }

    public int getFirstUserScore() {
        return firstUserScore;
    }

    public int getSecondUserScore() {
        return secondUserScore;
    }

    public String getQuestionMessage() {
        return questionMessage;
    }

    public CorrectAnswerAndScoreMessage(String correctAnswer,
                                        String comment,
                                        int firstUserScore,
                                        int secondUserScore,
                                        String questionMessage) {
        super(MessageCodes.SENDING_CORRECT_ANSWER_AND_SCORE);
        this.correctAnswer = correctAnswer;
        this.comment = comment;
        this.firstUserScore = firstUserScore;
        this.secondUserScore = secondUserScore;
        this.questionMessage = questionMessage;
    }

    public CorrectAnswerAndScoreMessage(@NonNull DataInputStream inputStream) throws IOException {
        super(MessageCodes.SENDING_CORRECT_ANSWER_AND_SCORE);
        correctAnswer = inputStream.readUTF();
        comment = inputStream.readUTF();
        firstUserScore = inputStream.readInt();
        secondUserScore = inputStream.readInt();
        questionMessage = inputStream.readUTF();
    }

    @Override
    protected byte[] toByteArray(@NonNull MessageGenerator generator) {
        return generator.writeString(correctAnswer)
                .writeString(comment)
                .writeInt(firstUserScore)
                .writeInt(secondUserScore)
                .writeString(questionMessage).toByteArray();
    }
}
