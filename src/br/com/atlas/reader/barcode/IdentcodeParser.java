package br.com.atlas.reader.barcode;

import br.com.atlas.reader.Barcode1DReader;

public class IdentcodeParser extends Code25InterleavedParser {
	public boolean parse25() {
		this.checkSumExpected = false;
		return super.parse25();
	}

	protected int getChecksum(String data) {
		int sum = 0;
		for (int i = 0; i < data.length(); i++) {
			int v = new Integer("" + data.charAt(i)).intValue();
			if (i % 2 == 0)
				sum += v * 4;
			else {
				sum += v * 9;
			}
		}
		return sum % 10;
	}

	public int getSymbology() {
		return Barcode1DReader.IDENTCODE;
	}
}
