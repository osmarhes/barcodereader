package br.com.atlas.reader.recognition;

public class Point {
	public double x = 0.0D;
	public double y = 0.0D;

	public Point(double x, double y) {
		this.x = (Math.round(x * 1000.0D) / 1000.0D);
		this.y = (Math.round(y * 1000.0D) / 1000.0D);
	}

	public double distanceTo(Point p) {
		return Math.sqrt((p.x - this.x) * (p.x - this.x) + (p.y - this.y)
				* (p.y - this.y));
	}

	public Point toPixel() {
		return new Point(Math.round(this.x), Math.round(this.y));
	}

	public String toString() {
		return "(" + this.x + "," + this.y + ")";
	}
}
