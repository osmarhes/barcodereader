package br.com.atlas.reader.recognition;

import java.util.Vector;

import br.com.atlas.reader.RImage;

public class PAVectorizer {
	Vector<Point> points;
	public int minArea;
	public int maxArea;
	// private boolean[][] visited;
	Point pointer;
	private int currentI;
	private int currentJ;
	public long findObjectStats;
	protected static boolean showStats = System.getProperty(
			"com.java4less.vision.stats", "0").equals("1");

	public PAVectorizer() {
		this.points = new Vector<>();

		this.minArea = Integer.parseInt(System.getProperty(
				"com.java4less.vision.minarea", "30"));

		this.maxArea = Integer.parseInt(System.getProperty(
				"com.java4less.vision.maxarea", "9000"));

		// this.visited = null;
		this.pointer = new Point(0.0D, 0.0D);

		this.currentI = 0;
		this.currentJ = 0;
		this.findObjectStats = 0L;
	}

	public VectorizedImage vectorize(RImage image) {
		VectorizedImage result = new VectorizedImage();
		Vector<ImageObject> objects = new Vector<>();
		long prv = System.currentTimeMillis();

		image.initializeVisited();

		prv = System.currentTimeMillis();

		this.currentI = 0;
		this.currentJ = 0;
		ImageObject object = findObject(image);
		while (object != null) {
			if ((object.getArea() > this.minArea)
					&& (object.getArea() < this.maxArea)) {
				objects.add(object);
			}

			object = findObject(image);
		}

		image.initializeVisited();
		if (showStats)
			System.out.println("Time required to create objects "
					+ this.findObjectStats);
		prv = System.currentTimeMillis();
		for (int i = 0; i < objects.size(); i++) {
			object = (ImageObject) objects.elementAt(i);

			object.calculatePA(image);
			result.addObject(object);
		}

		if (showStats)
			System.out.println("Time required to calculate PAs "
					+ (System.currentTimeMillis() - prv));
		return result;
	}

	private ImageObject findObject(RImage image) {
		long prv = System.currentTimeMillis();
		int h = image.getHeight();
		int w = image.getWidth();

		while (this.currentJ < h) {
			this.currentI = 0;
			while (this.currentI < w) {
				if (!image.getVisited(this.currentI, this.currentJ)) {
					image.setVisited(this.currentI, this.currentJ);
					if (image.getPixel(this.currentJ, this.currentI) == RImage.FOREGROUND) {
						ImageObject obj = new ImageObject(image, new Point(
								this.currentI, this.currentJ));
						this.findObjectStats += System.currentTimeMillis()
								- prv;
						return obj;
					}
				}
				this.currentI += 1;
			}
			this.currentJ += 1;
		}

		this.findObjectStats += System.currentTimeMillis() - prv;

		return null;
	}
}