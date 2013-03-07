/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

/**
 *
 * @author Sonak Patel, sp293
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static final int pool = 500;
    private static final int range = 545;
    static float crossRate = 0.7f;
    static float mutationRate = .02f;
    static float probSum = 0;
    static Random rand = new Random();
    private static ArrayList<String> currentPop = new ArrayList<String>();
    private static float[] fitnessPop = new float[pool];
    private static HashMap<Float, Float> goalPlots = new HashMap<Float, Float>();
    private static ArrayList<Float> probabilityPop = new ArrayList<Float>();
    static float fitnessSum = 0;

    /**
     * 
     * @param args
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        int gen = 0;
        goalPlots = getData();

        System.out.println(gen);
        if (currentPop.isEmpty()) {
            currentPop.addAll(genPopulation());
        }

        while (true) {
            fitnessPop = getFitness(currentPop.toArray(new String[currentPop.size()]), goalPlots);
            System.out.println("Current Fitest: ");
            System.out.println(currentPop.get(selectFittest(fitnessPop)[0]));
            isBest(gen);

            calculateRoulette(fitnessPop);
            genNewPopulation();
            gen++;
        }
    }

    /**
     *
     * @return
     */
    private static ArrayList<String> genPopulation() {
        ArrayList<String> newPop = new ArrayList<String>();
        for (int i = 0; i < pool; i++) {
            String params = "";
            String a = Float.toString(10 * rand.nextFloat());
            String b = Float.toString(8 * rand.nextFloat());
            String c = Float.toString(7 * rand.nextFloat());
            String d = Float.toString(5 * rand.nextFloat());
            String e = Float.toString(1 * rand.nextFloat());

            params = params.concat(a + "," + b + "," + c + "," + d + "," + e);
            newPop.add(params);
        }
        return newPop;
    }

    /**
     *
     * @param population
     * @param goalPlots
     * @return
     */
    private static float[] getFitness(String[] population, HashMap<Float, Float> goalPlots) {
        float[] popFitness = new float[population.length];
        fitnessSum = 0;
        int i = 0;
        for (String coefficient : population) {
            popFitness[i] = fxFitness(coefficient, goalPlots);
            fitnessSum += popFitness[i];
            i++;
        }
        return popFitness;
    }

    /**
     *
     * @param coefficient
     * @param goalPlots
     * @return
     */
    private static float fxFitness(String coefficient, HashMap<Float, Float> goalPlots) {
        Stack<Float> samplePoints = new Stack<Float>();
        Float[] coords = {-900f,-661.0991379f,-450f,-75f,112.5f,300f};
        samplePoints.addAll(Arrays.asList(coords));
        
        float score = 0;
        for (Float point : samplePoints) {
            float a = goalPlots.get(point);
            float b = generateFx(coefficient, point);
            float delta = Math.abs((a - b));
            if (delta == 0) {
                System.exit(0);
            }
            score += 1 / delta;
        }
        return score;
    }

    /**
     *
     * @param data
     * @return
     */
    public static float avgValue(Stack<Float> data) {
        float sum = 0f;
        int size = data.size();
        while (!data.isEmpty()) {
            sum += data.pop();
        }
        float a = sum / size;
        return a;
    }

    /**
     *
     * @param fitnessPop
     */
    public static void calculateRoulette(float[] fitnessPop) {
        float previous = 0;
        probabilityPop.clear();
        for (int i = 0; i < fitnessPop.length; i++) {
            probabilityPop.add((fitnessPop[i] / fitnessSum) + previous);
            previous = probabilityPop.get(i);
        }
    }

    /**
     *
     * @param fitnessPop
     * @return
     */
    public static int[] selectFittest(float[] fitnessPop) {
        int[] fittest = {0, 0};
        for (int i = 0; i < fitnessPop.length; i++) {
            if (fitnessPop[i] < fitnessPop[fittest[0]]) {
                fittest[0] = i;
            }
        }
        return fittest;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private static HashMap<Float, Float> getData() throws IOException {
        HashMap<Float, Float> numbers = new HashMap<Float, Float>();
        String[] pair;
        File file = new File("data.csv");

        BufferedReader bufRdr = new BufferedReader(new FileReader(file));
        String line = null;
        int row = 0;

        //read each line of text file
        while ((line = bufRdr.readLine()) != null && row < range) {
            pair = line.split(",");
            numbers.put(Float.parseFloat(pair[0]), Float.parseFloat(pair[1]));
            row++;
        }
        return numbers;
    }

    /**
     *
     * @return
     * @throws IOException
     */
//    private static HashMap<Float, Float> getStandard() throws IOException {
//        HashMap<Float, Float> numbers = new HashMap<Float, Float>();
//
//        String[] pair;
//        File file = new File("data.csv");
//
//        BufferedReader bufRdr = new BufferedReader(new FileReader(file));
//        String line = null;
//        int row = 0;
//
//        while ((line = bufRdr.readLine()) != null && row < range) {
//            pair = line.split(",");
//            numbers.put(Float.parseFloat(pair[0]), 0f);
//            row++;
//        }
//        return numbers;
//    }

    /**
     *
     */
    private static void genNewPopulation() {
        Stack<String> newPop = new Stack<String>();
        Stack<String> parents = new Stack<String>();
        float currProb, prevProb;

        while (newPop.size() < pool) {
            do {
                float seed = rand.nextFloat();
                prevProb = 0;
                for (int i = 0; i < currentPop.size(); i++) {
                    currProb = probabilityPop.get(i);
                    if (seed > prevProb && seed <= currProb) {
                        parents.add(currentPop.get(i));
                        currentPop.remove(i);
                        probabilityPop.remove(i);
                        break;
                    }
                    prevProb = currProb;
                }
            } while (parents.size() < 2);
            newPop.addAll(genOffspring(parents));
            newPop.addAll(parents);
            parents.clear();
        }
        currentPop.clear();
        currentPop.addAll(newPop);
    }

    /**
     *
     * @param parents
     * @return
     */
    private static Stack<String> genOffspring(Stack<String> parents) {
        Stack<String> children = new Stack<String>();
        for (int i = 0; i < parents.size(); i++) {
            if (rand.nextFloat() < crossRate) {
                //do crossover
                children.push(offspring(parents));
            } else {
                //copy parents into new pop
                children.addAll(parents);
            }
        }
        return children;
    }

    /**
     *
     * @param parents
     * @return
     */
    private static String offspring(Stack<String> parents) {
        int crossover = rand.nextInt(4);
        float[] chromo1 = splitParameters(parents.get(0));
        float[] chromo2 = splitParameters(parents.get(1));
        String child = chromo1[0] + ",";

        int x = 1;
        while (x <= crossover) {
            child += Float.toString(chromo1[x++]) + ",";
        }
        while (x > crossover && x < 5) {
            child += Float.toString(chromo2[x++]);
            if (x < 5) {
                child += ",";
            }
        }
        return mutate(child);
    }

    /**
     *
     * @param child
     * @return
     */
    public static String mutate(String child) {
        if (rand.nextFloat() <= mutationRate) {
            float[] preMutate = splitParameters(child);
            int rand1 = rand.nextInt(5);
            int rand2 = rand.nextInt(5);

            float temp = preMutate[rand1];
            preMutate[rand1] = preMutate[rand2];
            preMutate[rand2] = temp;

            child = joinString(preMutate);
        }
        return child;
    }

    /**
     *
     * @param coefficient
     * @param x
     * @return
     */
    private static float generateFx(String coefficient, float x) {
        float[] coeffs = splitParameters(coefficient);
        return (coeffs[0] + (coeffs[1] * x) + (coeffs[2] * x * x) + (coeffs[3] * x * x * x) + (coeffs[4] * x * x * x * x));
    }

    /**
     *
     * @param coefficients
     * @return
     */
    private static float[] splitParameters(String coefficients) {
        float[] coeffs = new float[5];

        String[] parts = coefficients.split(",");
        for (int i = 0; i < parts.length; i++) {
            coeffs[i] = Float.parseFloat(parts[i].trim());
        }
        return coeffs;
    }

    /**
     *
     * @param coefficients
     * @return
     */
    private static String joinString(float[] coefficients) {
        String params = "";
        for (int i = 0; i < 5; i++) {
            params = params.concat(Float.toString(coefficients[i]));
            if (i != 4) {
                params += (",");
            }
        }
        return params;
    }

    /**
     *
     * @param gen
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void isBest(int gen) throws FileNotFoundException, IOException {
        String filename = "BestSolutions.txt";
        File file = new File(filename);
        boolean exists = file.exists();
        if (gen == 0 && exists) {
            file.delete();
        }

        try {
            BufferedWriter bw;

            if (exists) {
                bw = new BufferedWriter(new FileWriter(filename, true));
            } else {
                bw = new BufferedWriter(new FileWriter(filename));
            }
            int fittest = selectFittest(fitnessPop)[0];
            bw.write(   "" + currentPop.get(fittest) + "\t\t\tFitness: " +
                        (1000 * (fitnessPop[fittest] * 10) / 10) +
                        ("\t Fittest - " + fitnessPop[fittest] + "\tSum\t" + fitnessSum)
                    );
            bw.newLine();
            bw.close();

        } catch (Exception e) {
            System.out.print("Error: " + e);
        }
    }
}
