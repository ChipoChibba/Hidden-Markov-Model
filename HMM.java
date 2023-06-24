import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Chipo Chibbamulilo
 * @author Chikwanda Chisha
 * writing code for the training part of the assignment
 * Building the HMM model by reading from files and inputing
 * data and scores in maps
 */
public class HMM {


    String start;
    double unseenVal;
    ArrayList<Map<String, String>> backtrack;

    //map that keeps the observation scores for a particular state
    Map<String, Map<String, Double>> observation;

    //this is a map that keeps transition scores to next state from a current state
    Map<String, Map<String, Double>> transition;


    //    making the HMM model
    HMM(String sentenceInput, String tagsInput) throws IOException {
        //instantiate variables
        start = "#";
        backtrack = new ArrayList<>();
        observation = new HashMap<>();
        transition = new HashMap<>();
        unseenVal = -100;

        BufferedReader input = null;
        BufferedReader tagReader = null;

        //opening files as input to our HMM
        try{
            input = new BufferedReader(new FileReader(sentenceInput));
            tagReader = new BufferedReader(new FileReader(tagsInput));
        }

        catch(FileNotFoundException e){
            System.err.println(e.getMessage());
        }
        try {

            String sentence, tag;

            while ((sentence = input.readLine()) != null) {
                if((tag = tagReader.readLine()) == null){
                    throw new Exception("One of the files is null");
                }

                //ensuring that tagReader is also not empty/ we can throw an exception
                else {

                    //split on punctuation and white space
                    String[] observations = sentence.toLowerCase().split(" ");
                    String[] tagArray = tag.split(" ");

                    //filling the observation graph
                    int j = 0;
                    for (String s : tagArray) {
                        //initial occurrence of the tag
                        if (!observation.containsKey(s)) {
                            observation.put(s, new HashMap<>());
                            observation.get(s).put(observations[j], 1.0);
                        }
                        //if observation does not exist
                        else if (!observation.get(s).containsKey(observations[j])) {
                            observation.get(s).put(observations[j], 1.0);
                        }

                        //if observation exists
                        else {
                            Double wordFreq = observation.get(s).get(observations[j]);
                            observation.get(s).put(observations[j], wordFreq + 1);
                        }
                        //increment of j
                        j++;
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            try{
                //closing files
                input.close();
                tagReader.close();
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }

        //opening the files once more to build the transitions graph(technically a map)
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(tagsInput));
        }
        catch(FileNotFoundException e){
            System.err.println(e.getMessage());
        }

        try {
            String Tag;
            while ((Tag = in.readLine()) != null) {
                String[] pieces = Tag.split(" ");

                for (int i = 0; i < pieces.length; i++) {
                    //dealing with the start entry
                    if (i == 0) {
                        if (!transition.containsKey(start)) {
                            transition.put(start, new HashMap<>());
                            transition.get(start).put(pieces[i], 1.0);
                        }
                        //if observation does not exist
                        else if (!transition.get(start).containsKey(pieces[i])) {
                            transition.get(start).put(pieces[i], 1.0);
                        }
                        //if observation exists
                        else {
                            Double startFreq = transition.get(start).get(pieces[0]);
                            transition.get(start).put(pieces[0], startFreq + 1);
                        }
                    }

                    if (i == pieces.length - 1) {
                        //initial occurrence of tag
                        if (!transition.containsKey(pieces[i])) {
                            transition.put(pieces[i], new HashMap<>());
                            transition.get(pieces[i]).put(pieces[i], 1.0);
                        }
                        //if observation does not exist
                        else if (!transition.get(pieces[i]).containsKey(pieces[i])) {
                            transition.get(pieces[i]).put(pieces[i], 1.0);

                        }
                        //if observation exists
                        else {
                            double freq = transition.get(pieces[i]).get(pieces[i]);
                            transition.get(pieces[i]).put(pieces[i], freq + 1);
                        }
                    }
                    else {
                        //initial occurrence of tag
                        if (!transition.containsKey(pieces[i])) {
                            transition.put(pieces[i], new HashMap<>());
                            transition.get(pieces[i]).put(pieces[i + 1], 1.0);
                        }

                        //if observation does not exist
                        else if (!transition.get(pieces[i]).containsKey(pieces[i + 1])) {
                            transition.get(pieces[i]).put(pieces[i + 1], 1.0);
                        }

                        //if observation exists
                        else {
                            Double tagFreq = transition.get(pieces[i]).get(pieces[i + 1]);
                            transition.get(pieces[i]).put(pieces[i + 1], tagFreq + 1);
                        }
                    }
                }
            }
        }
        finally {
            try {
                //closing file
                in.close();
            }
            catch (IOException e){
                System.err.println(e.getMessage());
            }
        }

        //calculating probability scores for observations
        for (String t : observation.keySet()) {
            int totalObs = 0;

            //calculating total number of observations for a tag
            for (String i : observation.get(t).keySet()) {
                totalObs += observation.get(t).get(i);
            }

            //calculating the frequency of the observation
            for (String i : observation.get(t).keySet()) {
                double probability = observation.get(t).get(i) / totalObs;
                observation.get(t).put(i, Math.log(probability));
            }
        }

        //calculating probability scores for the next tagReader/ states
        for (String t : transition.keySet()) {
            int totalTags = 0;

            //calculating total number of observations for a tag
            for (String i : transition.get(t).keySet()) {
                totalTags += transition.get(t).get(i);
            }

            //calculating the frequency of the observation
            for (String i : transition.get(t).keySet()) {
                double probability = transition.get(t).get(i) / totalTags;
                transition.get(t).put(i, Math.log(probability));
            }
        }
    }

    /**
     * This method reads a file and passes the array it generates to Viterbi
     * @param pathname the file to be read
     * @throws IOException from Viterbi
     */

    public void reading(String pathname) throws IOException {
        BufferedReader input = null;

        try{//opening new file
            input = new BufferedReader(new FileReader(pathname));
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
        try{
            String line;
            while ((line = input.readLine()) != null) {
                String[] pieces = line.split(" ");
                Viterbi(pieces);
            }
        }
        finally{
            try{//closing files
                input.close();
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }
    }
    /**
     *
     * @param pieces an array
     * @return  an arraylist which is the path
     * @throws IOException
     */

    public ArrayList<String> Viterbi(String[] pieces) throws IOException {
        Set<String> currStates = new HashSet<>();
        Map<String, Double> currScores = new HashMap<>();
        backtrack = new ArrayList<>();
        currStates.add(start); // adding start to the set
        currScores.put(start, 0.0);

        //looping over the pieces arraylist
        for (int i = 0; i < pieces.length; i++) {
            Map<String, String> pred = new HashMap<>();
            Set<String> nextStates = new HashSet<>();
            Map<String, Double> nextScores = new HashMap<>();

            backtrack.add(pred);

            for (String currState : currStates) {

                for (String nextState : transition.get(currState).keySet()) {
                    nextStates.add(nextState);

                    if(transition.containsKey(currState)) {

                        double nextScore = currScores.get(currState) + transition.get(currState).get(nextState);

                        //add unseen if val is not present in observation.get(nextstate).keySet() or
                        //add the observation score

                        nextScore += observation.get(nextState).getOrDefault(pieces[i].toLowerCase(), unseenVal);

                        if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                            nextScores.put(nextState, nextScore);
                            backtrack.get(i).put(nextState, currState); // updating pred
                        }
                    }
                }
            }
            currStates = nextStates;
            currScores = nextScores;
        }


        // Getting the state with the highest Score in currScore
        double max = 1;
        String finalState = "";
        for (String state : currScores.keySet()) {
            if (max > 0 || currScores.get(state) > max) {
                finalState = state;
                max = currScores.get(finalState);
            }
        }

        //instantiate arraylist of strings for the
        ArrayList<String> path = new ArrayList<>();

        //backtracking
        int i= backtrack.size()-1;

        //add the highest final state found
        path.add(finalState);

        while(i > 0) {
            path.add(backtrack.get(i).get(finalState));
            finalState = backtrack.get(i).get(finalState);
            i--;
        }

        Collections.reverse(path);//reverses the path to the right way
        return path;
    }
    /**
     * This function compares the tags generated by Viterbi and the actual tags
     * @param data the file which has sentences to read
     * @param tags the file with the right tags
     * @throws IOException from Viterbi
     */


    public void compareTags(String data, String tags) throws IOException {
        BufferedReader dataFile = null;
        BufferedReader tagFile = null;

        try{
            //opening file
            dataFile = new BufferedReader(new FileReader(data));
            tagFile=new BufferedReader(new FileReader(tags));
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }

        String dataLine,tagLine;
        int incorrect= 0;
        int correct = 0;

        try {
            while ((dataLine = dataFile.readLine()) != null) {
                tagLine = tagFile.readLine();
                String[] tagPieces = tagLine.split(" ");
                String[] dataPieces = dataLine.split(" ");

                ArrayList<String> tagList = Viterbi(dataPieces);

                for (int i = 0; i < tagList.size(); i++) {
                    if (tagList.get(i).equals(tagPieces[i])) {
                        //increase correct
                        correct += 1;
                    } else {
                        //increase incorrect if tags aren't equal
                        incorrect += 1;
                    }
                }
            }
        }
        finally{
            try{//closing file
                dataFile.close();
                tagFile.close();
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }

        //getting total number of the sentences/ tags present in file
        int totalTags = incorrect + correct;

        System.out.println("TRAINING DONE!!!");
        System.out.println("Correct tags: " + correct);
        System.out.println("Incorrect tags: " + incorrect);
        System.out.println("Total tags: " + totalTags);
    }

    /**
     * This method lets the user type in a sentence and tells the user which part of speech each word is.
     * @throws IOException from Viterbi
     */
    public void wordType() throws IOException {
        System.out.println("Please type in a sentence to check: ");
        //user input
        Scanner sent = new Scanner(System.in);

        while(true){
            String [] line = sent.nextLine().split(" ");
            String sentence = "";
            ArrayList<String> route = Viterbi(line);

            for (int i = 0; i < line.length; i++){
                sentence += line[i]+"/" + route.get(i) + " ";
            }

            //ending while loop by pressing "q"
            if (line[0].toLowerCase().equals("q") && line.length == 1){
                System.out.println("You left!");
                break;
            }

            System.out.println(sentence + "\n");
        }
    }


    public static void main(String[] args) throws IOException {
        HMM testing=new HMM("PSET5/brown-train-sentences.txt","PSET5/brown-train-tags.txt");
        testing.reading("PSET5/simple-test-sentences.txt");
        testing.compareTags("PSET5/simple-test-sentences.txt","PSET5/simple-test-tags.txt");
        testing.wordType();



    }
}
