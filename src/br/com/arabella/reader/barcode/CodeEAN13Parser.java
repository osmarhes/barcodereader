package br.com.arabella.reader.barcode;

import br.com.arabella.reader.Barcode1DReader;

public class CodeEAN13Parser extends BarcodeParser {
	protected static String STOP = "111";
	protected static String START = "111";
	protected static String CENTER = "11111";

	protected String[][] setEANLeftA = { { "0", "3211" }, { "1", "2221" },
			{ "2", "2122" }, { "3", "1411" }, { "4", "1132" }, { "5", "1231" },
			{ "6", "1114" }, { "7", "1312" }, { "8", "1213" }, { "9", "3112" } };

	protected String[][] setEANLeftB = { { "0", "1123" }, { "1", "1222" },
			{ "2", "2212" }, { "3", "1141" }, { "4", "2311" }, { "5", "1321" },
			{ "6", "4111" }, { "7", "2131" }, { "8", "3121" }, { "9", "2113" } };

	protected String[][] setEANRight = { { "0", "3211" }, { "1", "2221" },
			{ "2", "2122" }, { "3", "1411" }, { "4", "1132" }, { "5", "1231" },
			{ "6", "1114" }, { "7", "1312" }, { "8", "1213" }, { "9", "3112" } };

	protected String[] setEANCode = { "AAAAA", "ABABB", "ABBAB", "ABBBA",
			"BAABB", "BBAAB", "BBBAA", "BABAB", "BABBA", "BBABA" };
	String[] eanPatternLeft = null;
	String[] eanPatternRight = null;

	public CodeEAN13Parser() {
		this.startPatterns = new String[] { START };
		this.endPattern = STOP;
		this.maxModuleSize = 4;
		this.eanPatternLeft = new String[this.setEANLeftA.length
				+ this.setEANLeftB.length];
		this.eanPatternRight = new String[this.setEANRight.length];
		int counter = 0;
		for (int i = 0; i < this.setEANLeftA.length; i++) {
			this.eanPatternLeft[(counter++)] = this.setEANLeftA[i][1];
		}
		for (int i = 0; i < this.setEANLeftB.length; i++) {
			this.eanPatternLeft[(counter++)] = this.setEANLeftB[i][1];
		}
		counter = 0;
		for (int i = 0; i < this.setEANRight.length; i++) {
			this.eanPatternRight[(counter++)] = this.setEANRight[i][1];
		}

		this.modulesProCharacter = 7;
	}

	public boolean parse() {
		this.code = "";
		super.resetPointer();

		boolean r = parse13();

		if (!r) {
			reverseBarcode();
			r = parse13();
		}

		return r;
	}

	public boolean parse13() {
		String parity = "";

		String pattern = getPattern(3, true, new String[] { START });

		if (!pattern.equals(START)) {
			addDebug("Start pattern code EAN 13 not recognized: " + pattern);
			return false;
		}

		for (int i = 0; i < 6; i++) {
			pattern = getPattern(4, true, this.eanPatternLeft);

			int index = -1;

			index = super.getPatternIndex(this.setEANLeftA, 1, pattern);
			if (index == -1) {
				if (i > 0)
					index = super.getPatternIndex(this.setEANLeftB, 1, pattern);

				if (index == -1) {
					addDebug("Pattern not recognized: " + pattern);
					return false;
				}
				if (i > 0)
					parity = parity + "B";
			} else if (i > 0) {
				parity = parity + "A";
			}
			addToCode(this.setEANLeftA[index][0]);
		}

		int parityIndex = super.getPatternIndex(this.setEANCode, parity);
		this.code = ("" + parityIndex + this.code);

		pattern = getPattern(5, true, new String[] { CENTER });

		if (!pattern.equals(CENTER)) {
			addDebug("Center guard pattern of code EAN 13 not recognized: "
					+ pattern);
			return false;
		}

		for (int i = 0; i < 6; i++) {
			pattern = getPattern(4, true, this.eanPatternRight);

			int index = -1;

			index = super.getPatternIndex(this.setEANRight, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}

			String c = this.setEANRight[index][0];

			if (i == 5) {
				String checksum = UPCEANCheck(this.code);
				if (!c.equals(checksum)) {
					addDebug("Checksum does not match: " + c + " <> "
							+ checksum);
				}
			}

			addToCode(c);
		}

		pattern = getPattern(3, true, new String[] { STOP });

		if (!pattern.equals(STOP)) {
			addDebug("End pattern code EAN 13 not recognized: " + pattern);
			return false;
		}

		return true;
	}

	protected String UPCEANCheck(String s) {
		boolean odd = true;
		int sumodd = 0;
		int sum = 0;

		for (int i = s.length() - 1; i >= 0; i--) {
			if (odd)
				sumodd += new Integer("" + s.charAt(i)).intValue();
			else
				sum += new Integer("" + s.charAt(i)).intValue();
			odd = !odd;
		}

		sum = sumodd * 3 + sum;

		int c = sum % 10;

		if (c != 0)
			c = 10 - c;

		return "" + c;
	}

	public int getSymbology() {
		return Barcode1DReader.EAN13;
	}
}