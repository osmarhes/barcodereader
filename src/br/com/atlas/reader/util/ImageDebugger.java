package br.com.atlas.reader.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import br.com.atlas.reader.recognition.Line;
import br.com.atlas.reader.recognition.Point;

public class ImageDebugger {
	BufferedImage outputImage = null;
	Graphics g2 = null;
	Color[] colors = { Color.red, Color.green, Color.blue, Color.yellow,
			Color.cyan, Color.orange, Color.pink };
	int lastColor = 0;

	public ImageDebugger(Image backImage) {
		int w = 700;
		int h = 400;
		this.outputImage = new BufferedImage(w, h, 1);
		this.g2 = this.outputImage.createGraphics();
		this.g2.setColor(Color.white);
		this.g2.fillRect(0, 0, w, h);
		if (backImage != null)
			this.g2.drawImage(backImage, 0, 0, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void saveToPNG(Image image, OutputStream os) throws Exception {
		Class enClass = Class.forName("com.bigfoot.bugar.image.PNGEncoder");
		Class[] constructorParams = new Class[2];
		constructorParams[0] = Class.forName("java.awt.Image");
		constructorParams[1] = Class.forName("java.io.OutputStream");
		Object[] constructorObj = new Object[2];
		constructorObj[0] = image;
		constructorObj[1] = os;

		Object encoder = enClass.getConstructor(constructorParams).newInstance(
				constructorObj);

		Class[] encodeParams = new Class[0];
		Object[] encodeObj = new Object[0];

		enClass.getMethod("encodeImage", encodeParams).invoke(encoder,
				encodeObj);

		os.close();

		image = null;
		os = null;
	}

	public void exportLines(Vector<Line> pLines) {
		this.g2.setColor(Color.black);
		for (int i = 0; i < pLines.size(); i++) {
			Line l = (Line) pLines.elementAt(i);
			this.g2.setColor(this.colors[(i % this.colors.length)]);
			this.g2.drawLine((int) l.x1, (int) l.y1, (int) l.x2, (int) l.y2);
		}
	}

	public void exportText(int x, int y, String t) {
		this.g2.setColor(Color.black);
		this.g2.setFont(new Font("Arial", 0, 7));
		this.g2.drawString(t, x, y);
	}

	public void exportLine(Line l) {
		this.lastColor += 1;
		this.lastColor %= this.colors.length;
		this.g2.setColor(this.colors[this.lastColor]);
		this.g2.drawLine((int) l.x1, (int) l.y1, (int) l.x2, (int) l.y2);
	}

	public void exportPoint(Point p) {
		this.lastColor += 1;
		this.lastColor %= this.colors.length;
		this.g2.setColor(this.colors[this.lastColor]);
		this.g2.drawLine((int) p.x, (int) p.y, (int) p.x, (int) p.y);
	}

	public void export(String file) {
		this.g2.dispose();
		this.g2 = null;
		try {
			saveToPNG(this.outputImage, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}