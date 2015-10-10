package br.com.arabella.reader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import br.com.arabella.reader.recognition.Point;

public class RImage {
	public static int FOREGROUND = 0;
	public static int BACKGROUND = 1;

	boolean[] visited = null;
	int[] values = null;

	BufferedImage image = null;
	int width = 0;
	int height = 0;

	public RImage(BufferedImage i) {
		this.image = i;
		setSize();
	}

	public RImage(BufferedImage i, Rectangle scanArea) {
		BufferedImage input = new BufferedImage(scanArea.width,
				scanArea.height, i.getType());
		Graphics g = input.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, scanArea.width, scanArea.height);
		g.drawImage(i, 0, 0, scanArea.width, scanArea.height, scanArea.x,
				scanArea.y, scanArea.x + scanArea.width, scanArea.y
						+ scanArea.height, null);
		g.dispose();
		g = null;

		this.image = input;
		setSize();
	}

	public RImage(int w, int h) {
		this(new BufferedImage(w, h, 12));
	}

	public void initializePixels() {
		if (this.values == null)
			imageToMemory();
	}

	private void imageToMemory() {
		WritableRaster raster = this.image.getRaster();

		int bands = raster.getNumBands();

		this.values = new int[this.height * this.width];

		if (bands == 1) {
			raster.getSamples(0, 0, this.width, this.height, 0, this.values);
		} else {
			raster.getSamples(0, 0, this.width, this.height, 3, this.values);

			int[] reds = raster.getSamples(0, 0, this.width, this.height, 1,
					(int[]) null);
			int[] greens = raster.getSamples(0, 0, this.width, this.height, 2,
					(int[]) null);
			for (int j = 0; j < this.height; j++)
				for (int h = 0; h < this.width; h++)
					this.values[(h + j * this.width)] = ((reds[(h + j
							* this.width)] << 16)
							+ (greens[(h + j * this.width)] << 8) + this.values[(h + j
							* this.width)]);
		}
	}

	protected void memoryToImage() {
		// long prv = System.currentTimeMillis();
		WritableRaster raster = this.image.getRaster();
		int bands = raster.getNumBands();

		if (bands == 1)
			raster.setSamples(0, 0, this.width, this.height, 0, this.values);
		else
			for (int j = 0; j < this.height; j++)
				for (int h = 0; h < this.width; h++) {
					raster.setSample(h, j, 1, getRPixel(j, h));
					raster.setSample(h, j, 2, getGPixel(j, h));
					raster.setSample(h, j, 3, getBPixel(j, h));
				}
	}

	@SuppressWarnings("unused")
	private BufferedImage getImageCopy(BufferedImage image) {
		BufferedImage input = new BufferedImage(image.getWidth(null),
				image.getHeight(null), 2);
		Graphics g = input.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.drawImage(image, 0, 0, null);
		g.dispose();
		g = null;

		return input;
	}

	public void resetImage() {
		imageToMemory();
	}

	private void setSize() {
		this.width = this.image.getWidth();
		this.height = this.image.getHeight();
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public BufferedImage getImage() {
		return this.image;
	}

	public void setPixel(int row, int col, int val) {
		try {
			this.values[(col + row * this.width)] = val;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("row = " + row + " col=" + col);
			System.err.println("h = " + this.height + " w=" + this.width);
			throw new RuntimeException(e);
		}
	}

	public void setPixel(Point p, int val) {
		setPixel((int) p.y, (int) p.x, val);
	}

	public int getGreyPixel(double row, double col) {
		int y = (int) Math.abs(row);
		int x = (int) Math.abs(col);

		return (int) (getRPixel(y, x) * 0.33D + getGPixel(y, x) * 0.33D
				+ getBPixel(y, x) * 0.33D + 0.5D);
	}

	public int getPixel(Point p) {
		if (p == null)
			return BACKGROUND;

		return getPixel(p.y, p.x);
	}

	public int getPixel(double row, double col) {
		return getPixel((int) Math.round(row), (int) Math.round(col));
	}

	public int getPixel(int row, int col) {
		if (row < 0)
			return BACKGROUND;
		if (col < 0)
			return BACKGROUND;

		if (row >= this.height)
			return BACKGROUND;
		if (col >= this.width)
			return BACKGROUND;

		return this.values[(col + row * this.width)];
	}

	public int getRPixel(int row, int col) {
		return (getPixel(row, col) & 0xFF0000) >> 16;
	}

	public void setRPixel(int row, int col, int val) {
		int cur = getPixel(row, col);
		cur = cur & 0xFFFF | val << 16;

		setPixel(row, col, cur);
	}

	public int getGPixel(int row, int col) {
		return (getPixel(row, col) & 0xFF00) >> 8;
	}

	public void setGPixel(int row, int col, int val) {
		int cur = getPixel(row, col);
		cur = cur & 0xFF00FF | val << 8;
		setPixel(row, col, cur);
	}

	public int getBPixel(int row, int col) {
		return getPixel(row, col) & 0xFF;
	}

	public void setBPixel(int row, int col, int val) {
		int cur = getPixel(row, col);
		cur = cur & 0xFFFF00 | val;
		setPixel(row, col, cur);
	}

	public void initializeVisited() {
		this.visited = new boolean[this.width * this.height];
	}

	public void setVisited(Point p) {
		setVisited((int) p.x, (int) p.y);
	}

	public void setVisited(int x, int y) {
		this.visited[(x + y * this.width)] = true;
	}

	public boolean getVisited(Point p) {
		return getVisited((int) p.x, (int) p.y);
	}

	public boolean getVisited(int x, int y) {
		return this.visited[(x + y * this.width)];
	}

	public boolean isInPerimeter4(Point point) {
		if (getPixel(getNeightbour(point, 1)) == BACKGROUND)
			return true;
		if (getPixel(getNeightbour(point, 3)) == BACKGROUND)
			return true;
		if (getPixel(getNeightbour(point, 5)) == BACKGROUND)
			return true;
		return getPixel(getNeightbour(point, 7)) == BACKGROUND;
	}

	public Point getNeightbour(Point point, int p) {
		if ((p == 1) && (point.x < getWidth() - 1))
			return new Point(point.x + 1.0D, point.y);

		if ((p == 8) && (point.x < getWidth() - 1) && (point.y > 0.0D))
			return new Point(point.x + 1.0D, point.y - 1.0D);

		if ((p == 7) && (point.y > 0.0D))
			return new Point(point.x, point.y - 1.0D);

		if ((p == 6) && (point.x > 0.0D) && (point.y > 0.0D))
			return new Point(point.x - 1.0D, point.y - 1.0D);

		if ((p == 5) && (point.x > 0.0D))
			return new Point(point.x - 1.0D, point.y);

		if ((p == 4) && (point.x > 0.0D) && (point.y < getHeight() - 1))
			return new Point(point.x - 1.0D, point.y + 1.0D);

		if ((p == 3) && (point.y < getHeight() - 1))
			return new Point(point.x, point.y + 1.0D);

		if ((p == 2) && (point.y < getHeight() - 1)
				&& (point.x < getWidth() - 1))
			return new Point(point.x + 1.0D, point.y + 1.0D);

		return null;
	}

	public RImage toBackWhite() {
		BufferedImage input = new BufferedImage(getWidth(), getHeight(), 12);

		Graphics g = input.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(getImage(), 0, 0, null);
		g.dispose();
		g = null;

		return new RImage(input);
	}
}