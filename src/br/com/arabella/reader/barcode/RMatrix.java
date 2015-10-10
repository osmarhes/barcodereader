package br.com.arabella.reader.barcode;

class RMatrix {
	private int rows;
	private int columns;
	private double[][] data;

	public RMatrix(double[][] d) {
		this.rows = d.length;
		this.columns = d[0].length;
		this.data = d;
	}

	public RMatrix(int[][] d) {
		this.rows = d.length;
		this.columns = d[0].length;
		this.data = new double[this.rows][this.columns];

		for (int i = 0; i < this.rows; i++)
			for (int j = 0; j < this.columns; j++)
				this.data[i][j] = d[i][j];
	}

	public RMatrix(int prows, int pcolumns) {
		this.rows = prows;
		this.columns = pcolumns;
		this.data = new double[this.rows][this.columns];

		for (int i = 0; i < this.rows; i++)
			for (int j = 0; j < this.columns; j++)
				this.data[i][j] = 0.0D;
	}

	public RMatrix add(RMatrix m2) {
		RMatrix newMatrix = new RMatrix(this.rows, this.columns);

		if ((this.rows == m2.rows) && (this.columns == m2.columns)) {
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j < this.columns; j++) {
					this.data[i][j] += m2.data[i][j];
				}
			}
		}
		return newMatrix;
	}

	public RMatrix invert() {
		int n = this.data.length;
		double[][] D = new double[n + 1][2 * n + 2];
		int j;
		for (int i = 0; i < this.rows; i++) {
			for (j = 0; j < this.columns; j++) {
				D[(i + 1)][(j + 1)] = this.data[i][j];
			}
		}
		// int error = 0;
		int n2 = 2 * n;

		for (int i = 1; i <= n; i++) {
			for (j = 1; j <= n; j++) {
				D[i][(j + n)] = 0.0D;
			}
			D[i][(i + n)] = 1.0D;
		}

		for (int i = 1; i <= n; i++) {
			double alpha = D[i][i];
			if (alpha == 0.0D) {
				// error = 1;
				break;
			}

			for (j = 1; j <= n2; j++) {
				D[i][j] /= alpha;
			}
			for (int k = 1; k <= n; k++) {
				if (k - i == 0)
					continue;
				double beta = D[k][i];
				for (j = 1; j <= n2; j++) {
					D[k][j] -= beta * D[i][j];
				}

			}

		}

		RMatrix m = new RMatrix(this.rows, this.columns);

		for (int i = 0; i < this.rows; i++) {
			for (j = 0; j < this.columns; j++) {
				m.data[i][j] = D[(i + 1)][(j + 1 + n)];
			}
		}
		return m;
	}

	public RMatrix mult(double v) {
		RMatrix newMatrix = new RMatrix(this.rows, this.columns);
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++)
				newMatrix.data[i][j] = (v * this.data[i][j]);
		}
		return newMatrix;
	}

	public void setValue(int r, int c, double v) {
		this.data[r][c] = v;
	}

	public double getValue(int r, int c) {
		return this.data[r][c];
	}

	public RMatrix mult(RMatrix m2) {
		RMatrix m1 = this;
		RMatrix newMatrix = new RMatrix(m1.rows, m2.columns);

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < m2.columns; j++)
				for (int k = 0; k < this.columns; k++)
					newMatrix.data[i][j] += this.data[i][k] * m2.data[k][j];
		}
		return newMatrix;
	}

	public RMatrix transpose() {
		RMatrix newMatrix = new RMatrix(this.columns, this.rows);

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++)
				newMatrix.data[j][i] = this.data[i][j];
		}
		return newMatrix;
	}
}