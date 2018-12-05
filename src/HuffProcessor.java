
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
	public void compress(BitInputStream in, BitOutputStream out) {

		while (true) {
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1)
				break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
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
	//	if (bits < 0) {
	//		throw new HuffException("Not a valid entry.");
	//	}
		if (bits != HUFF_TREE) {
			throw new HuffException("Not a valid entry.");
		}

		HuffNode x = node(in);
		//y(x, in, out);

		while (true) {
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1)
				break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
	}
	//helper
	private HuffNode node(BitInputStream x) {
		int y = x.readBits(BITS_PER_INT);
		if(y == -1) {
			throw new HuffException("Not a valid entry");
		}
		if(y == 0) {
			
			HuffNode z = node(x);
			HuffNode left = z.myLeft;
			HuffNode right = z.myRight;
			return new HuffNode(0,0,left,right);
		}
		else {
			int value = x.readBits(BITS_PER_WORD+1);
			return new HuffNode(value,0,null,null);
		}
	}
	//another helper
	private void y(HuffNode x, BitInputStream in, BitOutputStream out) {
		int y = in.readBits(BITS_PER_INT);
		while( y != PSEUDO_EOF) {
		  y = in.readBits(BITS_PER_WORD);
		}
	}
}