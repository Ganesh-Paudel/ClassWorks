import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        /* Name of the dataset file and the dynamic array data
        * inflationfile is the filepath for inflation,
        * the three following are for keeping track of how many data was encountered that was duplicate, invalid, flagged
        * years and avgInflation is for storing the inflation data. I am using parrel array storage for easy acessing and insertion. inflation count is to keep track of
        * how many data they both contain. basically the size*/
        String fileName;
        String inflationFile;
        DArray data;
        int duplicateRecords = 0;
        int invalidRecords = 0;
        int issueFlagCount = 0;
        int[] years = new int[30];
        double[] avgInflations = new double[30];
        int inflationCount  = 0;


        /* Checking if the user is giving the arguments via cmd line or we have to ask for it
        * Also the infaltion file path*/

        if (args.length < 1) {
            Scanner scan = new Scanner(System.in);
            System.out.print("Please enter the file path:");
            fileName = scan.nextLine();
            System.out.println();
            System.out.println("Enter the inflation file path: ");
            inflationFile = scan.nextLine();
        } else {
            fileName = args[0];
            inflationFile = args[1];
        }

        /*
        Here we read the file and add the data. the duplicate check is done here. It could have been done later but this also works so. also
        duplicate check is done before inserting since if i insert it will exist in the data, i just added it.
         */
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            data = new DArray();
            String line;
            FinanceRecord financeRecord;
            while ((line = reader.readLine()) != null) {
                financeRecord = new FinanceRecord(line);
//                if(data.contains( financeRecord)) {
//                    duplicateRecords++;
//                }
                data.add(financeRecord);
            }

        } catch (IOException e){
            throw new IOError(e);
        }
        /*
        This is reading the inflation file
        we get the year, and the average inflation rate which is end col. and just append it to the arrays parallely
         */
        try (BufferedReader reader = new BufferedReader(new FileReader(inflationFile))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] singleData = line.split(",");
                try {
                    int year =  Integer.parseInt(singleData[0]);
                    double avgInflation = Double.parseDouble(singleData[singleData.length - 1]);

                    years[inflationCount] = year;
                    avgInflations[inflationCount] = avgInflation;
                    inflationCount++;
                } catch (NumberFormatException e) {
                }
            }

        } catch (IOException e){
            throw new IOError(e);
        }


        /*
         * This is the first bit of processing, check for invalid and calculate inflation. cumulatively going yearly
         *
         * get the current year or record year of the data, then get corresponding avg Inflation rate form the parallel arrays
         * keep calculatin the arrays
         * issueFlags is implemented in the class record itself. here we just check and increment the flagcount
         */
        for (int i = 0; i < data.getSize(); i++) {
            String currentData = data.get(i).getWholeData();
            for(int j = i-1; j > 0; j--) {
                if(currentData.equals(data.get(j).getWholeData())) {
                    duplicateRecords++;
                    System.out.println("Duplicate records at line " + i );
                }
            }
                if (!data.get(i).isValid()){
                    //invalid data
                    invalidRecords++;
                    System.out.println("Invalid data at line " + i );
                    continue;
                }

                String recordDate = data.get(i).getSplitData()[data.get(i).getSplitData().length - 1];
                double recordYear =  Double.parseDouble(recordDate.split("-")[0]);
                double currentSavings = Double.parseDouble(data.get(i).getSplitData()[8]);
                double currentLoan = Double.parseDouble(data.get(i).getSplitData()[11]);

                for(int j = 0; j < inflationCount; j++){
                    if (years[j] >= recordYear){
                        currentSavings = currentSavings * (1 + avgInflations[j] / 100.0);
                        currentLoan = currentLoan * (1 + avgInflations[j] / 100.0);
                    }
                }

                String issueFlag = data.get(i).getIssueFlag();
                if(!(issueFlag.toLowerCase().equals("ok"))) issueFlagCount++;
                String newRecord = data.get(i).getWholeData() + "," + currentSavings + "," + currentLoan +  "," + issueFlag;
                data.get(i).setWholeData(newRecord);

            }

        //changing the columns title row i.e the first row with update
        data.get(0).setWholeData(data.get(0).getWholeData() + ",currentSavings,currentLoan,issueFlag");

        // prinbing the data reversely
        for(int i = data.getSize() - 1; i >= 0; i--){
            System.out.println(data.get(i).getWholeData());
        }

        //printing the summary
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Data lines read: " + (duplicateRecords + invalidRecords + data.getSize())
                + "; Records in memory: " + data.getSize()
                + "; Invalid records: " + invalidRecords
                + "; Duplicate records: " + duplicateRecords
                + "; Assumed issue records: " + issueFlagCount);

        }




/*
The dynamic array implementation, creates array of 10 as initial, then has different methods implemented for the usage
add, simply adds the value unless it's null. also checks if it has enough size and if it does the nit adds to the last possible empty position,
checksize, if the size is the same as the length, we need to increase, we double the size and if the size is zero we default it to 10.
contains, for duplication, calls the equals method which is implemented in the record class
getsize, returns the length of the filled values
get, returns value for provided index
 */
    static class DArray {
        private FinanceRecord[] data;
        private int initialLength = 10;
        private int count = 0;

        public DArray(){
            data = new FinanceRecord[initialLength];
        }

        public void add(FinanceRecord record){
            if( record == null) throw new IllegalArgumentException("Value cannot be null");
            checkSize();
            data[count] = record;
            count++;
        }

        private void checkSize(){
            if (count == this.data.length) {
                int additionalSpace = this.data.length * 2;

                if(additionalSpace < 1) additionalSpace = 10;
                FinanceRecord[] temp = new FinanceRecord[additionalSpace];

                for (int i = 0; i < count; i++) {
                    temp[i] = data[i];
                }
                this.data = temp;
            }
        }

        public boolean contains(FinanceRecord value) {
            if (value == null) {
                return false;
            }
            for (int i = 0; i < count; i++) {
                if (data[i].equals(value)) {
                    return true;
                }
            }
            return false;
        }

        public int getSize(){
            return this.count;
        }

        public FinanceRecord get(int index){
            if (index < 0 || index >= count) {throw new IllegalArgumentException("Index out of bounds");}

            return data[index];
        }
    }


    /*
     *This is the reperesentation of the data
     * a string and array of strings,
     * if given the data unless null we add it. and split it for the array data
     * bunch of getters and setters,
     * generated the equals method where we compare the data of two different objects,
     * isValid is for checking if the data is valid, the isValid method from 1.0 and the valid date as well
     * The issue flag checks for the values in the data and issues flags if it has any.
     *
     */
    static class FinanceRecord {

        private String wholeData;
        private String[] splitData;

        public FinanceRecord(String wholeData) {
            if (wholeData != null){
                this.wholeData = wholeData;
                this.splitData =  wholeData.split(",");
            }
        }


        public String getWholeData() {
            return wholeData;
        }

        public void setWholeData(String wholeData) {

            if (wholeData == null) throw new IllegalArgumentException("Null value");
            this.wholeData = wholeData;
        }

        public String[] getSplitData() {

            if(splitData == null && wholeData != null) return wholeData.split(",");
            return splitData;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FinanceRecord that)) return false;
            return Objects.equals(wholeData, that.wholeData);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(wholeData);
        }

        public boolean isValid(){

            for (int i = 0; i < splitData.length; i++) {

                if (splitData[i].equals("?") || splitData[i].equals("unknown") || splitData[i].startsWith("-")) {
                    return false;
                } else if (i == splitData.length - 1) {
                    return validDate(splitData[i]);

                }
            }
            return true;
        }
        private static boolean validDate(String colDatum) {
            String[] date = colDatum.split("-");
            try{
                return Integer.parseInt(date[0]) >= 2019 && Integer.parseInt(date[0]) <= 2025;
            } catch (NumberFormatException e){
                return false;
            }
        }

        public String getIssueFlag() {
            try {
                double savings = Double.parseDouble(splitData[8]);
                double loan = Double.parseDouble(splitData[11]);
                double income = Double.parseDouble(splitData[6]);
                String employment = splitData[4].toLowerCase();
                if (savings > 500000) {
                    return "HIGH_SAVINGS";
                }
                if (loan > income * 30) {
                    return "LOAN_INCONSISTENT";
                }
                if (employment.contains("unemployed") && income > 500000) {
                    return "EMPLOYMENT_MISMATCH";
                }
            } catch (Exception e) {
                return "CHECK_ERROR";
            }
            return "OK";
        }


    }
}
