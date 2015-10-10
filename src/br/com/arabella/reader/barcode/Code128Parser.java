package br.com.arabella.reader.barcode;

import br.com.arabella.reader.Barcode1DReader;

public class Code128Parser extends BarcodeParser {
	private static String SETA = "A";
	private static String SETB = "B";
	private static String SETC = "C";
	String STOP = "2331112";
	String STARTA = "211412";
	String STARTB = "211214";
	String STARTC = "211232";

	protected String[] set128 = { "212222", "222122", "222221", "121223",
			"121322", "131222", "122213", "122312", "132212", "221213",
			"221312", "231212", "112232", "122132", "122231", "113222",
			"123122", "123221", "223211", "221132", "221231", "213212",
			"223112", "312131", "311222", "321122", "321221", "312212",
			"322112", "322211", "212123", "212321", "232121", "111323",
			"131123", "131321", "112313", "132113", "132311", "211313",
			"231113", "231311", "112133", "112331", "132131", "113123",
			"113321", "133121", "313121", "211331", "231131", "213113",
			"213311", "213131", "311123", "311321", "331121", "312113",
			"312311", "332111", "314111", "221411", "431111", "111224",
			"111422", "121124", "121421", "141122", "141221", "112214",
			"112412", "122114", "122411", "142112", "142211", "241211",
			"221114", "413111", "241112", "134111", "111242", "121142",
			"121241", "114212", "124112", "124211", "411212", "421112",
			"421211", "212141", "214121", "412121", "111143", "111341",
			"131141", "114113", "114311", "411113", "411311", "113141",
			"114131", "311141", "411131" };

	protected String[] set128A = { " ", "!", "\"", "#", "$", "%", "&", "'",
			"(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4",
			"5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A",
			"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[",
			"\\", "]", "^", "_", "", "\001", "\002", "\003", "\004", "\005",
			"\006", "\007", "\b", "\t", "\n", "\013", "\f", "\r", "\016",
			"\017", "\020", "\021", "\022", "\023", "\024", "\025", "\026",
			"\027", "\030", "\031", "\032", "\033", "\034", "\035", "\036",
			"\037", "_96", "_97", "_98", "_99", "_100", "_101", "_102" };

	protected String[] set128B = { " ", "!", "\"", "#", "$", "%", "&", "'",
			"(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4",
			"5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A",
			"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[",
			"\\", "]", "^", "_", "`", "a", "b", "c", "d", "e", "f", "g", "h",
			"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
			"v", "w", "x", "y", "z", "{", "_92", "}", "~", "_95", "_96", "_97",
			"_98", "_99", "_100", "_101", "_102" };

	protected String[] set128C = { "00", "01", "02", "03", "04", "05", "06",
			"07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
			"29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
			"40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
			"51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61",
			"62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72",
			"73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83",
			"84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94",
			"95", "96", "97", "98", "99", "_100", "_101", "_102" };

	public Code128Parser() {
		this.startPatterns = new String[] { this.STARTA, this.STARTB,
				this.STARTC };
		this.endPattern = this.STOP;
		this.maxModuleSize = 4;
		this.modulesProCharacter = 11;
	}

	public boolean parse() {
		this.code = "";
		super.resetPointer();

		boolean r = parse128();

		if (!r) {
			reverseBarcode();
			r = parse128();
		}

		return r;
	}

	public boolean parse128() {
		int SHIFT_B = 98;
		int LATCH_B = 100;
		int SHIFT_A = 98;
		int LATCH_A = 101;
		int LATCH_C = 99;
		String set = "";
		String shiftSet = null;
		int sum = 0;
		int weight = 1;

		String pattern = getPattern(6, true, new String[] { this.STARTA,
				this.STARTB, this.STARTC });

		if (pattern.equals(this.STARTA)) {
			set = SETA;
			sum = 103;
		} else if (pattern.equals(this.STARTB)) {
			set = SETB;
			sum = 104;
		} else if (pattern.equals(this.STARTC)) {
			sum = 105;
			set = SETC;
		} else {
			addDebug("Start pattern code 128 not recognized: " + pattern);
			return false;
		}

		do {
			pattern = getPattern(6, true, this.set128);

			int index = -1;

			String activeSet = set;
			if (shiftSet != null)
				activeSet = shiftSet;
			shiftSet = null;

			index = super.getPatternIndex(this.set128, pattern);
			if (index == -1) {
				addDebug("Pattern not recognized: " + pattern);
				return false;
			}
			sum += index * weight;

			if (activeSet.equals(SETA)) {
				if (index == SHIFT_B)
					shiftSet = SETB;
				else if (index == LATCH_B)
					set = SETB;
				else if (index == LATCH_C)
					set = SETC;
				else
					addToCode(this.set128A[index]);
			} else if (activeSet.equals(SETB)) {
				if (index == SHIFT_A)
					shiftSet = SETA;
				else if (index == LATCH_A)
					set = SETA;
				else if (index == LATCH_C)
					set = SETC;
				else {
					addToCode(this.set128B[index]);
				}
			} else if (activeSet.equals(SETC)) {
				if (index == LATCH_A)
					set = SETA;
				else if (index == LATCH_B)
					set = SETB;
				else {
					addToCode(this.set128C[index]);
				}
			}

			weight++;
		} while (this.pointer < this.barcode.length - 13);

		pattern = getPattern(6, true, this.set128);
		int checkSum = super.getPatternIndex(this.set128, pattern);
		int expected = sum % 103;
		if (checkSum != expected) {
			addDebug("Checksum does not match: " + checkSum + " <> " + expected);
			return false;
		}

		pattern = getPattern(7, true, new String[] { this.STOP });
		if (!pattern.equals(this.STOP)) {
			addDebug("Stop pattern code 128 not recognized: " + pattern);
			return false;
		}

		return true;
	}

	protected void addToCode(String s) {
		if (s.equals("_102"))
			super.addToCode(202);
		else
			super.addToCode(s);
	}

	public int getSymbology() {
		return Barcode1DReader.CODE128;
	}
}