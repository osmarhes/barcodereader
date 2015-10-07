package br.com.atlas.reader.barcode;

import br.com.atlas.reader.Barcode1DReader;

public class Code25InterleavedParser extends BarcodeParser {
	String START = "1111";
	String STOP = "211";

	protected String[][] set25 = { { "0", "11221" }, { "1", "21112" },
			{ "2", "12112" }, { "3", "22111" }, { "4", "11212" },
			{ "5", "21211" }, { "6", "12211" }, { "7", "11122" },
			{ "8", "21121" }, { "9", "12121" } };

	String[] patterns = new String[this.set25.length];

	public Code25InterleavedParser() {
		this.startPatterns = new String[] { this.START };
		this.endPattern = this.STOP;
		this.maxModuleSize = 2;
		int counter = 0;
		for (int i = 0; i < this.set25.length; i++) {
			this.patterns[(counter++)] = this.set25[i][1];
		}

		this.modulesProCharacter = 14;
	}

	public boolean parse() {
		this.checkSumExpected = false;

		this.code = "";
		super.resetPointer();

		boolean r = parse25();

		if (!r) {
			reverseBarcode();
			r = parse25();
		}

		return r;
	}

	public boolean parse25() {
		String decoded = "";

		String pattern = getPattern(4, true, new String[] { this.START });

		if (!pattern.equals(this.START)) {
			addDebug("Start pattern code 25 not recognized: " + pattern);
			return false;
		}

		int index = -1;
		while (this.pointer < this.barcode.length - 3) {
			String tmp1 = getPattern(10, true, this.patterns);

			if (tmp1.length() != 10) {
				addDebug("Barcode I 25 too short");
				return false;
			}

			String pattern1 = "" + tmp1.charAt(0) + tmp1.charAt(2)
					+ tmp1.charAt(4) + tmp1.charAt(6) + tmp1.charAt(8);
			String pattern2 = "" + tmp1.charAt(1) + tmp1.charAt(3)
					+ tmp1.charAt(5) + tmp1.charAt(7) + tmp1.charAt(9);

			index = super.getPatternIndex(this.set25, 1, pattern1);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			decoded = decoded + this.set25[index][0];

			index = super.getPatternIndex(this.set25, 1, pattern2);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			decoded = decoded + this.set25[index][0];
		}

		if (this.checkSumExpected) {
			int expected = getChecksum(decoded.substring(0,
					decoded.length() - 1));
			int checkSum = index;

			if (checkSum != expected) {
				addDebug("Checksum does not match: " + checkSum + " <> "
						+ expected);
				return false;
			}
		}

		super.addToCode(decoded);

		pattern = getPattern(3, true, new String[] { this.STOP });
		if (!pattern.equals(this.STOP)) {
			addDebug("Stop pattern code 25 not recognized: " + pattern);
			return false;
		}

		return true;
	}

	protected int getChecksum(String data) {
		int sum = 0;
		for (int i = 0; i < data.length(); i++) {
			int v = new Integer("" + data.charAt(i)).intValue();
			if (i % 2 == 0)
				sum += v * 3;
			else {
				sum += v;
			}
		}
		return sum % 10;
	}

	public int getSymbology() {
		return Barcode1DReader.INTERLEAVED25;
	}
}
