package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResultsActivity extends Activity {
	
	private static class GraphViewData implements GraphViewDataInterface {
	    private double x,y;

	    public GraphViewData(double x, double y) {
	        this.x = x;
	        this.y = y;
	    }

	    @Override
	    public double getX() {
	        return this.x;
	    }

	    @Override
	    public double getY() {
	        return this.y;
	    }
	}
	
	private static class ColorGenerator {
		private int numGen = -1;
		
		public int generate() {
			numGen++;
			int base = numGen % 3;
			int shift = numGen / 3;
			int res = 0xff000000;
			switch(base) {
			case 0:
				res = 0xffff0000;
				res |= (shift << 12) | (shift << 4);
				return res;
			case 1:
				res = 0xff00ff00;
				res |= (shift << 20) | (shift << 4);
				return res;
			case 2:
				res = 0xff0000ff;
				res |= (shift << 20) | (shift << 12);
				return res;
			}
			return res;
		}
		
		@SuppressWarnings("unused")
		public void reset() {
			numGen = -1;
		}
	}

	private static final int lineThickness = 3;
	
	ArrayList<LaunchResults> mResList = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		setupActionBar();
		
		mResList = (ArrayList<LaunchResults>) getIntent().getSerializableExtra(RunActivity.launchResultsTag);
		LinearLayout layout = (LinearLayout) findViewById(R.id.scrollLayout);
		
		ColorGenerator col = new ColorGenerator();
		CustomLabelFormatter form = new CustomLabelFormatter() {
		   @Override
		   public String formatLabel(double value, boolean isValueX) {
		      if (!isValueX) {
		    	  return String.format("%.1f", value);
		      }
		      return null;
		   }
		};
		
		CustomLabelFormatter formBatt = new CustomLabelFormatter() {
		   @Override
		   public String formatLabel(double value, boolean isValueX) {
		      if (!isValueX) {
		    	  return String.format("%.3f", value);
		      }
		      return null;
		   }
		};
		
		CustomLabelFormatter formSpeed = new CustomLabelFormatter() {
		   @Override
		   public String formatLabel(double value, boolean isValueX) {
		      if (!isValueX) {
		    	  return String.format("%.1f", value);
		      }
		      return null;
		   }
		};
		
		CustomLabelFormatter formRate = new CustomLabelFormatter() {
		   @Override
		   public String formatLabel(double value, boolean isValueX) {
		      if (!isValueX) {
		    	  return String.format("%.3f", value);
		      }
		      return null;
		   }
		};
		
		ArrayList<ArrayList<LaunchResults>> bySequence = LaunchResultsManager.splitBySequenceName(mResList);
		for(ArrayList<LaunchResults> seqRes : bySequence) {
			TextView seqTitle = new TextView(App.getContext());
			SequenceInfo seq = seqRes.get(0).seq;
			seqTitle.setText(String.format("%s (%dx%d, %d frames)\n", seq.getDesc(), 
					seq.getWidth(), seq.getHeight(), seq.getFrameNum()));
			seqTitle.setTextColor(App.getContext().getResources().getColor(R.color.textColor));
			layout.addView(seqTitle);
			
			LineGraphView metricGraph = new LineGraphView(this, seqRes.get(0).metric.getName() + "\n");
			LineGraphView batteryGraph = new LineGraphView(this, "Battery drain\n");
			LineGraphView speedGraph = new LineGraphView(this, "Encoding speed\n");
			LineGraphView bitrateGraph = new LineGraphView(this, "Bitrate handling\n");
			
			ArrayList<ArrayList<LaunchResults>> byEncoder = LaunchResultsManager.splitByEncoderName(seqRes);
			for(ArrayList<LaunchResults> encRes : byEncoder) {
				GraphViewSeriesStyle style = new GraphViewSeriesStyle(col.generate(), lineThickness);
				LaunchResultsManager.sortByBitrate(encRes);
				GraphViewSeries series = new GraphViewSeries(encRes.get(0).encName, style, new GraphViewData[] {});
				GraphViewSeries seriesBat = new GraphViewSeries(encRes.get(0).encName, style, new GraphViewData[] {});
				GraphViewSeries seriesSpeed = new GraphViewSeries(encRes.get(0).encName, style, new GraphViewData[] {});
				GraphViewSeries seriesRate = new GraphViewSeries(encRes.get(0).encName, style, new GraphViewData[] {});
				for(int i = 0; i < encRes.size(); ++i) {
					LaunchResults res = encRes.get(i);
					appendToSeries(series, res.realRate, res.metric.getAverage());
					appendToSeries(seriesBat, res.targetRate, res.batt.getResult());
					appendToSeries(seriesSpeed, res.targetRate, res.fps);
					appendToSeries(seriesRate, res.targetRate, res.realRate/(double)res.targetRate);
				}
				metricGraph.addSeries(series);
				batteryGraph.addSeries(seriesBat);
				speedGraph.addSeries(seriesSpeed);
				bitrateGraph.addSeries(seriesRate);
			}
			
			setupGraph(metricGraph, form);
			setupGraph(batteryGraph, formBatt);
			setupGraph(speedGraph, formSpeed);
			setupGraph(bitrateGraph, formRate);
			
			layout.addView(metricGraph);
			layout.addView(batteryGraph);
			layout.addView(speedGraph);
			layout.addView(bitrateGraph);
		}
		
		TextView rawTitle = new TextView(App.getContext());
		rawTitle.setText("\nRaw launch results:\n");
		rawTitle.setTextColor(App.getContext().getResources().getColor(R.color.textColor));
		layout.addView(rawTitle);
		
		for(LaunchResults res : mResList) {
			TextView tv = new TextView(App.getContext());
			String txt = String.format("Encoded with %s\n" +
					"Decoded with %s\n" +
					"Target bitrate: %d bps\n" +
					"Real bitrate: %d bps\n" +
					"Sequence: %s (%dx%d, %d frames)\n" +
					"Metric %s: %f\n" +
					"Energy consumption: %f joules\n" +
					"Encoder speed: %f fps\n\n", 
					res.encName, res.decName, res.targetRate,
					res.realRate, res.seq.getDesc(), res.seq.getWidth(),
					res.seq.getHeight(), res.seq.getFrameNum(), res.metric.getName(),
					res.metric.getAverage(), res.batt.getResult(), res.fps);
			tv.setText(txt);
			tv.setTextColor(App.getContext().getResources().getColor(R.color.textColor));
			layout.addView(tv);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void setupGraph(LineGraphView graph, CustomLabelFormatter form) {
		graph.setShowLegend(true);
		graph.setLegendWidth(230);
		graph.getGraphViewStyle().setTextSize(11);
		graph.setLegendAlign(LegendAlign.BOTTOM);
		graph.setDrawDataPoints(true);
		graph.setLayoutParams(new LayoutParams(410, 350));
		graph.setCustomLabelFormatter(form);
	}
	
	private void appendToSeries(GraphViewSeries series, double x, double y) {
		series.appendData(new GraphViewData(x, y), false, 100000);
		series.appendData(new GraphViewData(x, y), false, 100000);
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
	    startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
		    startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
