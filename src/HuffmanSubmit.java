import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

public class HuffmanSubmit implements Huffman {
	
	public static class BTNode {
		int data;
		String binaryCode;
		char character;
		BTNode left;
		BTNode right;

		public BTNode(int data, char character, String binaryCode) {
			this.data = data;
			this.binaryCode = binaryCode;
			this.character = character;
		}
	}

	public static class compareBTNode implements Comparator<BTNode> {
		@Override
		public int compare(BTNode o1, BTNode o2) {
			return o1.data - o2.data;
		}
	}

	public void encode(String inputFile, String outputFile, String freqFile) {
		// creates the frequency of chars map
		HashMap<Character, Integer> freqChar = new HashMap<Character, Integer>();
		freqChar = characterFrequencyTable(inputFile);

		// writes the frequency file
		freq(freqChar, freqFile);

		// finds the root of the huffMan tree
		BTNode root = rootNode(freqChar);

		// finds the binary representation of each letter and makes the frequency.txt
		// file
		HashMap<Character, String> binaryRep = new HashMap<Character, String>();
		binaryRep = binaryReps(root, freqChar);

		// takes in the given file and encodes it with the HashMap. It then saves it as
		// an output file
		try {
			encodedFile(inputFile, outputFile, binaryRep);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void decode(String inputFile, String outputFile, String freqFile) {
		//hashmap that converts the frequency file back into characters and frequencies
		HashMap<Character, Integer> cha = new HashMap<Character, Integer>();

		try {
			cha = backToChar(freqFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BTNode root = rootNode(cha);
		read(inputFile, outputFile, root);

	}

	// DECODE:
	public static void read(String encFile, String decFile, BTNode root) {
		BTNode position = root;
		BinaryIn in = new BinaryIn(encFile);
		BinaryOut out = new BinaryOut(decFile);

		while (!in.isEmpty()) {
			Boolean read = in.readBoolean();
			if (position.left == null && position.right == null) {
				out.write(position.character);
				out.flush();
				position = root;
			}
			if (read) {
				position = position.right;
			} else {
				position = position.left;
			}
		}
		out.flush();
	}

	// DECODE: changes the frequency table. Puts it into a hashMap of characters to
	// frequency
	public static HashMap<Character, Integer> backToChar(String freqFile) throws IOException {
		HashMap<Character, Integer> back = new HashMap<Character, Integer>();
		BufferedReader buff = new BufferedReader(new FileReader(freqFile));
		ArrayList<String> vals = new ArrayList<String>();
		String in = "";

		while (buff.ready()) {
			in = buff.readLine();
			StringTokenizer st = new StringTokenizer(in, ":");
			while (st.hasMoreTokens()) {
				String temp = st.nextToken();
				vals.add(temp);
			}
		}
		for (int i = 0; i < vals.size() - 1; i++) {
			if (i % 2 == 0) {
				char outC = (char) Integer.parseInt(vals.get(i), 2);
				back.put(outC, Integer.parseInt(vals.get(++i)));
			}
		}
		return back;
	}

	// takes in a hashmap of each of the character frequencies and returns the
	// biggest value. Uses a priority queue
	public static BTNode rootNode(HashMap<Character, Integer> input) {
		PriorityQueue<BTNode> queue = new PriorityQueue<BTNode>(new compareBTNode());

		// creates BTNodes for all of the characters in the frequency table
		for (char c : input.keySet()) {
			BTNode vals = new BTNode(input.get(c), c, "");
			queue.add(vals);
		}

		while (queue.size() != 1) {
			BTNode small = queue.poll();
			BTNode secSmall = queue.poll();
			BTNode sum = new BTNode((small.data + secSmall.data), ' ', "");
			sum.left = small;
			sum.right = secSmall;
			queue.add(sum);
		}

		// the root of the huffMan tree
		BTNode fin = queue.poll();
		return fin;
	}

	// starts with the root and traverses the tree assigning 1's and 0's
	public static HashMap<Character, String> binaryReps(BTNode root, HashMap<Character, Integer> map) {
		Queue<BTNode> qu = new LinkedList<BTNode>();
		HashMap<Character, String> binaryMap = new LinkedHashMap<Character, String>();
		qu.add(root);

		while (!qu.isEmpty()) {
			Queue<BTNode> newQu = new LinkedList<BTNode>();
			for (BTNode newest : qu) {
				if (newest.left != null && newest.right != null) {
					newQu.add(newest.left);
					newest.left.binaryCode = newest.binaryCode + "0";
					newQu.add(newest.right);
					newest.right.binaryCode = newest.binaryCode + "1";
				} else if (newest.left != null && newest.right == null) {
					newQu.add(newest.left);
					newest.left.binaryCode = newest.binaryCode + "0";
				} else if (newest.right != null && newest.left == null) {
					newQu.add(newest.right);
					newest.right.binaryCode = newest.binaryCode + "1";
				} else if (newest.left == null && newest.right == null) {
					binaryMap.put(newest.character, newest.binaryCode);
				}
			}
			qu = newQu;
		}
		return binaryMap;
	}

	// ENCODE: encodes the file
	public static void encodedFile(String input, String outputFile, HashMap<Character, String> freqFile)
			throws FileNotFoundException {
		BinaryIn in = new BinaryIn(input);
		BinaryOut out = new BinaryOut(outputFile);

		while (!in.isEmpty()) {
			char st = in.readChar();
			String c = freqFile.get(st);
			for (int i = 0; i < c.length(); i++) {
				out.write(c.charAt(i) == '1');
			}
		}
		out.flush();
	}

	// ENCODE: writes the frequency file (ascii to frequency)
	public static void freq(HashMap<Character, Integer> input, String freqFile) {
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		BinaryOut out = new BinaryOut(freqFile); // freq.txt

		for (char c : input.keySet()) {
			String binString = Integer.toBinaryString(c);
			while (binString.length() < 8) {
				binString = "0" + binString;
			}
			freq.put(binString, input.get(c));
		}

		for (String a : freq.keySet()) {
			out.write(a + ":" + freq.get(a));
			out.write("\n");
			out.flush();
		}
	}

	// ENCODE: returns a hashMap that consists of the character and the number of
	// times it appeared
	public static HashMap<Character, Integer> characterFrequencyTable(String inputFile) {
		HashMap<Character, Integer> freqTable = new HashMap<Character, Integer>();
		BinaryIn in = new BinaryIn(inputFile);

		while (!in.isEmpty()) {
			char a = in.readChar();
			if (freqTable.containsKey(a)) {
				int val = freqTable.get(a);
				freqTable.put(a, ++val);
			} else {
				freqTable.put(a, 1);
			}
		}
		return freqTable;
	}

	public static void main(String[] args) throws FileNotFoundException {
		Huffman huffman = new HuffmanSubmit();
		huffman.encode("alice30.txt", "ur.enc", "freq.txt");
		huffman.decode("ur.enc", "ur_dec.txt", "freq.txt");

		// After decoding, both ur.jpg and ur_dec.jpg should be the same.
		// On linux and mac, you can use `diff' command to check if they are the same.
	}

}
