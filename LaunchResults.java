package ru.msu.cs.graphics.veqeclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import ru.msu.cs.graphics.veqeclient.MetricsCalculator.MetricInfo;

public class LaunchResults implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public boolean success = true;
	public String encName = "";
	public String decName = "";
	public int targetRate = 0; //bps
	public int realRate = 0; //bps
	public double fps = 0;
	public MetricInfo metric = null;
	public BatteryMetric batt = null;
	public SequenceInfo seq = null;
	
	public void save() {
		File file = new File(App.getContext().getExternalFilesDir("results"), 
				String.format("launch_%s_%s_%d_%s", encName, decName, targetRate, seq.getDesc()));
		try {
			OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file, true));
			w.write(String.format("Speed: %f fps\n", fps));
			w.write(String.format("Bitrate: %d bps\n", realRate));
			//w.write(String.format("%s: %f\n", metric.getName(), metric.getAverage()));
			w.write(String.format("Consumption: %f j\n", batt.getResult()));
			w.write(String.format("Detailed battery:\n"));
			batt.print(w);
			w.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
