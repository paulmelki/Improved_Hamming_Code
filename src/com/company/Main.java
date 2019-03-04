package com.company;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

//        System.out.print("Please enter the length of the random data stream to send: ");
//        int n = input.nextInt();

        ArrayList<Integer> dataStream = new ArrayList<>();
//        for (int i = 0; i < n; i++) {
//            dataStream.add((int) Math.round(Math.random()));
//        }
        dataStream.add(0);
        dataStream.add(1);
        dataStream.add(0);
        dataStream.add(0);
        dataStream.add(1);
        dataStream.add(1);
        dataStream.add(0);
        System.out.println("Original data stream: " + dataStream.toString());
        int r = calculateNumberOfRedundancyBits(dataStream.size());
        ArrayList<Integer> sentDataStream = send(dataStream);
//        sentDataStream.set(4, 0);
        receive(sentDataStream, r);


    }


    public static ArrayList<Integer> send(ArrayList<Integer> dataStream) {

        ArrayList<Integer> redundancyBits = getRedundancyBits(dataStream);

//        System.out.println("Old data stream: " + dataStream.toString());

        dataStream.addAll(redundancyBits);

//        System.out.println("New data stream: " + dataStream.toString());

        return dataStream;

    }

    public static void receive(ArrayList<Integer> receivedDataStream, int r) {


        ArrayList<Integer> dataStream = new ArrayList<>();
        for (int i = 0; i < receivedDataStream.size() - r; i++) {
            dataStream.add(receivedDataStream.get(i));
        }

        System.out.println("dataStream: " + dataStream.toString());

        ArrayList<Integer> receivedRedundancyBits = new ArrayList<>();
        for (int i = receivedDataStream.size() - r; i < receivedDataStream.size(); i++) {
            receivedRedundancyBits.add(receivedDataStream.get(i));
        }

        System.out.println("receivedRedundancyBits: " + receivedRedundancyBits.toString());

        ArrayList<Integer> calculatedRedundancyBits = new ArrayList<>();
        calculatedRedundancyBits = getRedundancyBits(dataStream);

        System.out.println("calculatedRedundancyBits: " + calculatedRedundancyBits.toString());

        ArrayList<Integer> parityCheckArray = detectError(receivedRedundancyBits, calculatedRedundancyBits);

        System.out.println("parityCheckArray: " + parityCheckArray.toString());
        Collections.reverse(parityCheckArray);
        System.out.println("parityCheckArray after reverse: " + parityCheckArray.toString());


        String parityCheckString = "";

        for (Integer i : parityCheckArray) {
            parityCheckString += i.toString();
        }

        int parityCheckResult = Integer.parseInt(parityCheckString, 2);
        System.out.println("finalResult: " + parityCheckResult);

        if (parityCheckResult == 0) System.out.println("Data received correctly!!");
        else {
            // correct error
            boolean bit = receivedDataStream.get(parityCheckResult - 1) == 1;
            dataStream.set(parityCheckResult - 1, bit ? 0 : 1);
        }

        System.out.println("corrected result: " + dataStream.toString());


    }


    /**
     * Function that calculates the redundancy bits that need to be added to the data stream, based on the improved
     * Hamming Code Algorithm.
     * 1. The method computes the number (r) of redundancy bits needed, based on the length of the data stream.
     * 2. The method computer the first r-1 redundancy bits following the same pattern of the original Hamming Code
     * algorithm. If the number of 1s at the positions to be checked by a certain redundancy bits is even, the
     * redundancy bit added will be 0. If that number is odd, then the redundancy bit will be even.
     * 3. The last redundancy bit will be calculated based on the parity of the r-1 redundancy bits.
     *
     * @param dataStream
     * @return
     */
    public static ArrayList<Integer> getRedundancyBits(ArrayList<Integer> dataStream) {

        /**
         * Create an array of redundancy bit that will hold the redundancy bits obtained based on the Hamming Code
         * calculations. This array will be returned and used either to:
         *      1. Append it to the data stream before being sent.
         *      2. Calculate the redundancy bits of the received message and compare them to the original ones in order
         *          to check and correct errors.
         */
        ArrayList<Integer> redundancyBits = new ArrayList<>();

        int n = dataStream.size();

        int r = calculateNumberOfRedundancyBits(n);

        /**
         * Begin calculation of the first r-1 redundancy bits, as described in the function's documentation.
         */
        for (int i = 0; i < r - 1; i++) {

            int countOnes = 0;
            double lol = (double) i;
            int startCheck = (int) Math.pow(2, lol) - 1;
            int skipsAndChecks = startCheck + 1;


            do {
                for (int j = 0; j < skipsAndChecks; j++) {
                    if ((int) dataStream.get(startCheck + j) == 1) countOnes++;
                }

                startCheck += 2 * skipsAndChecks;
            } while (startCheck + skipsAndChecks <= dataStream.size());

            if (countOnes % 2 == 0) redundancyBits.add(0);
            else redundancyBits.add(1);
        }

        /**
         * Begin calculation of the last redundancy bit based on the parity of the first r-1 redundancy bits.
         */
        int countOnes = 0;
        for (int i = 0; i < redundancyBits.size(); i++) {
            if (redundancyBits.get(i) == 1) {
                countOnes++;
            }
        }

        if (countOnes % 2 == 0) redundancyBits.add(0);
        else redundancyBits.add(1);

        return redundancyBits;
    }


    public static int calculateNumberOfRedundancyBits(int n) {

        /**
         * Based on the Hamming Code algorithm, the number of parity bits needed (r)
         * should verify the inequation 2^(r-1) - 1 >= n, where 'n' is the number of
         * bits in the data stream.
         * This equation always has a solution of [1 + log(n)/log(2), oo].
         * We will take r to be the integer value of 1 + log(n)/log(2).
         */
        int r = (int) (1 + Math.log(n) / Math.log(2));

        // If r is odd, an extra parity bit is needed.
        if (r % 2 != 0) r++;

        return r;
    }

    public static ArrayList<Integer> detectError(ArrayList<Integer> received, ArrayList<Integer> calculated) {

        ArrayList<Integer> parityCheck = new ArrayList<>();

        for (int i = 0; i < received.size(); i++) {
            if (received.get(i) == calculated.get(i)) parityCheck.add(0);
            else parityCheck.add(1);
        }

        return parityCheck;
    }
}
