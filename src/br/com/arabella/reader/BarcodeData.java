package br.com.arabella.reader;

public class BarcodeData {
	protected int symbology = Barcode1DReader.CODE128;

	protected String value = "";

	protected int x = 0;

	protected int y = 0;

	public int getSymbology() {
		return this.symbology;
	}

	public void setSymbology(int s) {
		this.symbology = s;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String v) {
		this.value = v;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int i) {
		this.x = i;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int i) {
		this.y = i;
	}

	public String toString() {
		String s = "Type: ";

		if (this.symbology == Barcode1DReader.CODE128)
			s = s + "CODE128";
		if (this.symbology == Barcode1DReader.EAN8)
			s = s + "EAN8";
		if (this.symbology == Barcode1DReader.EAN13)
			s = s + "EAN13";
		if (this.symbology == Barcode1DReader.UPCA)
			s = s + "UPCA";
		if (this.symbology == Barcode1DReader.UPCE)
			s = s + "UPCE";
		if (this.symbology == Barcode1DReader.CODE39)
			s = s + "CODE39";
		if (this.symbology == Barcode1DReader.IDENTCODE)
			s = s + "IDENTCODE";
		if (this.symbology == Barcode1DReader.INTERLEAVED25)
			s = s + "INTERLEAVED25";

		s = s + "\n";

		s = s + "Value: " + this.value + "\n";
		s = s + "X: " + this.x + "\n";
		s = s + "Y: " + this.y + "\n";

		return s;
	}
}
