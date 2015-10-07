import br.com.atlas.reader.Barcode1DReader;
import br.com.atlas.reader.BarcodeData;
import br.com.atlas.reader.RImage;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Sample {

	private String[][] testSet = {
			{ "img001.jpg", "" + Barcode1DReader.CODE128 },
			{ "img002.jpg", "" + Barcode1DReader.CODE128 },
			{ "img003.jpg", "" + Barcode1DReader.CODE128 },
			{ "img005.jpg", "" + Barcode1DReader.CODE128 },
			{ "img007.jpg", "" + Barcode1DReader.CODE128 },
			{ "img009.jpg", "" + Barcode1DReader.CODE128 },
			{ "img011.jpg", "" + Barcode1DReader.CODE128 },
			{ "img013.jpg", "" + Barcode1DReader.CODE128 },
			{ "img015.jpg", "" + Barcode1DReader.CODE128 },
			{ "img015.jpg", "" + Barcode1DReader.CODE128 } };

	private String dir = "C:\\index\\images\\";

	public static void main(String[] args) {

		Sample sample = new Sample();
		try {

			// sample.scanImage("identcode.PNG",Barcode1DReader.IDENTCODE);
			/*
			 * sample.scanImage("scan_ean4.png",Barcode1DReader.EAN8 |
			 * Barcode1DReader.EAN13 );
			 * sample.scanImage("ean13_shoe.png",Barcode1DReader.EAN8 |
			 * Barcode1DReader.EAN13 );
			 * sample.scanImage("barcode_scan7_i125.png"
			 * ,Barcode1DReader.INTERLEAVED25);
			 * sample.scanImage("ean8_bag.png",Barcode1DReader.EAN8 |
			 * Barcode1DReader.EAN13 );
			 * sample.scanImage("barcode_scan3_code128.png"
			 * ,Barcode1DReader.CODE128 | Barcode1DReader.EAN13 );
			 * sample.scanImage("ean128_90_1.GIF",Barcode1DReader.CODE128 );
			 */

			sample.runTests();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * read all test files
	 *
	 */
	public void runTests() {
		for (int i = 0; i < testSet.length; i++) {
			try {

				System.out.println("");
				System.out.println("------------------------------");
				System.out.println("Scanning File: " + testSet[i][0]);
				System.out.println("");
				scanImage(testSet[i][0], new Integer(testSet[i][1]).intValue());
				System.out.println("------------------------------");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * scan file
	 * 
	 * @param file
	 *            image file
	 * @param type
	 *            type of expected barcode
	 * @throws Exception
	 */
	public void scanImage(String file, int type) throws Exception {

		Image im = loadImage(dir + file);

		Barcode1DReader reader = new Barcode1DReader();
		reader.setSymbologies(type);

		RImage rim = new RImage((BufferedImage) im);

		long tim = System.currentTimeMillis();
		BarcodeData[] barcodes = reader.scan(rim);
		rim = null;
		im = null;

		System.out.print("End. " + (System.currentTimeMillis() - tim) + ". ");

		if (barcodes.length == 0)
			System.out.println("*** NO BARCODE FOUND ***");

		for (int i = 0; i < barcodes.length; i++) {
			System.out.println("Barcode  found " + barcodes[i].toString());

		}

	}

	/**
	 * load image from file
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public Image loadImage(String f) throws Exception {
		Image im2 = null;
		java.awt.MediaTracker mt2 = null;

		java.io.FileInputStream in = null;
		byte[] b = null;
		int size = 0;

		in = new java.io.FileInputStream(f);
		if (in != null) {
			size = in.available();
			b = new byte[size];
			in.read(b);
			im2 = java.awt.Toolkit.getDefaultToolkit().createImage(b);
			in.close();
		}

		mt2 = new java.awt.MediaTracker(new Canvas());
		if (im2 != null) {
			if (mt2 != null) {
				mt2.addImage(im2, 0);
				mt2.waitForID(0);
			}
		}

		BufferedImage input = new BufferedImage(im2.getWidth(null),
				im2.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics g = input.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, im2.getWidth(null), im2.getHeight(null));
		g.drawImage(im2, 0, 0, null);
		g.dispose();
		g = null;

		return input;

	}

}
