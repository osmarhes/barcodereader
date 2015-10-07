package br.com.atlas.reader.recognition;

import br.com.atlas.reader.util.ImageDebugger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

public class Barcode1DObject {
	public Vector<ImageObject> bars = new Vector<>();
	public Point corner1 = new Point(0.0D, 0.0D);
	public Point corner2 = new Point(0.0D, 0.0D);
	public Point corner3 = new Point(0.0D, 0.0D);
	public Point corner4 = new Point(0.0D, 0.0D);
	double[] barWidths;
	private int flag = 0;
	double firstBarAngle = 0.0D;

	public Barcode1DObject(Vector<ImageObject> bars) {
		this.bars = bars;

		ImageObject object1 = (ImageObject) bars.elementAt(0);
		ImageObject object2 = (ImageObject) bars.elementAt(bars.size() - 1);

		double angle = object1.getPA().getAngle() - 90.0D;
		this.firstBarAngle = angle;
		double len = object1.getLength();
		double ystep = len * Math.cos(Math.toRadians(angle));
		double xstep = len * Math.sin(Math.toRadians(angle));
		this.corner1 = object1.nearestCorner;
		this.corner2 = new Point(Math.round(this.corner1.x - xstep),
				Math.round(this.corner1.y + ystep));
		this.corner3 = object2.farestCorner;
		this.corner4 = new Point(Math.round(this.corner3.x + xstep),
				Math.round(this.corner3.y - ystep));
	}

	public Vector<Line> getLines() {
		Vector<Line> v = new Vector<Line>();

		for (int i = 0; i < this.bars.size(); i++) {
			ImageObject object1 = (ImageObject) this.bars.elementAt(i);
			v.add(object1.getPA());
		}
		return v;
	}

	public Line[] getBarsArray() {
		Line[] a = new Line[this.bars.size()];

		for (int i = 0; i < this.bars.size(); i++) {
			ImageObject object1 = (ImageObject) this.bars.elementAt(i);
			a[i] = object1.getPA();
		}
		return a;
	}

	public double getOrientation() {
		return ((ImageObject) this.bars.elementAt(0)).getPA().getAngle();
	}

	public double getHeight() {
		return ((ImageObject) this.bars.elementAt(0)).getLength();
	}

	public void setWidths(double[] w) {
		this.barWidths = w;
	}

	public double[] getWidths() {
		return this.barWidths;
	}

	public void setFlag(int f) {
		this.flag = f;
	}

	public int getFlag() {
		return this.flag;
	}

	public double getFirstBarAngle() {
		return this.firstBarAngle;
	}

	public void exportToPNG(String file, BufferedImage backImage)
			throws IOException {
		ImageDebugger debugger = new ImageDebugger(backImage);

		for (int i = 0; i < this.bars.size(); i++) {
			Line l = ((ImageObject) this.bars.elementAt(i)).getPA();
			debugger.exportLine(l);
		}

		debugger.export(file);
	}
}