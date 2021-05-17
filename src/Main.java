import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        TSP tsp;

        //each findAnswer() takes a minute
        for (int i = 0; i < 2; i++) {
            tsp = new TSP("bier127.txt", 280);
            tsp.findAnswer();
        }
        for (int i = 0; i < 2; i++) {
            tsp = new TSP("pr144.txt", 330);
            tsp.findAnswer();
        }





    }

}
