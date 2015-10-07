package br.com.atlas.reader.util;

import br.com.atlas.reader.RImage;

public class Histogram {
	private int MAX = 256;
	private int[] histogram = new int[this.MAX];
	private RImage image;

	public Histogram(RImage im) {
		this.image = im;

		int w = this.image.getWidth();
		int h = this.image.getHeight();

		boolean isGrey = this.image.getImage().getType() == 10;

		// int count = 0;

		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				// count++;

				if (isGrey) {
					this.histogram[this.image.getPixel(y, x)] += 1;
				} else {
					double v = this.image.getRPixel(y, x) * 0.3333D
							+ this.image.getGPixel(y, x) * 0.3333D
							+ this.image.getBPixel(y, x) * 0.3333D + 0.5D;
					this.histogram[(int) v] += 1;
				}
			}
	}

	public void stretch() {
		int hmin = 0;
		int hmax = 0;

		int saturated = 10;
		int threshold;
		if (saturated > 0.0D)
			threshold = (int) (this.image.getWidth() * this.image.getHeight()
					* saturated / 200.0D);
		else {
			threshold = 0;
		}

		int i = -1;
		boolean found = false;
		int count = 0;
		do {
			i++;
			count += this.histogram[i];
			found = count > threshold;
		} while ((!found) && (i < 255));
		hmin = i;

		i = 256;
		count = 0;
		do {
			i--;
			count += this.histogram[i];
			found = count > threshold;
		} while ((!found) && (i > 0));
		hmax = i;

		if (hmax <= hmin) {
			return;
		}

		int[] lookupTable = new int[256];
		for (int j = 0; j < 256; j++) {
			int v = j - hmin;

			v = (int) (256.0D * v / (hmax - hmin));
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			lookupTable[j] = v;
		}

		int w = this.image.getWidth();
		int h = this.image.getHeight();

		// int c = 0;

		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				// c++;

				int r = lookupTable[this.image.getRPixel(y, x)];
				int g = lookupTable[this.image.getGPixel(y, x)];
				int b = lookupTable[this.image.getBPixel(y, x)];
				this.image.setRPixel(y, x, r);
				this.image.setGPixel(y, x, g);
				this.image.setBPixel(y, x, b);
			}
	}

	public void equalize() {
		int w = this.image.getWidth();
		int h = this.image.getHeight();

		int[] normalized = new int[this.MAX];
		int sum = 0;
		double multiplier = (this.MAX - 1) / (w * h);
		for (int i = 0; i < this.MAX; i++) {
			sum += this.histogram[i];
			normalized[i] = (int) Math.round(sum * multiplier);
		}

		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				this.image
						.setPixel(y, x, normalized[this.image.getPixel(y, x)]);
	}

	public int getThreshold() {
		long t = 0L;
		long tnew = 128L;
		long c = 0L;

		int iteration = 0;
		long tmpi = 0L;
		try {
			while (tnew != t) {
				iteration++;
				t = tnew;
				double m2;
				double m1 = m2 = 0.0D;
				for (int i = 0; i < t; i++) {
					m1 += this.histogram[i] * i;
					c += this.histogram[i];
				}

				if (c == 0L)
					m1 = t;
				else {
					m1 /= c;
				}
				c = 0L;
				for (int i = (int) t; i < this.MAX; i++) {
					tmpi = i;
					m2 += this.histogram[i] * i;
					c += this.histogram[i];
				}

				if (c == 0L)
					m2 = t;
				else {
					m2 /= c;
				}
				tnew = (int) ((m1 + m2) / 2.0D);

				if (iteration > 40)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage() + " i=" + tmpi);
		}

		return (int) tnew;
	}
}