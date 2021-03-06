import java.util.*;
/**
 * Although this class has a history of several years, it is starting from a
 * blank-slate, new and clean implementation as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information and including
 * debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD);
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE = HUFF_NUMBER | 1;

	private final int myDebugLevel;

	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;

	public HuffProcessor() {
		this(0);
	}

	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in  Buffered bit stream of the file to be compressed.
	 * @param out Buffered bit stream writing to the output file.
	 */
	//I started your compress finish this baby up.
	public void compress(BitInputStream in, BitOutputStream out) {
		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String[] codings = makeCodingsFromTree(root);
		
		out.writeBits(BITS_PER_INT,HUFF_TREE);
		writeHeader(root,out);
		
		in.reset();
		writeCompressedBits(codings,in,out);
		out.close();
	}
	//general idea could be wrong
	private int[] readForCounts(BitInputStream in) {
		int[] x = new int[ALPH_SIZE + 1];
		
		x[PSEUDO_EOF] = 1;
		while(true) {
			int bits = in.readBits(BITS_PER_WORD);			
			if (bits == -1) break;
			x[bits]++;
		}

		return x;
	}

	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();

		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > 0) {
				pq.add(new HuffNode(i, counts[i], null, null));
			}
		}

		while (pq.size() > 1) {
		    HuffNode left = pq.remove();
		    HuffNode right = pq.remove();
		    HuffNode t = new HuffNode(0,left.myWeight+right.myWeight,left,right);
		    pq.add(t);
		}
		HuffNode root = pq.remove();
		return root;
	}
	
	private String[] makeCodingsFromTree(HuffNode x) {
		String[] encodings = new String[ALPH_SIZE + 1];
	    codingHelper(x,"",encodings);
		return encodings;
	}
	private void codingHelper(HuffNode x, String path, String[] encodings) {
		if (x == null) return;
		if (x.myLeft == null && x.myRight == null) {
			encodings[x.myValue] = path;
			return;
		}
		
		codingHelper(x.myLeft, path + "0", encodings);
		codingHelper(x.myRight, path + "1", encodings);
	}

	private void writeHeader(HuffNode root, BitOutputStream out) {
		if (root == null) return;
		if (root.myLeft == null && root.myRight == null) {
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, root.myValue);
			return;
		}
		out.writeBits(1, 0);
		writeHeader(root.myLeft, out);
		writeHeader(root.myRight, out);
	}
	private void writeCompressedBits(String[] codings,BitInputStream in, BitOutputStream out)  {
		while(true) {
			int bits = in.readBits(BITS_PER_WORD);
			if (bits == -1) break;
			String code = codings[bits];
			out.writeBits(code.length(), Integer.parseInt(code, 2));
		}
		
		String code = codings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code, 2));
	}
	

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in  Buffered bit stream of the file to be decompressed.
	 * @param out Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out) {
		int bits = in.readBits(BITS_PER_INT);
		if (bits != HUFF_TREE) {
			throw new HuffException("Not a valid entry.");
		}

		else {
			HuffNode x = node(in);
			reader(x, in, out);
			out.close();
		}
	}

	// helper
	private HuffNode node(BitInputStream x) {
		int y = x.readBits(1);
		if (y == -1) {
			throw new HuffException("Not a valid entry");
		}
		if (y == 0) {
			HuffNode left = node(x);
			HuffNode right = node(x);
			return new HuffNode(0, 0, left, right);
		} else {
			int value = x.readBits(BITS_PER_WORD + 1);
			return new HuffNode(value, 0, null, null);
		}
	}

	// another helper
	private void reader(HuffNode x, BitInputStream in, BitOutputStream out) {
		HuffNode y = x;
		while (true) {
			int bits = in.readBits(1);
			if (bits == -1) {
				throw new HuffException("Not valid");
			} else {
				if (bits == 0) {
					y = y.myLeft;
				} else {
					y = y.myRight;
				}

				if (y.myLeft == null && y.myRight == null) {
					if (y.myValue == PSEUDO_EOF) {
						break;
					} else {
						out.writeBits(BITS_PER_WORD, y.myValue);
						y = x;
					}
				}
			}
		}
	}
}