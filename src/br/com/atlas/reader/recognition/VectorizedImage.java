package br.com.atlas.reader.recognition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import br.com.atlas.reader.util.ImageDebugger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class VectorizedImage {
	protected Vector<Line> lines = new Vector<>();
	protected Vector<ImageObject> objects = new Vector<>();

	protected void addLine(Line l) {
		this.lines.add(l);
	}

	protected void addObject(ImageObject l) {
		this.objects.add(l);
		this.lines.add(l.getPA());
	}

	public Vector<Line> getLines() {
		return this.lines;
	}

	public Line[] getLinesAsArray() {
		Line[] a = new Line[this.lines.size()];

		for (int i = 0; i < a.length; i++)
			a[i] = ((Line) this.lines.elementAt(i));

		return a;
	}

	public Vector<ImageObject> getObjects() {
		return this.objects;
	}

	private void render(Graphics g, Vector<Line> pLines, int x, int y) {
		g.setColor(Color.black);
		Color[] colors = { Color.red, Color.green, Color.blue, Color.yellow,
				Color.cyan, Color.orange, Color.pink };
		for (int i = 0; i < pLines.size(); i++) {
			Line l = (Line) pLines.elementAt(i);
			g.setColor(colors[(i % colors.length)]);
			g.drawLine((int) l.x1 - x, (int) l.y1 - y, (int) l.x2 - x,
					(int) l.y2 - y);
		}
	}

	public void render(Graphics g) {
		render(g, this.lines, 0, 0);
	}

	public void exportToPNG(String file, Vector<Line> pLines,
			BufferedImage backImage, double px, double py)
			throws FileNotFoundException, Exception {
		// Object im = null;
		int x = (int) px;
		int y = (int) py;

		if (pLines == null)
			pLines = this.lines;

		int w = 2000;
		int h = 1500;
		BufferedImage output = new BufferedImage(w, h, 1);
		Graphics g2 = output.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, w, h);
		if (backImage != null)
			g2.drawImage(backImage, 0, w, 0, h, x, y, x + w, y + h, null);
		render(g2, pLines, x, y);
		g2.dispose();
		g2 = null;

		ImageDebugger.saveToPNG(output, new FileOutputStream(file));
	}

	public void exportToJPG(String file, Vector<Line> pLines,
			BufferedImage backImage, double px, double py)
			throws FileNotFoundException, Exception {
		// Object im = null;
		int x = (int) px;
		int y = (int) py;

		if (pLines == null)
			pLines = this.lines;

		int w = 700;
		int h = 700;
		BufferedImage output = new BufferedImage(w, h, 1);
		Graphics g2 = output.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, w, h);
		if (backImage != null)
			g2.drawImage(backImage, 0, w, 0, h, x, y, x + w, y + h, null);
		render(g2, pLines, x, y);
		g2.dispose();
		g2 = null;

		OutputStream os = new FileOutputStream(file);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
		JPEGEncodeParam params = encoder.getDefaultJPEGEncodeParam(output);
		params.setQuality(1.0F, false);
		encoder.setJPEGEncodeParam(params);
		encoder.encode(output);

		os.close();
		os = null;
	}
}