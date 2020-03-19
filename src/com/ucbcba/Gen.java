package com.ucbcba;

import java.util.*;
import java.util.stream.Collectors;

public class Gen {

    private static final float CROSS_PROBABILITY = (float) 0.7;
    private static final float MUTATION_PROBABILITY = (float) 0.001;
    private static final float CONVERGENCE_PERCENTAGE = (float) 1.0;
    private static final int POPULATION_SIZE = 1000;
    private static final int NUMBER_OF_CITIES = 10;
    private static final int CHROMOSOME_SIZE = NUMBER_OF_CITIES + 1;
    private static final int[][] DISTANCES = {{0, 5, 9, 1, 8, 5, 1, 4, 4, 2}, {5, 0, 8, 6, 7, 4, 2, 6, 5, 3},
            {9, 8, 0, 4, 2, 6, 3, 5, 2, 1}, {1, 6, 4, 0, 3, 5, 5, 3, 3, 4}, {8, 7, 2, 3, 0, 4, 2, 2, 4, 2},
            {5, 4, 6, 5, 4, 0, 5, 3, 2, 3}, {1, 2, 3, 5, 2, 5, 0, 1, 4, 4}, {4, 6, 5, 3, 2, 3, 1, 0, 3, 5},
            {4, 5, 2, 3, 4, 2, 4, 3, 0, 3}, {2, 3, 1, 4, 2, 3, 4, 5, 3, 0}};

    public int getCharPosition(char character) {
        return character - 65;
    }

    public int getDistance(char a, char b) {
        return DISTANCES[getCharPosition(a)][getCharPosition(b)];
    }

    public int fitness(String state) {
        int fit = 0;
        for (int i = 0, stateLength = state.length(); i < stateLength - 1; i++) {
            fit += getDistance(state.charAt(i), state.charAt(i + 1));
        }
        return 60 - fit;
    }

    public String convertTravelToString(List<Character> cities) {
        char[] citiesAsCharArray = new char[CHROMOSOME_SIZE];
        for (int i = 0; i < CHROMOSOME_SIZE; i++)
            citiesAsCharArray[i] = cities.get(i);
        return new String(citiesAsCharArray);
    }

    public String generateRandomTravel() {
        char city = 'B';
        List<Character> cities = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CITIES - 1; i++) {
            cities.add(city);
            city++;
        }
        Collections.shuffle(cities);
        cities.add(0, 'A');
        cities.add('A');
        return convertTravelToString(cities);
    }

    public List<Pair<String, Integer>> generatePopulation() {
        List<Pair<String, Integer>> population = new ArrayList<>();
        String indivudual;
        while (population.size() != POPULATION_SIZE) {
            indivudual = generateRandomTravel();
            population.add(new Pair<>(indivudual, fitness(indivudual)));
        }
        return population;
    }

    public Map<Integer, Integer> createMapWithHowManyTimeFitnessAppears(List<Pair<String, Integer>> population) {
        // Map<fitnessValue, number of times the fitness value appears>
        Map<Integer, Integer> fitnessFrequency = new TreeMap<>();
        for (Pair<String, Integer> p : population) {
            if (fitnessFrequency.containsKey(p.getValue()))
                fitnessFrequency.put(p.getValue(), fitnessFrequency.get(p.getValue()) + 1);
            else
                fitnessFrequency.put(p.getValue(), 1);
        }
        return fitnessFrequency;
    }

    public boolean doesItConverge(List<Pair<String, Integer>> population) {
        final int elementsThatMustHaveTheSameFitnessValue = (int) (population.size() * CONVERGENCE_PERCENTAGE);
        Map<Integer, Integer> fitnessFrequency = createMapWithHowManyTimeFitnessAppears(population);

        for (Integer timesTheFitnessValueAppears : fitnessFrequency.values()) {
            if (timesTheFitnessValueAppears >= elementsThatMustHaveTheSameFitnessValue)
                // A fitness has appeared the required number of times
                return true;
        }
        return false;
    }

    public List<String> rouletteWheelSelection(List<Pair<String, Integer>> population) {
        List<String> flatteredPopulation = new ArrayList<>(Collections.emptyList());
        for (Pair<String, Integer> individual : population) {
            for (int i = 0; i < individual.getValue(); i++)
                flatteredPopulation.add(individual.getKey());
        }
        return flatteredPopulation;
    }

    public List<Character> removeHomeCity(List<Character> state) {
        state.remove(0);
        state.remove(state.size() - 1);
        return state;
    }

    public String crossingFirstSon(String father, String mother) {
        List<Character> fatherAsAList = father.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        List<Character> motherAsAList = mother.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        List<Character> firstSonAsList = new ArrayList<>();
        int isPair = 0;
        char homeCity = father.charAt(0);
        fatherAsAList = removeHomeCity(fatherAsAList);
        motherAsAList = removeHomeCity(motherAsAList);

        firstSonAsList.add(homeCity);
        firstSonAsList.add(fatherAsAList.get(0));
        fatherAsAList.remove(0);

        if (firstSonAsList.get(1) == motherAsAList.get(1)) {
            firstSonAsList.add(fatherAsAList.get(0));
            motherAsAList.remove(fatherAsAList.get(0));
            fatherAsAList.remove(0);
            // motherAsAList.remove(firstSon[2]);

            firstSonAsList.add(motherAsAList.get(2));
            fatherAsAList.remove(motherAsAList.get(2));
            motherAsAList.remove(2);
            // fatherAsAList.remove(firstSon[3]);
        } else {
            firstSonAsList.add(motherAsAList.get(1));
            motherAsAList.remove(1);
            fatherAsAList.remove(firstSonAsList.get(2));
        }
        motherAsAList.remove(firstSonAsList.get(1));

        while (firstSonAsList.size() < CHROMOSOME_SIZE - 1) {
            if (isPair % 2 == 0) {
                firstSonAsList.add(fatherAsAList.get(0));
                fatherAsAList.remove(0);
                motherAsAList.remove(firstSonAsList.get(firstSonAsList.size() - 1));
            } else {
                firstSonAsList.add(motherAsAList.get(0));
                motherAsAList.remove(0);
                fatherAsAList.remove(firstSonAsList.get(firstSonAsList.size() - 1));
            }
            isPair++;
        }
        firstSonAsList.add(homeCity);
        return convertTravelToString(firstSonAsList);
    }

    public String crossingFirstSon2(String father, String mother) {
        Random random = new Random();
        int l1, l2, inferiorLimit, superiorLimit;

        List<Character> fatherAsAList = father.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        List<Character> motherAsAList = mother.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        char initialCity = fatherAsAList.get(0);
        fatherAsAList = removeHomeCity(fatherAsAList);
        motherAsAList = removeHomeCity(motherAsAList);

        do {
            l1 = random.nextInt(fatherAsAList.size());
            l2 = random.nextInt(fatherAsAList.size());
        } while (l2 == l1 || l1 == 0 && l2 == (fatherAsAList.size() - 1)
                || l1 == (fatherAsAList.size() - 1) && l2 == 0);
        inferiorLimit = Math.min(l1, l2);
        superiorLimit = Math.max(l1, l2);
        char[] firstSon = new char[CHROMOSOME_SIZE];
        firstSon[0] = initialCity;
        firstSon[CHROMOSOME_SIZE - 1] = initialCity;
        for (int i = inferiorLimit; i <= superiorLimit; i++) {
            firstSon[i + 1] = fatherAsAList.get(i);
            motherAsAList.remove(new Character(firstSon[i + 1]));
        }
        for (int index = 0; index < CHROMOSOME_SIZE; index++) {
            if (firstSon[index] == Character.MIN_VALUE) {
                firstSon[index] = motherAsAList.remove(0);
            }
        }
        return new String(firstSon);
    }

    public String crossingSecondSon2(String father, String mother) {
        Random random = new Random();
        int l1, l2, inferiorLimit, superiorLimit;

        List<Character> fatherAsAList = father.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        List<Character> motherAsAList = mother.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        char initialCity = fatherAsAList.get(0);
        fatherAsAList = removeHomeCity(fatherAsAList);
        motherAsAList = removeHomeCity(motherAsAList);

        do {
            l1 = random.nextInt(fatherAsAList.size());
            l2 = random.nextInt(fatherAsAList.size());
        } while (l2 == l1 || l1 == 0 && l2 == (fatherAsAList.size() - 1)
                || l1 == (fatherAsAList.size() - 1) && l2 == 0);
        inferiorLimit = Math.min(l1, l2);
        superiorLimit = Math.max(l1, l2);
        char[] firstSon = new char[CHROMOSOME_SIZE];
        firstSon[0] = initialCity;
        firstSon[CHROMOSOME_SIZE - 1] = initialCity;
        for (int i = inferiorLimit; i <= superiorLimit; i++) {
            firstSon[i + 1] = motherAsAList.get(i);
            fatherAsAList.remove(new Character(firstSon[i + 1]));
        }
        for (int index = 0; index < CHROMOSOME_SIZE; index++) {
            if (firstSon[index] == Character.MIN_VALUE) {
                firstSon[index] = fatherAsAList.remove(0);
            }
        }
        return new String(firstSon);
    }

    public String crossingSecondSon(String father, String mother) {
        List<Character> fatherAsAList = father.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        List<Character> motherAsAList = mother.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        char[] secondSon = new char[CHROMOSOME_SIZE];
        int secondSonLimit, isPair = 0;
        char firstCity = father.charAt(0);
        fatherAsAList = removeHomeCity(fatherAsAList);
        motherAsAList = removeHomeCity(motherAsAList);

        secondSon[0] = firstCity;
        secondSon[CHROMOSOME_SIZE - 1] = firstCity;

        secondSon[CHROMOSOME_SIZE - 2] = fatherAsAList.get(fatherAsAList.size() - 1);
        fatherAsAList.remove(fatherAsAList.size() - 1);

        if (secondSon[CHROMOSOME_SIZE - 2] == motherAsAList.get(motherAsAList.size() - 2)) {
            secondSon[CHROMOSOME_SIZE - 4] = motherAsAList.get(motherAsAList.size() - 3);
            motherAsAList.remove(motherAsAList.size() - 3);
            fatherAsAList.remove(new Character(secondSon[CHROMOSOME_SIZE - 4]));

            secondSon[CHROMOSOME_SIZE - 3] = fatherAsAList.get(fatherAsAList.size() - 1);
            fatherAsAList.remove(fatherAsAList.size() - 1);
            motherAsAList.remove(new Character(secondSon[CHROMOSOME_SIZE - 3]));
            secondSonLimit = CHROMOSOME_SIZE - 4;
        } else {
            secondSon[CHROMOSOME_SIZE - 3] = motherAsAList.get(motherAsAList.size() - 2);
            motherAsAList.remove(motherAsAList.size() - 2);
            fatherAsAList.remove(new Character(secondSon[CHROMOSOME_SIZE - 3]));
            secondSonLimit = CHROMOSOME_SIZE - 3;
        }
        motherAsAList.remove(new Character(secondSon[CHROMOSOME_SIZE - 2]));

        for (int i = 1; i < secondSonLimit; i++) {
            if (isPair % 2 == 0) {
                secondSon[i] = fatherAsAList.get(0);
                fatherAsAList.remove(0);
                motherAsAList.remove(new Character(secondSon[i]));
            } else {
                secondSon[i] = motherAsAList.get(0);
                motherAsAList.remove(0);
                fatherAsAList.remove(new Character(secondSon[i]));
            }
            isPair++;
        }
        return new String(secondSon);
    }

    public List<String> crossing(String father, String mother) {
        List<String> descendents = new ArrayList<>(Collections.emptyList());
        descendents.add(crossingFirstSon2(father, mother));
        descendents.add(crossingSecondSon2(father, mother));
        return descendents;
    }

    public int probabilityAsInteger() {
        int probability = 1;
        float aux = MUTATION_PROBABILITY;
        while (aux < 1) {
            probability *= 10;
            aux *= 10.0;
        }
        probability = (probability / ((int) aux));
        return probability;
    }

    public String swapArrayAtRandomPosition(char[] array) {
        Random random = new Random();
        int positionToMutate = random.nextInt(CHROMOSOME_SIZE - 3) + 1;
        char temp = array[positionToMutate];
        array[positionToMutate] = array[positionToMutate + 1];
        array[positionToMutate + 1] = temp;
        return new String(array);
    }

    public boolean hasToMutate() {
        Random random = new Random();
        int probability = probabilityAsInteger();
        int randomNumber = random.nextInt(probability);
        // arbitrary number. because the probability is number between 1 and 1000
        return randomNumber == 10;
    }

    public String mutate(String state) {
        char[] stateAsCharArray = state.toCharArray();
        String mutatedState = state;
        if (hasToMutate()) {
            mutatedState = swapArrayAtRandomPosition(stateAsCharArray);
        }
        return mutatedState;
    }

    public List<Pair<String, Integer>> sortAccordingToFitnessAscending(List<Pair<String, Integer>> population) {
        // the lower fitness is the best
        population.sort((o1, o2) -> (o2.getValue().compareTo(o1.getValue())));
        return population;
    }

    public List<Pair<String, Integer>> takeTheFirstPopulationSizeElements(List<Pair<String, Integer>> population) {
        return population.subList(0, POPULATION_SIZE);
    }

    public List<Pair<String, Integer>> reducePopulation(List<Pair<String, Integer>> population) {
        List<Pair<String, Integer>> reducedPopulation = new ArrayList<>();
        population = sortAccordingToFitnessAscending(population);
        reducedPopulation = takeTheFirstPopulationSizeElements(population);
        return reducedPopulation;
    }

    public int numberOfCrosses() {
        // number of crosses is calculated according to cross probability
        return (int) (POPULATION_SIZE * CROSS_PROBABILITY) / 2;
    }

    public String simpleGeneticAlgorithm() {
        List<Pair<String, Integer>> population = generatePopulation();
        List<Pair<String, Integer>> descendents = new ArrayList<>();
        List<String> rouletteWheelSelection;
        int numberOfCrosses;
        Random random = new Random();
        String I1, I2, D1, D2;
        int skewedRouletteLength, generations = 0;
        boolean doesConverge = false;

        while (!doesConverge) {
            descendents.clear();
            numberOfCrosses = numberOfCrosses();
            rouletteWheelSelection = rouletteWheelSelection(population);
            skewedRouletteLength = rouletteWheelSelection.size();

            while (numberOfCrosses != 0) {
                // Select the two individuals that will cross
                I1 = rouletteWheelSelection.get(random.nextInt(skewedRouletteLength));
                I2 = rouletteWheelSelection.get(random.nextInt(skewedRouletteLength));

                // Cross the individuals
                List<String> crossResult = crossing(I1, I2);

                // Obtain their descendents
                D1 = crossResult.get(0);
                D2 = crossResult.get(1);

                // Mutate the descendents
                D1 = mutate(D1);
                D2 = mutate(D2);

                // Insert the descendents into the descenders population
                descendents.add(new Pair<>(D1, fitness(D1)));
                descendents.add(new Pair<>(D2, fitness(D2)));

                numberOfCrosses--;
            }
            // Join old population (population) with the new population (descendents)
            population.addAll(descendents);
            population = reducePopulation(population);
            generations++;
            if (doesItConverge(population))
                doesConverge = true;
        }
        System.out.println("Generaciones necesarias: " + generations);
        System.out.println(population.get(0).getKey() + "=" + (-population.get(0).getValue() + 60));
        // Return the best chromosome
        return Collections.max(population, Comparator.comparing(Pair::getValue)).getKey();
    }
}
