package br.com.atlas.reader.recognition;

public class LineWalker {
	int startX = 0;
	int startY = 0;
	int endX = 0;
	int endY;
	double stepX = 999999.0D;
	double stepY = 999999.0D;
	double w = 0.0D;
	double h = 0.0D;
	double lengthModifier = 1.0D;

	double pointerX = 0.0D;
	double pointerY = 0.0D;

	public LineWalker(Line l) {
		this((int) l.x1, (int) l.y1, (int) l.x2, (int) l.y2, 1.0D);
	}

	public LineWalker(Line l, double length) {
		this((int) l.x1, (int) l.y1, (int) l.x2, (int) l.y2, length);
	}

	public LineWalker(int x1, int y1, int x2, int y2) {
		this(x1, y1, x2, y2, 1.0D);
	}

	public LineWalker(int x1, int y1, int x2, int y2, double length) {
		this.startX = x1;
		this.startY = y1;
		this.endX = x2;
		this.endY = y2;
		this.lengthModifier = length;

		this.w = (x2 - x1);
		this.h = (y2 - y1);

		if ((this.w == 0.0D) && (this.h == 0.0D)) {
			this.stepY = 0.1D;
			this.stepX = 0.1D;
		} else if (Math.abs(this.h) > Math.abs(this.w)) {
			this.stepY = 1.0D;
			this.stepX = (Math.abs(this.w) / Math.abs(this.h));
		} else {
			this.stepX = 1.0D;
			this.stepY = (Math.abs(this.h) / Math.abs(this.w));
		}

		if (this.h < 0.0D)
			this.stepY *= -1.0D;
		if (this.w < 0.0D)
			this.stepX *= -1.0D;

		this.pointerY = 0.0D;
		this.pointerX = 0.0D;
	}

	public Point getNextPoint() {
		if (Math.abs(this.pointerY) > Math.abs(this.h * this.lengthModifier))
			return null;

		if (Math.abs(this.pointerX) > Math.abs(this.w * this.lengthModifier))
			return null;

		this.pointerY += this.stepY;
		this.pointerX += this.stepX;

		return new Point(this.startX + this.pointerX, this.startY
				+ this.pointerY);
	}
}