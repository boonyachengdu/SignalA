package com.zsoft.hubdemo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.zsoft.SignalA.Hubs.HubConnection;
import com.zsoft.SignalA.Hubs.HubInvokeCallback;
import com.zsoft.SignalA.Hubs.IHubProxy;
import com.zsoft.SignalA.Transport.StateBase;
import com.zsoft.SignalA.transport.longpolling.LongPollingTransport;

import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ConnectionFragment.OnConnectionRequestedListener,
	CalculatorFragment.OnCalculationRequestedListener
{
	protected HubConnection con = null;
	protected IHubProxy hub = null;
	protected TextView tvStatus = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvStatus = (TextView) findViewById(R.id.connection_status);
		
		ChangeFragment(new ConnectionFragment());
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void ConnectionRequested(Uri address) {
		
		con = new HubConnection(address.toString(), this, new LongPollingTransport())
		{
			@Override
			public void OnStateChanged(StateBase oldState, StateBase newState) {
				tvStatus.setText(oldState.getState() + " -> " + newState.getState());
				
				switch(newState.getState())
				{
					case Disconnected:
						break;
					case Connected:
						CalculatorFragment fragment = new CalculatorFragment();
						ChangeFragment(fragment);
						break;
				}
			}
			
			@Override
			public void OnError(Exception exception) {
	            Toast.makeText(MainActivity.this, "On error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
			}

		};
		
		try {
			hub = con.CreateHubProxy("calculatorhub");
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		con.Start();
	}

	@Override
	public void calculate(int value1, int value2, String operator) {
//		int answer = operator.equalsIgnoreCase("Plus") ? value1+value2 : value1-value2;
//			
//		StringBuilder sb = new StringBuilder();
//		sb.append(value1);
//		sb.append(operator=="plus" ? "+":"-");
//		sb.append(value2);
//		sb.append(" = ");
//		sb.append(answer);
		
		HubInvokeCallback callback = new HubInvokeCallback() {
			@Override
			public void OnResult(boolean succeeded, String response) {
				Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void OnError(Exception ex) {
				Toast.makeText(MainActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		};
		
		List<Integer> args = new ArrayList<Integer>(2);
		args.add(value1);
		args.add(value2);
		hub.Invoke(operator, args, callback);
		
	}

	protected void ChangeFragment(Fragment fragment)
	{
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
		.addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		.commit();
		
	}
}
