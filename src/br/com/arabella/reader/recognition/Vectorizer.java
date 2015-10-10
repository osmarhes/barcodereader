package br.com.arabella.reader.recognition;

import java.util.Vector;

import br.com.arabella.reader.RImage;

public class Vectorizer {
	Vector<Point> points = new Vector<>();

	public VectorizedImage vectorize(RImage image) {
		VectorizedImage result = new VectorizedImage();

		Line line = findLine(image);
		while (line != null) {
			result.addLine(line);
			line = findLine(image);
		}

		return result;
	}

	private Line findLine(RImage image) {
		Point start = null;
		Point end = null;
		Point lastValidEnd = null;
		Vector<Point> points = new Vector<>();
		int foreground = 0;
		int background = 1;

		for (int j = 0; (j < image.getHeight()) && (start == null); j++) {
			for (int i = 0; (i < image.getWidth()) && (start == null); i++) {
				if (image.getPixel(j, i) == foreground)
					start = new Point(i, j);
			}
		}
		if (start == null)
			return null;

		points.add(start);
		lastValidEnd = start;
		image.setPixel(start, background);

		end = getNeighbour(image, start, foreground);
		while (end != null) {
			if (!checkChord((int) end.x, (int) end.y, points)) {
				// Line l = new Line(start.x, start.y, lastValidEnd.x,
				// lastValidEnd.y);

				break;
			}
			points.add(end);
			lastValidEnd = end;
			image.setPixel(end, background);

			end = getNeighbour(image, end, foreground);

			if (end != null)
				continue;
			System.out.println("end point " + lastValidEnd);
		}

		return new Line(start.x, start.y, lastValidEnd.x, lastValidEnd.y);
	}

	private Point getNeighbour(RImage image, Point p, int foreground) {
		int[][] n = { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
				{ -1, -1 }, { 0, -1 }, { 1, -1 } };

		for (int i = 0; i < n.length; i++) {
			Point p2 = new Point(p.x + n[i][0], p.y + n[i][1]);

			if ((p2.x < image.getWidth()) && (p.y < image.getHeight())
					&& (p2.x >= 0.0D) && (p.y >= 0.0D)
					&& (image.getPixel(p2.y, p2.x) == foreground))
				return p2;
		}

		return null;
	}

	private boolean checkChord(int x, int y, Vector<Point> pts) {
		float distancex = 0.0F;
		float distancey = 0.0F;
		float slope = 0.0F;
		float b = 0.0F;
		double xp = 0.0D;
		double yp = 0.0D;
		double distance = 0.0D;

		Point p = (Point) pts.elementAt(0);
		distancex = (float) (p.x - x);
		distancey = (float) (p.y - y);
		if ((distancex != 0.0F) && (distancey != 0.0F)) {
			slope = distancey / distancex;
			b = y - slope * x;
		}

		for (int i = 1; i < pts.size(); i++) {
			p = pts.elementAt(i);
			if (distancex == 0.0F) {
				distance = (float) Math.abs(p.x - x);
			} else if (distancey == 0.0F) {
				distance = (float) Math.abs(p.y - y);
			} else {
				xp = (p.y - b) / slope;
				yp = slope * p.x + b;
				distance = Math.min(Math.abs(p.x - xp), Math.abs(p.y - yp));
			}

			if (distance <= 1.0D)
				continue;
			System.out.println("fail " + distance);
			return false;
		}

		return true;
	}
}
