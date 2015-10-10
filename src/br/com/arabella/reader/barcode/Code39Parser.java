package br.com.arabella.reader.barcode;

import br.com.arabella.reader.Barcode1DReader;

public class Code39Parser extends BarcodeParser {
	String START = "121121211";
	String STOP = "121121211";
	String SEPARATOR = "1";

	protected String[][] set39 = { { "0", "111221211" }, { "1", "211211112" },
			{ "2", "112211112" }, { "3", "212211111" }, { "4", "111221112" },
			{ "5", "211221111" }, { "6", "112221111" }, { "7", "111211212" },
			{ "8", "211211211" }, { "9", "112211211" }, { "A", "211112112" },
			{ "B", "112112112" }, { "C", "212112111" }, { "D", "111122112" },
			{ "E", "211122111" }, { "F", "112122111" }, { "G", "111112212" },
			{ "H", "211112211" }, { "I", "112112211" }, { "J", "111122211" },
			{ "K", "211111122" }, { "L", "112111122" }, { "M", "212111121" },
			{ "N", "111121122" }, { "O", "211121121" }, { "P", "112121121" },
			{ "Q", "111111222" }, { "R", "211111221" }, { "S", "112111221" },
			{ "T", "111121221" }, { "U", "221111112" }, { "V", "122111112" },
			{ "W", "222111111" }, { "X", "121121112" }, { "Y", "221121111" },
			{ "Z", "122121111" }, { "-", "121111212" }, { ".", "221111211" },
			{ " ", "122111211" }, { "$", "121212111" }, { "/", "121211121" },
			{ "+", "121112121" }, { "%", "111212121" }, { "*", "121121211" } };

	protected String[] set39Ext = { "%U", "$A", "$B", "$C", "$D", "$E", "$F",
			"$G", "$H", "$I", "$J", "$K", "$L", "$M", "$N", "$O", "$P", "$Q",
			"$R", "$S", "$T", "$U", "$V", "$W", "$X", "$Y", "$Z", "%A", "%B",
			"%C", "%D", "%E", " ", "/A", "/B", "/C", "/D", "/E", "/F", "/G",
			"/H", "/I", "/J", "/K", "/L", "-", ".", "/O", "0", "1", "2", "3",
			"4", "5", "6", "7", "8", "9", "/Z", "%F", "%G", "%H", "%I", "%J",
			"%V", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z", "%K", "%L", "%M", "%N", "%O", "%W", "+A", "+B", "+C", "+D",
			"+E", "+F", "+G", "+H", "+I", "+J", "+K", "+L", "+M", "+N", "+O",
			"+P", "+Q", "+R", "+S", "+T", "+U", "+V", "+W", "+X", "+Y", "+Z",
			"%P", "%Q", "%R", "%S", "%T" };

	String[] patterns = new String[this.set39.length];
	public boolean extended = false;

	public Code39Parser() {
		this.startPatterns = new String[] { this.START };
		this.endPattern = this.STOP;
		this.maxModuleSize = 2;
		int counter = 0;
		for (int i = 0; i < this.set39.length; i++) {
			this.patterns[(counter++)] = this.set39[i][1];
		}

		this.modulesProCharacter = 12;
	}

	public boolean parse() {
		this.checkSumExpected = false;

		this.code = "";
		super.resetPointer();

		boolean r = parse39();

		if (!r) {
			reverseBarcode();
			r = parse39();
		}

		return r;
	}

	public boolean parse39() {
		int sum = 0;

		String pattern = getPattern(9, true, new String[] { this.START });

		// String inter = getPattern(1, true, new String[] { this.SEPARATOR });

		if (!pattern.equals(this.START)) {
			addDebug("Start pattern code 39 not recognized: " + pattern);
			return false;
		}

		do {
			pattern = getPattern(9, true, this.patterns);

			// inter = getPattern(1, true, new String[] { this.SEPARATOR });

			int index = -1;

			index = super.getPatternIndex(this.set39, 1, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}

			sum += index;

			String c = this.set39[index][0];

			if ((this.extended)
					&& ((c.equals("$")) || (c.equals("%")) || (c.equals("/")) || (c
							.equals("+")))) {
				String controlChar = c;
				pattern = getPattern(9, true, this.patterns);

				// inter = getPattern(1, true, new String[] { this.SEPARATOR });

				index = super.getPatternIndex(this.set39, 1, pattern);
				if (index == -1) {
					addDebug("Pattern not recognized: " + pattern);
					return false;
				}
				c = this.set39[index][0];
				sum += index;

				pattern = controlChar + c;
				index = super.getPatternIndex(this.set39Ext, pattern);
				if (index == -1) {
					addDebug("Pattern not recognized: " + pattern);
					return false;
				}

				if (index >= 32)
					addToCode("" + (char) index);
				else
					addToCode(index);
			} else {
				addToCode(c);
			}
		} while (this.pointer < this.barcode.length - 19 - 2);

		pattern = getPattern(9, true, this.patterns);

		// inter = getPattern(1, true, new String[] { this.SEPARATOR });

		int checkSum = super.getPatternIndex(this.set39, 1, pattern);

		if (this.checkSumExpected) {
			int expected = sum % 43;
			if (checkSum != expected) {
				addDebug("Checksum does not match: " + checkSum + " <> "
						+ expected);
				return false;
			}

		} else {
			addToCode(this.set39[checkSum][0]);
		}

		pattern = getPattern(9, true, new String[] { this.STOP });
		if (!pattern.equals(this.STOP)) {
			addDebug("Stop pattern code 39 not recognized: " + pattern);
			return false;
		}

		return true;
	}

	public int getSymbology() {
		return Barcode1DReader.CODE39;
	}
}
