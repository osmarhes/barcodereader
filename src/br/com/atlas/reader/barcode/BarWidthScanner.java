package br.com.atlas.reader.barcode;

import br.com.atlas.reader.recognition.Line;
import br.com.atlas.reader.util.ImageDebugger;

public class BarWidthScanner {
	boolean debug = System.getProperty("com.java4less.vision.debug", "0")
			.equals("1");

	public int[] convertToModules(double[] bars) {
		if (this.debug)
			createHistogram(bars);

		double minVal = bars[0];
		for (int i = 0; i < bars.length; i += 2) {
			if (bars[i] >= minVal)
				continue;
			minVal = bars[i];
		}
		double minSpace = bars[1];
		for (int i = 1; i < bars.length; i += 2) {
			if (bars[i] >= minSpace)
				continue;
			minSpace = bars[i];
		}

		int[] result2 = new int[bars.length];

		for (int i = 0; i < bars.length; i++) {
			if (i % 2 == 0)
				bars[i] /= minVal;
			else
				bars[i] /= minSpace;
			result2[i] = (int) Math.round(bars[i]);
			if (Math.abs(bars[i] - result2[i]) <= 0.4D)
				continue;
			System.out.println("**** Can't be sure about bar width (" + bars[i]
					+ ") ****");
		}

		return result2;
	}

	private void createHistogram(double[] bars) {
		int[] hist = new int[700];

		for (int i = 0; i < bars.length; i++) {
			hist[(int) (bars[i] * 10.0D)] += 1;
		}
		ImageDebugger ie = new ImageDebugger(null);
		int minx = 20;
		int maxx = 620;
		int miny = 20;
		int maxy = 220;
		Line x = new Line(minx, miny, maxx, miny);
		Line y = new Line(minx, miny, minx, maxy);
		ie.exportLine(x);
		ie.exportLine(y);

		int counter = 0;
		for (int i = minx; i < maxx; i += 10) {
			ie.exportLine(new Line(i, miny, i, miny - 5));
			ie.exportText(i, miny - 5, "" + (i - minx) / 5);
			counter++;
			counter++;
		}

		counter = 0;
		for (int i = miny; i < maxy; i += 10) {
			ie.exportLine(new Line(minx, i, minx - 5, i));
			ie.exportText(minx - 20, i, "" + counter);
			counter++;
			counter++;
		}

		for (int i = 0; i < hist.length; i++) {
			if (hist[i] > 0) {
				Line l = new Line(i * 5 + minx, miny, i * 5 + minx, hist[i] * 5
						+ miny);
				ie.exportLine(l);
			}
		}
		ie.export("barwidths.png");
	}
}