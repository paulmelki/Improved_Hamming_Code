package com.company;

import java.util.ArrayList;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {

        // Create a data stream that will be sent.
        // We created the same data stream used in the example in our report and in the paper based on which we did
        // our work.
        ArrayList<Integer> data_stream = new ArrayList<>();
        data_stream.add(0);
        data_stream.add(1);
        data_stream.add(0);
        data_stream.add(0);
        data_stream.add(1);
        data_stream.add(1);
        data_stream.add(0);
        System.out.println("1. Original Data Stream: " + data_stream.toString());
        int r = calculateNumberOfRedundancyBits(data_stream.size());
        // Simulate sending of data
        ArrayList<Integer> sentDataStream = send(data_stream);
        System.out.println("2. Data Stream with added Redundancy Bits: " + sentDataStream.toString());

        // Simulate error in transmission
        sentDataStream.set(4, 0);

        // Simulate receiving of data, error detection and correction
        receive(sentDataStream, r);


    }


    /**
     * Function that simulates sending of data from a source node. This function calls another function,
     * getRedundancyBits, that will apply the Improved Hamming Code algorithm and return the resulting redundancy.
     * The redundancy bits obtained will be appended to the data being "sent".
     *
     * @param dataStream ArrayList of 1s and 0s, representing the raw form of the data.
     * @return ArrayList which is the original data stream, with the calculated redundancy bits appended to its end.
     */
    public static ArrayList<Integer> send(ArrayList<Integer> dataStream) {

        // Calculate the redundancy bits, based on Improved Hamming Code algorithm
        ArrayList<Integer> redundancyBits = getRedundancyBits(dataStream);

        // Append redundancy bits to the data stream
        dataStream.addAll(redundancyBits);

        return dataStream;

    }

    /**
     * Function that simulates the receiving of data at a destination node. This function will calculate the redundancy
     * bits based on the received data, and will compare them to the actual redundancy bits appended to the data stream.
     * Based on the results, the destination node will detect an error and will correct it, if it exists. Otherwise,
     * it will reply stating that the data has been received correctly.
     *
     * @param received_data_stream ArrayList of 1s and 0s representing the data stream received by the destination.
     * @param r                  is the number of redundancy bits added to the original data stream. We need this parameter in order to
     *                           be able to retrieve the original message from the received message (includes original + redundancy bits).
     */
    public static void receive(ArrayList<Integer> received_data_stream, int r) {

        System.out.println("3. Received Data Stream with Redundancy Bits: " + received_data_stream.toString());

        // Retrieve the original message, without the redundancy bits
        ArrayList<Integer> received_message = new ArrayList<>();
        for (int i = 0; i < received_data_stream.size() - r; i++) {
            received_message.add(received_data_stream.get(i));
        }
        System.out.println("4. Received Data Stream without Redundancy Bits: " + received_message.toString());

        // Retrieve the received redundancy bits, from the received message
        ArrayList<Integer> received_redundancy_bits = new ArrayList<>();
        for (int i = received_data_stream.size() - r; i < received_data_stream.size(); i++) {
            received_redundancy_bits.add(received_data_stream.get(i));
        }

        // Calculate the redundancy bits based on the received message
        ArrayList<Integer> calculated_redundancy_bits;
        calculated_redundancy_bits = getRedundancyBits(received_message);

        /* ERROR DETECTION */
        // Create the parity check array which will result from comparing the received redundancy bits and the
        // calculated redundancy bits
        ArrayList<Integer> parity_check_array = detectError(received_redundancy_bits, calculated_redundancy_bits);
        // parity_check_array needs to be read in reverse in order to be able to locate the position of the error bit
        Collections.reverse(parity_check_array);

        /* ERROR CORRECTION */
        // Read the position of the detected error from the parity check array
        // The following process is done in order to convert the array into a binary understandable integer, which will
        // be the position of the error
        String parity_check_string = "";
        for (Integer i : parity_check_array) {
            parity_check_string += i.toString();
        }
        int error_position = Integer.parseInt(parity_check_string, 2);
        if (error_position == 0) System.out.println("\nDATA RECEIVED CORRECTLY! WOUHOU!");
        else {
            System.out.println("\nERROR DETECTED! at bit #" + error_position);
            // correct error
            boolean bit = received_data_stream.get(error_position - 1) == 1;
            // flip the error bit
            received_message.set(error_position - 1, bit ? 0 : 1);
            System.out.println("\nError corrected! Corrected Message: " + received_message.toString());
        }

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
     * @param data_stream
     * @return
     */
    public static ArrayList<Integer> getRedundancyBits(ArrayList<Integer> data_stream) {

        /**
         * Create an array of redundancy bit that will hold the redundancy bits obtained based on the Hamming Code
         * calculations. This array will be returned and used either to:
         *      1. Append it to the data stream before being sent.
         *      2. Calculate the redundancy bits of the received message and compare them to the original ones in order
         *          to check and correct errors.
         */
        ArrayList<Integer> redundancy_bits = new ArrayList<>();

        int n = data_stream.size();

        int r = calculateNumberOfRedundancyBits(n);

        /**
         * Begin calculation of the first r-1 redundancy bits.
         * Let i be number of redundancy bit: check i bits, skip i bits.
         * Should start checking at 2^i - 1
         */
        for (int i = 0; i < r - 1; i++) {

            int count_ones = 0;
            double x = (double) i;
            int start_check = (int) Math.pow(2, x) - 1;
            int skips_and_checks = start_check + 1;

            do {
                for (int j = 0; j < skips_and_checks; j++) {
                    if ((int) data_stream.get(start_check + j) == 1) count_ones++;
                }

                start_check += 2 * skips_and_checks;
            } while (start_check + skips_and_checks <= data_stream.size());

            if (count_ones % 2 == 0) redundancy_bits.add(0);
            else redundancy_bits.add(1);
        }

        /**
         * Begin calculation of the last redundancy bit based on the parity of the first r-1 redundancy bits.
         */
        int count_ones = 0;
        for (int i = 0; i < redundancy_bits.size(); i++) {
            if (redundancy_bits.get(i) == 1) {
                count_ones++;
            }
        }
        if (count_ones % 2 == 0) redundancy_bits.add(0);
        else redundancy_bits.add(1);

        // return the result
        return redundancy_bits;
    }


    /**
     * Function that calculates the number of redundancy bits needed for a certain data stream, based on the number of
     * bits in the data stream.
     * @param n the number of data bits in the data stream.
     * @return r, the number of redundancy bits needed.
     */
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

    /**
     * Function that compares the received redundancy bits, and the calculated redundancy bits and returns the results
     * as an ArrayList that contains 0s and 1s.
     * 0, if the two bits are the same.
     * 1, if the two bits are not the same.
     * @param received ArrayList of the received redundancy bits
     * @param calculated ArrayList of the calculated redundancy bits
     * @return parity_check_array, ArrayList that contains the results of the comparison
     */
    public static ArrayList<Integer> detectError(ArrayList<Integer> received, ArrayList<Integer> calculated) {

        ArrayList<Integer> parity_check_array = new ArrayList<>();

        for (int i = 0; i < received.size(); i++) {
            if (received.get(i) == calculated.get(i)) parity_check_array.add(0);
            else parity_check_array.add(1);
        }

        return parity_check_array;
    }
}
