package ru.spbhse.brainring.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestionTest {
    @Test
    public void questionConstructorTest() {
        Question question = new Question("aa", "bb", "00", "gg");
        assertEquals("aa", question.getQuestion());
        assertEquals("gg", question.getComment());

        question = new Question("aa", "bb", null, null);
        assertEquals("aa", question.getQuestion());
        assertEquals("", question.getComment());

        new Question("aa", "bb", "aa/bb/dd", null);
        assertThrows(IllegalArgumentException.class, () -> new Question(null, "aa", "bb", "gg"));
        assertThrows(IllegalArgumentException.class, () -> new Question("aa", null, "bb", "gg"));
    }

    @Test
    public void checkAnswerIncorrectAnswer() {
        Question question = new Question("aa", "bb", "00/cc", "gg");
        assertFalse(question.checkAnswer("Ab"));
        assertFalse(question.checkAnswer(null));
        assertFalse(question.checkAnswer("dd"));
    }

    @Test
    public void mainAnswerTest() {
        Question question = new Question("aa", "bb", "00/cc", "gg");
        assertEquals("bb", question.getMainAnswer());
    }

    @Test
    public void checkTotallyCorrectAnswer() {
        Question question = new Question("aa", "bb", "00/cc", "gg");
        assertTrue(question.checkAnswer("bb"));
        assertTrue(question.checkAnswer("00"));
        assertTrue(question.checkAnswer("cc"));
    }

    // TODO: check answers that close to right
}
