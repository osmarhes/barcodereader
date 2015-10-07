package br.com.atlas.reader.util;

import br.com.atlas.reader.RImage;
import br.com.atlas.reader.recognition.Line;
import br.com.atlas.reader.recognition.Point;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class DebugFrame extends Frame {
	private static final long serialVersionUID = -2062841506377274619L;

	public static DebugFrame defaultInstance = new DebugFrame();

	Panel panel1 = new ImagePanel();
	Button button1 = new Button();
	Label label1 = new Label();
	boolean exit = false;
	RImage rimage = null;
	Line[] lines = null;
	Vector<Point> points = null;

	public DebugFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setSize(900, 850);
		setLayout(null);
		this.panel1.setBounds(new Rectangle(6, 30, 889, 690));
		this.button1.setLabel("next");
		this.button1.setBounds(new Rectangle(5, 725, 75, 27));
		this.button1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				DebugFrame.this.button1_mouseClicked(e);
			}
		});
		this.label1.setText("label1");
		this.label1.setBounds(new Rectangle(85, 725, 708, 16));
		add(this.panel1, null);
		add(this.label1, null);
		add(this.button1, null);
	}

	public void show(RImage i, Line l, String t) {
		show(i, new Line[] { l }, t);
	}

	public void show(RImage i, Line[] l, String t) {
		this.rimage = i;
		this.lines = l;
		this.points = null;
		this.label1.setText(t);
		setVisible(false);
		setVisible(true);
		this.exit = false;
		paintAll(getGraphics());
		while (!this.exit)
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void show(RImage i, Line[] l, Vector<Point> ps, String t) {
		this.rimage = i;
		this.lines = l;
		this.points = null;
		this.label1.setText(t);
		setVisible(false);
		setVisible(true);
		this.exit = false;
		this.points = ps;
		paintAll(getGraphics());
		while (!this.exit)
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void show(RImage i, Vector<Point> ps, String t) {
		this.rimage = i;
		this.points = ps;
		this.lines = null;
		this.label1.setText(t);
		setVisible(false);
		setVisible(true);
		this.exit = false;
		paintAll(getGraphics());
		while (!this.exit)
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	void button1_mouseClicked(MouseEvent e) {
		this.exit = true;
	}

	class ImagePanel extends Panel {

		private static final long serialVersionUID = 1L;

		ImagePanel() {
		}

		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, 100, 100);
			if (DebugFrame.this.rimage != null)
				g.drawImage(DebugFrame.this.rimage.getImage(), 0, 0, null);

			g.setColor(Color.red);
			if (DebugFrame.this.lines != null) {
				for (int i = 0; i < DebugFrame.this.lines.length; i++) {
					if (DebugFrame.this.lines[i] == null)
						continue;
					g.drawLine((int) DebugFrame.this.lines[i].x1,
							(int) DebugFrame.this.lines[i].y1,
							(int) DebugFrame.this.lines[i].x2,
							(int) DebugFrame.this.lines[i].y2);
				}
			}
			g.setColor(Color.green);

			if (DebugFrame.this.points != null)
				for (int i = 0; i < DebugFrame.this.points.size(); i++) {
					Point p = (Point) DebugFrame.this.points.elementAt(i);
					g.drawLine((int) p.x, (int) p.y, (int) p.x, (int) p.y);
				}
		}
	}
}
