package markov;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This program will calculate the chances of a letter being in a specific place in a word and uses those chances to build real looking words.
 * This program needs a file called names with one word on every line.
 * Words in this file that contain characters other than those in the alphabet variable, will not be counted in the statistics.
 * The larger the sample size, the better this will work.
 */
public class Main {
    private static ArrayList<String> names;

    private static Map<String, Double> firstLetterChance; // String, Double
    private static Map<String, Double> lastLetterChance; // String, Double
    private static Map<String, Map<String, Double>> letterToLetterChance; // String, Double, values are percent chance of being the second letter, if given the first letter

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz"; // This can be modified to fit the data in the names file, just add any characters or numbers or symbols that may be found in the file
    private static final char[] alphabetArray = alphabet.toCharArray();


    public static void main(String[] args) {
        names = fillNameArray();

        firstLetterChance = generateFirstLetterArray();
        lastLetterChance = generateLastLetterArray();
        letterToLetterChance = generateLetterToLetterArray();

        for (int i = 0; i < 20; i++) {
            System.out.println(generateWord());
        }
    }

    /**
     * Use the statistics gathered to generate a random word.
     */
    private static String generateWord() {
        String word = "";

        // pick a letter to start the word
        while (true) {
            char firstLetter = alphabet.charAt((int) (Math.random() * 26)); // random character

            if (Math.random() < firstLetterChance.get(String.valueOf(firstLetter)) * 2 + .05) { // check to see if it should start the word
                word += firstLetter;
                break;
            }
        }

        while (true) {
            char nextLetter = alphabet.charAt((int) (Math.random() * 26)); // pick a random letter
            if (Math.random() < letterToLetterChance.get(String.valueOf(word.charAt(word.length() - 1))).get(String.valueOf(nextLetter)) * 2) { // check to see if that character should be next
                word += nextLetter;

                // check if the word should end
                double chance = lastLetterChance.get(String.valueOf(word.charAt(word.length() - 1)));
                if (word.length() >= 4 && Math.random() < chance * 1.5 + .05) { // if the length is over 4 characters, check the chance of that character ending a word
                    break;
                } else if (word.length() > 8 && Math.random() < .3) { // if the length is over 8 characters, there is a 30% chance it will end, regardless of the last letter
                    break;
                }
            }
        }

        return word;
    }

    /**
     * Read from the file.
     */
    private static ArrayList<String> fillNameArray() {
        ArrayList<String> array = new ArrayList<String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("names"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (true) {
            String temp = null;

            try {
                temp = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (temp == null) {
                break;
            }

            temp.toLowerCase(); // the alphabet is all lowercase
            array.add(temp);
        }
        return array;
    }

    /**
     * Calculate the chances of a letter starting a word.
     */
    private static Map<String, Double> generateFirstLetterArray() {
        Map<String, Double> map = getAlphabetMap();

        // count the amount of times a certain letter starts a word
        double total = 0;
        for (String c : names) {
            String character = String.valueOf(c.charAt(0));

            if (map.containsKey(character)) { // if the word has characters not in alphabet, ignore it
                map.put(character, map.get(character) + 1);
                total++;
            }
        }

        // find the percent of character starting a word out of all of the first letters
        for (char character : alphabetArray) {
            map.put(String.valueOf(character), map.get(String.valueOf(character)) / total);
        }
        return map;
    }

    /**
     * Calculate the chance of a letter ending a word.
     */
    private static Map<String, Double> generateLastLetterArray() {
        Map<String, Double> map = getAlphabetMap();

        // count the amount of times a certain letter ends a word
        double total = 0;
        for (String c : names) {
            String character = String.valueOf(c.charAt(c.length() - 1));

            if (map.containsKey(character)) { // if the word has characters not in alphabet, ignore it
                map.put(character, map.get(character) + 1);
                total++;
            }
        }

        // find the percent of character ending a word out of all of the last letters
        for (char character : alphabetArray) {
            map.put(String.valueOf(character), map.get(String.valueOf(character)) / total);
        }
        return map;
    }

    /**
     * Calculate the chance of a letter being next to another letter in the middle of a word.
     */
    private static Map<String, Map<String, Double>> generateLetterToLetterArray() {
        Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();

        // fill map with the alphabet and getAlphabetMap()
        for (char c : alphabetArray) {
            map.put(String.valueOf(c), getAlphabetMap());
        }

        // count the amount of times two letters are found next to eachother
        for (String c : names) {
            for (int pos = 0; pos < c.length() - 1; pos++) {
                String firstChar = String.valueOf(c.charAt(pos));
                String secondChar = String.valueOf(c.charAt(pos + 1));

                if (map.containsKey(firstChar)) { // if the word has characters not in alphabet, ignore it
                    if (map.get(firstChar).containsKey(secondChar)) {
                        map.get(firstChar).put(secondChar, map.get(firstChar).get(secondChar) + 1);
                    }
                }
            }
        }

        // find the percent of second characters found next to the first character, out of the total
        for (char firstChar : alphabetArray) {
            // add up the total number of adjacent letters that were found for the current first letter
            double total = 0;
            for (double d : map.get(String.valueOf(firstChar)).values()) {
                total += d;
            }

            // calculate percents
            for (char secondChar : alphabetArray) {
                double value = map.get(String.valueOf(firstChar)).get(String.valueOf(secondChar)) / total;
                map.get(String.valueOf(firstChar)).put(String.valueOf(secondChar), value);
            }
        }
        return map;
    }

    /**
     * Fill a hashmap with each character in alphabet for keys, and 0 for each value.
     */
    private static Map<String, Double> getAlphabetMap() {
        Map<String, Double> map = new HashMap<String, Double>();
        for (char c : alphabetArray) {
            map.put(String.valueOf(c), (double) 0);
        }
        return map;
    }
}
