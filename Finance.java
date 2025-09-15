import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Scanner;

public class Finance {

    public static void main(String[] args) {

        /* Name of the dataset file and the dynamic array data */
        String fileName;
        DArray data;

        /* Checking if the user is giving the arguments via cmd line or we have to ask for it */
        if (args.length < 1) {
            Scanner scan = new Scanner(System.in);
            fileName = scan.nextLine();
        } else {
            fileName = args[0];
        }

        /* Reading the file within catch since the file can have IO exception */
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

            /* data is created with 1000 as size, line is for storing single line, colData is for printing purposes at the end,
            lineCount is set to -1 as i am ignoring the first line; when i read through the first line the linecount will be 0 and we won't count it

             */
            data = new DArray(1000);
            String line;
            String[] colData;
            int lineCount = -1;

            /* reading through the file line by line, increasing linecount immediately as i will need it if i have to print any errors,
            using contains method that i created in teh darray class,
            This checks if duplicate print and move on else check for invalid ; if invalid print and move on else print the values seperated by ;
            There is a condition with invalid where i am making sure the linecount >= 1. this is for the exception that i am not counting the first line
            And it generates error like NumberFormatException since it's not parsable also we ignore it so i don't want to print invalid at -1.
             */
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (data.contains(line)) {
                    System.out.println("Duplicate Data on line " + lineCount);
                    continue;

                } else {
                    data.add(line);
                    if (!validData(line) && lineCount >= 1) {
                        System.out.println("Invalid Data on line " + lineCount);
                        continue;
                    }
                }
                colData = line.split(",");
                for (String datum : colData) {
                    System.out.print(datum + ";");
                }
                System.out.println();
            }

        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Checks for date condition, easy just make sure we have date after 2019, the date is in format y/m/d so just checking the y
     * @param colDatum the single data of the date onely
     * @return true if satisfies the condition
     */
    private static boolean validDate(String colDatum) {
        String[] date = colDatum.split("-");
        try{
            return Integer.parseInt(date[0]) >= 2019 && Integer.parseInt(date[0]) <= 2025;
        } catch (NumberFormatException e){
            return false;
        }
    }

    /**
     * This is for valid data, splitting it and checking if the data is ? or unknown or negative since any negative num starts with -. There is
     * an edgecase that any other string data starts with -. That is not handled here
     * @param data whole line of record
     * @return true if valid
     */
    private static boolean validData(String data) {
        String[] colData = data.split(",");

        for (int i = 0; i < colData.length; i++) {

            if (colData[i].equals("?") || colData[i].equals("unknown") || colData[i].startsWith("-")) {
                return false;
            } else if (i == colData.length - 1) {
                return validDate(colData[i]);

            }
        }
        return true;
    }

    /**
     * Implementation of a simple Dynamic array just for this problem
     */
    static class DArray {
        /* I defined it as string since i know it's 100% string that i will use it for this program,
        As for filled value length it's to keep track of how many spaces have been filled in the array so i can check to make sure
        i have enough space when adding elements
         */
        private String[] data;
        private int filledValuesLength = 0;

        /**
         * Constructor takes the size and creates an array of that size. It doesnot create an array more than given size at the creation but
         * it will increase as it need
         * @param size size of the array that will be created
         */
        public DArray(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be greater than 0");
            }
            data = new String[size];
        }

        /**
         * Implements the adding feature of the Dynamic arrray. we make sure the value is not null. Then, we checkSize() which is
         * anoither method that makes sure we have enough space. after which w3e add the element in the last index thaat's not filled
         * which is the index till which filledValuesLength is since arrays starts from 0 and filledValueslength starts from 1
         * Since we added a value the no of values increses so we increment
         * @param value
         */
        public void add(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value cannot be null");
            }
            checkSize();
            data[filledValuesLength] = value;
            filledValuesLength++;
        }

        /**
         * This method is created solely for the duplication checking for the program. This goes through the array and checks
         * if the provided data is already repeated in teh array.
         * @param value data to be checked
         * @return true if there is a duplicate, false if not
         */
        public boolean contains(String value) {
            if (value == null) {
                return false;
            }
            for (int i = 0; i < filledValuesLength; i++) {
                if (data[i].equals(value)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * This method makes sure teh array have enough space to add elements
         * The idea is i have certain criteria below which the size can't decrease. 15% in this case
         * if that happens i will increase the value by 50% of the currentLength.
         * This is good for the current dataset since i know hwo long it is but it's the ideal
         * i am creating a temp array and copying the data to it then reference the original array to this temp array
         * which means the last array will fullfill the criteeria for garbage and is removed by java's garbage colleciton
         */
        private void checkSize() {
            int currentArrayLength = this.data.length;
            int requiredSpace = (int) (0.15 * currentArrayLength);
            if ((currentArrayLength - filledValuesLength) < requiredSpace) {
                int additionalSpace = currentArrayLength + (currentArrayLength / 2);
                String[] temp = new String[currentArrayLength + additionalSpace];
                for (int i = 0; i < currentArrayLength; i++) {
                    temp[i] = data[i];
                }
                this.data = temp;
            }
        }


    }
}
