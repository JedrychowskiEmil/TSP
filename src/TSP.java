import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/*
* TPS - travelling salesman problem,
*
* Program is using genetic algorithm trying to find
* the shortest route connecting all cities.
* Pass file with witch coordinates of the cities and value
* of population you want to have in each generation
*
* Function findAnswer appends results to the file wyniki.txt
*
* */

public class TSP {

    private long[][] matrixOfDistances;
    private int[][] generation;
    private int population;
    private double crossChance = 0.8;
    private double mutationChance = 0.1;
    private int sizeOfTournament = 3;
    private int flag = 0;
    private Random random = new Random();

    public TSP(String fileName, int population) {

        ArrayList<Long> x = new ArrayList<>();
        ArrayList<Long> y = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            for (int i = 0; i < 6; i++) {
                bufferedReader.readLine();
            }
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                String[] data = input.split("\\s+");
                if (data.length < 3) break;
                x.add(Long.parseLong(data[data.length - 2]));
                y.add(Long.parseLong(data[data.length - 1]));
            }
        } catch (IOException e) {
            System.out.println("Zła nazwa pliku");
        }
        setMatrixOfDistances(x, y);

        this.population = population;
        this.generation = new int[population][x.size()];
    }

    public void findAnswer() {
        drawFirstGeneration(population, generation[0].length);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setFlag(1);
            }
        }, 1000 * 60);

        while (this.flag == 0)generation = generateNewGeneration(generation);
        timer.cancel();

        try(FileWriter fw = new FileWriter("wyniki.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(getWholeDistanceInUnit(generation[0]));
            out.println(getTravelPath(generation[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(getWholeDistanceInUnit(generation[0]));


    }

    private void setFlag(int value) {
        this.flag = value;
    }

    private String getTravelPath(int [] unit){
        StringBuilder stringBuilder = new StringBuilder(unit.length * 2);
        for (int j = 0; j < unit.length - 1; j++) {
            stringBuilder.append((unit[j] + 1) + "-");
        }
        stringBuilder.append(unit[unit.length - 1] + 1);

        return stringBuilder.toString();
    }

    private int[][] generateNewGeneration(int[][] oldGeneration) {
        int[][] newGeneration = new int[oldGeneration.length][oldGeneration[0].length];


        //Cross-breed
        for (int i = 0; i < newGeneration.length - 1; i += 2) {
            if (Math.random() <= this.crossChance) {
                newGeneration[i] = crossOver(oldGeneration[i], oldGeneration[i + 1]);
                newGeneration[i + 1] = crossOver(oldGeneration[i + 1], oldGeneration[i]);
            } else {
                newGeneration[i] = oldGeneration[i % newGeneration.length];
                newGeneration[i + 1] = oldGeneration[i + 1];
            }
        }

        //Mutate
        for (int i = 0; i < newGeneration.length; i++) {
            if (Math.random() <= this.mutationChance) {
                newGeneration[i] = mutate(newGeneration[i]);
            }
        }

        //Select best units
        int[][] wholePopulation = new int[oldGeneration.length + newGeneration.length][oldGeneration[0].length];
        int index = 0;
        for (int i = 0; i < oldGeneration.length; i++) {
            wholePopulation[index] = oldGeneration[i];
            index++;
        }

        for (int i = 0; i < newGeneration.length; i++) {
            wholePopulation[index] = newGeneration[i];
            index++;
        }

        newGeneration = tournamentSelection(wholePopulation);

        return newGeneration;
    }

    private int[][] tournamentSelection(int[][] wholePopulation) {
        int[][] champions = new int[wholePopulation.length / 2][wholePopulation[0].length];
        Random random = new Random();

        //Best goes to champions without going thought selection
        champions[0] = wholePopulation[0];

        //Do small tournaments to pick up units to be selected
        for (int i = 1; i < wholePopulation.length / 2; i++) {
            int smallest = random.nextInt(wholePopulation.length);
            for (int j = 0; j < sizeOfTournament - 1; j++) {
                int draw = random.nextInt(wholePopulation.length);
                if (getWholeDistanceInUnit(wholePopulation[draw]) < getWholeDistanceInUnit(wholePopulation[smallest])) {
                    smallest = draw;
                }
            }
            champions[i] = wholePopulation[smallest];
        }

        //Find new best unit
        int newBest = 0;
        for (int i = 1; i < champions.length; i++) {
            if (getWholeDistanceInUnit(champions[i]) < getWholeDistanceInUnit(champions[newBest])) {
                newBest = i;
            }
        }


        //Place best unit (elite) on the 1st pos
        int[] tmp = champions[0];
        champions[0] = champions[newBest];
        champions[newBest] = tmp;


        return champions;
    }

    private void drawFirstGeneration(int size, int numOfGenes) {
        int[][] generation = new int[size][numOfGenes];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < numOfGenes; j++) {
                generation[i][j] = j;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < numOfGenes; j++) {
                int newRandom = random.nextInt(numOfGenes);
                int tmp = generation[i][j];
                generation[i][j] = generation[i][newRandom];
                generation[i][newRandom] = tmp;
            }
        }
        //System.out.println(Arrays.deepToString(generation));
        this.generation = generation;
    }

    private long getDistance(int city1, int city2) {
        if (city2 > city1) {
            return getDistance(city2, city1);
        } else {
            return matrixOfDistances[city1][city2];
        }
    }

    private long getWholeDistanceInUnit(int[] unit) {
        long distance = 0;
        for (int i = 0; i < unit.length - 1; i++) {
            distance += getDistance(unit[i], unit[i + 1]);
        }
        distance += getDistance(unit[unit.length - 1], unit[0]);

        return distance;
    }

    private void setMatrixOfDistances(ArrayList x, ArrayList y) {
        this.matrixOfDistances = new long[x.size()][];

        for (int i = 1; i <= x.size(); i++) {
            this.matrixOfDistances[i - 1] = new long[i];
        }

        for (int i = 0; i < matrixOfDistances.length; i++) {
            for (int j = 0; j < matrixOfDistances[i].length; j++) {
                matrixOfDistances[i][j] = compileManhattanDistance((long) x.get(i), (long) x.get(j)) + compileManhattanDistance((long) y.get(i), (long) y.get(j));

                //  show matrix
                //System.out.print(matrixOfDistances[i][j]);System.out.print(", ");
            }
            // System.out.println(); //end of show matrix
        }

    }

    private int[] mutate(int[] unit) {
        int numOfPeaks = 3;
        int[] peaks = new int[numOfPeaks];
        for (int i = 0; i < peaks.length; i++) {
            peaks[i] = random.nextInt(unit.length);
        }
        for (int i = 0; i < peaks.length - 1; i++) {
            int tmp = unit[peaks[i]];
            unit[peaks[i]] = unit[peaks[i + 1]];
            unit[peaks[i + 1]] = tmp;
        }
        //System.out.println(Arrays.toString(unit));
        return unit;
    }

    private int[] crossOver(int[] parent1, int[] parent2) {
        int[] child = new int[parent1.length];
        Arrays.fill(child, -2);

        //Wylosuj granice
        int lowerLimit = random.nextInt(parent1.length - 1);
        int higherLimit = random.nextInt(parent1.length - 1 - lowerLimit) + lowerLimit + 1;

        //Przekopiuj rodzica w zakresie do dziecka
        for (int i = lowerLimit; i <= higherLimit; i++) {
            child[i] = parent1[i];
        }

        //Znajdz miejsce na liczby z drugiego rodzica w dziecku i wstaw
        for (int i = lowerLimit; i <= higherLimit; i++) {
            if (parent1[i] != parent2[i]) {
                int index = findIndexOf(parent1[i], parent2);

                while (child[index] != -2 && findIndexOf(parent2[i], child) == -1) {
                    index = findIndexOf(parent1[index], parent2);
                }
                //upewnij sie ze w dziecku jeszcze nie ma tej wartosci i wstaw
                if (findIndexOf(parent2[i], child) == -1) {
                    child[index] = parent2[i];
                }
            }
        }

        //Przenies pozostale
        for (int i = 0; i < child.length; i++) {
            if (child[i] == -2) child[i] = parent2[i];
        }

//        System.out.println(Arrays.toString(parent1));
//        System.out.println(Arrays.toString(parent2));
//        System.out.println(Arrays.toString(child));

        return child;
    }

    //Na potrzeby crossOver
    private int findIndexOf(int searchFor, int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (searchFor == array[i]) return i;
        }
        return -1;
    }


    private long compileManhattanDistance(long x, long y) {
        if (x < y) {
            return compileManhattanDistance(y, x);
        } else {
            return x - y;
        }
    }

    //Bug checking
    private boolean spr(int[][] sp) {


        for (int i = 0; i < sp.length; i++) {
            int sum = 10296;
            for (int j = 0; j < sp[0].length; j++) {
                sum -= sp[i][j];
            }
            if (sum != 0) {
                System.out.println("length " + sp.length);
                System.out.println(sum);
                System.out.println(i);
                return true;
            }
        }
        return false;
    }

    private boolean spr(int[] sp) {


        int sum = 10296;
        for (int j = 0; j < sp.length; j++) {
            sum -= sp[j];
        }
        if (sum != 0) {
            System.out.println("small check");
            System.out.println(sum);
            return true;
        }

        return false;
    }

}
