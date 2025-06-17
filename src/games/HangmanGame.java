package games;

import java.util.HashSet;
import java.util.Set;

public class HangmanGame {

    private final String wordToGuess;
    private final Set<Character> correctGuesses;
    private final Set<Character> incorrectGuesses;
    private int remainingGuesses;


    public HangmanGame(String word) {
        wordToGuess = word.toLowerCase();
        correctGuesses  = new HashSet<>();
        incorrectGuesses = new HashSet<>();
        remainingGuesses = 6;
    }

    public boolean guessLetter(char c){
        c = Character.toLowerCase(c);
        if(wordToGuess.indexOf(c) >= 0) {
            correctGuesses.add(c);
            return true;
        }   else {
            incorrectGuesses.add(c);
            remainingGuesses--;
            return false;
        }
    }

    public String getMaskedWord(){
        StringBuilder sb = new StringBuilder();
        for (char c: wordToGuess.toCharArray()) {
            sb.append(correctGuesses.contains(c) ? c : '_').append(' ');
        }
        return sb.toString();
    }

    public boolean isGameOver() {
        return isWin() || remainingGuesses <= 0;
    }

    public boolean isWin() {
        for (char c: wordToGuess.toCharArray()) {
            if (!correctGuesses.contains(c)) return false;
        }
        return true;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public Set<Character> getIncorrectGuesses() {
        return incorrectGuesses;
    }

    public String getWordToGuess() {
        return wordToGuess;
    }


}

