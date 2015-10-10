package br.com.arabella.reader.barcode;

import br.com.arabella.reader.Barcode1DReader;

public class CodeUPCEParser extends CodeEAN13Parser {
	protected String[] UPCESystem0 = { "EEEOOO", "EEOEOO", "EEOOEO", "EEOOOE",
			"EOEEOO", "EOOEEO", "EOOOEE", "EOEOEO", "EOEOOE", "EOOEOE" };

	protected String[] UPCESystem1 = { "OOOEEE", "OOEOEE", "OOEEOE", "OOEEEO",
			"OEOOEE", "OEEOOE", "OEEEOO", "OEOEOE", "OEOEEO", "OEEOEO" };

	protected String[][] setUPCEOdd = { { "0", "3211" }, { "1", "2221" },
			{ "2", "2122" }, { "3", "1411" }, { "4", "1132" }, { "5", "1231" },
			{ "6", "1114" }, { "7", "1312" }, { "8", "1213" }, { "9", "3112" } };

	protected String[][] setUPCEEven = { { "0", "1123" }, { "1", "1222" },
			{ "2", "2212" }, { "3", "1141" }, { "4", "2311" }, { "5", "1321" },
			{ "6", "4111" }, { "7", "2131" }, { "8", "3121" }, { "9", "2113" } };

	protected String[] patterns = new String[this.setUPCEOdd.length
			+ this.setUPCEEven.length];

	public CodeUPCEParser() {
		int counter = 0;
		for (int i = 0; i < this.setUPCEOdd.length; i++) {
			this.patterns[(counter++)] = this.setUPCEOdd[i][1];
		}

		for (int i = 0; i < this.setUPCEEven.length; i++)
			this.patterns[(counter++)] = this.setUPCEEven[i][1];
	}

	public boolean parse() {
		this.code = "";
		super.resetPointer();

		boolean r = parseUPCE();

		if (!r) {
			reverseBarcode();
			r = parseUPCE();
		}

		return r;
	}

	public boolean parseUPCE() {
		String pattern = getPattern(3, true,
				new String[] { CodeEAN13Parser.START });
		String parity = "";

		if (!pattern.equals(CodeEAN13Parser.START)) {
			addDebug("Start pattern code UPCE not recognized: " + pattern);
			return false;
		}

		for (int i = 0; i < 6; i++) {
			pattern = getPattern(4, true, this.patterns);

			int index = -1;

			index = super.getPatternIndex(this.setUPCEOdd, 1, pattern);
			if (index == -1) {
				index = super.getPatternIndex(this.setUPCEEven, 1, pattern);
				if (index == -1) {
					addDebug("Pattern not recognized: " + pattern);
					return false;
				}
				parity = parity + "E";
			} else {
				parity = parity + "O";
			}
			addToCode(this.setEANLeftA[index][0]);
		}

		int parityIndex = super.getPatternIndex(this.UPCESystem0, parity);
		if (parityIndex == -1)
			parityIndex = super.getPatternIndex(this.UPCESystem1, parity);
		if (parityIndex == -1) {
			addDebug("Parity not recognized: " + parity);
			return false;
		}

		pattern = getPattern(5, true, new String[] { CodeEAN13Parser.CENTER });

		if (!pattern.equals(CodeEAN13Parser.CENTER)) {
			addDebug("Stop guard pattern of code UPCE not recognized: "
					+ pattern);
			return false;
		}

		String tmpCode = this.code;

		if ((tmpCode.charAt(5) == '0') || (tmpCode.charAt(5) == '1')
				|| (tmpCode.charAt(5) == '2')) {
			tmpCode = "" + tmpCode.charAt(0) + tmpCode.charAt(1)
					+ tmpCode.charAt(5) + "0000" + tmpCode.charAt(2)
					+ tmpCode.charAt(3) + tmpCode.charAt(4);
		} else if (tmpCode.charAt(5) == '3') {
			tmpCode = "" + tmpCode.charAt(0) + tmpCode.charAt(1)
					+ tmpCode.charAt(2) + "00000" + tmpCode.charAt(3)
					+ tmpCode.charAt(4);
		} else if (tmpCode.charAt(5) == '4') {
			tmpCode = "" + tmpCode.charAt(0) + tmpCode.charAt(1)
					+ tmpCode.charAt(2) + tmpCode.charAt(3) + "00000"
					+ tmpCode.charAt(4);
		} else {
			tmpCode = "" + tmpCode.charAt(0) + tmpCode.charAt(1)
					+ tmpCode.charAt(2) + tmpCode.charAt(3) + tmpCode.charAt(4)
					+ "0000" + tmpCode.charAt(5);
		}

		String checksum = "" + parityIndex;

		String checksumExpected = UPCEANCheck(tmpCode);
		if (!checksumExpected.equals(checksum)) {
			addDebug("Checksum does not match: " + checksumExpected + " <> "
					+ checksum);
		}

		this.code = (tmpCode + checksum);

		return true;
	}

	public int getSymbology() {
		return Barcode1DReader.UPCE;
	}
}