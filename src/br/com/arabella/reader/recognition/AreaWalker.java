package br.com.arabella.reader.recognition;

import java.util.Vector;

import br.com.arabella.reader.RImage;

public class AreaWalker {
	Point startPoint = null;
	RImage image = null;
	Vector<Point> pendingPoints = new Vector<>();
	public long added = 0L;
	public long removed = 0L;

	public AreaWalker(RImage im, Point p, boolean initVisited) {
		this.startPoint = p;
		this.image = im;
		if (initVisited)
			this.image.initializeVisited();

		this.pendingPoints.add(p);
	}

	public Point getNextPoint() {
		if (this.pendingPoints.size() == 0)
			return null;

		Point p = (Point) this.pendingPoints.elementAt(0);
		this.pendingPoints.removeElementAt(0);
		this.removed += 1L;

		for (int i = 1; i <= 8; i++) {
			Point p2 = this.image.getNeightbour(p, i);

			if ((p2 == null) || (this.image.getVisited(p2)))
				continue;
			this.image.setVisited(p2);
			if (this.image.getPixel(p2) == RImage.FOREGROUND) {
				this.added += 1L;
				this.pendingPoints.add(p2);
			}

		}

		return p;
	}
}