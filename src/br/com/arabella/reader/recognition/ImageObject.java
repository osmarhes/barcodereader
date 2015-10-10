package br.com.arabella.reader.recognition;

import java.util.Vector;

import br.com.arabella.reader.RImage;

public class ImageObject {
	Point startPoint = null;
	Point centerOfMass = null;
	int area = 0;
	private Line longPA = null;
	Line realPA = null;
	Point[] perimeterPixels;
	double maxLength = 0.0D;
	Point nearestCorner = new Point(999999.0D, 999999.0D);
	double nearestCornerDistance = 999999.0D;
	protected Point farestCorner = new Point(0.0D, 0.0D);
	double farestCornerDistance = 0.0D;
	public double minX = 1000000.0D;
	public double minY = 1000000.0D;
	public double maxY = 0.0D;
	public double maxX = 0.0D;

	public ImageObject(RImage image, Point p) {
		this.startPoint = p;

		initialize(image, p);
	}

	private void initialize(RImage image, Point start) {
		AreaWalker walker = new AreaWalker(image, start, false);
		double c1 = 0.0D;
		double c2 = 0.0D;
		this.minX = start.x;
		this.maxX = start.x;
		this.minY = start.y;
		this.maxY = start.y;
		this.area = 0;

		Vector<Point> v = new Vector<>();
		Point p = walker.getNextPoint();
		while (p != null) {
			if (p.x < this.minX)
				this.minX = p.x;
			if (p.y < this.minY)
				this.minY = p.y;
			if (p.x > this.maxX)
				this.maxX = p.x;
			if (p.y > this.maxY)
				this.maxY = p.y;

			double distanceToOrigin = Math.sqrt(p.x * p.x + p.y * p.y);
			if (distanceToOrigin < this.nearestCornerDistance) {
				this.nearestCornerDistance = distanceToOrigin;
				this.nearestCorner = p;
			}
			if (distanceToOrigin > this.farestCornerDistance) {
				this.farestCornerDistance = distanceToOrigin;
				this.farestCorner = p;
			}

			this.area += 1;
			c1 += (int) p.y;
			c2 += (int) p.x;

			if (image.isInPerimeter4(p))
				v.add(p);

			p = walker.getNextPoint();
		}

		this.perimeterPixels = new Point[v.size()];
		for (int i = 0; i < v.size(); i++) {
			this.perimeterPixels[i] = ((Point) v.elementAt(i));
		}
		this.centerOfMass = new Point(c2 / this.area, c1 / this.area);
	}

	public Point getStarPoint() {
		return this.startPoint;
	}

	public Point getCenterOfMass() {
		return this.centerOfMass;
	}

	public int getArea() {
		return this.area;
	}

	public Line getPA() {
		return this.realPA;
	}

	public void calculatePA(RImage image) {
		// long prv = System.currentTimeMillis();

		if (this.longPA != null)
			return;

		double dmax = -1.0D;
		Point selected = null;

		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p = this.perimeterPixels[i];

			if (p.y <= this.centerOfMass.y + 0.5D) {
				Line line = new Line(this.centerOfMass.x, this.centerOfMass.y,
						p.x, p.y);
				double dd = calculateDistances(image, line, dmax);
				if ((dd < dmax) || (dmax == -1.0D)) {
					dmax = dd;
					selected = p;
				}
			}

		}

		// prv = System.currentTimeMillis();

		Line best = new Line(this.centerOfMass.x, this.centerOfMass.y,
				selected.x, selected.y);
		double len = selected.distanceTo(this.centerOfMass);

		dmax = -1.0D;
		Point selected2 = this.centerOfMass;
		double maxLen = 0.0D;

		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p = this.perimeterPixels[i];

			if (p.y >= this.centerOfMass.y - 0.5D) {
				double pointLen = selected.distanceTo(p);
				if (pointLen > len) {
					Line line = new Line(selected.x, selected.y, p.x, p.y);
					double dd = calculateDistances(image, line, dmax);

					if ((dd < dmax) || (dmax == -1.0D)) {
						dmax = dd;
						selected2 = p;
					}

				}

			}

		}

		maxLen = 0.0D;

		this.realPA = new Line(selected.x, selected.y, selected2.x, selected2.y);

		best = this.realPA;

		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p = this.perimeterPixels[i];

			if (p.y >= this.centerOfMass.y - 0.5D) {
				double pointLen = selected.distanceTo(p);
				if (pointLen > len) {
					double dist = best.getDistance(p);
					if ((dist <= 1.0D) && (pointLen > maxLen)) {
						selected2 = p;
						maxLen = pointLen;
					}
				}
			}

		}

		maxLen = 0.0D;
		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p = this.perimeterPixels[i];

			if (p.y <= this.centerOfMass.y + 0.5D) {
				double pointLen = selected2.distanceTo(p);
				if (pointLen > len) {
					double dist = best.getDistance(p);
					if ((dist <= 1.0D) && (pointLen > maxLen)) {
						selected = p;
						maxLen = pointLen;
					}
				}

			}

		}

		this.longPA = new Line(selected.x, selected.y, selected2.x, selected2.y);

		this.maxLength = this.longPA.getLength();

		this.perimeterPixels = null;
	}

	@SuppressWarnings("unused")
	private double calculateMaxDistance(RImage image, Point p) {
		double max = 0.0D;

		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p2 = this.perimeterPixels[i];
			double d = p.distanceTo(p2);
			if (d <= max)
				continue;
			max = d;
		}

		return max;
	}

	private double calculateDistances(RImage image, Line line, double currentMax) {
		double sum = 0.0D;
		// double max = 0.0D;

		for (int i = 0; i < this.perimeterPixels.length; i++) {
			Point p = this.perimeterPixels[i];
			double d = line.getDistance(p);
			sum += d * d;
			if ((currentMax != -1.0D) && (sum > currentMax))
				return currentMax + 1.0D;

		}

		return sum;
	}

	public boolean isAligned(ImageObject object) {
		Line perp = getPA().getPerp();

		double alignedDistance = perp.getDistance(object.getCenterOfMass());

		double tmp = 0.4D * Math.abs(this.maxLength);

		return alignedDistance < tmp;
	}

	public double getLength() {
		return this.maxLength;
	}
}