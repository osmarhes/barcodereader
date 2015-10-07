package br.com.atlas.reader.recognition;

import java.util.Vector;

import br.com.atlas.reader.RImage;

public class Barcode1DFinder {
	public int minbars = 10;

	public int minBarLength = Integer.parseInt(System.getProperty(
			"com.java4less.vision.minbarlength", "15"));

	public int maxBarLength = Integer.parseInt(System.getProperty(
			"com.java4less.vision.maxbarlength", "1000"));

	private boolean debug = System.getProperty("com.java4less.vision.debug",
			"0").equals("1");

	public int quiteZoneMultiplier = 10;

	public boolean supportBrokenBars = System.getProperty(
			"com.java4less.vision.brokenbars", "1").equals("1");

	protected double minDistanceMult = Double.parseDouble(System.getProperty(
			"com.java4less.vision.brokenbars", "0.5"));

	public int angleDif = Integer.parseInt(System.getProperty(
			"com.java4less.vision.angledif", "5"));
	RImage rimage;

	public Vector<Barcode1DObject> findBarcodes(VectorizedImage image, RImage im) {
		this.rimage = im;

		Vector<ImageObject> objects = image.getObjects();
		Vector<Barcode1DObject> candidates = new Vector<>();

		while (objects.size() > 0) {
			Vector<ImageObject> unsorted = getParallelObjectsSet(objects);
			if (unsorted.size() > 0) {
				Vector<ImageObject> set = sortParallelAlignedObjects(unsorted);
				calculateDistances(set);
				if (set.size() <= this.minbars)
					continue;
				Vector<Vector<ImageObject>> groups = getGroupsOfObjects(set);
				for (int i = 0; i < groups.size(); i++) {
					candidates.add(new Barcode1DObject(groups.elementAt(i)));
				}
			}

		}

		return candidates;
	}

	private Vector<ImageObject> sortParallelAlignedObjects(
			Vector<ImageObject> objects) {
		Line first = ((ImageObject) objects.elementAt(0)).getPA();

		Line xAxis = new Line(0.0D, 0.0D, 1.0D, 0.0D);
		Line yAxis = new Line(0.0D, 0.0D, 0.0D, 1.0D);
		Line perp = first.getPerp();

		Line axis = xAxis;

		double angle = perp.getAngle();
		if (Math.abs(angle % 360.0D) < 25.0D)
			axis = yAxis;
		if (Math.abs(angle % 360.0D) > 335.0D)
			axis = yAxis;
		if ((Math.abs(angle % 360.0D) < 205.0D)
				&& (Math.abs(angle % 360.0D) > 155.0D))
			axis = yAxis;

		Vector<Double> distances = new Vector<>();
		Vector<ImageObject> sorted = new Vector<>();

		Point referencePoint = axis.getIntersect(perp);

		for (int i = 0; i < objects.size(); i++) {
			ImageObject object = (ImageObject) objects.elementAt(i);
			Line line = object.getPA();
			double dist = line.getDistance(referencePoint);

			int position = distances.size();
			for (int j = 0; j < distances.size(); j++) {
				if (((Double) distances.elementAt(j)).doubleValue() > dist) {
					position = j;
					break;
				}
			}

			distances.add(position, new Double(dist));
			sorted.add(position, object);
		}

		int j = distances.size() - 1;
		double firstBarWidth = ((ImageObject) sorted.elementAt(0)).getArea()
				/ ((ImageObject) sorted.elementAt(0)).getLength();
		double minDistance = firstBarWidth * this.minDistanceMult;
		if (minDistance < 1.5D)
			minDistance = 1.5D;

		if (this.supportBrokenBars) {
			while (j > 0) {
				double previous = ((Double) distances.elementAt(j - 1))
						.doubleValue();
				double current = ((Double) distances.elementAt(j))
						.doubleValue();

				if (current - previous < minDistance) {
					ImageObject pobject = sorted.elementAt(j - 1);
					ImageObject cobject = sorted.elementAt(j);

					if (cobject.getLength() > pobject.getLength()) {
						distances.removeElementAt(j - 1);
						sorted.removeElementAt(j - 1);
						addDebug("Removing bar, distance too small "
								+ (current - previous) + " < " + minDistance);
					} else {
						distances.removeElementAt(j);
						sorted.removeElementAt(j);
					}
				}

				j--;
			}

		}

		return sorted;
	}

	private boolean compareAngles(double a1, double a2, double diff) {
		if (Math.abs(a1 - a2) < diff)
			return true;

		double tmp = a1 + 180.0D;
		if (tmp > 360.0D)
			tmp -= 360.0D;
		if (Math.abs(tmp - a2) < diff)
			return true;

		tmp = a2 + 180.0D;
		if (tmp > 360.0D)
			tmp -= 360.0D;
		return Math.abs(a1 - tmp) < diff;
	}

	private Vector<ImageObject> getParallelObjectsSet(
			Vector<ImageObject> objects) {
		Vector<ImageObject> result = new Vector<>();
		Vector<Integer> resultIndexes = new Vector<>();
		ImageObject first = (ImageObject) objects.elementAt(0);

		while ((first.getLength() < this.minBarLength)
				|| (first.getLength() > this.maxBarLength)) {
			objects.removeElementAt(0);
			if (objects.size() == 0)
				return result;
			first = objects.elementAt(0);
		}

		ImageObject lastAccepted = first;
		result.add(first);
		resultIndexes.add(new Integer(0));

		for (int i = 1; i < objects.size(); i++) {
			ImageObject object = (ImageObject) objects.elementAt(i);
			Line line = object.getPA();

			if (compareAngles(line.getAngle(), first.getPA().getAngle(),
					this.angleDif)) {
				double ratio = first.getLength() / object.getLength();
				if (ratio < 1.0D)
					ratio = object.getLength() / first.getLength();

				if ((ratio < 5.0D) && (object.getLength() > this.minBarLength)
						&& (object.getLength() < this.maxBarLength)) {
					if ((lastAccepted.isAligned(object))
							|| (first.isAligned(object))) {
						result.add(object);
						resultIndexes.add(new Integer(i));
						lastAccepted = object;
					} else {
						addDebug("Not aligned");
					}
				} else
					addDebug("Not long enough " + object.getLength());
			} else {
				addDebug("Not same angle " + object.getPA().getAngle()
						+ "  <> " + first.getPA().getAngle() + " length "
						+ first.getLength());
			}
		}

		for (int i = resultIndexes.size() - 1; i >= 0; i--) {
			objects.removeElementAt(((Integer) resultIndexes.elementAt(i))
					.intValue());
		}

		return result;
	}

	private void addDebug(String s) {
		if (this.debug)
			System.out.println(s);
	}

	private void calculateDistances(Vector<ImageObject> result) {
		for (int i = 0; i < result.size() - 1; i++) {
			Line line = (result.elementAt(i)).getPA();
			Line line2 = (result.elementAt(i + 1)).getPA();

			line2.distanceToNextLine = 0.0D;

			line.distanceToNextLine = line.getDistance(new Point(line2.x1,
					line2.y1));
		}
	}

	private Vector<Vector<ImageObject>> getGroupsOfObjects(
			Vector<ImageObject> parallelObjects) {
		int minLine = getMinDistanceLine(parallelObjects);
		Vector<Vector<ImageObject>> groups = new Vector<>();

		while (minLine != -1) {
			Vector<ImageObject> group = new Vector<>();
			double minDistance = ((ImageObject) parallelObjects
					.elementAt(minLine)).getPA().distanceToNextLine;

			// int startLine = minLine;
			for (int i = minLine - 1; i >= 0; i--) {
				ImageObject object = (ImageObject) parallelObjects.elementAt(i);
				Line line = ((ImageObject) parallelObjects.elementAt(i))
						.getPA();
				if ((line.marked)
						|| (line.distanceToNextLine > minDistance
								* this.quiteZoneMultiplier))
					break;
				line.marked = true;
				group.add(0, object);
				// startLine = i;
			}

			// int endLine = minLine;
			for (int i = minLine; i < parallelObjects.size(); i++) {
				ImageObject object = (ImageObject) parallelObjects.elementAt(i);
				Line line = ((ImageObject) parallelObjects.elementAt(i))
						.getPA();
				if (line.marked)
					break;
				line.marked = true;
				group.add(object);
				// endLine = i;

				if (line.distanceToNextLine > minDistance
						* this.quiteZoneMultiplier) {
					break;
				}
			}
			if (group.size() >= this.minbars)
				groups.add(group);

			minLine = getMinDistanceLine(parallelObjects);
		}

		return groups;
	}

	private int getMinDistanceLine(Vector<ImageObject> objects) {
		double min = 999999.0D;
		int minLine = -1;

		for (int i = 0; i < objects.size(); i++) {
			Line line = (objects.elementAt(i)).getPA();

			if ((line.marked) || (min <= line.distanceToNextLine)
					|| (line.distanceToNextLine <= 0.0D))
				continue;
			minLine = i;
			min = line.distanceToNextLine;
		}

		return minLine;
	}
}
