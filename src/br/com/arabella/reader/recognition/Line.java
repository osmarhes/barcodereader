package br.com.arabella.reader.recognition;

public class Line {
	public double x1 = 0.0D;
	public double y1 = 0.0D;
	public double x2 = 0.0D;
	public double y2 = 0.0D;
	double slopeAngle = 0.0D;
	double slope = 0.0D;
	double length = 0.0D;
	protected double a = 0.0D;
	protected double b = 0.0D;
	protected double c = 0.0D;

	protected double distanceToNextLine = 0.0D;

	protected boolean marked = false;

	public Line(Point p1, Point p2) {
		this(p1.x, p1.y, p2.x, p2.y);
	}

	public String toString() {
		return "" + this.x1 + " , " + this.y1 + " - " + this.x2 + " , "
				+ this.y2;
	}

	public Line toPixel() {
		return new Line(Math.round(this.x1), Math.round(this.y1),
				Math.round(this.x2), Math.round(this.y2));
	}

	public Line(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;

		this.a = (y2 - y1);
		this.b = (x1 - x2);
		this.c = (-(x1 - x2) * y1 + (y1 - y2) * x1);

		float distancex = (float) (x2 - x1);
		float distancey = (float) (y2 - y1);

		if (distancex == 0.0F) {
			this.slopeAngle = 90.0D;
			this.slope = 0.0D;
			this.length = Math.abs(distancey);
		} else if (distancey == 0.0F) {
			this.slopeAngle = 0.0D;
			this.slope = 0.0D;
			this.length = Math.abs(distancex);
		} else {
			this.slopeAngle = Math.toDegrees(Math.atan(distancey / distancex));

			if ((distancey < 0.0F) && (distancex < 0.0F))
				this.slopeAngle += 180.0D;

			if ((distancey >= 0.0F) && (distancex < 0.0F))
				this.slopeAngle += 180.0D;

			this.slope = (distancey / distancex);

			this.length = Math.sqrt(distancey * distancey + distancex
					* distancex);
		}
	}

	public Line enlargeLine(double newLen) {
		Point endPoint = getPointInSegment(newLen);

		return new Line(this.x1, this.y1, endPoint.x, endPoint.y);
	}

	public double getLength() {
		return this.length;
	}

	public double getSlope() {
		return this.slope;
	}

	public double getAngle() {
		return this.slopeAngle;
	}

	public Point getPointInSegment(double distance) {
		double middleX = this.x1 + distance / this.length * (this.x2 - this.x1);
		double middleY = this.y1 + distance / this.length * (this.y2 - this.y1);

		return new Point(middleX, middleY);
	}

	public Point getMiddlePoint() {
		double middleX = this.x1 + 0.5D * (this.x2 - this.x1);
		double middleY = this.y1 + 0.5D * (this.y2 - this.y1);

		return new Point(middleX, middleY);
	}

	public boolean isCenterAligned(Line line) {
		return isCenterAligned(line, 0.1D);
	}

	public boolean isCenterAligned(Line line, double thr) {
		Line perp = getPerp();

		double alignedDistance = perp.getDistance(line.getMiddlePoint());

		double tmp = thr * Math.abs(line.getLength());

		return alignedDistance < tmp;
	}

	public Point getIntersect(Line line) {
		double a1 = 0.0D;
		double a2 = 0.0D;
		double b1 = 0.0D;
		double b2 = 0.0D;
		double c1 = 0.0D;
		double c2 = 0.0D;
		double denom = 0.0D;

		a1 = this.y2 - this.y1;
		b1 = this.x1 - this.x2;
		c1 = this.x2 * this.y1 - this.x1 * this.y2;

		a2 = line.y2 - line.y1;
		b2 = line.x1 - line.x2;
		c2 = line.x2 * line.y1 - line.x1 * line.y2;

		denom = a1 * b2 - a2 * b1;
		if (denom == 0.0D)
			throw new RuntimeException("Cannot intersect lines");

		double px = (b1 * c2 - b2 * c1) / denom;
		double py = (a2 * c1 - a1 * c2) / denom;

		return new Point(px, py);
	}

	public double getDistance(Point p) {
		return Math.abs(this.a * p.x + this.b * p.y + this.c)
				/ Math.sqrt(this.a * this.a + this.b * this.b);
	}

	public Line getPerp(double dist) {
		Point p = getMiddlePoint();

		double perpAngle = getAngle();
		perpAngle += 90.0D;
		if (perpAngle >= 360.0D)
			perpAngle -= 360.0D;

		return new Line(p, new Point((int) (p.x + dist
				* Math.cos(Math.toRadians(perpAngle))), (int) (p.y + dist
				* Math.sin(Math.toRadians(perpAngle)))));
	}

	public Line getPerp(Point p, double dist) {
		double perpAngle = getAngle();
		perpAngle += 90.0D;
		if (perpAngle >= 360.0D)
			perpAngle -= 360.0D;

		return new Line(p, new Point((int) (p.x + dist
				* Math.cos(Math.toRadians(perpAngle))), (int) (p.y + dist
				* Math.sin(Math.toRadians(perpAngle)))));
	}

	public Line getPerp() {
		return getPerp(500.0D);
	}

	public boolean isParallel(Line line, int h) {
		return false;
	}

	public Point getStartPoint() {
		return new Point(this.x1, this.y1);
	}

	public Point getEndPoint() {
		return new Point(this.x2, this.y2);
	}

	public void debug() {
		System.out.println("Slope " + this.slope);
		System.out.println("Slope angle " + this.slopeAngle);
	}
}