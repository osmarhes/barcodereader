package br.com.arabella.reader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;

import br.com.arabella.reader.barcode.BarcodeParser;
import br.com.arabella.reader.barcode.Code128Parser;
import br.com.arabella.reader.barcode.Code25InterleavedParser;
import br.com.arabella.reader.barcode.Code39Parser;
import br.com.arabella.reader.barcode.CodeEAN13Parser;
import br.com.arabella.reader.barcode.CodeEAN8Parser;
import br.com.arabella.reader.barcode.CodeUPCAParser;
import br.com.arabella.reader.barcode.CodeUPCEParser;
import br.com.arabella.reader.barcode.IdentcodeParser;
import br.com.arabella.reader.progress.IProgressListener;
import br.com.arabella.reader.progress.core.ProgressMonitor;
import br.com.arabella.reader.progress.impl.ConsoleProgressListener;
import br.com.arabella.reader.recognition.Barcode1DFinder;
import br.com.arabella.reader.recognition.Barcode1DObject;
import br.com.arabella.reader.recognition.BarsReader;
import br.com.arabella.reader.recognition.PAVectorizer;
import br.com.arabella.reader.recognition.VectorizedImage;
import br.com.arabella.reader.util.Histogram;
import br.com.arabella.reader.util.ImageDebugger;

public class Barcode1DReader {
	public static int CODE128 = 1;
	public static int EAN8 = 2;
	public static int EAN13 = 4;
	public static int UPCA = 8;
	public static int UPCE = 16;
	public static int CODE39 = 32;
	public static int INTERLEAVED25 = 64;
	public static int IDENTCODE = 128;

	protected int symbologies = CODE128;

	public boolean applyContrast = System.getProperty(
			"com.java4less.vision.contrast", "0").equals("1");

	public boolean isBWImage = false;

	private boolean debug = System.getProperty("com.java4less.vision.debug",
			"0").equals("1");

	protected boolean checkSum = true;

	private ProgressMonitor progressMonitor = null;
	private IProgressListener progressListener = new ConsoleProgressListener();

	public void setSymbologies(int s) {
		this.symbologies = s;
	}

	public void setProgressListener(IProgressListener l) {
		this.progressListener = l;
	}

	public BarcodeData[] scan(RImage inputImage) throws VisionException {
		BarcodeData[] data = scanInternal(inputImage);

		if ((System.getProperty("com.java4less.vision.secondpass", "1")
				.equals("1")) && (data.length == 0)) {
			inputImage.resetImage();
			this.applyContrast = (!this.applyContrast);
			data = scanInternal(inputImage);
		}
		return data;
	}

	private BarcodeData[] scanInternal(RImage inputImage) {
		this.progressMonitor = new ProgressMonitor(this.progressListener, 5.0D);

		Vector<BarcodeData> result = new Vector<>();

		this.progressMonitor.startSubProcess(2);
		if (this.applyContrast) {
			inputImage.initializePixels();
			Histogram hist = new Histogram(inputImage);
			this.progressMonitor.reportProgress(1.0D);
			hist.stretch();
			inputImage.memoryToImage();
			if (this.debug)
				try {
					ImageDebugger.saveToPNG(inputImage.getImage(),
							new FileOutputStream("contrast.png"));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		}
		this.progressMonitor.endSubProcess();

		this.progressMonitor.startSubProcess(1);
		RImage bwImage = null;
		if (!this.isBWImage)
			bwImage = inputImage.toBackWhite();
		else
			bwImage = inputImage;
		inputImage = null;
		bwImage.initializePixels();
		this.progressMonitor.endSubProcess();

		if (this.debug)
			try {
				ImageDebugger.saveToPNG(bwImage.getImage(),
						new FileOutputStream("bw.png"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		PAVectorizer vect = new PAVectorizer();
		this.progressMonitor.startSubProcess(1);
		VectorizedImage vi = vect.vectorize(bwImage);
		this.progressMonitor.endSubProcess();

		if (this.debug)
			try {
				vi.exportToPNG("vectorized.png", null, bwImage.getImage(),
						0.0D, 0.0D);
			} catch (Exception e) {
				e.printStackTrace();
			}

		Barcode1DFinder finder = new Barcode1DFinder();
		BarsReader barReader = new BarsReader();

		this.progressMonitor.startSubProcess(1);
		Vector<Barcode1DObject> barcodes = finder.findBarcodes(vi, bwImage);
		this.progressMonitor.endSubProcess();

		this.progressMonitor.startSubProcess(barcodes.size());
		for (int i = 0; i < barcodes.size(); i++) {
			this.progressMonitor.reportProgress(i);

			if (this.debug)
				try {
					/*
					 * Barcode1DObject bc = (Barcode1DObject) barcodes
					 * .elementAt(i);
					 */

					vi.exportToPNG("candidate" + i + ".png",
							((Barcode1DObject) barcodes.elementAt(i))
									.getLines(), bwImage.getImage(), 0.0D, 0.0D);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			Barcode1DObject bc = (Barcode1DObject) barcodes.elementAt(i);
			if (this.debug) {
				System.out.println("Barcode candidate found " + bc.bars.size());
			}
			double[] widths = barReader.convertBarsToWidths(
					BarsReader.ALG_AREAS, bwImage, bc);

			if (this.debug) {
				System.out.print("     Bar Witdhs: ");
				for (int j = 0; j < widths.length; j++)
					System.out.print(widths[j] + " , ");
				System.out.println("");
			}

			BarcodeData data = null;

			if ((this.symbologies & CODE128) > 0) {
				data = parseBarcode(new Code128Parser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & EAN8) > 0) {
				data = parseBarcode(new CodeEAN8Parser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & EAN13) > 0) {
				data = parseBarcode(new CodeEAN13Parser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & CODE39) > 0) {
				data = parseBarcode(new Code39Parser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & UPCE) > 0) {
				data = parseBarcode(new CodeUPCEParser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & UPCA) > 0) {
				data = parseBarcode(new CodeUPCAParser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & INTERLEAVED25) > 0) {
				data = parseBarcode(new Code25InterleavedParser(), widths);
				if (data != null)
					result.add(data);
			}
			if ((this.symbologies & IDENTCODE) > 0) {
				data = parseBarcode(new IdentcodeParser(), widths);
				if (data != null)
					result.add(data);
			}

			if (data != null) {
				data.x = (int) bc.corner1.x;
				data.y = (int) bc.corner1.y;
			}

		}

		this.progressMonitor.endSubProcess();

		BarcodeData[] array = new BarcodeData[result.size()];
		for (int i = 0; i < result.size(); i++) {
			array[i] = ((BarcodeData) result.elementAt(i));
		}

		return array;
	}

	private BarcodeData parseBarcode(BarcodeParser parser, double[] widths) {
		parser.init(widths);
		if (parser.parse()) {
			BarcodeData data = new BarcodeData();
			data.setValue(parser.getParsedCode());
			data.setSymbology(parser.getSymbology());
			return data;
		}

		return null;
	}

	@SuppressWarnings("unused")
	private RImage applyAdaptativeThreshold(RImage image) {
		int h = image.getHeight();
		int w = image.getWidth();
		BufferedImage output = new BufferedImage(w, h, 12);

		Graphics g = output.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.drawImage(image.getImage(), 0, 0, null);
		g.dispose();
		g = null;

		RImage outImage = new RImage(output);

		int size = 9;
		int mean = 0;
		int sum = 0;
		int count = 0;
		int row = 0;
		int col = 0;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				mean = 0;
				count = 0;
				sum = 0;

				for (int y2 = 0; y2 < size; y2++) {
					for (int x2 = 0; x2 < size; x2++) {
						row = y - (int) Math.floor(size / 2) + y2;
						col = x - (int) Math.floor(size / 2) + x2;

						if ((row >= 0) && (col >= 0) && (row < h) && (col < w)) {
							sum += image.getGreyPixel(row, col);
							count++;
						}
					}
				}

				mean = sum / count - 7;

				if (image.getGreyPixel(y, x) > mean)
					outImage.setPixel(y, x, RImage.BACKGROUND);
				else {
					outImage.setPixel(y, x, RImage.FOREGROUND);
				}
			}
		}

		return outImage;
	}
}
