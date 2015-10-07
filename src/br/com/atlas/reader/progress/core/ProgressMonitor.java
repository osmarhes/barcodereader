package br.com.atlas.reader.progress.core;

import br.com.atlas.reader.progress.IProgressListener;

public class ProgressMonitor {
	private IProgressListener listener;
	private int numberOfSteps = 0;
	private double percentStep = 0.0D;
	private double currentPercent = 0.0D;
	private int currentStep = 0;
	private int numberofStepsSubprocess = 0;
	private long lastReport;

	public ProgressMonitor(IProgressListener l, double steps) {
		this.listener = l;
		this.percentStep = (100.0D / steps);
		this.numberOfSteps = (int) steps;
		this.currentStep = 0;
		this.currentPercent = 0.0D;
	}

	public void startSubProcess(int steps) {
		this.currentStep += 1;
		this.numberofStepsSubprocess = steps;
		if (this.listener != null)
			this.listener.onProgress(this.currentPercent, "");
		this.lastReport = System.currentTimeMillis();
	}

	public void reportProgress(double step) {
		double percent = step / this.numberofStepsSubprocess * 100.0D;

		percent = percent * this.percentStep / 100.0D;

		percent += this.currentPercent;

		if ((this.listener != null)
				&& (System.currentTimeMillis() - this.lastReport > 2L)) {
			this.listener.onProgress(percent, "");
			this.lastReport = System.currentTimeMillis();
		}
	}

	public void endSubProcess() {
		this.currentPercent += this.percentStep;
		if (this.currentStep == this.numberOfSteps)
			this.currentPercent = 100.0D;

		if (this.listener != null)
			this.listener.onProgress(this.currentPercent, "");
		this.lastReport = System.currentTimeMillis();
	}
}
