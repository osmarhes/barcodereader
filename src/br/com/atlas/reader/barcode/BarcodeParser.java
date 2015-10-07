package br.com.atlas.reader.barcode;

public class BarcodeParser {
	protected double[] barcode = null;

	protected int pointer = 0;

	protected String code = "";

	protected boolean debug = System.getProperty("com.java4less.vision.debug",
			"0").equals("1");

	protected String[] startPatterns = { "11" };
	protected String endPattern = "11";
	protected int maxModuleSize = 4;
	protected int modulesProCharacter = 9;
	protected double[] knownBarPatterns = new double[10];
	protected double[] knownSpacePatterns = new double[10];
	protected boolean initializedPatterns = false;

	public boolean checkSumExpected = true;

	public String getParsedCode() {
		return this.code;
	}

	public void init(double[] widths) {
		this.barcode = widths;
	}

	public boolean parse() {
		return true;
	}

	protected void reverseBarcode() {
		double[] reversed = new double[this.barcode.length];

		for (int i = 0; i < this.barcode.length; i++) {
			reversed[(this.barcode.length - 1 - i)] = this.barcode[i];
		}
		resetPointer();

		this.barcode = reversed;
	}

	protected int getPatternIndex(String[] set, String pattern) {
		for (int i = 0; i < set.length; i++) {
			if (set[i].equals(pattern))
				return i;
		}

		return -1;
	}

	protected int getPatternIndex(String[][] set, int index, String pattern) {
		for (int i = 0; i < set.length; i++) {
			if (set[i][index].equals(pattern))
				return i;
		}

		return -1;
	}

	protected String getPattern(int numberOfBars, boolean movePointer,
			String[] allowedSet) {
		int j = this.pointer;
		String pattern = "";
		double[] errors = new double[numberOfBars];
		int moduleSum = 0;

		if (!initializePatterns())
			return "";

		for (int i = 0; (i < numberOfBars) && (j < this.barcode.length); i++) {
			if (movePointer)
				this.pointer += 1;
			double val = this.barcode[j];

			double[] knownPatterns = this.knownBarPatterns;
			if (j % 2 == 1)
				knownPatterns = this.knownSpacePatterns;

			if (val <= knownPatterns[1]) {
				pattern = pattern + "1";
				moduleSum++;
			} else if (val > knownPatterns[this.maxModuleSize]) {
				pattern = pattern + this.maxModuleSize;
				errors[i] = (val - knownPatterns[this.maxModuleSize]);
				moduleSum += this.maxModuleSize;
			} else {
				for (int h = 1; h < this.maxModuleSize; h++) {
					double dist = (knownPatterns[(h + 1)] - knownPatterns[h]) / 2.0D;
					if ((val <= knownPatterns[(h + 1)])
							&& (val >= knownPatterns[(h + 1)] - dist)) {
						pattern = pattern + (h + 1);
						errors[i] = (val - knownPatterns[(h + 1)]);
						moduleSum = moduleSum + h + 1;
						break;
					}
					if ((val >= knownPatterns[h])
							&& (val <= knownPatterns[h] + dist)) {
						pattern = pattern + h;
						errors[i] = (val - knownPatterns[h]);
						moduleSum += h;
						break;
					}
				}
			}

			j++;
		}

		if (allowedSet.length > 1) {
			while (moduleSum != this.modulesProCharacter) {
				int diff = this.modulesProCharacter - moduleSum;

				int candidate = -1;

				for (int i = 0; i < pattern.length(); i++) {
					if (errors[i] == 0.0D)
						continue;
					int currentModule = new Integer("" + pattern.charAt(i))
							.intValue();

					if ((diff < 0) && (currentModule > 1)) {
						if (candidate == -1)
							candidate = i;

						if (errors[i] < errors[candidate])
							candidate = i;

					}

					if ((diff > 0) && (currentModule < this.maxModuleSize)) {
						if (candidate == -1)
							candidate = i;

						if (errors[i] <= errors[candidate])
							continue;
						candidate = i;
					}

				}

				if (candidate < 0)
					break;
				int module = new Integer("" + pattern.charAt(candidate))
						.intValue();

				errors[candidate] = 0.0D;

				if (diff > 0) {
					module++;
					moduleSum++;
				} else {
					module--;
					moduleSum--;
				}

				pattern = pattern.substring(0, candidate) + module
						+ pattern.substring(candidate + 1);
			}

		}

		return pattern;
	}

	protected boolean initializePatterns() {
		if (this.initializedPatterns)
			return true;
		this.initializedPatterns = true;

		String knownPattern = "";
		double[] receivedPattern = null;
		if (this.startPatterns.length == 1) {
			knownPattern = this.startPatterns[0];
			receivedPattern = new double[knownPattern.length()];
			for (int i = 0; i < knownPattern.length(); i++)
				receivedPattern[i] = this.barcode[i];
		} else {
			knownPattern = this.endPattern;
			receivedPattern = new double[knownPattern.length()];
			for (int i = 0; i < knownPattern.length(); i++)
				receivedPattern[i] = this.barcode[(this.barcode.length
						- knownPattern.length() + i)];

		}

		for (int i = 0; i < knownPattern.length(); i++) {
			int currentModule = new Integer("" + knownPattern.charAt(i))
					.intValue();
			double[] knownPatterns = this.knownBarPatterns;
			if (i % 2 == 1)
				knownPatterns = this.knownSpacePatterns;

			if (knownPatterns[currentModule] == 0.0D) {
				knownPatterns[currentModule] = receivedPattern[i];
			} else {
				double diff = knownPatterns[currentModule] - receivedPattern[i];
				if (Math.abs(diff / knownPatterns[currentModule]) > 0.5D) {
					return false;
				}
			}

			if ((currentModule <= 1) || (knownPatterns[0] <= 0.0D))
				continue;
			double diff = knownPatterns[currentModule] - receivedPattern[i]
					/ knownPatterns[0];

			if (Math.abs(diff / knownPatterns[currentModule]) > 0.5D) {
				return false;
			}

		}

		completePattern();

		return true;
	}

	private void completePattern() {
		double correction = this.knownSpacePatterns[1]
				- this.knownBarPatterns[1];
		if (Math.abs(correction) > 0.75D)
			correction /= 2.0D;
		else {
			correction = 0.0D;
		}

		double[] p = this.knownBarPatterns;

		if ((p[2] == 0.0D) && (p[3] > 0.0D))
			p[2] = (p[1] + (p[3] - p[1]) / 2.0D);

		if (p[2] == 0.0D)
			p[2] = ((p[1] + correction) * 2.0D - correction);
		if (p[3] == 0.0D)
			p[3] = ((p[1] + correction) * 3.0D - correction);

		if (p[4] == 0.0D)
			p[4] = (p[3] + (p[2] - p[1]));

		p = this.knownSpacePatterns;

		if ((p[2] == 0.0D) && (p[3] > 0.0D))
			p[2] = (p[1] + (p[3] - p[1]) / 2.0D);

		if (p[2] == 0.0D)
			p[2] = ((p[1] - correction) * 2.0D + correction);
		if (p[3] == 0.0D)
			p[3] = ((p[1] - correction) * 3.0D + correction);

		if (p[4] == 0.0D)
			p[4] = (p[3] + (p[2] - p[1]));
	}

	protected void resetPointer() {
		this.pointer = 0;
		this.initializedPatterns = false;
		this.knownBarPatterns = new double[10];
		this.knownSpacePatterns = new double[10];
	}

	protected void addDebug(String s) {
		if (this.debug)
			System.out.println(s);
	}

	protected boolean endReached() {
		return this.pointer >= this.barcode.length;
	}

	protected void addToCode(String s) {
		if (s.equals("~"))
			this.code += "~";
		this.code += s;
	}

	protected void addToCode(int s) {
		String tmp = "" + s;
		if (tmp.length() < 3)
			tmp = "0" + tmp;
		if (tmp.length() < 3)
			tmp = "0" + tmp;

		this.code = (this.code + "~d" + tmp);
	}

	public int getSymbology() {
		return 0;
	}

	@SuppressWarnings("unused")
	private double getLeastSquares(int[] values) {
		int pts = values.length;

		int degree = 1;

		RMatrix Pm = new RMatrix(pts, degree + 1);
		RMatrix Ym = new RMatrix(pts, 1);

		double minx = values[0];
		double maxx = values[0];

		for (int i = 0; i < pts; i++) {
			if (values[i] < minx)
				minx = values[i];
			if (values[i] > maxx)
				maxx = values[i];

			for (int k = 0; k <= degree; k++)
				Pm.setValue(i, k, Math.pow(values[i], k));
			Ym.setValue(i, 0, values[i]);
		}

		RMatrix T = Pm.transpose();
		RMatrix MT = T.mult(Pm);
		RMatrix resultMatrix = MT.invert().mult(T).mult(Ym);
		double a1 = resultMatrix.getValue(0, 0);
		double a2 = resultMatrix.getValue(1, 0);

		int x1 = values[0];
		int y1 = (int) Math.round(a1 + x1 * a2);
		int x2 = values[(pts - 1)];
		int y2 = (int) Math.round(a1 + x2 * a2);

		return y1;
	}
}