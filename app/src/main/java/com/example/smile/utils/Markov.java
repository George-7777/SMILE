package com.example.smile.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Markov
{

    private static int ngram = 2;		// markov chain ngram parameter (contiguous sequence of words)
    private Map<String,String> markovTable = new HashMap<String,String>();
    List<String> textwords = new ArrayList<String>();

    public Markov (String text)		// constructor
    {
        textwords = Arrays.asList(text.split(" "));
        int maxwords = textwords.size() - ngram - 1;
        String keyString = null;
        int end;

        for (int j = 0; j < maxwords; j++)
        {
            keyString = "";
            end = j + ngram;
            for(int k = j; k < end; k++)
            {
                keyString = keyString + textwords.get(k) + " ";
            }
            keyString = keyString.trim();		// get rid of trailing spaces

            String value;
            if(markovTable.containsKey(keyString))	// if chain in list then add extra word
            {
                value = markovTable.get(keyString);
                value += " " + textwords.get(end);
                markovTable.put(keyString, value);
            }
            else
            {
                if(end <= maxwords)
                {
                    markovTable.put(keyString, textwords.get(end));
                }
            }
        }
    }

    public String genMarkov(int numWords)
    {
        // Build a random string using the above Markov chain table
        String buffer = "";
        String newword = "";
        String keyString = "";

        Random rgen = new Random();		// initialise random number generator
        List<String> lastwords = new ArrayList<String>();
        int possible = textwords.size() - ngram;
        int startnum = rgen.nextInt(possible);
        for (int i = startnum, j = 0; i < startnum+ngram; i++,j++) 	//get the random start word chain
        {
            newword = textwords.get(i);
            lastwords.add(j, newword);
            buffer += newword + " ";
        }

        for(int i = ngram; i < numWords; i++)
        {
            keyString = "";
            for (int j = 0; j < ngram; j++)
            {
                keyString = keyString + lastwords.get(j) + " ";
            }
            keyString = keyString.trim();		// get rid of trailing spaces

            if(markovTable.containsKey(keyString))	// if ngram in list then add next word
            {
                List<String> possiblenext = new ArrayList<String>();
                possiblenext = Arrays.asList(markovTable.get(keyString).split(" "));
                int c = possiblenext.size();	//must be at least 1
                int r = rgen.nextInt(c);

                String nextword = possiblenext.get(r);
                buffer += nextword +" ";
                for (int j = 0; j < ngram-1; j++)
                {
                    lastwords.set(j, lastwords.get(j+1));	// shift words to the left
                }
                lastwords.set(ngram-1, nextword);			// add the next word to end
            }
        }

        return buffer.trim();
    }

}