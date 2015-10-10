package br.com.arabella.reader.progress.impl;

import br.com.arabella.reader.progress.IProgressListener;

public class ConsoleProgressListener implements IProgressListener {
	public void onProgress(double percentage, String info) {
		String s = "Progress " + percentage + "%";
		if (info.length() > 0)
			s = s + " (" + info + " )";
		System.out.println(s);
	}
}
