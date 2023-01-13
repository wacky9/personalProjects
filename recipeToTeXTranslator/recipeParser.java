import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;

public class recipeParser {

    static String[] tokens = {"NAME", "HEAD", "HEADNOTE", "INGREDIENTS", "COOKNOTE","DIRECTIONS","SERVING","PREP","COOK"};
    @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        System.out.println("Please select either single file or folder mode");
        System.out.println("Enter 1 for single file or 2 for folder mode");
        int selection = input.nextInt();
        if(selection == 1){
            singleFileMode();
        } else if (selection == 2) {
            folderMode();
        }
    }


    /*Reads in a single file from anywhere and generates corresponding TeX file*/
    public static void singleFileMode(){
        Scanner input = new Scanner(System.in);

        /*Model file to be used to base LaTeX semantics off of*/
        System.out.println("Please enter the model file name");
        String modelFile = input.nextLine();
        Scanner modelFileInput = null;
        boolean incorrectModelFileInput = false;
        do {
            try {
                modelFileInput = new Scanner(new File(modelFile));
            } catch (IOException e) {
                incorrectModelFileInput = true;
                System.err.println("Please re-enter the model file name");
                modelFile = input.nextLine();
            }
        }while(incorrectModelFileInput);

        /*Recipe File that stores recipe information*/
        System.out.println("Please enter the recipe file name");
        String recipe = input.nextLine();
        Scanner recipeInput = null;
        boolean incorrectRecipeInput = false;
        do {
            try {
                recipeInput = new Scanner(new File(recipe));
            } catch (IOException e) {
                incorrectRecipeInput = true;
                System.err.println("Please re-enter the recipe file name");
                recipe = input.nextLine();
            }
        }while(incorrectRecipeInput);

        Map<String,String> recipeInformation = new HashMap<>();
        String token = recipeInput.next();
        parseToken(recipeInformation, recipeInput, token);

        //Just for testin'
        recipeInformation.forEach((key, value) -> System.out.println(key + ": " + value));

        System.out.println("Please enter the file to write to");
        String outputFile = input.nextLine();
        //This just assumes that the model input is correct\
        PrintWriter output = null;
        try {
            output = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e){
            System.err.println("Something went wrong");
            System.exit(0);
        }
        while(modelFileInput.hasNextLine()){
            String line = modelFileInput.nextLine();
            String modifiedLine = findAndReplaceToken(line,recipeInformation);
            output.println(modifiedLine);
        }

        recipeInput.close();
        modelFileInput.close();
        input.close();
        output.close();
    }

    /*Selects all files in a given directory and writes corresponding TeX files to a different directory*/
    public static void folderMode(){
        Scanner input = new Scanner(System.in);

        /*Model file to be used to base LaTeX semantics off of*/
        System.out.println("Please enter the model file name");
        String modelFile = input.nextLine();
        Scanner modelFileInput = null;
        boolean incorrectModelFileInput = false;
        do {
            try {
                modelFileInput = new Scanner(new File(modelFile));
            } catch (IOException e) {
                incorrectModelFileInput = true;
                System.err.println("Please re-enter the model file name");
                modelFile = input.nextLine();
            }
        }while(incorrectModelFileInput);

        /*Folder that stores all recipes*/
        System.out.println("Please enter the folder path that contains the recipes");
        String recipeFolder = input.nextLine();
        File folder = new File(recipeFolder);
        String[] allRecipes = folder.list();

        System.out.println("Please enter folder to put outputted files in");
        String outputFolder = input.nextLine();
        for (String S: allRecipes){
            /*Create a scanner to read in from given file*/
            Scanner recipeInput = null;
            try {
                recipeInput = new Scanner(new File(recipeFolder + "\\" +S));
            } catch (IOException e){
                System.err.println("Something went wrong while processing " + S);
                System.exit(0);
            }

            /*Create file to be written to*/
            int loc = S.indexOf('.');
            String filename = S.substring(0,loc);
            filename = filename + ".tex";
            PrintWriter output = null;
            try {
                output = new PrintWriter(new BufferedWriter(new FileWriter(outputFolder+ "\\" + filename)));
            } catch (IOException e){
                System.err.println("Something went wrong while processing " + filename);
                System.exit(0);
            }

            Map<String,String> recipeInformation = new HashMap<>();
            String token = recipeInput.next();
            parseToken(recipeInformation, recipeInput, token);

            /*Read in model file and replace pertinent information*/
            while(modelFileInput.hasNextLine()){
                String line = modelFileInput.nextLine();
                String modifiedLine = findAndReplaceToken(line,recipeInformation);
                output.println(modifiedLine);
            }
            output.close();
            recipeInput.close();
        }
        modelFileInput.close();
        input.close();
    }
    /*Parses the recipe text file*/
    public static void parseToken(Map<String,String> information, Scanner input, String token){
           switch (token) {
               case "NAME": parseSingleLine(information,input,token); break;
               case "HEADNOTE":
               case "COOKNOTE":
                   parseMultipleLines(information,input,token); break;
               case "INGREDIENTS":
               case "SERVING":
               case "PREP":
               case "COOK":
                   parseNumberedList(information,input,token); break;
               case "DIRECTIONS": parseDirections(information, input,token); break;
           }
    }

    /*Parses a token that expects only a single line to follow
    * Expects to receive input ending immediately after the token*/
    private static void parseSingleLine(Map<String,String> information, Scanner input, String token){
        /*Throw away the rest of the line*/
        input.nextLine();
        String name = input.nextLine();
        information.put(token,name);
        //Get rid of empty line
        input.nextLine();
        //Properly defined input will have a token next
        try {
            String newToken = input.next();
            parseToken(information,input,newToken);
        } catch (NoSuchElementException e){
            return;
        }
    }

    /*Parses a token that expects multiple lines to follow*/
    private static void parseMultipleLines(Map<String,String> information, Scanner input, String token){
        /*Throw away the rest of the line*/
        input.nextLine();
        //Any number of lines after token, so must do check
        String nextItem;
        String headnote = "";
        boolean test = true;
        while(input.hasNextLine() && test){
            nextItem = input.nextLine();
            test = nextItem != "";
            headnote += nextItem + " ";
        }
        information.put(token,headnote);
        try {
            String newToken = input.next();
            parseToken(information,input,newToken);
        } catch (NoSuchElementException e){
            return;
        }
    }

    /*Parse ingredients*/
    private static void parseDirections(Map<String,String> information, Scanner input, String token){
        /*Throw away the rest of the line*/
        input.nextLine();
        //Any number of lines after token, so must do check
        String nextItem;
        String allDirections = "";
        boolean test = true;
        while(input.hasNextLine() && test) {
            if (input.hasNextInt()) {
                String nextDirection = "";
                boolean subtest = true;
                //Throws away numbered line
                input.nextLine();
                while (input.hasNextLine() && subtest) {
                    nextItem = input.nextLine();
                    subtest = nextItem != "";
                    nextDirection += nextItem + " ";
                }
                allDirections += nextDirection + "\n\n";
            } else {
                test = false;
            }
        }
        information.put(token,allDirections);
        try {
            String newToken = input.next();
            parseToken(information,input,newToken);
        } catch (NoSuchElementException e){
            return;
        }
    }

    /*Parse text with format num item
    * Throws an error if this condition is not met*/
    private static void parseNumberedList(Map<String,String> information, Scanner input, String token){
        /*Throw away the rest of the line*/
        input.nextLine();
        //Any number of lines after token, so must do check
        String nextItem;
        String headnote = "";
        boolean test = true;
        while(input.hasNextLine() && test){
            if(input.hasNextInt()){
                nextItem = input.nextLine();
                headnote += nextItem + "\n";
            } else {
                input.nextLine();
                test = false;
            }
        }
        information.put(token,headnote);
        try {
            String newToken = input.next();
            parseToken(information,input,newToken);
        } catch (NoSuchElementException e){
            return;
        }
    }



    public static boolean isToken(String S){
        boolean blah = false;
        for(String token: tokens){
            if(S.equals(token)){
                blah = true;
            }
        }
        return blah;
    }

    /* Finds every token in S and replaces it with the corresponding value in recipeInformation
       Returns the resulting string
     */
    public static String findAndReplaceToken(String S, Map<String,String> recipeInformation){
        String replaced = S;
        for(String token: tokens){
            try {
                replaced = replaced.replace(token,recipeInformation.get(token));
            } catch (NullPointerException e){
                continue;
            }
        }
        return replaced;
    }
}
