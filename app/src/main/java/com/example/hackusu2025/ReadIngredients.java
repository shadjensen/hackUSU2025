package com.example.hackusu2025;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;


class UrlScore {
    String url;
    double score;

    // Constructor
    public UrlScore(String url, double score) {
        this.url = url;
        this.score = score;
    }
}

class Tuple {
    String first;
    double second;
    String third;

    // Constructor
    public Tuple(String first, double second, String third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    // Getters and toString method for better representation
    public String getFirst() {
        return first;
    }

    public double getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }
}

public class ReadIngredients {
	
    private static Map<String, Double> unitToTeaspoon;


    static {
        unitToTeaspoon = new HashMap<String, Double>();
        unitToTeaspoon.put("pinch", 1.0/16.0);
        unitToTeaspoon.put("teaspoon", 1.0);
        unitToTeaspoon.put("tablespoon", 3.0);
        unitToTeaspoon.put("fluid ounce", 6.0);
        unitToTeaspoon.put("cup", 48.0);
        unitToTeaspoon.put("pint", 96.0);
        unitToTeaspoon.put("quart", 192.0);
        unitToTeaspoon.put("gallon", 768.0);
        
    }

    public static double convert(double amount, String fromUnit, String toUnit)
    {
        fromUnit = fromUnit.toLowerCase();
        toUnit = toUnit.toLowerCase();
        
        if (fromUnit.equals("count"))
        	return amount;

        if (!unitToTeaspoon.containsKey(fromUnit) || !unitToTeaspoon.containsKey(toUnit))
        {
            throw new IllegalArgumentException("Invalid unit: " + fromUnit + " or " + toUnit);
        }

        // Convert to teaspoons first
        double amountInTeaspoons = amount * unitToTeaspoon.get(fromUnit);

        // Convert from teaspoons to target unit
        return amountInTeaspoons / unitToTeaspoon.get(toUnit);
    }
    


    public static ArrayList<String> getBestFits (ArrayList<Tuple> ingredients, int minIngredients, String category) throws FileNotFoundException, IOException
    {


        PriorityQueue<UrlScore> maxHeap = new PriorityQueue<>(new Comparator<UrlScore>() {
            @Override
            public int compare(UrlScore o1, UrlScore o2) {
                // Max-Heap, so we reverse the comparison to put higher scores first
                return Double.compare(o2.score, o1.score);
            }
        });

        HashMap<String, String> urlToGenre = new HashMap<String, String>();

        try (BufferedReader br = new BufferedReader(new FileReader("filtered_recipe_urls_with_genres.txt")))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                String url = line.substring(0, line.indexOf(" - "));
                String genre = line.substring(line.indexOf(" - ")+3, line.length());
                urlToGenre.put(url, genre);
                //System.out.println(url);
            }
        }


        try (BufferedReader br = new BufferedReader(new FileReader("cleaned_ingredients.txt")))
        {
            String line;
            UrlScore currentUrl = new UrlScore("", 0);

            while ((line = br.readLine()) != null)
            {
                // Read URL line
                String url = line.trim(); // Store the URL in a separate variable
                int score = 0; // Reset score for this URL

                HashMap<String, Double> neededByIngredient = new HashMap<String, Double>();
                for (Tuple ingredient : ingredients)
                    neededByIngredient.put(ingredient.first + " " + ingredient.third, 0.0);

                int totIngredients = 0;
                int realTotIngredients = 0;

                // Skip reading next lines until an empty line or EOF
                while ((line = br.readLine()) != null && !line.trim().isEmpty())
                {
                    totIngredients ++;
                    realTotIngredients ++;
                    String words[] = line.split(" ");

                    for (Tuple ingredient : ingredients)
                    {
                        if (line.contains(ingredient.first))
                        {
                            // Found ingredient
                            double quantity = Double.parseDouble(words[0]);

                            if (words[1].equals(ingredient.third))
                            {
                                double curQuant = neededByIngredient.get(ingredient.first + " " + ingredient.third);
                                neededByIngredient.put(ingredient.first + " " + ingredient.third, curQuant + quantity);
                                break;
                            }
                        }
                    }

                }



                double enough = 0.0;
                for (Tuple ingredient : ingredients)
                {
                    double needed = neededByIngredient.get(ingredient.first + " " +ingredient.third);
                    double have = ingredient.second;
                    if (needed == 0)
                        totIngredients ++; // Just sort of add to still penalize stuff that barely use any ingredients out of stuff we have
                    if (ingredient.second < 0.0 && needed > 0.0)
                        have = needed;
                    if (needed == have)
                    {
                        score += 100; // Perfect score for this ingredient
                        enough += 1.0;
                    }
                    else if (have < needed) // Penalize heavily for not enough
                        score += Math.max(100-(needed-have)*(100/needed)*2, 0);
                    else // Penalize min 70 for too many
                        score += Math.max(100-(have-needed)*10, 90);
                }

                if (totIngredients > 0)
                    score = score/totIngredients;
                else
                    score = 0;

                if (realTotIngredients > 0)
                    score = Math.max(score-=100/realTotIngredients, 0);
                else
                    score = 0;


                double enoughRatio = enough/realTotIngredients;

//                if (url.equals("https://www.allrecipes.com/recipe/7737/chocolate-cheese-frosting/"))
//                	System.out.println(enoughRatio + " here");


                String genre;
                if (urlToGenre.get(url) == null)
                    genre = "";
                else
                    genre = urlToGenre.get(url);

                boolean check;
                if (category.length() > 0)
                    check = genre.equals(category);
                else
                    check = true;

                if (enoughRatio > 0 && enough >= minIngredients && check)
                {
                    maxHeap.add(new UrlScore(url, enoughRatio)); // Create a new UrlScore object for each URL
                }

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ArrayList<String> toRet = new ArrayList<String>();

        for (int i = 0; i < 500; i++)
        {
            if (maxHeap.isEmpty())
                break;
            UrlScore curr = maxHeap.poll();


            toRet.add(curr.score + ", " + curr.url + "\n");

        }

        return toRet;
    }

	public static ArrayList<String> getBestFits (ArrayList<Tuple> ingredients, int minIngredients, String category, Context context) throws FileNotFoundException, IOException
	{
//        ingredients.add(new Tuple("carrot", -1.0, "count"));
//        ingredients.add(new Tuple("carrot", -1.0, "fl_oz"));


        int ingLen = ingredients.size();
        for (int i = 0; i < ingLen; i ++)
            if (ingredients.get(i).third.equals("count"))
                ingredients.add(new Tuple(ingredients.get(i).first, -1.0, "fl_oz"));

        PriorityQueue<UrlScore> maxHeap = new PriorityQueue<>(new Comparator<UrlScore>() {
            @Override
            public int compare(UrlScore o1, UrlScore o2) {
                // Max-Heap, so we reverse the comparison to put higher scores first
                return Double.compare(o2.score, o1.score); 
            }
        });
		
		HashMap<String, String> urlToGenre = new HashMap<String, String>();

        try (InputStream inputStream = context.getResources().openRawResource(R.raw.filtered_recipe_urls_with_genres);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))		{
            String line;
			
            while ((line = br.readLine()) != null) 
            {
            	String url = line.substring(0, line.indexOf(" - "));
            	String genre = line.substring(line.indexOf(" - ")+3, line.length());
            	urlToGenre.put(url, genre);
            	//System.out.println(url);
            }
		}


        try (InputStream inputStream = context.getResources().openRawResource(R.raw.cleaned_ingredients);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))		{
            String line;
            UrlScore currentUrl = new UrlScore("", 0);
            
            while ((line = br.readLine()) != null) 
            {
                // Read URL line
                String url = line.trim(); // Store the URL in a separate variable
                int score = 0; // Reset score for this URL
                
                HashMap<String, Double> neededByIngredient = new HashMap<String, Double>();
                for (Tuple ingredient : ingredients)
                	neededByIngredient.put(ingredient.first + " " + ingredient.third, 0.0);
                
                int totIngredients = 0;
                int realTotIngredients = 0;
                
                // Skip reading next lines until an empty line or EOF
                while ((line = br.readLine()) != null && !line.trim().isEmpty()) 
                {
                	totIngredients ++;
                	realTotIngredients ++;
                    String words[] = line.split(" ");
                    
                    for (Tuple ingredient : ingredients) 
                    {
                    	if (line.contains(ingredient.first)) 
                    	{
                    		// Found ingredient
                    		double quantity = Double.parseDouble(words[0]);

                    		if (words[1].equals(ingredient.third) || (words[1].equals("countable") && ingredient.third.equals("count")))
                    		{
                    			double curQuant = neededByIngredient.get(ingredient.first + " " + ingredient.third);
                    			neededByIngredient.put(ingredient.first + " " + ingredient.third, curQuant + quantity);
                    			break;
                    		}
                    	}
                    }
                    
                }
                
                
                
                double enough = 0.0;
                for (Tuple ingredient : ingredients)
                {	
                	double needed = neededByIngredient.get(ingredient.first + " " +ingredient.third);
                	double have = ingredient.second;
                	if (needed == 0)
                		totIngredients ++; // Just sort of add to still penalize stuff that barely use any ingredients out of stuff we have
                	if (ingredient.second < 0.0 && needed > 0.0)
                  		have = needed;
                	if (needed == have)
                  	{
                		score += 100; // Perfect score for this ingredient
                		enough += 1.0;
                  	}
                  	else if (have < needed) // Penalize heavily for not enough
                  		score += Math.max(100-(needed-have)*(100/needed)*2, 0);
                  	else // Penalize min 70 for too many
                    {
                        score += Math.max(100 - (have - needed) * 10, 90);
                        if (needed > 0)
                            enough += 1.0;
                    }
                }
                
                if (totIngredients > 0)
                	score = score/totIngredients;
                else
                	score = 0;
                
                if (realTotIngredients > 0)
                	score = Math.max(score-=100/realTotIngredients, 0);
                else
                	score = 0;
                

                double enoughRatio = enough/realTotIngredients;
                
//                if (url.equals("https://www.allrecipes.com/recipe/7737/chocolate-cheese-frosting/"))
//                	System.out.println(enoughRatio + " here");
                
                
                String genre;
                if (urlToGenre.get(url) == null)
                	genre = "";
                else
                	genre = urlToGenre.get(url);
                
                boolean check;
                if (category.length() > 0)
                	check = genre.equals(category);
                else
                	check = true;
                	
                if (enoughRatio > 0 && check)
                {
                    maxHeap.add(new UrlScore(url, enoughRatio)); // Create a new UrlScore object for each URL
                }
                
            }
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }

		ArrayList<String> toRet = new ArrayList<String>();
		
		for (int i = 0; i < 12; i++)
		{
			if (maxHeap.isEmpty())
				break;
			UrlScore curr = maxHeap.poll();
		

			toRet.add(curr.score + ", " + curr.url + "\n");
			
		}

		return toRet;
	}

}
