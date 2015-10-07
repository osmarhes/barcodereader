package br.com.atlas.reader.barcode;

import br.com.atlas.reader.Barcode1DReader;

public class CodeEAN8Parser extends CodeEAN13Parser {

	public CodeEAN8Parser() {
		String eanPatternLeft[] = new String[setEANLeftA.length];
		int counter = 0;
		for (int i = 0; i < setEANLeftA.length; i++)
			eanPatternLeft[counter++] = setEANLeftA[i][1];

	}

	public boolean parse() {
		super.code = "";
		super.resetPointer();
		boolean r = parse8();
		if (!r) {
			reverseBarcode();
			r = parse8();
		}
		return r;
	}

	public boolean parse8() {
		String pattern = getPattern(3, true,
				new String[] { CodeEAN13Parser.START });
		if (!pattern.equals(CodeEAN13Parser.START)) {
			addDebug("Start pattern code EAN 8 not recognized: " + pattern);
			return false;
		}
		for (int i = 0; i < 4; i++) {
			pattern = getPattern(4, true, eanPatternLeft);
			int index = -1;
			index = super.getPatternIndex(setEANLeftA, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			addToCode(setEANLeftA[index][0]);
		}

		pattern = getPattern(5, true, new String[] { CodeEAN13Parser.CENTER });
		if (!pattern.equals(CodeEAN13Parser.CENTER)) {
			addDebug("Center guard pattern of code EAN 8 not recognized: "
					+ pattern);
			return false;
		}
		for (int i = 0; i < 4; i++) {
			pattern = getPattern(4, true, eanPatternRight);
			int index = -1;
			index = super.getPatternIndex(setEANRight, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			String c = setEANRight[index][0];
			if (i == 3) {
				String checksum = super.UPCEANCheck(super.code);
				if (!c.equals(checksum))
					addDebug("Checksum does not match: " + c + " <> "
							+ checksum);
			}
			addToCode(c);
		}

		pattern = getPattern(3, true, new String[] { CodeEAN13Parser.STOP });
		if (!pattern.equals(CodeEAN13Parser.STOP)) {
			addDebug("End pattern code EAN 8 not recognized: " + pattern);
			return false;
		} else {
			return true;
		}
	}

	public int getSymbology() {
		return Barcode1DReader.EAN8;
	}
}
