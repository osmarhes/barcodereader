package br.com.arabella.reader.barcode;

import br.com.arabella.reader.Barcode1DReader;

public class CodeUPCAParser extends CodeEAN13Parser {

	public CodeUPCAParser() {
		eanPatternLeft = new String[setUPCALeft.length];
		eanPatternRight = new String[setEANRight.length];
		int counter = 0;
		for (int i = 0; i < setEANLeftA.length; i++)
			eanPatternLeft[counter++] = setUPCALeft[i][1];

		counter = 0;
		for (int i = 0; i < setUPCARight.length; i++)
			eanPatternRight[counter++] = setUPCARight[i][1];

	}

	public boolean parse() {
		super.code = "";
		super.resetPointer();
		boolean r = parseUPCA();
		if (!r) {
			reverseBarcode();
			r = parseUPCA();
		}
		return r;
	}

	public boolean parseUPCA() {
		String pattern = getPattern(3, true,
				new String[] { CodeEAN13Parser.START });
		if (!pattern.equals(CodeEAN13Parser.START)) {
			addDebug("Start pattern code UPCA not recognized: " + pattern);
			return false;
		}
		for (int i = 0; i < 6; i++) {
			pattern = getPattern(4, true, eanPatternLeft);
			int index = -1;
			index = super.getPatternIndex(setUPCALeft, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			addToCode(setUPCALeft[index][0]);
		}

		pattern = getPattern(5, true, new String[] { CodeEAN13Parser.CENTER });
		if (!pattern.equals(CodeEAN13Parser.CENTER)) {
			addDebug("Center guard pattern of code UPCA not recognized: "
					+ pattern);
			return false;
		}
		for (int i = 0; i < 6; i++) {
			pattern = getPattern(4, true, eanPatternRight);
			int index = -1;
			index = super.getPatternIndex(setUPCARight, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			String c = setUPCARight[index][0];
			if (i == 5) {
				String checksum = super.UPCEANCheck(super.code);
				if (!c.equals(checksum))
					addDebug("Checksum does not match: " + c + " <> "
							+ checksum);
			}
			addToCode(c);
		}

		pattern = getPattern(3, true, new String[] { CodeEAN13Parser.STOP });
		if (!pattern.equals(CodeEAN13Parser.STOP)) {
			addDebug("End pattern code UPCA not recognized: " + pattern);
			return false;
		} else {
			return true;
		}
	}

	public int getSymbology() {
		return Barcode1DReader.UPCA;
	}

	protected String setUPCALeft[][] = { { "0", "3211" }, { "1", "2221" },
			{ "2", "2122" }, { "3", "1411" }, { "4", "1132" }, { "5", "1231" },
			{ "6", "1114" }, { "7", "1312" }, { "8", "1213" }, { "9", "3112" } };
	protected String setUPCARight[][] = { { "0", "3211" }, { "1", "2221" },
			{ "2", "2122" }, { "3", "1411" }, { "4", "1132" }, { "5", "1231" },
			{ "6", "1114" }, { "7", "1312" }, { "8", "1213" }, { "9", "3112" } };
}