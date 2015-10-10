package br.com.arabella.reader.util;

import br.com.arabella.reader.RImage;

public class ImageTools {
	public static RImage toBackWhite(RImage image) {
		return applyThreshold(image);
	}

	public static RImage applyAdaptativeThreshold(RImage image) {
		return null;
	}

	public static RImage applyThreshold(RImage image) {
		int h = image.getHeight();
		int w = image.getWidth();

		RImage outImage = image;

		Histogram hist = new Histogram(image);
		int threshold = hist.getThreshold();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int grey = image.getGreyPixel(y, x);

				if (grey > threshold)
					outImage.setPixel(y, x, RImage.BACKGROUND);
				else {
					outImage.setPixel(y, x, RImage.FOREGROUND);
				}
			}
		}

		return outImage;
	}
}