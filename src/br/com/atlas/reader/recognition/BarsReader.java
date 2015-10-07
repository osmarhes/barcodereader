package br.com.atlas.reader.recognition;

import java.awt.Polygon;
import java.util.Vector;

import br.com.atlas.reader.RImage;
import br.com.atlas.reader.util.ImageDebugger;

public class BarsReader {
	public static int ALG_AREAS = 0;
	public static int ALG_LINE = 1;
	public static int ALG_HISTOGRAM = 2;

	public double[] convertBarsToWidths(int algorithm, RImage image,
			Barcode1DObject barcode) {
		if (algorithm == ALG_AREAS)
			return convertAreasToBars(image, barcode);
		if (algorithm == ALG_HISTOGRAM)
			return convertHistogramToBars(image, barcode);
		return convertCentralLineToBars(image, barcode);
	}

	private double[] convertHistogramToBars(RImage image,
			Barcode1DObject barcode) {
		Vector<Double> values = new Vector<>();
		double counter = 0.0D;
		double totalCounter = 0.0D;
		// int currentValue = 0;
		Point startPoint = barcode.corner2;
		Point endPoint = barcode.corner3;

		// Line baseLine = new Line(startPoint, endPoint);
		double angle = barcode.getOrientation() + 90.0D;
		double len = barcode.getHeight();
		double ystep = len * Math.cos(Math.toRadians(angle));
		double xstep = len * Math.sin(Math.toRadians(angle));

		LineWalker walker = new LineWalker((int) startPoint.x,
				(int) startPoint.y, (int) endPoint.x, (int) endPoint.y);

		Point p = walker.getNextPoint();
		Point previousPointH = p;

		while (p != null) {
			if ((Math.round(p.x) != Math.round(previousPointH.x))
					|| (Math.round(p.y) != Math.round(previousPointH.y))) {
				Point basePoint = p;
				Point verticalEnd = new Point(Math.round(basePoint.x - xstep),
						Math.round(basePoint.y + ystep));
				LineWalker verticalWalker = new LineWalker((int) basePoint.x,
						(int) basePoint.y, (int) verticalEnd.x,
						(int) verticalEnd.y);

				counter = 0.0D;
				totalCounter = 0.0D;

				Point p2 = verticalWalker.getNextPoint();
				Point previousPointV = p2;
				while (p2 != null) {
					if ((Math.round(p2.x) != Math.round(previousPointV.x))
							|| (Math.round(p2.y) != Math
									.round(previousPointV.y))) {
						totalCounter += 1.0D;

						int value = image.getPixel(p2);
						if (value == RImage.FOREGROUND)
							counter += 1.0D;

					}

					previousPointV = p2;
					p2 = verticalWalker.getNextPoint();
				}

				values.add(new Double(counter / totalCounter));
			}

			previousPointH = p;
			p = walker.getNextPoint();
		}

		System.out.println("");
		for (int i = 0; i < values.size(); i++) {
			System.out.print(((Double) values.elementAt(i)).doubleValue()
					+ " , ");
		}
		System.out.println("");

		return null;
	}

	private double[] convertCentralLineToBars(RImage image,
			Barcode1DObject barcode) {
		Vector<Integer> result = new Vector<>();
		Vector<Integer> values = new Vector<>();
		int counter = 0;
		int currentValue = 0;
		Point startPoint = new Line(barcode.corner1, barcode.corner2)
				.getMiddlePoint();
		Point endPoint = new Line(barcode.corner3, barcode.corner4)
				.getMiddlePoint();
		ImageDebugger debugger = new ImageDebugger(image.getImage());

		LineWalker walker = new LineWalker((int) startPoint.x,
				(int) startPoint.y, (int) endPoint.x, (int) endPoint.y);

		Point p = walker.getNextPoint();
		debugger.exportPoint(p);
		Point previousPoint = p;
		currentValue = image.getPixel(p);
		counter++;

		while (p != null) {
			if ((Math.round(p.x) != Math.round(previousPoint.x))
					|| (Math.round(p.y) != Math.round(previousPoint.y))) {
				debugger.exportPoint(p);
				int value = image.getPixel(p);

				if (value != currentValue) {
					values.add(new Integer(currentValue));
					result.add(new Integer(counter));
					currentValue = value;
					counter = 1;
				} else {
					counter++;
				}
			}

			previousPoint = p;
			p = walker.getNextPoint();
		}

		values.add(new Integer(currentValue));
		result.add(new Integer(counter));

		if (((Integer) values.elementAt(values.size() - 1)).intValue() == RImage.BACKGROUND) {
			result.removeElementAt(values.size() - 1);
			values.removeElementAt(values.size() - 1);
		}
		if (((Integer) values.elementAt(0)).intValue() == RImage.BACKGROUND) {
			result.removeElementAt(0);
			values.removeElementAt(0);
		}

		double[] resultDouble = new double[result.size()];
		for (int i = 0; i < result.size(); i++)
			resultDouble[i] = ((Integer) result.elementAt(i)).intValue();

		double minVal = resultDouble[0];
		for (int i = 0; i < resultDouble.length; i++) {
			if (resultDouble[i] >= minVal)
				continue;
			minVal = resultDouble[i];
		}

		double[] result2 = new double[resultDouble.length];

		for (int i = 0; i < resultDouble.length; i++) {
			resultDouble[i] /= minVal;
			result2[i] = Math.round(resultDouble[i]);
			if (Math.abs(resultDouble[i] - result2[i]) <= 0.4D)
				continue;
			System.out.println("**** Can't be sure about bar width ("
					+ resultDouble[i] + ") ****");
		}

		return result2;
	}

	private double[] convertAreasToBars(RImage image, Barcode1DObject barcode) {
		double[] result = new double[barcode.bars.size() * 2 - 1];

		for (int i = 0; i < barcode.bars.size(); i++) {
			ImageObject object = (ImageObject) barcode.bars.elementAt(i);

			int areaSize = object.getArea();
			double barWidth = Math.abs(areaSize / object.getLength());

			result[(i * 2)] = barWidth;
		}

		long prv = System.currentTimeMillis();

		for (int i = 0; i < barcode.bars.size() - 1; i++) {
			Line line1 = ((ImageObject) barcode.bars.elementAt(i)).getPA();
			Line line2 = ((ImageObject) barcode.bars.elementAt(i + 1)).getPA();

			if (line1.getLength() > line2.getLength()) {
				Line tmp = line2;
				line2 = line1;
				line1 = tmp;
			}

			Line perp1 = line1.getPerp(new Point(line1.x1, line1.y1), 500.0D);
			Line perp2 = line1.getPerp(new Point(line1.x2, line1.y2), 500.0D);

			Point intersect1 = line2.getIntersect(perp1);
			Point intersect2 = line2.getIntersect(perp2);

			Polygon pol = new Polygon();

			pol.addPoint((int) line1.x1, (int) line1.y1);
			pol.addPoint((int) line1.x2, (int) line1.y2);
			pol.addPoint((int) intersect2.x, (int) intersect2.y);
			pol.addPoint((int) intersect1.x, (int) intersect1.y);

			Point pointInPolygon = new Line(line1.x1, line1.y1, intersect2.x,
					intersect2.y).getMiddlePoint();
			pointInPolygon.x = Math.round(pointInPolygon.x);
			pointInPolygon.y = Math.round(pointInPolygon.y);
			if (!pol.contains(pointInPolygon.x, pointInPolygon.y)) {
				throw new RuntimeException(
						"No point in polygon could be located");
			}
			result[(i * 2 + 1)] = getPolygonColorSize(image, pol,
					pointInPolygon, RImage.BACKGROUND);

			result[(i * 2 + 1)] = Math.abs(result[(i * 2 + 1)]
					/ line1.getLength());
		}

		if (PAVectorizer.showStats)
			System.out.println("Calculate areas time "
					+ (System.currentTimeMillis() - prv) + "   "
					+ barcode.bars.size());
		return result;
	}

	public int getAreaSize(RImage image, Point startingPoint, int black) {
		Vector<Point> pendingPoints = new Vector<>();
		boolean[][] seen = new boolean[image.getWidth()][image.getHeight()];

		int size = 1;

		Point p = startingPoint;
		while (p != null) {
			if (p.x > 1.0D) {
				Point p2 = new Point(p.x - 1.0D, p.y);

				if ((seen[(int) p2.x][(int) p2.y] == false)
						&& (image.getPixel(p2) == black)) {
					size++;
					pendingPoints.add(p2);
				}
				seen[(int) p2.x][(int) p2.y] = true;
			}

			if (p.x < image.getWidth() - 1) {
				Point p2 = new Point(p.x + 1.0D, p.y);
				if ((seen[(int) p2.x][(int) p2.y] == false)
						&& (image.getPixel(p2) == black)) {
					size++;
					pendingPoints.add(p2);
				}
				seen[(int) p2.x][(int) p2.y] = true;
			}

			if (p.y > 1.0D) {
				Point p2 = new Point(p.x, p.y - 1.0D);
				if ((seen[(int) p2.x][(int) p2.y] == false)
						&& (image.getPixel(p2) == black)) {
					size++;
					pendingPoints.add(p2);
				}
				seen[(int) p2.x][(int) p2.y] = true;
			}

			if (p.y < image.getHeight() - 1) {
				Point p2 = new Point(p.x, p.y + 1.0D);
				if ((seen[(int) p2.x][(int) p2.y] == false)
						&& (image.getPixel(p2) == black)) {
					size++;
					pendingPoints.add(p2);
				}
				seen[(int) p2.x][(int) p2.y] = true;
			}

			p = null;
			if (pendingPoints.size() > 0) {
				p = (Point) pendingPoints.elementAt(0);
				pendingPoints.removeElementAt(0);
			}

		}

		return size;
	}

	private int getPolygonColorSize(RImage image, Polygon pol,
			Point startingPoint, int color) {
		Vector<Point> pendingPoints = new Vector<>();
		int w = image.getWidth();
		int h = image.getHeight();
		boolean[] seen = new boolean[w * h];

		Point p = startingPoint;
		int size = 0;
		if (image.getPixel(p) == color)
			size++;
		seen[((int) p.x + (int) p.y * w)] = true;

		while (p != null) {
			if (p.x > 1.0D) {
				Point p2 = new Point(p.x - 1.0D, p.y);

				if ((seen[((int) p2.x + (int) p2.y * w)] == false)
						&& (pol.contains((int) p2.x, (int) p2.y))) {
					if (image.getPixel(p2) == color)
						size++;
					pendingPoints.add(p2);
				}
				seen[((int) p2.x + (int) p2.y * w)] = true;
			}

			if (p.x < image.getWidth() - 1) {
				Point p2 = new Point(p.x + 1.0D, p.y);
				if ((seen[((int) p2.x + (int) p2.y * w)] == false)
						&& (pol.contains((int) p2.x, (int) p2.y))) {
					if (image.getPixel(p2) == color)
						size++;
					pendingPoints.add(p2);
				}
				seen[((int) p2.x + (int) p2.y * w)] = true;
			}

			if (p.y > 1.0D) {
				Point p2 = new Point(p.x, p.y - 1.0D);
				if ((seen[((int) p2.x + (int) p2.y * w)] == false)
						&& (pol.contains((int) p2.x, (int) p2.y))) {
					if (image.getPixel(p2) == color)
						size++;
					pendingPoints.add(p2);
				}
				seen[((int) p2.x + (int) p2.y * w)] = true;
			}

			if (p.y < image.getHeight() - 1) {
				Point p2 = new Point(p.x, p.y + 1.0D);
				if ((seen[((int) p2.x + (int) p2.y * w)] == false)
						&& (pol.contains((int) p2.x, (int) p2.y))) {
					if (image.getPixel(p2) == color)
						size++;
					pendingPoints.add(p2);
				}
				seen[((int) p2.x + (int) p2.y * w)] = true;
			}

			p = null;
			if (pendingPoints.size() > 0) {
				p = (Point) pendingPoints.elementAt(0);
				pendingPoints.removeElementAt(0);
			}

		}

		return size;
	}
}